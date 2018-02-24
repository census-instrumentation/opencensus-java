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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableSet;
import io.opencensus.trace.MessageEvent;
import io.opencensus.trace.MessageEvent.Type;
import io.opencensus.trace.Span;
import io.opencensus.trace.Status;
import io.opencensus.trace.Status.CanonicalCode;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.propagation.TextFormat;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link HttpHandler}. */
@RunWith(JUnit4.class)
public class HttpHandlerTest {

  @Mock private FakeHttpResponse response;
  @Mock private HttpExtractor<FakeHttpRequest, FakeHttpResponse> extractor;
  @Mock private Tracer tracer;
  @Mock private TextFormat textFormat;
  @Mock private Span span;

  @Rule public final ExpectedException thrown = ExpectedException.none();

  private HttpHandler<FakeHttpRequest, FakeHttpResponse> handler;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    handler =
        new HttpClientHandler<FakeHttpRequest, FakeHttpResponse>(tracer, textFormat, extractor) {
          @Override
          public String getSpanName(FakeHttpRequest request, String method) {
            return "";
          }
        };
  }

  @Test
  public void getters() {
    assertThat(handler.getTracer()).isEqualTo(tracer);
    assertThat(handler.getTextFormat()).isEqualTo(textFormat);
    assertThat(handler.getExtractor()).isEqualTo(extractor);
  }

  @Test
  public void sequentialSentMessageEventId() {
    assertThat(handler.nextSentMessageEventId()).isEqualTo(0);
    assertThat(handler.nextSentMessageEventId()).isEqualTo(1);
    assertThat(handler.nextSentMessageEventId()).isEqualTo(2);
    assertThat(handler.nextSentMessageEventId()).isEqualTo(3);
  }

  @Test
  public void sequentialRecvMessageEventId() {
    assertThat(handler.nextRecvMessageEventId()).isEqualTo(0);
    assertThat(handler.nextRecvMessageEventId()).isEqualTo(1);
    assertThat(handler.nextRecvMessageEventId()).isEqualTo(2);
    assertThat(handler.nextRecvMessageEventId()).isEqualTo(3);
  }

  @Test
  public void recordMessageEventDisallowNullSpan() {
    thrown.expect(NullPointerException.class);
    handler.recordMessageEvent(null /* span */, 0L, Type.SENT, 0L, 0L);
  }

  @Test
  public void recordMessageEventDisallowNullType() {
    thrown.expect(NullPointerException.class);
    handler.recordMessageEvent(span, 0L, null /* Type */, 0L, 0L);
  }

  @Test
  public void recordMessageEventShouldAddToSpan() {
    Type type = Type.RECEIVED;
    long id = 123L;
    long uncompressed = 456L;
    long compressed = 789L;
    MessageEvent messageEvent =
        MessageEvent.builder(type, id)
            .setUncompressedMessageSize(uncompressed)
            .setCompressedMessageSize(compressed)
            .build();
    handler.recordMessageEvent(span, id, type, uncompressed, compressed);
    verify(span).addMessageEvent(eq(messageEvent));
  }

  @Test
  public void parseResponseStatusSucceed() {
    when(extractor.getStatusCode(isA(FakeHttpResponse.class))).thenReturn(201);
    assertThat(handler.parseResponseStatus(response, null)).isEqualTo(Status.OK);
  }

  @Test
  public void parseResponseStatusNoResponse() {
    when(extractor.getStatusCode(any(FakeHttpResponse.class))).thenReturn(null);
    assertThat(handler.parseResponseStatus(null, null).getDescription())
        .contains("statusCode:null");
    assertThat(handler.parseResponseStatus(null, null).getCanonicalCode())
        .isEqualTo(CanonicalCode.UNKNOWN);
  }

  @Test
  public void parseResponseStatusErrorWithMessage() {
    Throwable error = new Exception("testError");
    assertThat(handler.parseResponseStatus(null, error).getDescription())
        .contains("error:testError");
  }

  @Test
  public void parseResponseStatusErrorWithoutMessage() {
    Throwable error = new NullPointerException();
    assertThat(handler.parseResponseStatus(null, error).getDescription())
        .contains("error:NullPointerException");
  }

  @Test
  public void parseResponseStatusError_499() {
    when(extractor.getStatusCode(isA(FakeHttpResponse.class))).thenReturn(499);
    assertThat(handler.parseResponseStatus(response, null).getCanonicalCode())
        .isEqualTo(CanonicalCode.CANCELLED);
  }

  @Test
  public void parseResponseStatusError_500() {
    when(extractor.getStatusCode(isA(FakeHttpResponse.class))).thenReturn(500);
    assertThat(
            ImmutableSet.of(CanonicalCode.INTERNAL, CanonicalCode.UNKNOWN, CanonicalCode.DATA_LOSS))
        .contains(handler.parseResponseStatus(response, null).getCanonicalCode());
  }

  @Test
  public void parseResponseStatusError_400() {
    when(extractor.getStatusCode(isA(FakeHttpResponse.class))).thenReturn(400);
    assertThat(
            ImmutableSet.of(
                CanonicalCode.FAILED_PRECONDITION,
                CanonicalCode.OUT_OF_RANGE,
                CanonicalCode.INVALID_ARGUMENT))
        .contains(handler.parseResponseStatus(response, null).getCanonicalCode());
  }

  @Test
  public void parseResponseStatusError_504() {
    when(extractor.getStatusCode(isA(FakeHttpResponse.class))).thenReturn(504);
    assertThat(handler.parseResponseStatus(response, null).getCanonicalCode())
        .isEqualTo(CanonicalCode.DEADLINE_EXCEEDED);
  }

  @Test
  public void parseResponseStatusError_404() {
    when(extractor.getStatusCode(isA(FakeHttpResponse.class))).thenReturn(404);
    assertThat(handler.parseResponseStatus(response, null).getCanonicalCode())
        .isEqualTo(CanonicalCode.NOT_FOUND);
  }

  @Test
  public void parseResponseStatusError_409() {
    when(extractor.getStatusCode(isA(FakeHttpResponse.class))).thenReturn(409);
    assertThat(ImmutableSet.of(CanonicalCode.ALREADY_EXISTS, CanonicalCode.ABORTED))
        .contains(handler.parseResponseStatus(response, null).getCanonicalCode());
  }

  @Test
  public void parseResponseStatusError_403() {
    when(extractor.getStatusCode(isA(FakeHttpResponse.class))).thenReturn(403);
    assertThat(handler.parseResponseStatus(response, null).getCanonicalCode())
        .isEqualTo(CanonicalCode.PERMISSION_DENIED);
  }

  @Test
  public void parseResponseStatusError_401() {
    when(extractor.getStatusCode(isA(FakeHttpResponse.class))).thenReturn(401);
    assertThat(handler.parseResponseStatus(response, null).getCanonicalCode())
        .isEqualTo(CanonicalCode.UNAUTHENTICATED);
  }

  @Test
  public void parseResponseStatusError_429() {
    when(extractor.getStatusCode(isA(FakeHttpResponse.class))).thenReturn(429);
    assertThat(handler.parseResponseStatus(response, null).getCanonicalCode())
        .isEqualTo(CanonicalCode.RESOURCE_EXHAUSTED);
  }

  @Test
  public void parseResponseStatusError_501() {
    when(extractor.getStatusCode(isA(FakeHttpResponse.class))).thenReturn(501);
    assertThat(handler.parseResponseStatus(response, null).getCanonicalCode())
        .isEqualTo(CanonicalCode.UNIMPLEMENTED);
  }

  @Test
  public void parseResponseStatusError_503() {
    when(extractor.getStatusCode(isA(FakeHttpResponse.class))).thenReturn(503);
    assertThat(handler.parseResponseStatus(response, null).getCanonicalCode())
        .isEqualTo(CanonicalCode.UNAVAILABLE);
  }

  @Test
  public void parseResponseStatusError_Others() {
    // some random status code
    when(extractor.getStatusCode(isA(FakeHttpResponse.class))).thenReturn(434);
    assertThat(handler.parseResponseStatus(response, null).getCanonicalCode())
        .isEqualTo(CanonicalCode.UNKNOWN);
  }
}
