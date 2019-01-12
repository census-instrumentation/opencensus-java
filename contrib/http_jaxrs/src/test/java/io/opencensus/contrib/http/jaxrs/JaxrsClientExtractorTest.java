package io.opencensus.contrib.http.jaxrs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import org.junit.Test;

public class JaxrsClientExtractorTest {

  @Test
  public void testExtraction() {
    URI uri = URI.create("https://myhost/resource");

    ClientRequestContext requestContext = mock(ClientRequestContext.class);
    when(requestContext.getUri()).thenReturn(uri);
    when(requestContext.getMethod()).thenReturn("GET");
    when(requestContext.getHeaderString("user-agent")).thenReturn("java/1.8");

    ClientResponseContext responseContext = mock(ClientResponseContext.class);
    when(responseContext.getStatus()).thenReturn(200);

    JaxrsClientExtractor extractor = new JaxrsClientExtractor();
    assertEquals("myhost", extractor.getHost(requestContext));
    assertEquals("GET", extractor.getMethod(requestContext));
    assertEquals("/resource", extractor.getPath(requestContext));
    assertNull(extractor.getRoute(requestContext));
    assertEquals("https://myhost/resource", extractor.getUrl(requestContext));
    assertEquals("java/1.8", extractor.getUserAgent(requestContext));
    assertEquals(200, extractor.getStatusCode(responseContext));
  }

}
