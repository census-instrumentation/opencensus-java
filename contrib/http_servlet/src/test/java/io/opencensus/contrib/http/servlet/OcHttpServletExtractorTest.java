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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link OcHttpServletExtractor}. */
@RunWith(JUnit4.class)
public class OcHttpServletExtractorTest {

  @SuppressWarnings("JdkObsolete")
  private final StringBuffer urlBuffer = new StringBuffer("http://example.com:8080/user/foo");

  @Test
  public void testExtraction() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    OcHttpServletExtractor extractor = new OcHttpServletExtractor();
    when(request.getServerName()).thenReturn("example.com");
    when(request.getMethod()).thenReturn("GET");
    when(request.getHeader("User-Agent")).thenReturn("Test 1.0");
    when(request.getRequestURI()).thenReturn("/user/foo");
    when(request.getQueryString()).thenReturn("a=b");
    when(response.getStatus()).thenReturn(0);
    when(request.getRequestURL()).thenReturn(urlBuffer);

    assertThat(extractor.getHost(request)).contains("example.com");
    assertThat(extractor.getMethod(request)).contains("GET");
    assertThat(extractor.getPath(request)).contains("/user/foo");
    assertThat(extractor.getUserAgent(request)).contains("Test 1.0");
    assertThat(extractor.getStatusCode(response)).isEqualTo(0);
    assertThat(extractor.getRoute(request)).contains("");
    assertThat(extractor.getUrl(request)).contains(urlBuffer.toString() + "?" + "a=b");
  }
}
