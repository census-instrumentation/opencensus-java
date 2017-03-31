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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link HttpPropagationUtil}. */
@RunWith(JUnit4.class)
public class HttpPropagationUtilTest {
  private static final byte[] traceIdBytes =
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 'a'};
  private static final TraceId traceId = TraceId.fromBytes(traceIdBytes);
  private static final byte[] spanIdBytes = new byte[] {(byte) 0xFF, 0, 0, 0, 0, 0, 0, 0};
  private static final SpanId spanId = SpanId.fromBytes(spanIdBytes);
  private static final byte[] traceOptionsBytes = new byte[] {1, (byte) 0x34, 0, (byte) 0x12};
  private static final TraceOptions traceOptions = TraceOptions.fromBytes(traceOptionsBytes);

  private static void testSpanContextConversion(SpanContext tc) {
    SpanContext propagatedTc =
        HttpPropagationUtil.fromHttpHeaderValue(HttpPropagationUtil.toHttpHeaderValue(tc));
    assertWithMessage("Propagated Context is not null.").that(propagatedTc).isNotNull();
    assertWithMessage("Propagated Context is valid.")
        .that(propagatedTc.isValid())
        .isEqualTo(tc.isValid());
    assertWithMessage("Trace ids match.")
        .that(propagatedTc.getTraceId())
        .isEqualTo(tc.getTraceId());
    assertWithMessage("Span ids match.").that(propagatedTc.getSpanId()).isEqualTo(tc.getSpanId());
    assertWithMessage("TraceOptions should match.")
        .that(propagatedTc.getTraceOptions())
        .isEqualTo(tc.getTraceOptions());
  }

  @Test
  public void getHttpHeaderName() {
    assertThat(HttpPropagationUtil.HTTP_HEADER_NAME).isEqualTo("Trace-Context");
  }

  @Test
  public void propagate_SpanContextTracingEnabled() {
    testSpanContextConversion(
        new SpanContext(traceId, spanId, TraceOptions.builder().setIsSampled().build()));
  }

  @Test
  public void propagate_SpanContextNoTracing() {
    testSpanContextConversion(new SpanContext(traceId, spanId, TraceOptions.DEFAULT));
  }

  @Test
  public void format_SpanContext() {
    assertThat(
            HttpPropagationUtil.toHttpHeaderValue(new SpanContext(traceId, spanId, traceOptions)))
        .isEqualTo("0000000000000000000000000000000061FF0000000000000001340012");
  }

  @Test(expected = NullPointerException.class)
  public void toHttpHeaderValue_NullSpanContext() {
    HttpPropagationUtil.toHttpHeaderValue(null);
  }

  @Test
  public void toHttpHeaderValue_InvalidSpanContext() {
    assertThat(HttpPropagationUtil.toHttpHeaderValue(SpanContext.INVALID))
        .isEqualTo("0000000000000000000000000000000000000000000000000000000000");
  }

  @Test
  public void parseHeaderValue_ValidValues() {
    assertThat(
            HttpPropagationUtil.fromHttpHeaderValue(
                "0000000000000000000000000000000061FF0000000000000001340012"))
        .isEqualTo(new SpanContext(traceId, spanId, traceOptions));

    assertThat(
            HttpPropagationUtil.fromHttpHeaderValue(
                "0000000000000000000000000000000000FF0000000000000001340012"))
        .isEqualTo(new SpanContext(TraceId.INVALID, spanId, traceOptions));

    assertThat(
            HttpPropagationUtil.fromHttpHeaderValue(
                "0000000000000000000000000000000061000000000000000001340012"))
        .isEqualTo(new SpanContext(traceId, SpanId.INVALID, traceOptions));

    assertThat(
            HttpPropagationUtil.fromHttpHeaderValue(
                "0000000000000000000000000000000061FF0000000000000000000000"))
        .isEqualTo(new SpanContext(traceId, spanId, TraceOptions.DEFAULT));
  }

  @Test
  public void parseHeaderValue_ExampleValue() {
    assertThat(
            HttpPropagationUtil.fromHttpHeaderValue(
                "00404142434445464748494A4B4C4D4E4F616263646566676801000000"))
        .isEqualTo(
            new SpanContext(
                TraceId.fromBytes(
                    new byte[] {64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79}),
                SpanId.fromBytes(new byte[] {97, 98, 99, 100, 101, 102, 103, 104}),
                TraceOptions.fromBytes(new byte[] {1, 0, 0, 0})));
    assertThat(
            HttpPropagationUtil.fromHttpHeaderValue(
                    "00404142434445464748494A4B4C4D4E4F616263646566676801000000")
                .getTraceOptions()
                .getOptions())
        .isEqualTo(1);
  }

  @Test(expected = NullPointerException.class)
  public void parseHeaderValue_NullInput() {
    HttpPropagationUtil.fromHttpHeaderValue(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseHeaderValue_EmptyInput() {
    HttpPropagationUtil.fromHttpHeaderValue("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseHeaderValue_InvalidVersionId() {
    HttpPropagationUtil.fromHttpHeaderValue(
        "0100000000000000000000000000000061FF0000000000000000000001");
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseHeaderValue_LongerVersionFormat() {
    HttpPropagationUtil.fromHttpHeaderValue(
        "0000000000000000000000000000000061FF000000000000000000000100");
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseHeaderValue_ShorterVersionFormat() {
    HttpPropagationUtil.fromHttpHeaderValue(
        "0000000000000000000000000000000000ff00000000000000000001");
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseHeaderValue_InvalidCharactersInTraceId() {
    HttpPropagationUtil.fromHttpHeaderValue(
        "00000000000000007b0000SPAN000001c8ff0000000000000000000001");
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseHeaderValue_InvalidCharactersInSpanId() {
    HttpPropagationUtil.fromHttpHeaderValue(
        "0000000000000000000000000000000061000000ROOT00031500000001");
  }

  @Test(expected = IllegalArgumentException.class)
  public void parseHeaderValue_InvalidCharactersInTraceOptions() {
    HttpPropagationUtil.fromHttpHeaderValue(
        "0000000000000000000000000000000061FF0000000000000000BAR001");
  }
}
