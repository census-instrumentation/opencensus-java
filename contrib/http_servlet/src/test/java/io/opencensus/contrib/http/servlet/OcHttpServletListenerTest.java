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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opencensus.contrib.http.HttpServerHandler;
import io.opencensus.trace.Span;
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

  @Mock Span mockSpan;
  @Mock HttpServletResponse mockResponse;
  @Mock HttpServletRequest mockRequest;
  @Mock HttpServerHandler<HttpServletRequest, HttpServletResponse, HttpServletRequest> mockHandler;
  @Mock AsyncEvent mockAsyncEvent;
  @Mock Throwable mockThrowable;
  @Mock AsyncContext mockAsyncContext;

  OcHttpServletListener listener;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    listener = new OcHttpServletListener(mockHandler, mockSpan);

    when(mockHandler.handleStart(mockRequest, mockRequest)).thenReturn(mockSpan);
    when(mockAsyncEvent.getThrowable()).thenReturn(mockThrowable);
    when(mockAsyncEvent.getSuppliedResponse()).thenReturn(mockResponse);
    when(mockAsyncEvent.getAsyncContext()).thenReturn(mockAsyncContext);

    doNothing().when(mockHandler).handleEnd(mockSpan, mockResponse, null);
  }

  @Test
  public void testOnComplete() {
    listener.onComplete(mockAsyncEvent);
    verify(mockHandler).handleEnd(mockSpan, mockResponse, null);
  }

  @Test
  public void testOnTimeout() {
    listener.onTimeout(mockAsyncEvent);
    verify(mockHandler).handleEnd(mockSpan, mockResponse, null);
  }

  @Test
  public void testOnError() {
    doNothing().when(mockHandler).handleEnd(mockSpan, mockResponse, mockThrowable);
    listener.onError(mockAsyncEvent);
    verify(mockHandler).handleEnd(mockSpan, mockResponse, mockThrowable);
  }

  @Test
  public void testOnStartAsync() {
    doNothing().when(mockHandler).handleEnd(mockSpan, mockResponse, null);
    listener.onStartAsync(mockAsyncEvent);
    verify(mockHandler, never()).handleEnd(mockSpan, mockResponse, null);
    verify(mockAsyncContext)
        .addListener(
            any(OcHttpServletListener.class),
            any(ServletRequest.class),
            any(ServletResponse.class));
  }
}
