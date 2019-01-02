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

import static com.google.common.truth.Truth.assertThat;
import static io.opencensus.contrib.http.servlet.OcHttpServletUtil.CONTENT_LENGTH;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opencensus.contrib.http.HttpRequestContext;
import io.opencensus.contrib.http.HttpServerHandler;
import io.opencensus.trace.Span;
import java.io.IOException;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncListener;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link OcHttpServletFilter}. */
@RunWith(JUnit4.class)
public class OcHttpServletFilterTest {
  @Mock HttpServletResponse mockResponse;
  @Mock HttpServletRequest mockRequest;
  @Mock FilterChain mockChain;
  @Mock FilterConfig mockConfig;
  @Mock HttpServerHandler<HttpServletRequest, HttpServletResponse, HttpServletRequest> mockHandler;
  @Mock Span mockSpan;
  @Mock AsyncContext mockAsyncContext;
  @Mock HttpRequestContext mockContext;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testInit() throws IOException, ServletException {

    OcHttpServletFilter filter = new OcHttpServletFilter();
    filter.init(mockConfig);
    assertThat(filter.handler).isNotNull();
  }

  @Test
  public void testDoFilterSync() throws IOException, ServletException {

    OcHttpServletFilter filter = new OcHttpServletFilter();

    when(mockRequest.isAsyncStarted()).thenReturn(false);
    when(mockResponse.getHeader("")).thenReturn("");
    when(mockResponse.getHeader(CONTENT_LENGTH)).thenReturn("10");
    when(mockRequest.getContentLength()).thenReturn(10);

    filter.doFilter(mockRequest, mockResponse, mockChain);
    verify(mockRequest).getContentLength();
  }

  @Test
  public void testDoFilterAsync() throws IOException, ServletException {

    OcHttpServletFilter filter = new OcHttpServletFilter();

    when(mockRequest.isAsyncStarted()).thenReturn(true);
    when(mockResponse.getHeader("")).thenReturn("");
    when(mockResponse.getHeader(CONTENT_LENGTH)).thenReturn("10");
    when(mockRequest.getContentLength()).thenReturn(10);
    when(mockRequest.getAsyncContext()).thenReturn(mockAsyncContext);
    doNothing()
        .when(mockAsyncContext)
        .addListener(
            any(AsyncListener.class),
            any(HttpServletRequest.class),
            any(HttpServletResponse.class));

    filter.doFilter(mockRequest, mockResponse, mockChain);
    verify(mockResponse, never()).getHeader(CONTENT_LENGTH);
    verify(mockRequest).getContentLength();
    verify(mockAsyncContext)
        .addListener(
            any(AsyncListener.class),
            any(HttpServletRequest.class),
            any(HttpServletResponse.class));
  }
}
