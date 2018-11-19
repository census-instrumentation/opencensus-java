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

package io.opencensus.contrib.http;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opencensus.contrib.http.util.HttpTraceAttributeConstants;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

/** Unit tests for {@link AbstractHttpStats}. */
@RunWith(JUnit4.class)
public class HttpClientStatsTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();
  private final Object request = new Object();
  private final Object response = new Object();
  @Mock private HttpExtractor<Object, Object> extractor;
  private AbstractHttpStats<Object, Object> handler;
  Map<String, String> attributeMap = new HashMap<String, String>();
  @Spy private final HttpStatsCtx statsCtx = new HttpStatsCtx();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    handler = new HttpClientStats<Object, Object>(extractor) {};
    attributeMap.put(HttpTraceAttributeConstants.HTTP_HOST, "example.com");
    attributeMap.put(HttpTraceAttributeConstants.HTTP_ROUTE, "/get/:name");
    attributeMap.put(HttpTraceAttributeConstants.HTTP_PATH, "/get/helloworld");
    attributeMap.put(HttpTraceAttributeConstants.HTTP_METHOD, "GET");
    attributeMap.put(HttpTraceAttributeConstants.HTTP_USER_AGENT, "test 1.0");
    attributeMap.put(HttpTraceAttributeConstants.HTTP_URL, "http://example.com/get/helloworld");
  }

  @Test
  public void constructorDisallowNullExtractor() {
    thrown.expect(NullPointerException.class);
    new HttpClientStats<Object, Object>(null) {};
  }

  @Test
  public void testRequestStartAndEnd() {
    handler.requestStart(statsCtx, request);
    assertThat(statsCtx.requestStartTime).isGreaterThan(0L);

    when(extractor.getRoute(any(Object.class)))
        .thenReturn(attributeMap.get(HttpTraceAttributeConstants.HTTP_ROUTE));
    when(extractor.getHost(any(Object.class)))
        .thenReturn(attributeMap.get(HttpTraceAttributeConstants.HTTP_HOST));
    when(extractor.getPath(any(Object.class)))
        .thenReturn(attributeMap.get(HttpTraceAttributeConstants.HTTP_PATH));
    when(extractor.getMethod(any(Object.class)))
        .thenReturn(attributeMap.get(HttpTraceAttributeConstants.HTTP_METHOD));
    when(extractor.getUserAgent(any(Object.class)))
        .thenReturn(attributeMap.get(HttpTraceAttributeConstants.HTTP_USER_AGENT));
    when(extractor.getUrl(any(Object.class)))
        .thenReturn(attributeMap.get(HttpTraceAttributeConstants.HTTP_URL));

    statsCtx.addAndGetResponseMessageSize(10L);
    statsCtx.addAndGetRequestMessageSize(10L);
    handler.requestEnd(statsCtx, request, response, null);
    verify(extractor).getMethod(request);
    verify(extractor).getStatusCode(response);

    verify(statsCtx, times(1)).getResponseMessageSize();
    verify(statsCtx, times(1)).getRequestMessageSize();
  }

  @Test
  public void testDisallowedNullStatsCtxInRequestStart() {
    thrown.expect(NullPointerException.class);
    handler.requestStart(null, request);
  }

  @Test
  public void testDisallowedNullRequestInRequestStart() {
    thrown.expect(NullPointerException.class);
    handler.requestStart(statsCtx, null);
  }

  @Test
  public void testDisallowedNullStatsCtxInRequestEnd() {
    thrown.expect(NullPointerException.class);
    handler.requestEnd(null, response, response, null);
  }

  @Test
  public void testDisallowedNullRequestInRequestEnd() {
    thrown.expect(NullPointerException.class);
    handler.requestEnd(statsCtx, null, response, null);
  }

  @Test
  public void testDisallowedNullResponseInRequestEnd() {
    thrown.expect(NullPointerException.class);
    handler.requestEnd(statsCtx, request, null, null);
  }
}
