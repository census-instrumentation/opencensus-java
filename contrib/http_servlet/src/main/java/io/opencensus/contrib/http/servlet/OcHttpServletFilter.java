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
import io.opencensus.common.Scope;
import io.opencensus.contrib.http.HttpExtractor;
import io.opencensus.contrib.http.HttpRequestContext;
import io.opencensus.contrib.http.HttpServerHandler;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.propagation.TextFormat;
import io.opencensus.trace.propagation.TextFormat.Getter;
import java.io.IOException;
import javax.annotation.Nullable;
import javax.servlet.AsyncContext;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This filter class implements Filter interface called by web container. The filter is used as an
 * interceptor to enable tracing of http requests.
 *
 * @since 0.19
 */
@ExperimentalApi
public class OcHttpServletFilter implements Filter {

  /**
   * Set optional OC_TRACE_PROPAGATOR attribute in {@link ServletContext} with {@link TextFormat}
   * propagator. By default {@code TraceContextFormat} is used to propagate trace context.
   *
   * @since 0.20
   */
  public static final String OC_TRACE_PROPAGATOR = "opencensus.trace_propagator";

  /**
   * Set optional OC_EXTRACTOR attribute in {@link ServletContext} with {@link HttpExtractor}
   * customExtractor. Default extractor is used if custom extractor is not provided.
   *
   * @since 0.20
   */
  public static final String OC_EXTRACTOR = "opencensus.extractor";

  /**
   * Set optional OC_PUBLIC_ENDPOINT attribute in {@link ServletContext} with {@link Boolean}
   * publicEndpoint. set to true for publicly accessible HTTP(S) server. If true then incoming *
   * tracecontext will be added as a link instead of as a parent. By default it is set to true.
   *
   * @since 0.20
   */
  public static final String OC_PUBLIC_ENDPOINT = "opencensus.public_endpoint";

  static final String EXCEPTION_MESSAGE = "Invalid value for attribute ";

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

  /** Creates a new {@code OcHttpServletFilter}. */
  public OcHttpServletFilter() {
    TraceConfig traceConfig = Tracing.getTraceConfig();
    traceConfig.updateActiveTraceParams(traceConfig.getActiveTraceParams().toBuilder().build());
    handler = buildHttpServerHandler();
  }

  static HttpServerHandler<HttpServletRequest, HttpServletResponse, HttpServletRequest>
      buildHttpServerHandler() {
    return buildHttpServerHandlerWithOptions(
        new OcHttpServletExtractor(),
        Tracing.getPropagationComponent().getTraceContextFormat(),
        /* publicEndpoint= */ false);
  }

  static HttpServerHandler<HttpServletRequest, HttpServletResponse, HttpServletRequest>
      buildHttpServerHandlerWithOptions(
          HttpExtractor<HttpServletRequest, HttpServletResponse> extractor,
          TextFormat propagator,
          Boolean publicEndpoint) {
    return new HttpServerHandler<HttpServletRequest, HttpServletResponse, HttpServletRequest>(
        Tracing.getTracer(), extractor, propagator, getter, publicEndpoint);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void init(FilterConfig filterConfig) throws ServletException {
    if (handler == null) {
      throw new ServletException("Failed to build HttpServerHandler");
    }
    TextFormat propagator = null;
    HttpExtractor<HttpServletRequest, HttpServletResponse> extractor = null;
    Boolean publicEndpoint = null;

    ServletContext context = filterConfig.getServletContext();
    Object obj = context.getAttribute(OC_TRACE_PROPAGATOR);
    if (obj != null) {
      if (obj instanceof TextFormat) {
        propagator = (TextFormat) obj;
      } else {
        throw new ServletException(EXCEPTION_MESSAGE + OC_TRACE_PROPAGATOR);
      }
    } else {
      propagator = Tracing.getPropagationComponent().getTraceContextFormat();
    }

    obj = context.getAttribute(OC_EXTRACTOR);
    if (obj != null) {
      if (obj instanceof HttpExtractor) {
        extractor = (HttpExtractor<HttpServletRequest, HttpServletResponse>) obj;
      } else {
        throw new ServletException(EXCEPTION_MESSAGE + OC_EXTRACTOR);
      }
    } else {
      extractor = new OcHttpServletExtractor();
    }

    String publicEndVal = context.getInitParameter(OC_PUBLIC_ENDPOINT);
    if (publicEndVal != null) {
      publicEndpoint = Boolean.parseBoolean(publicEndVal);
    } else {
      publicEndpoint = false;
    }
    handler = buildHttpServerHandlerWithOptions(extractor, propagator, publicEndpoint);
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    // only interested in http requests
    if ((request instanceof HttpServletRequest) && (response instanceof HttpServletResponse)) {
      HttpServletRequest httpReq = (HttpServletRequest) request;
      HttpServletResponse httpResp = (HttpServletResponse) response;

      HttpRequestContext context = handler.handleStart(httpReq, httpReq);
      OcHttpServletListener listener = new OcHttpServletListener(handler, context);
      httpReq.setAttribute(OcHttpServletUtil.OPENCENSUS_SERVLET_LISTENER, listener);

      int length = httpReq.getContentLength();
      if (length > 0) {
        handler.handleMessageReceived(context, length);
      }

      Scope scope = Tracing.getTracer().withSpan(handler.getSpanFromContext(context));
      try {
        chain.doFilter(httpReq, httpResp);
      } finally {
        scope.close();
      }

      if (httpReq.isAsyncStarted()) {
        AsyncContext async = httpReq.getAsyncContext();
        async.addListener(listener, httpReq, httpResp);
      } else {
        OcHttpServletUtil.recordMessageSentEvent(handler, context, httpResp);
        handler.handleEnd(context, httpReq, httpResp, null);
      }
    } else {
      // pass request through unchanged
      chain.doFilter(request, response);
    }
  }

  @Override
  public void destroy() {}
}
