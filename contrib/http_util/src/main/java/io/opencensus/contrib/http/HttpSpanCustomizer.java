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

import io.opencensus.contrib.http.util.HttpTraceUtil;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanBuilder;
import javax.annotation.Nullable;

/**
 * A helper class to customize an HTTP span.
 *
 * <p>It contains basic instrumentation implementation. See <a
 * href="https://github.com/census-instrumentation/opencensus-specs/blob/master/trace/HTTP.md">HTTP
 * trace spec</a> for more information.
 *
 * <p>Users can extend this helper to customize builder and end status.
 *
 * @param <Q> the HTTP request entity.
 * @param <P> the HTTP response entity.
 * @since 0.13
 */
public class HttpSpanCustomizer<Q, P> {

  /**
   * Returns customized span name according to the request.
   *
   * <p>Note that in OpenCensus, {@link Span} is immutable and cannot be renamed.
   *
   * @param request the HTTP request entity.
   * @param extractor the framework specific extractor to get information from the request.
   * @return a string that represents the name of the span.
   */
  public String getSpanName(Q request, HttpExtractor<Q, P> extractor) {
    // default span name
    return "/" + extractor.getPath(request);
  }

  /**
   * Customize the builder for the {@link Span}.
   *
   * <p>This allows user to set {@code Sampler}, parent {@code Span}, and the {@code recordEvents}
   * decision.
   *
   * @param request the HTTP request entity.
   * @param spanBuilder the {@link SpanBuilder} used to start the HTTP span.
   * @param extractor the framework specific extractor to get information from the request.
   * @since 0.13
   */
  public void customizeSpanBuilder(
      Q request, SpanBuilder spanBuilder, HttpExtractor<Q, P> extractor) {
    // do nothing by default.
  }

  /**
   * Customize the span after it is started, but before sending (for client) or receiving (for
   * server) the message.
   *
   * <p>This method will do nothing by default.
   *
   * @param request the HTTP request entity.
   * @param span the span to be customized.
   * @param extractor the framework specific extractor to get information from the request.
   * @since 0.13
   */
  public void customizeSpanStart(Q request, Span span, HttpExtractor<Q, P> extractor) {
    // do nothing by default.
  }

  /**
   * Customize the span after receiving (for client) or sending (for server) the message.
   *
   * <p>This method will set the status by default. Users can override this to implement their own
   * parsing logic.
   *
   * @param response the HTTP response entity. {@code null} means invalid response.
   * @param error the error occurs when processing the response.
   * @param span the span to be customized.
   * @param extractor the framework specific extractor to get information from the request.
   * @since 0.13
   */
  public void customizeSpanEnd(
      @Nullable P response, @Nullable Throwable error, Span span, HttpExtractor<Q, P> extractor) {
    // set the status by default.
    span.setStatus(HttpTraceUtil.parseResponseStatus(extractor.getStatusCode(response), error));
  }
}
