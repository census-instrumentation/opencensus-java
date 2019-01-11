/*
 * Copyright 2019, OpenCensus Authors
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

package io.opencensus.contrib.http.jaxrs;

import io.opencensus.contrib.http.HttpExtractor;
import io.opencensus.contrib.http.HttpRequestContext;
import io.opencensus.contrib.http.HttpServerHandler;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.propagation.TextFormat;
import io.opencensus.trace.propagation.TextFormat.Getter;
import java.io.IOException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

/**
 * JAX-RS request and response filter to provide instrumentation of JAX-RS based endpoint with
 * OpenCensus.
 *
 * @since 0.19
 */
@Provider
public class JaxrsContainerFilter implements ContainerRequestFilter, ContainerResponseFilter {

  private static final String CONTEXT_PROPERTY = "opencensus.context";
  private static final Getter<ContainerRequestContext> GETTER = new Getter<ContainerRequestContext>() {
    @Override
    public String get(ContainerRequestContext request, String key) {
      return request.getHeaderString(key);
    }
  };

  private final HttpServerHandler<ContainerRequestContext, ContainerResponseContext, ContainerRequestContext> handler;

  /**
   * Default constructor construct new instance with {@link JaxrsContainerExtractor}, {@link
   * Tracing#getPropagationComponent()#getTraceContextFormat()} and as public endpoint.
   *
   * @see #JaxrsContainerFilter(HttpExtractor, TextFormat, Boolean)
   */
  public JaxrsContainerFilter() {
    this(new JaxrsContainerExtractor(), Tracing.getPropagationComponent().getTraceContextFormat(),
        true);
  }

  /**
   * Construct instance with custom configuration.
   *
   * @param extractor the {@code HttpExtractor} used to extract information from the
   * request/response.
   * @param propagationFormat the {@code TextFormat} used in HTTP propagation.
   * @param publicEndpoint set to true for publicly accessible HTTP(S) server. If true then incoming
   * tracecontext will be added as a link instead of as a parent.
   */
  public JaxrsContainerFilter(
      HttpExtractor<ContainerRequestContext, ContainerResponseContext> extractor,
      TextFormat propagationFormat, Boolean publicEndpoint) {
    this.handler = new HttpServerHandler<ContainerRequestContext, ContainerResponseContext, ContainerRequestContext>(
        Tracing.getTracer(),
        extractor,
        propagationFormat,
        GETTER,
        publicEndpoint);
  }

  @Override
  public void filter(ContainerRequestContext requestContext) throws IOException {
    HttpRequestContext context = handler.handleStart(requestContext, requestContext);
    requestContext.setProperty(CONTEXT_PROPERTY, context);
    if (requestContext.getLength() > 0) {
      handler.handleMessageReceived(context, requestContext.getLength());
    }
  }

  @Override
  public void filter(ContainerRequestContext requestContext,
      ContainerResponseContext responseContext) throws IOException {
    if (requestContext.getProperty(CONTEXT_PROPERTY) == null) {
      // JAX-RS response filters are always invoked - we only want to record something if
      // request came through this filter
      return;
    }
    HttpRequestContext context = (HttpRequestContext) requestContext.getProperty(CONTEXT_PROPERTY);
    if (responseContext.getLength() > 0) {
      handler.handleMessageSent(context, responseContext.getLength());
    }
    handler.handleEnd(context, requestContext, responseContext, null);
  }
}
