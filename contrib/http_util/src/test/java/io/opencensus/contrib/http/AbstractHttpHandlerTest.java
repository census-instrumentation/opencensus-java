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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.EndSpanOptions;
import io.opencensus.trace.MessageEvent;
import io.opencensus.trace.MessageEvent.Type;
import io.opencensus.trace.Span;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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

/** Unit tests for {@link AbstractHttpHandler}. */
@RunWith(JUnit4.class)
public class AbstractHttpHandlerTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();
  private final Object request = new Object();
  private final Object response = new Object();
  private final Exception error = new Exception("test");
  @Mock private Span span;
  @Mock private HttpExtractor<Object, Object> extractor;
  private AbstractHttpHandler<Object, Object> handler;
  @Captor private ArgumentCaptor<MessageEvent> captor;
  @Captor private ArgumentCaptor<AttributeValue> attributeCaptor;
  Map<String, String> attributeMap = new HashMap<String, String>();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    handler = new AbstractHttpHandler<Object, Object>(extractor) {};
    attributeMap.put("http.host", "example.com");
    attributeMap.put("http.route", "/get/:name");
    attributeMap.put("http.path", "/get/helloworld");
    attributeMap.put("http.method", "GET");
    attributeMap.put("http.user_agent", "test 1.0");
    attributeMap.put("http.url", "http://example.com/get/helloworld");
  }

  @Test
  public void constructorDisallowNullExtractor() {
    thrown.expect(NullPointerException.class);
    new AbstractHttpHandler<Object, Object>(null) {};
  }

  @Test
  public void handleMessageSent() {
    Type type = Type.SENT;
    long id = 123L;
    long uncompressed = 456L;
    handler.handleMessageSent(span, id, uncompressed);
    verify(span).addMessageEvent(captor.capture());

    MessageEvent messageEvent = captor.getValue();
    assertThat(messageEvent.getType()).isEqualTo(type);
    assertThat(messageEvent.getMessageId()).isEqualTo(id);
    assertThat(messageEvent.getUncompressedMessageSize()).isEqualTo(uncompressed);
    assertThat(messageEvent.getCompressedMessageSize()).isEqualTo(0);
  }

  @Test
  public void handleMessageReceived() {
    Type type = Type.RECEIVED;
    long id = 123L;
    long uncompressed = 456L;
    handler.handleMessageReceived(span, id, uncompressed);
    verify(span).addMessageEvent(captor.capture());

    MessageEvent messageEvent = captor.getValue();
    assertThat(messageEvent.getType()).isEqualTo(type);
    assertThat(messageEvent.getMessageId()).isEqualTo(id);
    assertThat(messageEvent.getUncompressedMessageSize()).isEqualTo(uncompressed);
    assertThat(messageEvent.getCompressedMessageSize()).isEqualTo(0);
  }

  @Test
  public void handleEndDisallowNullSpan() {
    thrown.expect(NullPointerException.class);
    handler.handleEnd(null, response, error);
  }

  @Test
  public void handleEndAllowNullResponseAndError() {
    handler.handleEnd(span, /*response=*/ null, /*error=*/ null);
  }

  @Test
  public void handleEndShouldEndSpan() {
    handler.handleEnd(span, response, error);
    verify(span).end(any(EndSpanOptions.class));
  }

  @Test
  public void testSpanName() {
    String spanName = handler.getSpanName(request, extractor);
    assertThat(spanName).isNotNull();
  }

  private void verifyAttributes(String key) {
    verify(span).putAttribute(eq(key), attributeCaptor.capture());
    AttributeValue attribute = attributeCaptor.getValue();
    assertThat(attribute.toString()).contains(attributeMap.get(key));
  }

  @Test
  public void testSpanRequestAttributes() {

    when(extractor.getRoute(any(Object.class))).thenReturn(attributeMap.get("http.route"));
    when(extractor.getHost(any(Object.class))).thenReturn(attributeMap.get("http.host"));
    when(extractor.getPath(any(Object.class))).thenReturn(attributeMap.get("http.path"));
    when(extractor.getMethod(any(Object.class))).thenReturn(attributeMap.get("http.method"));
    when(extractor.getUserAgent(any(Object.class))).thenReturn(attributeMap.get("http.user_agent"));
    when(extractor.getUrl(any(Object.class))).thenReturn(attributeMap.get("http.url"));

    handler.addSpanRequestAttributes(span, request, extractor);

    for (Entry<String, String> entry : attributeMap.entrySet()) {
      verifyAttributes(entry.getKey());
    }
  }
}
