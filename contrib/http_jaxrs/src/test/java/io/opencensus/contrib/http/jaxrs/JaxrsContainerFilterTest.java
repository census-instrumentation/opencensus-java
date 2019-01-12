package io.opencensus.contrib.http.jaxrs;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
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
import javax.ws.rs.core.UriInfo;
import org.junit.Test;

public class JaxrsContainerFilterTest {

  JaxrsContainerFilter filter = new JaxrsContainerFilter();

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
    verify(responseContext, times(2)).getStatus();
  }

}
