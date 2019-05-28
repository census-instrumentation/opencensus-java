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

package io.opencensus.contrib.spring.instrument.web.client;

import io.opencensus.common.ExperimentalApi;
import io.opencensus.common.Scope;
import io.opencensus.contrib.http.HttpClientHandler;
import io.opencensus.contrib.http.HttpExtractor;
import io.opencensus.contrib.http.HttpRequestContext;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.propagation.TextFormat.Setter;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.annotation.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

/**
 * This class intercepts {@link org.springframework.web.client.AsyncRestTemplate}.
 *
 * @since 0.23.0
 */
@SuppressWarnings("deprecation")
public final class TracingAsyncClientHttpRequestInterceptor
    implements org.springframework.http.client.AsyncClientHttpRequestInterceptor {
  final Tracer tracer;
  final HttpClientHandler<HttpRequest, ClientHttpResponse, HttpRequest> handler;

  private static final Setter<HttpRequest> setter =
      new Setter<HttpRequest>() {
        @Override
        public void put(HttpRequest carrier, String key, String value) {
          HttpHeaders hdrs = carrier.getHeaders();
          hdrs.set(key, value);
        }
      };

  /**
   * Create an instance of {@code TracingAsyncClientHttpRequestInterceptor}.
   *
   * @return {@code TracingAsyncClientHttpRequestInterceptor}
   * @since 0.23.0
   */
  public static TracingAsyncClientHttpRequestInterceptor create() {
    return new TracingAsyncClientHttpRequestInterceptor();
  }

  TracingAsyncClientHttpRequestInterceptor() {

    tracer = Tracing.getTracer();
    handler =
        new HttpClientHandler<HttpRequest, ClientHttpResponse, HttpRequest>(
            Tracing.getTracer(),
            new HttpClientExtractor(),
            Tracing.getPropagationComponent().getTraceContextFormat(),
            setter);
  }

  /**
   * It intercepts http requests and starts a span.
   *
   * @since 0.23.0
   */
  public ListenableFuture<ClientHttpResponse> intercept(
      HttpRequest request,
      byte[] body,
      org.springframework.http.client.AsyncClientHttpRequestExecution execution)
      throws IOException {
    HttpRequestContext context = handler.handleStart(tracer.getCurrentSpan(), request, request);

    Scope ws = tracer.withSpan(handler.getSpanFromContext(context));
    try {
      ListenableFuture<ClientHttpResponse> result = execution.executeAsync(request, body);
      result.addCallback(
          new TracingAsyncClientHttpRequestInterceptor.TraceListenableFutureCallback(
              context, handler));
      return result;
    } catch (IOException e) {
      handler.handleEnd(context, null, null, e);
      throw e;
    } finally {
      if (ws != null) {
        ws.close();
      }
    }
  }

  static final class TraceListenableFutureCallback
      implements ListenableFutureCallback<ClientHttpResponse> {
    HttpRequestContext context;
    final HttpClientHandler<HttpRequest, ClientHttpResponse, HttpRequest> handler;

    TraceListenableFutureCallback(
        HttpRequestContext context,
        HttpClientHandler<HttpRequest, ClientHttpResponse, HttpRequest> handler) {
      this.context = context;
      this.handler = handler;
    }

    public void onFailure(Throwable ex) {
      handler.handleEnd(context, null, null, ex);
    }

    public void onSuccess(@Nullable ClientHttpResponse result) {
      handler.handleEnd(context, null, result, (Throwable) null);
    }
  }

  /** This class extracts attributes from {@link HttpRequest} and {@link ClientHttpResponse}. */
  @ExperimentalApi
  static final class HttpClientExtractor extends HttpExtractor<HttpRequest, ClientHttpResponse> {
    @Override
    public String getHost(HttpRequest request) {
      return request.getURI().getHost();
    }

    @Override
    @Nullable
    public String getMethod(HttpRequest request) {
      HttpMethod method = request.getMethod();
      if (method != null) {
        return method.toString();
      }
      return null;
    }

    @Override
    public String getPath(HttpRequest request) {
      return request.getURI().getPath();
    }

    @Override
    @Nullable
    public String getUserAgent(HttpRequest request) {
      return request.getHeaders().getFirst("User-Agent");
    }

    @Override
    public int getStatusCode(@Nullable ClientHttpResponse response) {
      if (response != null) {
        try {
          return response.getStatusCode().value();
        } catch (Exception e) {
          return 0;
        }
      }
      return 0;
    }

    @Override
    public String getRoute(HttpRequest request) {
      return "";
    }

    @Override
    public String getUrl(HttpRequest request) {
      try {
        return request.getURI().toURL().toString();
      } catch (MalformedURLException e) {
        return "";
      }
    }
  }
}
