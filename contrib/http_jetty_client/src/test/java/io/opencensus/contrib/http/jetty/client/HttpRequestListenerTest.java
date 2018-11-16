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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opencensus.contrib.http.HttpClientHandler;
import io.opencensus.trace.Span;
import io.opencensus.trace.Tracing;
import java.nio.ByteBuffer;
import org.eclipse.jetty.client.api.ContentProvider;
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
  @Mock private Request mockRequest;
  @Mock private Result mockResult;
  @Mock private HttpClientHandler<Request, Response, Request> mockHandler;
  private static final long REQ_ID = 1L;
  @Mock private ContentProvider mockContent;
  private Span realSpan;
  private final byte[] bytes = new byte[2];
  private final ByteBuffer buf = ByteBuffer.wrap(bytes);

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    realSpan = Tracing.getTracer().spanBuilder("testSpan").startSpan();
  }

  @Test
  public void testListener() {
    HttpRequestListener listener = new HttpRequestListener(realSpan, mockHandler, REQ_ID);
    when(mockHandler.handleStart(any(Span.class), any(Request.class), any(Request.class)))
        .thenReturn(realSpan);
    doNothing()
        .when(mockHandler)
        .handleEnd(any(Span.class), any(Response.class), any(Throwable.class));
    listener.onBegin(mockRequest);
    verify(mockHandler).handleStart(any(Span.class), any(Request.class), any(Request.class));

    when(mockResult.getRequest()).thenReturn(mockRequest);
    when(mockRequest.getContent()).thenReturn(mockContent);
    when(mockContent.getLength()).thenReturn(0L);
    listener.onComplete(mockResult);
    verify(mockHandler).handleEnd(any(Span.class), any(Response.class), any(Throwable.class));
    verify(mockRequest).getContent();
    verify(mockContent).getLength();
  }

  @Test
  public void testOnContent() {
    HttpRequestListener listener = new HttpRequestListener(realSpan, mockHandler, REQ_ID);
    when(mockHandler.handleStart(any(Span.class), any(Request.class), any(Request.class)))
        .thenReturn(realSpan);
    doNothing()
        .when(mockHandler)
        .handleEnd(any(Span.class), any(Response.class), any(Throwable.class));

    listener.onContent(mockRequest, buf);
    assertThat(listener.recvMessageSize).isEqualTo(2L);
    listener.onContent(mockRequest, buf);
    assertThat(listener.recvMessageSize).isEqualTo(4L);
  }
}
