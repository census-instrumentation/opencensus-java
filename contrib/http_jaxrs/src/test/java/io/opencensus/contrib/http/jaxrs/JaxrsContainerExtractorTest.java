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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Collections;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.UriInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class JaxrsContainerExtractorTest {

  @Test
  @SuppressWarnings("unchecked")
  public void testExtraction() throws Exception {
    UriInfo uriInfo = mock(UriInfo.class);
    when(uriInfo.getPath()).thenReturn("mypath");
    when(uriInfo.getMatchedURIs()).thenReturn(Collections.singletonList("/resource/{route}"));
    when(uriInfo.getRequestUri()).thenReturn(URI.create("https://myhost/resource/1"));

    ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
    when(requestContext.getHeaderString("host")).thenReturn("myhost");
    when(requestContext.getMethod()).thenReturn("GET");
    when(requestContext.getUriInfo()).thenReturn(uriInfo);
    when(requestContext.getHeaderString("user-agent")).thenReturn("java/1.8");

    ResourceInfo info = mock(ResourceInfo.class);
    when(info.getResourceClass()).thenReturn((Class) MyResource.class);
    when(info.getResourceMethod()).thenReturn(MyResource.class.getMethod("route"));

    ExtendedContainerRequest extendedRequest = new ExtendedContainerRequest(requestContext, info);

    ContainerResponseContext responseContext = mock(ContainerResponseContext.class);
    when(responseContext.getStatus()).thenReturn(200);

    JaxrsContainerExtractor extractor = new JaxrsContainerExtractor();
    assertEquals("myhost", extractor.getHost(extendedRequest));
    assertEquals("GET", extractor.getMethod(extendedRequest));
    assertEquals("mypath", extractor.getPath(extendedRequest));
    assertEquals("/resource/{route}", extractor.getRoute(extendedRequest));
    assertEquals("https://myhost/resource/1", extractor.getUrl(extendedRequest));
    assertEquals("java/1.8", extractor.getUserAgent(extendedRequest));
    assertEquals(200, extractor.getStatusCode(responseContext));
  }

  @Path("/resource")
  static class MyResource {

    @GET
    @Path("{route}")
    public String route() {
      return "OK";
    }
  }
}
