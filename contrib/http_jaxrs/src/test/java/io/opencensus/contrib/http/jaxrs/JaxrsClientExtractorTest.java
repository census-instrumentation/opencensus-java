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
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
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
