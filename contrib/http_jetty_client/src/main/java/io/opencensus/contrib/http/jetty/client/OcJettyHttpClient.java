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
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.propagation.TextFormat.Setter;
import java.net.URI;
import java.util.concurrent.atomic.AtomicLong;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;

/**
 * This class is a wrapper to {@link HttpClient}. It enables tracing for all {@link Request} created
 * using this client.
 */
@ExperimentalApi
public final class OcJettyHttpClient extends HttpClient {
  @VisibleForTesting
  static final Setter<Request> setter =
      new Setter<Request>() {
        @Override
        public void put(Request carrier, String key, String value) {
          carrier.header(key, value);
        }
      };

  private final AtomicLong reqId = new AtomicLong();

  private static final Tracer tracer = Tracing.getTracer();
  @VisibleForTesting private final HttpClientHandler<Request, Response, Request> handler;

  /** Create a new {@code OcJettyHttpClient}. */
  public OcJettyHttpClient() {
    super();
    handler =
        new HttpClientHandler<Request, Response, Request>(
            Tracing.getTracer(),
            new OcJettyHttpClientExtractor(),
            Tracing.getPropagationComponent().getTraceContextFormat(),
            setter);
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
    Request.Listener listener =
        new HttpRequestListener(tracer.getCurrentSpan(), handler, reqId.addAndGet(1L));
    request.listener(listener);
    request.onComplete((Response.CompleteListener) listener);
    request.onResponseContent((Response.ContentListener) listener);
    return request;
  }
}
