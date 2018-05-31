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
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanBuilder;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.propagation.SpanContextParseException;
import io.opencensus.trace.propagation.TextFormat;

/**
 * This helper class provides routine methods to instrument HTTP servers.
 *
 * @param <Q> the HTTP request entity.
 * @param <P> the HTTP response entity.
 * @param <C> the type of the carrier.
 * @since 0.13
 */
@ExperimentalApi
public class HttpServerHandler<Q, P, C> extends HttpHandler<Q, P> {

  private final TextFormat.Getter<C> getter;

  /**
   * Creates a {@link HttpServerHandler} with given parameters.
   *
   * @param tracer the Open Census tracing component.
   * @param extractor the {@code HttpExtractor} used to extract information from the
   *     request/response.
   * @param customizer the {@link HttpSpanCustomizer} used to customize span behaviors.
   * @param textFormat the {@code TextFormat} used in HTTP propagation.
   * @param getter the getter used when extracting information from the {@code carrier}.
   * @since 0.13
   */
  public HttpServerHandler(
      Tracer tracer,
      HttpExtractor<Q, P> extractor,
      HttpSpanCustomizer<Q, P> customizer,
      TextFormat textFormat,
      TextFormat.Getter<C> getter) {
    super(tracer, extractor, customizer, textFormat);
    checkNotNull(getter, "getter");
    this.getter = getter;
  }

  /**
   * Instrument an incoming request before it is handled. Users should optionally invoke {@link
   * #handleMessageReceived} after the request is received.
   *
   * <p>This method will create a span under the deserialized propagated parent context. If the
   * parent context is not present, the span will be created under the current context.
   *
   * <p>This method will invoke {@link HttpSpanCustomizer#customizeSpanStart} after the span is
   * successfully created.
   *
   * <p>The generated span will NOT be set as current context. User can use the returned value to
   * control when to enter the scope of this span.
   *
   * @param carrier the entity that holds the HTTP information.
   * @param request the request entity.
   * @return a span that represents the response handling process.
   * @since 0.13
   */
  public Span handleStart(C carrier, Q request) {
    checkNotNull(carrier, "carrier");
    checkNotNull(request, "request");
    SpanBuilder spanBuilder = null;
    String spanName = customizer.getSpanName(request, extractor);
    String parseError = null;
    // de-serialize the context
    try {
      SpanContext spanContext = textFormat.extract(carrier, getter);
      spanBuilder = tracer.spanBuilderWithRemoteParent(spanName, spanContext);
    } catch (SpanContextParseException e) {
      // TODO: Currently we cannot distinguish between context parse error and missing context.
      // Logging would be annoying so we just ignore this error and do not even log a message.
      // TODO: If we expose HttpSpanCustomizer to user in the future, we might consider checking
      // the nullness of the spanName.
      spanBuilder = tracer.spanBuilder(spanName);
    }

    customizer.customizeSpanBuilder(request, spanBuilder, extractor);
    Span span = spanBuilder.startSpan();
    // user-defined behaviors
    customizer.customizeSpanStart(request, span, extractor);
    return span;
  }
}
