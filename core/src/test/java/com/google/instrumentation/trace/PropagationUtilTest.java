/*
 * Copyright 2017, Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.instrumentation.trace;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.when;

import com.google.instrumentation.trace.PropagationUtil.DefaultHandler;
import com.google.instrumentation.trace.PropagationUtil.Handler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link PropagationUtil}. */
@RunWith(JUnit4.class)
public class PropagationUtilTest {
  private static final byte[] traceIdBytes =
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 'a'};
  private static final TraceId traceId = TraceId.fromBytes(traceIdBytes);
  private static final byte[] spanIdBytes = new byte[] {(byte) 0xFF, 0, 0, 0, 0, 0, 0, 0};
  private static final SpanId spanId = SpanId.fromBytes(spanIdBytes);
  private static final byte[] traceOptionsBytes = new byte[] {1, (byte) 0x34, 0, (byte) 0x12};
  private static final TraceOptions traceOptions = TraceOptions.fromBytes(traceOptionsBytes);
  private static final byte[] EXAMPLE_BYTES =
      new byte[] {
        0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 97, 98, 99, 100, 101,
        102, 103, 104, 1, 0, 0, 0
      };
  private static final SpanContext EXAMPLE_SPAN_CONTEXT =
      new SpanContext(
          TraceId.fromBytes(
              new byte[] {64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79}),
          SpanId.fromBytes(new byte[] {97, 98, 99, 100, 101, 102, 103, 104}),
          TraceOptions.fromBytes(new byte[] {1, 0, 0, 0}));
  @Mock private Handler mockHandler;

  private static void testHttpSpanContextConversion(SpanContext spanContext) {
    SpanContext propagatedSpanContext =
        PropagationUtil.fromHttpHeaderValue(PropagationUtil.toHttpHeaderValue(spanContext));
    assertWithMessage("Propagated Context is not null.").that(propagatedSpanContext).isNotNull();
    assertWithMessage("Propagated Context is valid.")
        .that(propagatedSpanContext.isValid())
        .isEqualTo(spanContext.isValid());
    assertWithMessage("Trace ids match.")
        .that(propagatedSpanContext.getTraceId())
        .isEqualTo(spanContext.getTraceId());
    assertWithMessage("Span ids match.")
        .that(propagatedSpanContext.getSpanId())
        .isEqualTo(spanContext.getSpanId());
    assertWithMessage("TraceOptions should match.")
        .that(propagatedSpanContext.getTraceOptions())
        .isEqualTo(spanContext.getTraceOptions());
  }

  @Test
  public void getHttpHeaderName() {
    assertThat(PropagationUtil.HTTP_HEADER_NAME).isEqualTo("Trace-Context");
  }

  @Test
  public void propagate_SpanContextTracingEnabled() {
    testHttpSpanContextConversion(
        new SpanContext(traceId, spanId, TraceOptions.builder().setIsSampled().build()));
  }

  @Test
  public void propagate_SpanContextNoTracing() {
    testHttpSpanContextConversion(new SpanContext(traceId, spanId, TraceOptions.DEFAULT));
  }

  @Test
  public void format_SpanContext() {
    assertThat(PropagationUtil.toHttpHeaderValue(new SpanContext(traceId, spanId, traceOptions)))
        .isEqualTo("0000000000000000000000000000000061FF0000000000000001340012");
  }

  @Test(expected = NullPointerException.class)
  public void toHttpHeaderValue_NullSpanContext() {
    PropagationUtil.toHttpHeaderValue(null);
  }

  @Test
  public void toHttpHeaderValue_InvalidSpanContext() {
    assertThat(PropagationUtil.toHttpHeaderValue(SpanContext.INVALID))
        .isEqualTo("0000000000000000000000000000000000000000000000000000000000");
  }

  @Test
  public void parseHeaderValue_ValidValues() {
    assertThat(
            PropagationUtil.fromHttpHeaderValue(
                "0000000000000000000000000000000061FF0000000000000001340012"))
        .isEqualTo(new SpanContext(traceId, spanId, traceOptions));

    assertThat(
            PropagationUtil.fromHttpHeaderValue(
                "0000000000000000000000000000000000FF0000000000000001340012"))
        .isEqualTo(new SpanContext(TraceId.INVALID, spanId, traceOptions));

    assertThat(
            PropagationUtil.fromHttpHeaderValue(
                "0000000000000000000000000000000061000000000000000001340012"))
        .isEqualTo(new SpanContext(traceId, SpanId.INVALID, traceOptions));

    assertThat(
            PropagationUtil.fromHttpHeaderValue(
                "0000000000000000000000000000000061FF0000000000000000000000"))
        .isEqualTo(new SpanContext(traceId, spanId, TraceOptions.DEFAULT));
  }

  @Test
  public void parseHeaderValue_HttpExampleValue() {
    assertThat(
            PropagationUtil.fromHttpHeaderValue(
                "00404142434445464748494A4B4C4D4E4F616263646566676801000000"))
        .isEqualTo(
            new SpanContext(
                TraceId.fromBytes(
                    new byte[] {64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79}),
                SpanId.fromBytes(new byte[] {97, 98, 99, 100, 101, 102, 103, 104}),
                TraceOptions.fromBytes(new byte[] {1, 0, 0, 0})));
    assertThat(
            PropagationUtil.fromHttpHeaderValue(
                    "00404142434445464748494A4B4C4D4E4F616263646566676801000000")
                .getTraceOptions()
                .getOptions())
        .isEqualTo(1);
  }

  @Test
  public void parseHeaderValue_BinaryExampleValue() {
    assertThat(PropagationUtil.fromBinaryValue(EXAMPLE_BYTES))
        .isEqualTo(
            new SpanContext(
                TraceId.fromBytes(
                    new byte[] {64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79}),
                SpanId.fromBytes(new byte[] {97, 98, 99, 100, 101, 102, 103, 104}),
                TraceOptions.fromBytes(new byte[] {1, 0, 0, 0})));
    assertThat(PropagationUtil.fromBinaryValue(EXAMPLE_BYTES).getTraceOptions().getOptions())
        .isEqualTo(1);
  }

  @Test(expected = NullPointerException.class)
  public void parseHttpHeaderValue_NullInput() {
    PropagationUtil.fromHttpHeaderValue(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseHttpHeaderValue_EmptyInput() {
    PropagationUtil.fromHttpHeaderValue("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseHttpHeaderValue_UnsupportedVersionId() {
    PropagationUtil.fromHttpHeaderValue(
        "4200000000000000000000000000000061FF0000000000000000000001");
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseHttpHeaderValue_LongerVersionFormat() {
    PropagationUtil.fromHttpHeaderValue(
        "0000000000000000000000000000000061FF000000000000000000000100");
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseHttpHeaderValue_ShorterVersionFormat() {
    PropagationUtil.fromHttpHeaderValue("0000000000000000000000000000000000ff00000000000000000001");
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseHttpHeaderValue_InvalidCharactersInTraceId() {
    PropagationUtil.fromHttpHeaderValue(
        "00000000000000007b0000SPAN000001c8ff0000000000000000000001");
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseHttpHeaderValue_InvalidCharactersInSpanId() {
    PropagationUtil.fromHttpHeaderValue(
        "0000000000000000000000000000000061000000ROOT00031500000001");
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseHttpHeaderValue_InvalidCharactersInTraceOptions() {
    PropagationUtil.fromHttpHeaderValue(
        "0000000000000000000000000000000061FF0000000000000000BAR001");
  }

  @Test(expected = NullPointerException.class)
  public void parseBinaryValue_NullInput() {
    PropagationUtil.fromBinaryValue(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseBinaryValue_EmptyInput() {
    PropagationUtil.fromBinaryValue(new byte[0]);
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseBinaryValue_UnsupportedVersionId() {
    PropagationUtil.fromBinaryValue(
        new byte[] {
          66, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 97, 98, 99, 100, 101,
          102, 103, 104, 1, 0, 0, 0
        });
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseBinaryValue_LongerVersionFormat() {
    PropagationUtil.fromBinaryValue(
        new byte[] {
          0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 97, 98, 99, 100, 101,
          102, 103, 104, 1, 0, 0, 0, 0
        });
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseBinaryValue_ShorterVersionFormat() {
    PropagationUtil.fromBinaryValue(
        new byte[] {
          0, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 97, 98, 99, 100, 101,
          102, 103, 104, 1, 0
        });
  }

  @Test
  public void parseBinaryValue_WithNewAddedFormat() {
    MockitoAnnotations.initMocks(this);
    when(mockHandler.fromBinaryFormat(same(EXAMPLE_BYTES))).thenReturn(EXAMPLE_SPAN_CONTEXT);
    PropagationUtil.setHandler(mockHandler);
    try {
      assertThat(PropagationUtil.fromBinaryValue(EXAMPLE_BYTES)).isEqualTo(EXAMPLE_SPAN_CONTEXT);
    } finally{
      PropagationUtil.setHandler(DefaultHandler.INSTANCE);
    }
  }

  @Test
  public void serializeBinaryValue_WithNewAddedFormat() {
    MockitoAnnotations.initMocks(this);
    when(mockHandler.toBinaryFormat(same(EXAMPLE_SPAN_CONTEXT))).thenReturn(EXAMPLE_BYTES);
    PropagationUtil.setHandler(mockHandler);
    try {
      assertThat(PropagationUtil.toBinaryValue(EXAMPLE_SPAN_CONTEXT)).isEqualTo(EXAMPLE_BYTES);
    } finally{
      PropagationUtil.setHandler(DefaultHandler.INSTANCE);
    }
  }
}
