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
import static org.mockito.Mockito.verify;

import io.opencensus.trace.EndSpanOptions;
import io.opencensus.trace.MessageEvent;
import io.opencensus.trace.MessageEvent.Type;
import io.opencensus.trace.Span;
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

/** Unit tests for {@link HttpHandler}. */
@RunWith(JUnit4.class)
public class HttpHandlerTest {

  @Mock private Span span;
  @Mock private HttpExtractor<Object, Object> extractor;
  private HttpHandler<Object, Object> handler;

  private final Object response = new Object();
  private final Exception error = new Exception("test");
  @Captor private ArgumentCaptor<MessageEvent> captor;

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    handler = new HttpHandler<Object, Object>(extractor) {};
  }

  @Test
  public void constructorDisallowNullExtractor() {
    thrown.expect(NullPointerException.class);
    new HttpHandler<Object, Object>(null) {};
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
    handler.handleEnd(response, error, /*span=*/ null);
  }

  @Test
  public void handleEndAllowNullResponseAndError() {
    handler.handleEnd(/*response=*/ null, /*error=*/ null, span);
  }

  @Test
  public void handleEndShouldEndSpan() {
    handler.handleEnd(response, error, span);
    verify(span).end(any(EndSpanOptions.class));
  }
}
