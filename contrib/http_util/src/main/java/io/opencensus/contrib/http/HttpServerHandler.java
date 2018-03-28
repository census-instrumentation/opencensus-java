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

import io.opencensus.trace.Annotation;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanBuilder;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.propagation.TextFormat;

/**
 * This helper class provides routine methods to instrument HTTP servers.
 *
 * @param <Q> the HTTP request entity.
 * @param <P> the HTTP response entity.
 * @since 0.13
 */
public class HttpServerHandler<Q, P> extends HttpHandler<Q, P> {

  /**
   * Creates a {@link HttpServerHandler} with given parameters.
   *
   * @param tracer the Open Census tracing component.
   * @param extractor the {@code HttpExtractor} used to extract information from the
   *     request/response.
   * @param customizer the {@link HttpSpanCustomizer} used to customize span behaviors.
   * @since 0.13
   */
  public HttpServerHandler(
      Tracer tracer, HttpExtractor<Q, P> extractor, HttpSpanCustomizer<Q, P> customizer) {
    super(tracer, extractor, customizer);
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
   * @param <C> the type of the carrier
   * @param textFormat the {@code TextFormat} used in HTTP propagation.
   * @param getter the getter used when extracting information from the {@code carrier}.
   * @param carrier the entity that holds the HTTP information.
   * @param request the request entity.
   * @return a span that represents the response handling process.
   * @since 0.13
   */
  public <C> Span handleStart(
      TextFormat textFormat, TextFormat.Getter<C> getter, C carrier, Q request) {
    checkNotNull(textFormat, "textFormat");
    checkNotNull(getter, "getter");
    checkNotNull(carrier, "carrier");
    checkNotNull(request, "request");
    SpanBuilder spanBuilder = null;
    String spanName = customizer.getSpanName(request, extractor);
    String parseError = null;
    // de-serialize the context
    try {
      SpanContext spanContext = textFormat.extract(carrier, getter);
      spanBuilder = tracer.spanBuilderWithRemoteParent(spanName, spanContext);
    } catch (Exception e) {
      // record this exception
      spanBuilder = tracer.spanBuilder(spanName);
      parseError = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
    }

    spanBuilder = customizer.customizeSpanBuilder(request, spanBuilder, extractor);
    Span span = spanBuilder.startSpan();
    // log an annotation to indicate the error
    if (parseError != null) {
      span.addAnnotation(Annotation.fromDescription("Error parsing span context: " + parseError));
    }
    // user-defined behaviors
    customizer.customizeSpanStart(request, span, extractor);
    return span;
  }
}
