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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opencensus.contrib.http.HttpClientHandler;
import io.opencensus.contrib.http.HttpExtractor;
import io.opencensus.contrib.http.HttpRequestContext;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.propagation.TextFormat.Setter;
import javax.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link HttpRequestListener}. */
@RunWith(JUnit4.class)
public class HttpRequestListenerTest {
  private Request request;
  private HttpRequestListener listener;
  private HttpRequestListener listenerWithMockhandler;
  private HttpRequestContext context;
  private final Object requestObj = new Object();
  private final Object responseObj = new Object();
  static final Setter<Object> setter =
      new Setter<Object>() {
        @Override
        public void put(Object carrier, String key, String val) {}
      };

  private final HttpExtractor<Object, Object> extractor =
      new HttpExtractor<Object, Object>() {
        @Nullable
        @Override
        public String getRoute(Object request) {
          return "";
        }

        @Nullable
        @Override
        public String getUrl(Object request) {
          return "";
        }

        @Nullable
        @Override
        public String getHost(Object request) {
          return "";
        }

        @Nullable
        @Override
        public String getMethod(Object request) {
          return "";
        }

        @Nullable
        @Override
        public String getPath(Object request) {
          return "";
        }

        @Nullable
        @Override
        public String getUserAgent(Object request) {
          return "";
        }

        @Override
        public int getStatusCode(@Nullable Object response) {
          return 0;
        }
      };

  private final HttpClientHandler<Object, Object, Object> handler =
      new HttpClientHandler<Object, Object, Object>(
          Tracing.getTracer(),
          extractor,
          Tracing.getPropagationComponent().getTraceContextFormat(),
          setter) {};
  @Mock private HttpClientHandler<Request, Response, Request> mockHandler;
  @Mock private Result mockResult;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    OcJettyHttpClient client = new OcJettyHttpClient();
    request = client.newRequest("http://www.example.com/foo");
    listener = request.getRequestListeners(HttpRequestListener.class).get(0);

    // for onComplete() test
    context = handler.handleStart(null, requestObj, responseObj);
    listenerWithMockhandler = new HttpRequestListener(null, mockHandler);
  }

  @Test
  public void testOnBegin() {
    listener.onBegin(request);
    assertThat(listener.context).isNotNull();
  }

  @Test
  public void testOnCompleteWithNullResult() {
    listenerWithMockhandler.context = context;
    listenerWithMockhandler.onComplete(null);
    verify(mockHandler).handleEnd(context, null, null, null);
  }

  @Test
  public void testOnComplete() {
    listenerWithMockhandler.context = context;
    when(mockResult.getFailure()).thenReturn(null);
    when(mockResult.getRequest()).thenReturn(null);
    when(mockResult.getResponse()).thenReturn(null);
    listenerWithMockhandler.onComplete(null);
    verify(mockHandler).handleEnd(context, null, null, null);
  }
}
