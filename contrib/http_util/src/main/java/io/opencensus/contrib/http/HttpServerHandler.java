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

import io.opencensus.common.ExperimentalApi;
import io.opencensus.trace.Link;
import io.opencensus.trace.Link.Type;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanBuilder;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.propagation.SpanContextParseException;
import io.opencensus.trace.propagation.TextFormat;

/*>>>
import org.checkerframework.checker.nullness.qual.NonNull;
*/

/**
 * This helper class provides routine methods to instrument HTTP servers.
 *
 * @param <Q> the HTTP request entity.
 * @param <P> the HTTP response entity.
 * @param <C> the type of the carrier.
 * @since 0.17
 */
@ExperimentalApi
public class HttpServerHandler<
        Q /*>>> extends @NonNull Object*/, P, C /*>>> extends @NonNull Object*/>
    extends AbstractHttpHandler<Q, P> {

  private final TextFormat.Getter<C> getter;
  private final TextFormat textFormat;
  private final Tracer tracer;
  private final Boolean publicEndpoint;

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
   * @since 0.17
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
  }

  /**
   * Instrument an incoming request before it is handled. Users should optionally invoke {@link
   * #handleMessageReceived} after the request is received.
   *
   * <p>This method will create a span under the deserialized propagated parent context. If the
   * parent context is not present, the span will be created under the current context.
   *
   * <p>The generated span will NOT be set as current context. User can use the returned value to
   * control when to enter the scope of this span.
   *
   * @param carrier the entity that holds the HTTP information.
   * @param request the request entity.
   * @return a span that represents the response handling process.
   * @since 0.17
   */
  public Span handleStart(C carrier, Q request) {
    checkNotNull(carrier, "carrier");
    checkNotNull(request, "request");
    SpanBuilder spanBuilder = null;
    String spanName = getSpanName(request, extractor);
    // de-serialize the context
    SpanContext spanContext = null;
    try {
      spanContext = textFormat.extract(carrier, getter);
      if (publicEndpoint) {
        spanBuilder = tracer.spanBuilder(spanName);
      } else {
        spanBuilder = tracer.spanBuilderWithRemoteParent(spanName, spanContext);
      }
    } catch (SpanContextParseException e) {
      // TODO: Currently we cannot distinguish between context parse error and missing context.
      // Logging would be annoying so we just ignore this error and do not even log a message.
      spanBuilder = tracer.spanBuilder(spanName);
    }

    Span span = spanBuilder.startSpan();
    if (publicEndpoint && spanContext != null) {
      span.addLink(Link.fromSpanContext(spanContext, Type.PARENT_LINKED_SPAN));
    }
    return span;
  }
}
