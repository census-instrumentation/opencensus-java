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

import io.opencensus.contrib.http.util.HttpTraceAttributeConstants;
import io.opencensus.contrib.http.util.testing.FakeSpan;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.Tags;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.EndSpanOptions;
import io.opencensus.trace.MessageEvent;
import io.opencensus.trace.MessageEvent.Type;
import io.opencensus.trace.Span;
import io.opencensus.trace.Span.Options;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
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
import org.mockito.Spy;

/** Unit tests for {@link AbstractHttpHandler}. */
@RunWith(JUnit4.class)
public class AbstractHttpHandlerTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();
  private final Object request = new Object();
  private final Object response = new Object();
  private final Exception error = new Exception("test");
  private final Random random = new Random();
  private final SpanContext spanContext =
      SpanContext.create(
          TraceId.generateRandomId(random),
          SpanId.generateRandomId(random),
          TraceOptions.DEFAULT,
          null);
  Map<String, String> attributeMap = new HashMap<String, String>();
  @Mock private Span span;
  @Mock private HttpExtractor<Object, Object> extractor;
  private AbstractHttpHandler<Object, Object> handler;
  @Captor private ArgumentCaptor<MessageEvent> captor;
  @Captor private ArgumentCaptor<AttributeValue> attributeCaptor;
  @Captor private ArgumentCaptor<EndSpanOptions> optionsCaptor;

  @Spy private FakeSpan fakeSpan = new FakeSpan(spanContext, EnumSet.of(Options.RECORD_EVENTS));
  private final TagContext tagContext = Tags.getTagger().getCurrentTagContext();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    handler = new AbstractHttpHandler<Object, Object>(extractor) {};
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
    new AbstractHttpHandler<Object, Object>(null) {};
  }

  @Test
  public void handleMessageSent() {
    Type type = Type.SENT;
    long uncompressed = 456L;
    HttpRequestContext context = new HttpRequestContext(fakeSpan, tagContext);
    handler.handleMessageSent(context, uncompressed);
    verify(fakeSpan).addMessageEvent(captor.capture());

    MessageEvent messageEvent = captor.getValue();
    assertThat(messageEvent.getType()).isEqualTo(type);
    assertThat(messageEvent.getMessageId()).isEqualTo(1L);
    assertThat(messageEvent.getUncompressedMessageSize()).isEqualTo(uncompressed);
    assertThat(messageEvent.getCompressedMessageSize()).isEqualTo(0);
  }

  @Test
  public void handleMessageReceived() {
    Type type = Type.RECEIVED;
    long uncompressed = 456L;
    HttpRequestContext context = new HttpRequestContext(fakeSpan, tagContext);
    handler.handleMessageReceived(context, uncompressed);
    verify(fakeSpan).addMessageEvent(captor.capture());

    MessageEvent messageEvent = captor.getValue();
    assertThat(messageEvent.getType()).isEqualTo(type);
    assertThat(messageEvent.getMessageId()).isEqualTo(1L);
    assertThat(messageEvent.getUncompressedMessageSize()).isEqualTo(uncompressed);
    assertThat(messageEvent.getCompressedMessageSize()).isEqualTo(0);
  }

  @Test
  public void handleEndDisallowNullSpan() {
    thrown.expect(NullPointerException.class);
    handler.spanEnd(null, response, error);
  }

  @Test
  public void handleEndAllowNullResponseAndError() {
    handler.spanEnd(fakeSpan, /*response=*/ null, /*error=*/ null);
  }

  @Test
  public void handleEndShouldEndSpan() {
    when(extractor.getStatusCode(any(Object.class))).thenReturn(0);

    handler.spanEnd(fakeSpan, response, error);
    verify(fakeSpan).end(optionsCaptor.capture());
    EndSpanOptions options = optionsCaptor.getValue();
    assertThat(options).isEqualTo(EndSpanOptions.DEFAULT);
  }

  @Test
  public void handleEndWithRecordEvents() {
    when(extractor.getStatusCode(any(Object.class))).thenReturn(0);
    handler.spanEnd(fakeSpan, response, error);
    verify(fakeSpan)
        .putAttribute(eq(HttpTraceAttributeConstants.HTTP_STATUS_CODE), attributeCaptor.capture());
    AttributeValue attribute = attributeCaptor.getValue();
    assertThat(attribute).isEqualTo(AttributeValue.longAttributeValue(0));
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

    handler.addSpanRequestAttributes(span, request, extractor);

    for (Entry<String, String> entry : attributeMap.entrySet()) {
      verifyAttributes(entry.getKey());
    }
  }

  @Test
  public void testGetNewContext() {
    HttpRequestContext context = handler.getNewContext(fakeSpan, tagContext);
    assertThat(context).isNotNull();
  }

  @Test
  public void testGetSpanFromContext() {
    HttpRequestContext context = handler.getNewContext(fakeSpan, tagContext);
    assertThat(handler.getSpanFromContext(context)).isEqualTo(fakeSpan);
  }
}
