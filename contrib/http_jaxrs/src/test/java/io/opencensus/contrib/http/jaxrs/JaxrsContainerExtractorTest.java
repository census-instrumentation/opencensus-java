package io.opencensus.contrib.http.jaxrs;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Collections;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.core.UriInfo;
import org.junit.Test;

public class JaxrsContainerExtractorTest {
  @Test
  public void testExtraction() {
    UriInfo uriInfo = mock(UriInfo.class);
    when(uriInfo.getPath()).thenReturn("mypath");
    when(uriInfo.getMatchedURIs()).thenReturn(Collections.singletonList("/resource/{route}"));
    when(uriInfo.getRequestUri()).thenReturn(URI.create("https://myhost/resource/1"));

    ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
    when(requestContext.getHeaderString("host")).thenReturn("myhost");
    when(requestContext.getMethod()).thenReturn("GET");
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(requestContext.getHeaderString("user-agent")).thenReturn("java/1.8");

    ContainerResponseContext responseContext = mock(ContainerResponseContext.class);
    when(responseContext.getStatus()).thenReturn(200);

    JaxrsContainerExtractor extractor = new JaxrsContainerExtractor();
    assertEquals("myhost", extractor.getHost(requestContext));
    assertEquals("GET", extractor.getMethod(requestContext));
    assertEquals("mypath", extractor.getPath(requestContext));
    assertEquals("/resource/{route}", extractor.getRoute(requestContext));
    assertEquals("https://myhost/resource/1", extractor.getUrl(requestContext));
    assertEquals("java/1.8", extractor.getUserAgent(requestContext));
    assertEquals(200, extractor.getStatusCode(responseContext));
  }

}
