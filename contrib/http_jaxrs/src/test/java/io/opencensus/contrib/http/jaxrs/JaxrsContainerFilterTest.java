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

package io.opencensus.contrib.http.jaxrs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opencensus.contrib.http.HttpRequestContext;
import io.opencensus.contrib.http.jaxrs.JaxrsClientFilterTest.FakeSpan;
import io.opencensus.tags.TagContext;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import java.util.Collections;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.UriInfo;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class JaxrsContainerFilterTest {

  @Mock ResourceInfo info;

  @InjectMocks JaxrsContainerFilter filter = new JaxrsContainerFilter();

  @Before
  public void setUp() {
    // Mockito in this test depends on some class that's only available on JDK 1.8:
    // TypeDescription$Generic$AnnotationReader$Dispatcher$ForJava8CapableVm
    Assume.assumeTrue(System.getProperty("java.version").startsWith("1.8"));
  }

  @Test
  public void testRequestFilter() throws Exception {
    UriInfo uriInfo = mock(UriInfo.class);
    ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    filter.filter(requestContext);
    verify(requestContext).setProperty(eq("opencensus.context"), any());
  }

  @Test
  public void testResponseFilter() throws Exception {
    Span span = new FakeSpan(SpanContext.INVALID, null);
    TagContext tagContext = mock(TagContext.class);

    HttpRequestContext context = JaxrsClientFilterTest.createHttpRequestContext(span, tagContext);

    UriInfo uriInfo = mock(UriInfo.class);
    when(uriInfo.getMatchedURIs()).thenReturn(Collections.singletonList("/resource/{route}"));

    ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
    when(requestContext.getProperty("opencensus.context")).thenReturn(context);
    when(requestContext.getUriInfo()).thenReturn(uriInfo);

    ContainerResponseContext responseContext = mock(ContainerResponseContext.class);
    filter.filter(requestContext, responseContext);
    verify(requestContext).getProperty("opencensus.context");
    verify(responseContext, times(1)).getStatus();
  }
}
