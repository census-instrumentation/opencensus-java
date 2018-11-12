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

import com.google.common.annotations.VisibleForTesting;
import io.opencensus.common.ExperimentalApi;
import io.opencensus.contrib.http.HttpServerHandler;
import io.opencensus.trace.Span;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.propagation.TextFormat.Getter;
import java.io.IOException;
import javax.annotation.Nullable;
import javax.servlet.AsyncContext;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This filter class implements Filter interface called by web container. The filter is used as an
 * interceptor to enable tracing of http requests.
 *
 * @since 0.18
 */
@ExperimentalApi
public class OcHttpServletFilter implements Filter {
  @VisibleForTesting
  static final Getter<HttpServletRequest> getter =
      new Getter<HttpServletRequest>() {
        @Nullable
        @Override
        public String get(HttpServletRequest carrier, String key) {
          return carrier.getHeader(key);
        }
      };

  @VisibleForTesting
  HttpServerHandler<HttpServletRequest, HttpServletResponse, HttpServletRequest> handler;

  OcHttpServletFilter() {
    TraceConfig traceConfig = Tracing.getTraceConfig();
    traceConfig.updateActiveTraceParams(traceConfig.getActiveTraceParams().toBuilder().build());
    handler = buildHttpServerHandler();
  }

  private static HttpServerHandler<HttpServletRequest, HttpServletResponse, HttpServletRequest>
      buildHttpServerHandler() {
    return new HttpServerHandler<HttpServletRequest, HttpServletResponse, HttpServletRequest>(
        Tracing.getTracer(),
        // TODO[rghetia]:
        // 1. replace this with TraceContext propagator once it is merged.
        // 2. provide options to configure custom extractor, propagator and endpoint.
        new OcHttpServletExtractor(),
        Tracing.getPropagationComponent().getB3Format(),
        getter,
        true);
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    if (handler == null) {
      throw new ServletException("Failed to build HttpServerHandler");
    }
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    // only interested in http requests
    if ((request instanceof HttpServletRequest) && (response instanceof HttpServletResponse)) {
      HttpServletRequest httpReq = (HttpServletRequest) request;
      HttpServletResponse httpResp = (HttpServletResponse) response;

      Span span = handler.handleStart(httpReq, httpReq);

      chain.doFilter(httpReq, httpResp);

      if (httpReq.isAsyncStarted()) {
        OcHttpServletListener listener = new OcHttpServletListener(handler, span);
        AsyncContext async = httpReq.getAsyncContext();
        async.addListener(listener, httpReq, httpResp);
      } else {
        handler.handleEnd(span, httpResp, null);
      }
    } else {
      // pass request through unchanged
      chain.doFilter(request, response);
    }
  }

  @Override
  public void destroy() {}
}
