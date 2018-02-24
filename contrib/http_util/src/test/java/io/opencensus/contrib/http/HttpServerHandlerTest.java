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
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opencensus.trace.EndSpanOptions;
import io.opencensus.trace.MessageEvent;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanBuilder;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.Status;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.propagation.SpanContextParseException;
import io.opencensus.trace.propagation.TextFormat;
import java.util.Random;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

/** Unit tests for {@link HttpServerHandler}. */
@RunWith(JUnit4.class)
public class HttpServerHandlerTest {

  @Mock private FakeHttpRequest request;
  @Mock private FakeHttpResponse response;
  @Mock private HttpExtractor<FakeHttpRequest, FakeHttpResponse> extractor;
  @Mock private SpanBuilder spanBuilder;
  @Mock private Tracer tracer;
  @Mock private TextFormat textFormat;
  @Mock private TextFormat.Getter<FakeHttpRequest> textFormatGetter;

  @Rule public final ExpectedException thrown = ExpectedException.none();

  private HttpServerHandler<FakeHttpRequest, FakeHttpResponse> handler;

  private final Random random = new Random();
  private final SpanContext spanContext =
      SpanContext.create(
          TraceId.generateRandomId(random), SpanId.generateRandomId(random), TraceOptions.DEFAULT);
  private final Throwable error = new Exception("test");
  @Spy private FakeSpan span = new FakeSpan(spanContext);

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    handler =
        new HttpServerHandler<FakeHttpRequest, FakeHttpResponse>(tracer, textFormat, extractor);
    when(tracer.spanBuilderWithExplicitParent(any(String.class), any(Span.class)))
        .thenReturn(spanBuilder);
    when(spanBuilder.startSpan()).thenReturn(span);
  }

  @Test
  public void getSpanNameDefaultImplementation() {
    assertThat(handler.getSpanName(request, "test")).isEqualTo("Recv.test");
  }

  @Test
  public void handleRecvDisallowNullGetter() {
    thrown.expect(NullPointerException.class);
    handler.<FakeHttpRequest>handleRecv(
        null /* getter */, request /* carrier */, request /* request */, "test");
  }

  @Test
  public void handleRecvDisallowNullCarrier() {
    thrown.expect(NullPointerException.class);
    handler.<FakeHttpRequest>handleRecv(
        textFormatGetter, null /* carrier */, request /* request */, "test");
  }

  @Test
  public void handleRecvDisallowNullRequest() {
    thrown.expect(NullPointerException.class);
    handler.<FakeHttpRequest>handleRecv(
        textFormatGetter, request /* carrier */, null /* request */, "test");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void handleRecvShouldCreateChildSpanUnderParent() throws SpanContextParseException {
    when(textFormat.extract(isA(FakeHttpRequest.class), isA(TextFormat.Getter.class)))
        .thenReturn(spanContext);
    handler.<FakeHttpRequest>handleRecv(textFormatGetter, request, request, "test");
    verify(tracer).spanBuilderWithRemoteParent(eq("Recv.test"), same(spanContext));
  }

  @Test
  public void handleRecvShouldExtractFromCarrier() throws SpanContextParseException {
    handler.<FakeHttpRequest>handleRecv(textFormatGetter, request, request, "test");
    verify(textFormat).extract(same(request), same(textFormatGetter));
  }

  @Test
  public void handleRecvShouldRecordMessageEvent() {
    handler.<FakeHttpRequest>handleRecv(textFormatGetter, request, request, "test");
    verify(span).addMessageEvent(isA(MessageEvent.class));
  }

  @Test
  public void handleSendDisallowNullSpan() {
    thrown.expect(NullPointerException.class);
    handler.handleSend(response, error, null /* span */, false);
  }

  @Test
  public void handleSendAllowNullResponseAndError() {
    handler.handleSend(null /* response */, null /* error */, span, false);
  }

  @Test
  public void handleSendRecordMessageEvent() {
    handler.handleSend(response, error, span, false);
    verify(span).addMessageEvent(isA(MessageEvent.class));
  }

  @Test
  public void handleSendSetStatus() {
    handler.handleSend(response, error, span, false);
    verify(span).setStatus(isA(Status.class));
  }

  @Test
  public void handleSendNotEndSpan() {
    handler.handleSend(response, error, span, false);
    verify(span, never()).end(isA(EndSpanOptions.class));
  }

  @Test
  public void handleSendEndSpan() {
    handler.handleSend(response, error, span, true);
    verify(span).end(isA(EndSpanOptions.class));
  }
}
