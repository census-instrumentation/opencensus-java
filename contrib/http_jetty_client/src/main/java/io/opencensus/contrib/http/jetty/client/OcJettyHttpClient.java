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

package io.opencensus.contrib.http.jetty.client;

import com.google.common.annotations.VisibleForTesting;
import io.opencensus.common.ExperimentalApi;
import io.opencensus.contrib.http.HttpClientHandler;
import io.opencensus.contrib.http.HttpExtractor;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.propagation.TextFormat;
import io.opencensus.trace.propagation.TextFormat.Setter;
import java.net.URI;
import javax.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpClientTransport;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 * This class is a wrapper to {@link HttpClient}. It enables tracing for all {@link Request} created
 * using this client.
 *
 * @since 0.19
 */
@ExperimentalApi
public final class OcJettyHttpClient extends HttpClient {
  private static final Setter<Request> setter =
      new Setter<Request>() {
        @Override
        public void put(Request carrier, String key, String value) {
          carrier.header(key, value);
        }
      };

  private static final Tracer tracer = Tracing.getTracer();
  @VisibleForTesting final HttpClientHandler<Request, Response, Request> handler;

  /** Create a new {@code OcJettyHttpClient}. */
  public OcJettyHttpClient() {
    super();
    handler = buildHandler(null, null);
  }

  /**
   * Create a new {@code OcJettyHttpClient} with support for HTTPS, extractor and propagator.
   *
   * @param transport {@link HttpClientTransport} The transport implementation.
   * @param sslContextFactory {@link SslContextFactory} Used to configure SSL connectors.
   * @param extractor {@link HttpExtractor} to extract request and response specific attributes. If
   *     it is null then default extractor is used.
   * @param propagator {@link TextFormat} to propagate trace context to remote peer. If it is null
   *     then default propagator (TraceContextFormat) is used.
   * @since 0.20
   */
  public OcJettyHttpClient(
      HttpClientTransport transport,
      SslContextFactory sslContextFactory,
      @Nullable HttpExtractor<Request, Response> extractor,
      @Nullable TextFormat propagator) {
    super(transport, sslContextFactory);
    handler = buildHandler(extractor, propagator);
  }

  private static HttpClientHandler<Request, Response, Request> buildHandler(
      @Nullable HttpExtractor<Request, Response> extractor, @Nullable TextFormat propagator) {
    if (extractor == null) {
      extractor = new OcJettyHttpClientExtractor();
    }

    if (propagator == null) {
      propagator = Tracing.getPropagationComponent().getTraceContextFormat();
    }

    return new HttpClientHandler<Request, Response, Request>(
        Tracing.getTracer(), extractor, propagator, setter);
  }

  /**
   * Returns a new request created from a given {@link URI}.
   *
   * @param uri {@link URI} to create new request.
   * @return {@link Request}
   */
  @Override
  public Request newRequest(URI uri) {
    Request request = super.newRequest(uri);
    Request.Listener listener = new HttpRequestListener(tracer.getCurrentSpan(), handler);
    request.listener(listener);
    request.onComplete((Response.CompleteListener) listener);
    request.onResponseContent((Response.ContentListener) listener);
    return request;
  }
}
