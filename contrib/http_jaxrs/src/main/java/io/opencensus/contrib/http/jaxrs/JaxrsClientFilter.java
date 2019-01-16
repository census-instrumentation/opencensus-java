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

import io.opencensus.contrib.http.HttpClientHandler;
import io.opencensus.contrib.http.HttpExtractor;
import io.opencensus.contrib.http.HttpRequestContext;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.propagation.TextFormat;
import io.opencensus.trace.propagation.TextFormat.Setter;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.ext.Provider;

/**
 * JAX-RS client request and response filter to provide instrumentation of client calls with
 * OpenCensus.
 *
 * @since 0.19
 */
@Provider
public class JaxrsClientFilter implements ClientRequestFilter, ClientResponseFilter {

  private static final String OPENCENSUS_CONTEXT = "opencensus.context";
  private static final Setter<ClientRequestContext> SETTER =
      new Setter<ClientRequestContext>() {
        @Override
        public void put(ClientRequestContext carrier, String key, String value) {
          carrier.getHeaders().putSingle(key, value);
        }
      };

  private final HttpClientHandler<ClientRequestContext, ClientResponseContext, ClientRequestContext>
      handler;

  /** Constructs new client filter with default configuration. */
  public JaxrsClientFilter() {
    this(new JaxrsClientExtractor(), Tracing.getPropagationComponent().getTraceContextFormat());
  }

  /**
   * Construct new client filter with custom configuration.
   *
   * @param extractor the {@code HttpExtractor} used to extract information from the
   *     request/response.
   * @param propagationFormat the {@code TextFormat} used in HTTP propagation.
   */
  public JaxrsClientFilter(
      HttpExtractor<ClientRequestContext, ClientResponseContext> extractor,
      TextFormat propagationFormat) {
    handler = new HttpClientHandler<>(Tracing.getTracer(), extractor, propagationFormat, SETTER);
  }

  @Override
  public void filter(ClientRequestContext requestContext) {
    HttpRequestContext context = handler.handleStart(null, requestContext, requestContext);
    requestContext.setProperty(OPENCENSUS_CONTEXT, context);
  }

  @Override
  public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) {
    HttpRequestContext context =
        (HttpRequestContext) requestContext.getProperty(OPENCENSUS_CONTEXT);
    handler.handleEnd(context, requestContext, responseContext, null);
  }
}
