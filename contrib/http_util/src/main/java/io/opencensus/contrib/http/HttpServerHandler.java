/*
 * Copyright 2018, OpenCensus Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.contrib.http;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.opencensus.contrib.http.HttpRequestContext.METADATA_NO_PROPAGATION;
import static io.opencensus.contrib.http.util.HttpMeasureConstants.HTTP_SERVER_LATENCY;
import static io.opencensus.contrib.http.util.HttpMeasureConstants.HTTP_SERVER_METHOD;
import static io.opencensus.contrib.http.util.HttpMeasureConstants.HTTP_SERVER_RECEIVED_BYTES;
import static io.opencensus.contrib.http.util.HttpMeasureConstants.HTTP_SERVER_ROUTE;
import static io.opencensus.contrib.http.util.HttpMeasureConstants.HTTP_SERVER_SENT_BYTES;
import static io.opencensus.contrib.http.util.HttpMeasureConstants.HTTP_SERVER_STATUS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import io.opencensus.common.ExperimentalApi;
import io.opencensus.stats.Stats;
import io.opencensus.stats.StatsRecorder;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagValue;
import io.opencensus.tags.Tagger;
import io.opencensus.tags.Tags;
import io.opencensus.trace.Link;
import io.opencensus.trace.Link.Type;
import io.opencensus.trace.Span;
import io.opencensus.trace.Span.Kind;
import io.opencensus.trace.Span.Options;
import io.opencensus.trace.SpanBuilder;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.propagation.SpanContextParseException;
import io.opencensus.trace.propagation.TextFormat;
import javax.annotation.Nullable;

/*>>>
import org.checkerframework.checker.nullness.qual.NonNull;
*/

/**
 * This helper class provides routine methods to instrument HTTP servers.
 *
 * @param <Q> the HTTP request entity.
 * @param <P> the HTTP response entity.
 * @param <C> the type of the carrier.
 * @since 0.19
 */
@ExperimentalApi
public class HttpServerHandler<
        Q /*>>> extends @NonNull Object*/, P, C /*>>> extends @NonNull Object*/>
    extends AbstractHttpHandler<Q, P> {

  private final TextFormat.Getter<C> getter;
  private final TextFormat textFormat;
  private final Tracer tracer;
  private final Boolean publicEndpoint;
  private final StatsRecorder statsRecorder;
  private final Tagger tagger;

  /**
   * Creates a {@link HttpServerHandler} with given parameters.
   *
   * @param tracer the Open Census tracing component.
   * @param extractor the {@code HttpExtractor} used to extract information from the
   *     request/response.
   * @param textFormat the {@code TextFormat} used in HTTP propagation.
   * @param getter the getter used when extracting information from the {@code carrier}.
   * @param publicEndpoint set to true for publicly accessible HTTP(S) server. If true then incoming
   *     tracecontext will be added as a link instead of as a parent.
   * @since 0.19
   */
  public HttpServerHandler(
      Tracer tracer,
      HttpExtractor<Q, P> extractor,
      TextFormat textFormat,
      TextFormat.Getter<C> getter,
      Boolean publicEndpoint) {
    super(extractor);
    checkNotNull(tracer, "tracer");
    checkNotNull(textFormat, "textFormat");
    checkNotNull(getter, "getter");
    checkNotNull(publicEndpoint, "publicEndpoint");
    this.tracer = tracer;
    this.textFormat = textFormat;
    this.getter = getter;
    this.publicEndpoint = publicEndpoint;
    this.statsRecorder = Stats.getStatsRecorder();
    this.tagger = Tags.getTagger();
  }

  /**
   * Instrument an incoming request before it is handled.
   *
   * <p>This method will create a span under the deserialized propagated parent context. If the
   * parent context is not present, the span will be created under the current context.
   *
   * <p>The generated span will NOT be set as current context. User can control when to enter the
   * scope of this span. Use {@link AbstractHttpHandler#getSpanFromContext} to retrieve the span.
   *
   * @param carrier the entity that holds the HTTP information.
   * @param request the request entity.
   * @return the {@link HttpRequestContext} that contains stats and trace data associated with the
   *     request.
   * @since 0.19
   */
  public HttpRequestContext handleStart(C carrier, Q request) {
    checkNotNull(carrier, "carrier");
    checkNotNull(request, "request");
    SpanBuilder spanBuilder = null;
    String spanName = getSpanName(request, extractor);
    // de-serialize the context
    SpanContext spanContext = null;
    try {
      spanContext = textFormat.extract(carrier, getter);
    } catch (SpanContextParseException e) {
      // TODO: Currently we cannot distinguish between context parse error and missing context.
      // Logging would be annoying so we just ignore this error and do not even log a message.
    }
    if (spanContext == null || publicEndpoint) {
      spanBuilder = tracer.spanBuilder(spanName);
    } else {
      spanBuilder = tracer.spanBuilderWithRemoteParent(spanName, spanContext);
    }

    Span span = spanBuilder.setSpanKind(Kind.SERVER).startSpan();
    if (publicEndpoint && spanContext != null) {
      span.addLink(Link.fromSpanContext(spanContext, Type.PARENT_LINKED_SPAN));
    }

    if (span.getOptions().contains(Options.RECORD_EVENTS)) {
      addSpanRequestAttributes(span, request, extractor);
    }

    return getNewContext(span, tagger.getCurrentTagContext());
  }

  /**
   * Close an HTTP span and records stats specific to the request.
   *
   * <p>This method will set status of the span and end it. Additionally it will record message
   * events for the span and record measurements associated with the request.
   *
   * @param context the {@link HttpRequestContext} used with {@link
   *     HttpServerHandler#handleStart(Object, Object)}
   * @param request the HTTP request entity.
   * @param response the HTTP response entity. {@code null} means invalid response.
   * @param error the error occurs when processing the response.
   * @since 0.19
   */
  public void handleEnd(
      HttpRequestContext context, Q request, @Nullable P response, @Nullable Throwable error) {
    checkNotNull(context, "context");
    checkNotNull(request, "request");
    int httpCode = extractor.getStatusCode(response);
    recordStats(context, request, httpCode);
    spanEnd(context.span, httpCode, error);
  }

  private void recordStats(HttpRequestContext context, Q request, int httpCode) {
    double requestLatency = NANOSECONDS.toMillis(System.nanoTime() - context.requestStartTime);

    String methodStr = extractor.getMethod(request);
    String routeStr = extractor.getRoute(request);
    TagContext startCtx =
        tagger
            .toBuilder(context.tagContext)
            .put(
                HTTP_SERVER_METHOD,
                TagValue.create(methodStr == null ? "" : methodStr),
                METADATA_NO_PROPAGATION)
            .put(
                HTTP_SERVER_ROUTE,
                TagValue.create(routeStr == null ? "" : routeStr),
                METADATA_NO_PROPAGATION)
            .put(
                HTTP_SERVER_STATUS,
                TagValue.create(httpCode == 0 ? "error" : Integer.toString(httpCode)),
                METADATA_NO_PROPAGATION)
            .build();

    statsRecorder
        .newMeasureMap()
        .put(HTTP_SERVER_LATENCY, requestLatency)
        .put(HTTP_SERVER_RECEIVED_BYTES, context.receiveMessageSize.get())
        .put(HTTP_SERVER_SENT_BYTES, context.sentMessageSize.get())
        .record(startCtx);
  }
}
