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

import static io.opencensus.contrib.http.servlet.OcHttpServletUtil.CONTENT_LENGTH;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opencensus.contrib.http.HttpExtractor;
import io.opencensus.contrib.http.HttpRequestContext;
import io.opencensus.contrib.http.HttpServerHandler;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.propagation.TextFormat.Getter;
import javax.annotation.Nullable;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link OcHttpServletListener}. */
@RunWith(JUnit4.class)
public class OcHttpServletListenerTest {

  static final Getter<Object> getter =
      new Getter<Object>() {
        @Nullable
        @Override
        public String get(Object carrier, String key) {
          return "";
        }
      };
  private final Object request = new Object();
  private final Object response = new Object();
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
  private final HttpServerHandler<Object, Object, Object> handler =
      new HttpServerHandler<Object, Object, Object>(
          Tracing.getTracer(),
          extractor,
          Tracing.getPropagationComponent().getTraceContextFormat(),
          getter,
          true) {};
  @Mock HttpRequestContext mockContext;
  @Mock HttpServletResponse mockResponse;
  @Mock HttpServletRequest mockRequest;
  @Mock HttpServerHandler<HttpServletRequest, HttpServletResponse, HttpServletRequest> mockHandler;
  @Mock AsyncEvent mockAsyncEvent;
  @Mock Throwable mockThrowable;
  @Mock AsyncContext mockAsyncContext;
  OcHttpServletListener listener;
  private HttpRequestContext context;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    context = handler.handleStart(request, response);

    listener = new OcHttpServletListener(mockHandler, context);

    when(mockHandler.handleStart(mockRequest, mockRequest)).thenReturn(context);
    when(mockAsyncEvent.getThrowable()).thenReturn(mockThrowable);
    when(mockAsyncEvent.getSuppliedResponse()).thenReturn(mockResponse);
    when(mockAsyncEvent.getSuppliedRequest()).thenReturn(mockRequest);
    when(mockAsyncEvent.getAsyncContext()).thenReturn(mockAsyncContext);
    when(mockResponse.getHeader(CONTENT_LENGTH)).thenReturn("10");
    when(mockRequest.getContentLength()).thenReturn(10);

    doNothing().when(mockHandler).handleEnd(mockContext, mockRequest, mockResponse, null);
  }

  @Test
  public void testOnComplete() {
    listener.onComplete(mockAsyncEvent);
    verify(mockHandler).handleEnd(context, mockRequest, mockResponse, null);
  }

  @Test
  public void testOnTimeout() {
    listener.onTimeout(mockAsyncEvent);
    verify(mockHandler).handleEnd(context, mockRequest, mockResponse, null);
  }

  @Test
  public void testOnError() {
    doNothing().when(mockHandler).handleEnd(mockContext, mockRequest, mockResponse, mockThrowable);
    listener.onError(mockAsyncEvent);
    verify(mockHandler).handleEnd(context, mockRequest, mockResponse, mockThrowable);
  }

  @Test
  public void testOnStartAsync() {
    doNothing().when(mockHandler).handleEnd(mockContext, mockRequest, mockResponse, null);
    listener.onStartAsync(mockAsyncEvent);
    verify(mockHandler, never()).handleEnd(mockContext, mockRequest, mockResponse, null);
    verify(mockAsyncContext)
        .addListener(
            any(OcHttpServletListener.class),
            any(ServletRequest.class),
            any(ServletResponse.class));
  }
}
