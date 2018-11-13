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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpFields;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link OcJettyHttpClientExtractor}. */
@RunWith(JUnit4.class)
public class OcJettyHttpClientExtractorTest {
  private static final String URI_STR = "http://localhost/test/foo";
  private URI uri;

  @Before
  public void setUp() throws URISyntaxException {
    uri = new URI(URI_STR);
  }

  @Test
  public void testExtraction() {
    HttpFields fields = new HttpFields();
    fields.add(new HttpField("User-Agent", "Test 1.0"));

    Request request = mock(Request.class);
    Response response = mock(Response.class);
    OcJettyHttpClientExtractor extractor = new OcJettyHttpClientExtractor();
    when(request.getHost()).thenReturn("localhost");
    when(request.getMethod()).thenReturn("GET");
    when(request.getHeaders()).thenReturn(fields);
    when(request.getPath()).thenReturn("/test");
    when(request.getURI()).thenReturn(uri);
    when(response.getStatus()).thenReturn(0);

    assertThat(extractor.getHost(request)).contains("localhost");
    assertThat(extractor.getMethod(request)).contains("GET");
    assertThat(extractor.getPath(request)).contains("/test");
    assertThat(extractor.getUrl(request)).contains(URI_STR);
    assertThat(extractor.getRoute(request)).contains("");
    assertThat(extractor.getUserAgent(request)).contains("Test 1.0");
    assertThat(extractor.getStatusCode(response)).isEqualTo(0);
  }
}
