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
import static io.opencensus.contrib.http.util.HttpMeasureConstants.HTTP_CLIENT_METHOD;
import static io.opencensus.contrib.http.util.HttpMeasureConstants.HTTP_CLIENT_RECEIVED_BYTES;
import static io.opencensus.contrib.http.util.HttpMeasureConstants.HTTP_CLIENT_ROUNDTRIP_LATENCY;
import static io.opencensus.contrib.http.util.HttpMeasureConstants.HTTP_CLIENT_SENT_BYTES;
import static io.opencensus.contrib.http.util.HttpMeasureConstants.HTTP_CLIENT_STATUS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import io.opencensus.common.ExperimentalApi;
import io.opencensus.contrib.http.util.HttpTraceUtil;
import io.opencensus.stats.Stats;
import io.opencensus.stats.StatsRecorder;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagValue;
import io.opencensus.tags.Tagger;
import io.opencensus.tags.Tags;
import io.opencensus.trace.Span;
import io.opencensus.trace.Span.Options;
import io.opencensus.trace.SpanBuilder;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.propagation.TextFormat;
import javax.annotation.Nullable;

/*>>>
import org.checkerframework.checker.nullness.qual.NonNull;
*/

/**
 * This helper class provides routine methods to instrument HTTP clients.
 *
 * @param <Q> the HTTP request entity.
 * @param <P> the HTTP response entity.
 * @param <C> the type of the carrier.
 * @since 0.18
 */
@ExperimentalApi
public class HttpClientHandler<
        Q /*>>> extends @NonNull Object*/, P, C /*>>> extends @NonNull Object*/>
    extends AbstractHttpHandler<Q, P> {

  private final TextFormat.Setter<C> setter;
  private final TextFormat textFormat;
  private final Tracer tracer;
  private final StatsRecorder statsRecorder;
  private final Tagger tagger;

  /**
   * Creates a {@link HttpClientHandler} with given parameters.
   *
   * @param tracer the Open Census tracing component.
   * @param extractor the {@code HttpExtractor} used to extract information from the
   *     request/response.
   * @param textFormat the {@code TextFormat} used in HTTP propagation.
   * @param setter the setter used when injecting information to the {@code carrier}.
   * @since 0.18
   */
  public HttpClientHandler(
      Tracer tracer,
      HttpExtractor<Q, P> extractor,
      TextFormat textFormat,
      TextFormat.Setter<C> setter) {
    super(extractor);
    checkNotNull(setter, "setter");
    checkNotNull(textFormat, "textFormat");
    checkNotNull(tracer, "tracer");
    this.setter = setter;
    this.textFormat = textFormat;
    this.tracer = tracer;
    this.statsRecorder = Stats.getStatsRecorder();
    this.tagger = Tags.getTagger();
  }

  /**
   * Instrument a request before it is sent. Users should optionally invoke {@link
   * #handleMessageSent} after the request is sent.
   *
   * <p>This method will create a span in current context to represent the HTTP call. The created
   * span will be serialized and propagated to the server.
   *
   * <p>The generated span will NOT be set as current context. User can use the returned value to
   * control when to enter the scope of this span.
   *
   * @param parent the parent {@link Span}. {@code null} indicates using current span.
   * @param carrier the entity that holds the HTTP information.
   * @param request the request entity.
   * @return a span that represents the request process.
   * @deprecated This method is deprecated in lieu of {@link HttpClientHandler#requestStart(Span,
   *     Object, Object)}
   * @since 0.18
   */
  @Deprecated
  public Span handleStart(@Nullable Span parent, C carrier, Q request) {
    checkNotNull(carrier, "carrier");
    checkNotNull(request, "request");
    return handleBeginInternal(parent, carrier, request);
  }

  private Span handleBeginInternal(@Nullable Span parent, C carrier, Q request) {
    if (parent == null) {
      parent = tracer.getCurrentSpan();
    }
    String spanName = getSpanName(request, extractor);
    SpanBuilder builder = tracer.spanBuilderWithExplicitParent(spanName, parent);
    Span span = builder.startSpan();

    if (span.getOptions().contains(Options.RECORD_EVENTS)) {
      addSpanRequestAttributes(span, request, extractor);
    }

    // inject propagation header
    SpanContext spanContext = span.getContext();
    if (!spanContext.equals(SpanContext.INVALID)) {
      textFormat.inject(spanContext, carrier, setter);
    }
    return span;
  }

  /**
   * Close an HTTP span.
   *
   * <p>This method will set status of the span and end it.
   *
   * @param response the HTTP response entity. {@code null} means invalid response.
   * @param error the error occurs when processing the response.
   * @param span the span.
   * @deprecated This method is deprecated in lieu of {@link
   *     HttpClientHandler#requestEnd(HttpContext, Object, Object, Throwable)}
   * @since 0.18
   */
  @Deprecated
  public void handleEnd(Span span, @Nullable P response, @Nullable Throwable error) {
    spanEnd(span, response, error);
  }

  /**
   * Instrument a request for tracing and stats before it is sent.
   *
   * <p>This method will create a span in current context to represent the HTTP call. The created
   * span will be serialized and propagated to the server.
   *
   * <p>The generated span will NOT be set as current context. User can control when to enter the
   * scope of this span. Use {@link AbstractHttpHandler#getSpanFromContext} to retrieve the span.
   *
   * @param parent the parent {@link Span}. {@code null} indicates using current span.
   * @param carrier the entity that holds the HTTP information.
   * @param request the request entity.
   * @return the {@link HttpContext} that contains stats and trace data associated with the request.
   * @since 0.18
   */
  public HttpContext requestStart(@Nullable Span parent, C carrier, Q request) {
    checkNotNull(carrier, "carrier");
    checkNotNull(request, "request");
    return getNewContext(handleBeginInternal(parent, carrier, request));
  }

  /**
   * Close an HTTP span and records stats specific to the request.
   *
   * <p>This method will set status of the span and end it. Additionally it will record message
   * events for the span and record measurements associated with the request.
   *
   * @param context the {@link HttpContext} returned from {@link
   *     HttpClientHandler#requestStart(Span, Object, Object)}
   * @param request the HTTP request entity.
   * @param response the HTTP response entity. {@code null} means invalid response.
   * @param error the error occurs when processing the response.
   * @since 0.18
   */
  public void requestEnd(
      HttpContext context, Q request, @Nullable P response, @Nullable Throwable error) {
    checkNotNull(context, "context");
    checkNotNull(request, "request");
    recordStats(context, request, response, error);
    recordTraceEvents(context);
    spanEnd(context.span, response, error);
  }

  private void recordStats(
      HttpContext context, Q request, @Nullable P response, @Nullable Throwable error) {
    double requestLatency = NANOSECONDS.toMillis(System.nanoTime() - context.requestStartTime);

    String methodStr = extractor.getMethod(request);
    TagContext startCtx =
        tagger
            .currentBuilder()
            .put(HTTP_CLIENT_METHOD, TagValue.create(methodStr == null ? "" : methodStr))
            .put(
                HTTP_CLIENT_STATUS,
                TagValue.create(
                    HttpTraceUtil.parseResponseStatus(extractor.getStatusCode(response), error)
                        .toString()))
            .build();

    statsRecorder
        .newMeasureMap()
        .put(HTTP_CLIENT_ROUNDTRIP_LATENCY, requestLatency)
        .put(HTTP_CLIENT_SENT_BYTES, context.requestMessageSize.get())
        .put(HTTP_CLIENT_RECEIVED_BYTES, context.responseMessageSize.get())
        .record(startCtx);
  }

  private void recordTraceEvents(HttpContext context) {
    handleMessageSent(context.span, context.reqId, context.requestMessageSize.get());
    handleMessageReceived(context.span, context.reqId, context.responseMessageSize.get());
  }
}
