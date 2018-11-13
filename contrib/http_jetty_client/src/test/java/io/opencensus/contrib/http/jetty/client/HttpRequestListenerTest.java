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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opencensus.contrib.http.HttpClientHandler;
import io.opencensus.trace.Span;
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
  @Mock private Span mockSpan;
  @Mock private Request mockRequest;
  @Mock private Result mockResult;
  @Mock private HttpClientHandler<Request, Response, Request> mockHandler;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testListener() {
    HttpRequestListener listener = new HttpRequestListener(mockSpan, mockHandler);
    when(mockHandler.handleStart(any(Span.class), any(Request.class), any(Request.class)))
        .thenReturn(mockSpan);
    doNothing()
        .when(mockHandler)
        .handleEnd(any(Span.class), any(Response.class), any(Throwable.class));

    listener.onBegin(mockRequest);
    verify(mockHandler).handleStart(any(Span.class), any(Request.class), any(Request.class));

    listener.onComplete(mockResult);
    verify(mockHandler).handleEnd(any(Span.class), any(Response.class), any(Throwable.class));
  }
}
