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
import static io.opencensus.contrib.http.servlet.OcHttpServletFilter.EXCEPTION_MESSAGE;
import static io.opencensus.contrib.http.servlet.OcHttpServletFilter.OC_EXTRACTOR;
import static io.opencensus.contrib.http.servlet.OcHttpServletFilter.OC_PUBLIC_ENDPOINT;
import static io.opencensus.contrib.http.servlet.OcHttpServletFilter.OC_TRACE_PROPAGATOR;
import static io.opencensus.contrib.http.servlet.OcHttpServletUtil.CONTENT_LENGTH;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opencensus.contrib.http.HttpExtractor;
import io.opencensus.contrib.http.HttpRequestContext;
import io.opencensus.contrib.http.HttpServerHandler;
import io.opencensus.trace.Span;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.propagation.TextFormat;
import java.io.IOException;
import java.util.List;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncListener;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

/** Unit tests for {@link OcHttpServletFilter}. */
@RunWith(JUnit4.class)
public class OcHttpServletFilterTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  @Mock HttpServletResponse mockResponse;
  @Mock HttpServletRequest mockRequest;
  @Mock FilterChain mockChain;
  @Mock FilterConfig mockConfig;
  @Mock HttpServerHandler<HttpServletRequest, HttpServletResponse, HttpServletRequest> mockHandler;
  @Mock Span mockSpan;
  @Mock AsyncContext mockAsyncContext;
  @Mock HttpRequestContext mockContext;
  @Mock ServletContext mockServletContext;
  TextFormat b3Propagator;
  HttpExtractor<HttpServletRequest, HttpServletResponse> customExtractor;
  Boolean publicEndpoint = false;
  @Spy OcHttpServletFilter filter = new OcHttpServletFilter();
  @Captor ArgumentCaptor<String> stringArgumentCaptor;
  Object dummyAttr = new Object();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    b3Propagator = Tracing.getPropagationComponent().getB3Format();
    customExtractor = new OcHttpServletExtractor();
  }

  @Test
  public void testInit() throws IOException, ServletException {

    HttpServerHandler<HttpServletRequest, HttpServletResponse, HttpServletRequest> oldHandler =
        filter.handler;
    when(mockConfig.getServletContext()).thenReturn(mockServletContext);
    when(mockServletContext.getAttribute(OC_TRACE_PROPAGATOR)).thenReturn(null);
    when(mockServletContext.getAttribute(OC_EXTRACTOR)).thenReturn(null);
    when(mockServletContext.getAttribute(OC_PUBLIC_ENDPOINT)).thenReturn(null);

    filter.init(mockConfig);

    verify(mockConfig).getServletContext();
    verify(mockServletContext, times(3)).getAttribute(stringArgumentCaptor.capture());

    List<String> attributes = stringArgumentCaptor.getAllValues();
    assertThat(attributes.contains(OC_TRACE_PROPAGATOR)).isTrue();
    assertThat(attributes.contains(OC_EXTRACTOR)).isTrue();
    assertThat(attributes.contains(OC_PUBLIC_ENDPOINT)).isTrue();
    assertThat(filter.handler).isNotEqualTo(oldHandler);
    assertThat(filter.handler).isNotNull();
  }

  @Test
  public void testInitWithOptions() throws IOException, ServletException {

    HttpServerHandler<HttpServletRequest, HttpServletResponse, HttpServletRequest> oldHandler =
        filter.handler;
    when(mockConfig.getServletContext()).thenReturn(mockServletContext);
    when(mockServletContext.getAttribute(OC_TRACE_PROPAGATOR)).thenReturn(b3Propagator);
    when(mockServletContext.getAttribute(OC_EXTRACTOR)).thenReturn(customExtractor);
    when(mockServletContext.getAttribute(OC_PUBLIC_ENDPOINT)).thenReturn(publicEndpoint);

    filter.init(mockConfig);

    verify(mockConfig).getServletContext();
    verify(mockServletContext, times(3)).getAttribute(stringArgumentCaptor.capture());

    List<String> attributes = stringArgumentCaptor.getAllValues();
    assertThat(attributes.contains(OC_TRACE_PROPAGATOR)).isTrue();
    assertThat(attributes.contains(OC_EXTRACTOR)).isTrue();
    assertThat(attributes.contains(OC_PUBLIC_ENDPOINT)).isTrue();
    assertThat(filter.handler).isNotEqualTo(oldHandler);
    assertThat(filter.handler).isNotNull();
  }

  private void testInitInvalidAttr(String attr) throws ServletException {
    when(mockConfig.getServletContext()).thenReturn(mockServletContext);
    when(mockServletContext.getAttribute(attr)).thenReturn(dummyAttr);
    thrown.expect(ServletException.class);
    thrown.expectMessage(EXCEPTION_MESSAGE + attr);

    filter.init(mockConfig);
  }

  @Test
  public void testInitInvalidExtractor() throws ServletException {
    testInitInvalidAttr(OC_EXTRACTOR);
  }

  @Test
  public void testInitInvalidPropagator() throws ServletException {
    testInitInvalidAttr(OC_TRACE_PROPAGATOR);
  }

  @Test
  public void testInitInvalidPublicEndpoint() throws ServletException {
    testInitInvalidAttr(OC_PUBLIC_ENDPOINT);
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
