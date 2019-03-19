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

package io.opencensus.contrib.http.servlet;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.errorprone.annotations.MustBeClosed;
import io.opencensus.common.Scope;
import io.opencensus.contrib.http.HttpRequestContext;
import io.opencensus.contrib.http.HttpServerHandler;
import io.opencensus.trace.BlankSpan;
import io.opencensus.trace.Tracing;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class OcHttpServletUtil {
  static final String CONTENT_LENGTH = "Content-Length";
  static final String OPENCENSUS_SERVLET_LISTENER = "opencensus.servlet.listener";

  private OcHttpServletUtil() {}

  static void recordMessageSentEvent(
      HttpServerHandler<HttpServletRequest, HttpServletResponse, HttpServletRequest> handler,
      HttpRequestContext context,
      HttpServletResponse response) {
    if (response != null) {
      String length = response.getHeader(CONTENT_LENGTH);
      if (length != null && !length.isEmpty()) {
        try {
          handler.handleMessageSent(context, Integer.parseInt(length));
        } catch (NumberFormatException e) {
          return;
        }
      }
    }
  }

  /**
   * Enters the scope of code where the given {@link ServletRequest} will be processed and returns
   * an object that represents the scope. The scope is exited when the returned object is closed. A
   * span created for the {@link ServletRequest} is set to the current Context.
   *
   * <p>Supports try-with-resource idiom.
   *
   * <p>Example of usage:
   *
   * <pre>{@code
   * void AsyncRequestProcessor(AsyncContext asyncCtx) {
   *   try (Scope ws = OcHttpServletUtil.withScope(asyncCtx.getRequest) {
   *     tracer.getCurrentSpan().addAnnotation("my annotation");
   *     doSomeOtherWork();  // Here "span" is the current Span.
   *   }
   * }
   * }</pre>
   *
   * <p>Prior to Java SE 7, you can use a finally block to ensure that a resource is closed
   * regardless of whether the try statement completes normally or abruptly.
   *
   * <p>Example of usage prior to Java SE7:
   *
   * <pre>{@code
   * void AsyncRequestProcessor(AsyncContext asyncCtx) {
   *   Scope ws = OcHttpServletUtil.withScope(asyncCtx.getRequest)
   *   try {
   *     tracer.getCurrentSpan().addAnnotation("my annotation");
   *     doSomeOtherWork();  // Here "span" is the current Span.
   *   } finally {
   *     ws.close();
   *   }
   * }
   * }</pre>
   *
   * @param request The {@link ServletRequest} request that is about to be processed.
   * @return an object that defines a scope where the span associated with the given {@link
   *     ServletRequest} will be set to the current Context.
   * @throws NullPointerException if {@code request} is {@code null}.
   * @since 0.20.0
   */
  @MustBeClosed
  public static Scope withScope(ServletRequest request) {
    checkNotNull(request, "request");
    OcHttpServletListener listener =
        (OcHttpServletListener) request.getAttribute(OPENCENSUS_SERVLET_LISTENER);
    if (listener != null) {
      return listener.withSpan();
    }
    return Tracing.getTracer().withSpan(BlankSpan.INSTANCE);
  }
}
