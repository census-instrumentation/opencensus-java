/*
 * Copyright 2017, OpenCensus Authors
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

package io.opencensus.implcore.trace.propagation;

import static com.google.common.truth.Truth.assertThat;
import static io.opencensus.implcore.trace.propagation.CloudTraceFormat.HEADER_NAME;
import static io.opencensus.implcore.trace.propagation.CloudTraceFormat.NOT_SAMPLED;
import static io.opencensus.implcore.trace.propagation.CloudTraceFormat.SAMPLED;
import static io.opencensus.implcore.trace.propagation.CloudTraceFormat.SPAN_ID_DELIMITER;
import static io.opencensus.implcore.trace.propagation.CloudTraceFormat.TRACE_OPTION_DELIMITER;

import com.google.common.primitives.UnsignedLong;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.propagation.SpanContextParseException;
import io.opencensus.trace.propagation.TextFormat.Getter;
import io.opencensus.trace.propagation.TextFormat.Setter;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link CloudTraceFormat}. */
@RunWith(JUnit4.class)
public final class CloudTraceFormatTest {
  private final CloudTraceFormat cloudTraceFormat = new CloudTraceFormat();

  private static final String TRACE_ID_BASE16 = "ff000000000000000000000000000041";
  private static final String TRACE_ID_BASE16_SHORT = "ff00000000000041";
  private static final String TRACE_ID_BASE16_LONG = "0000" + TRACE_ID_BASE16;
  private static final String TRACE_ID_BASE16_INVALID = "ff00000000000000000000000abcdefg";
  private static final String SPAN_ID_BASE16 = "ff00000000000041";
  private static final String SPAN_ID_BASE10 = UnsignedLong.valueOf(SPAN_ID_BASE16, 16).toString();
  private static final String SPAN_ID_BASE10_SIGNED = "-12345";
  private static final String SPAN_ID_BASE10_MAX_UNSIGNED_LONG = UnsignedLong.MAX_VALUE.toString();
  private static final String SPAN_ID_BASE16_MAX_UNSIGNED_LONG =
      UnsignedLong.MAX_VALUE.toString(16);
  private static final String SPAN_ID_BASE10_OVERFLOW = SPAN_ID_BASE10_MAX_UNSIGNED_LONG + "1";
  private static final String SPAN_ID_BASE10_INVALID = "0x12345";

  private static final TraceId TRACE_ID = TraceId.fromLowerBase16(TRACE_ID_BASE16);
  private static final SpanId SPAN_ID = SpanId.fromLowerBase16(SPAN_ID_BASE16);
  private static final SpanId SPAN_ID_MAX =
      SpanId.fromLowerBase16(SPAN_ID_BASE16_MAX_UNSIGNED_LONG);

  private static final TraceOptions TRACE_OPTIONS_SAMPLED =
      TraceOptions.builder().setIsSampled(true).build();
  private static final TraceOptions TRACE_OPTIONS_NOT_SAMPLED = TraceOptions.DEFAULT;

  @Rule public ExpectedException thrown = ExpectedException.none();
  private final Setter<Map<String, String>> setter =
      new Setter<Map<String, String>>() {
        @Override
        public void put(Map<String, String> carrier, String key, String value) {
          carrier.put(key, value);
        }
      };
  private final Getter<Map<String, String>> getter =
      new Getter<Map<String, String>>() {
        @Nullable
        @Override
        public String get(Map<String, String> carrier, String key) {
          return carrier.get(key);
        }
      };

  private String constructHeader(String traceId, String spanId) {
    return traceId + SPAN_ID_DELIMITER + spanId;
  }

  private String constructHeader(String traceId, String spanId, String traceOptions) {
    return traceId + SPAN_ID_DELIMITER + spanId + TRACE_OPTION_DELIMITER + traceOptions;
  }

  private void parseSuccess(String headerValue, SpanContext expected)
      throws SpanContextParseException {
    Map<String, String> header = new HashMap<String, String>();
    header.put(HEADER_NAME, headerValue);
    assertThat(cloudTraceFormat.extract(header, getter)).isEqualTo(expected);
  }

  private void parseFailure(
      String headerValue, Class<? extends Throwable> expectedThrown, String expectedMessage)
      throws SpanContextParseException {
    Map<String, String> header = new HashMap<String, String>();
    header.put(HEADER_NAME, headerValue);
    thrown.expect(expectedThrown);
    thrown.expectMessage(expectedMessage);
    cloudTraceFormat.extract(header, getter);
  }

  @Test
  public void serializeSampledContextShouldSucceed() throws SpanContextParseException {
    Map<String, String> carrier = new HashMap<String, String>();
    cloudTraceFormat.inject(
        SpanContext.create(TRACE_ID, SPAN_ID, TRACE_OPTIONS_SAMPLED), carrier, setter);
    assertThat(carrier)
        .containsExactly(HEADER_NAME, constructHeader(TRACE_ID_BASE16, SPAN_ID_BASE10, SAMPLED));
  }

  @Test
  public void serializeNotSampledContextShouldSucceed() throws SpanContextParseException {
    Map<String, String> carrier = new HashMap<String, String>();
    cloudTraceFormat.inject(
        SpanContext.create(TRACE_ID, SPAN_ID, TRACE_OPTIONS_NOT_SAMPLED), carrier, setter);
    assertThat(carrier)
        .containsExactly(
            HEADER_NAME, constructHeader(TRACE_ID_BASE16, SPAN_ID_BASE10, NOT_SAMPLED));
  }

  @Test
  public void parseSampledShouldSucceed() throws SpanContextParseException {
    parseSuccess(
        constructHeader(TRACE_ID_BASE16, SPAN_ID_BASE10, SAMPLED),
        SpanContext.create(TRACE_ID, SPAN_ID, TRACE_OPTIONS_SAMPLED));
  }

  @Test
  public void parseNotSampledShouldSucceed() throws SpanContextParseException {
    parseSuccess(
        constructHeader(TRACE_ID_BASE16, SPAN_ID_BASE10, NOT_SAMPLED),
        SpanContext.create(TRACE_ID, SPAN_ID, TRACE_OPTIONS_NOT_SAMPLED));
  }

  @Test
  public void parseMissingTraceOptionsShouldSucceed() throws SpanContextParseException {
    parseSuccess(
        constructHeader(TRACE_ID_BASE16, SPAN_ID_BASE10),
        SpanContext.create(TRACE_ID, SPAN_ID, TRACE_OPTIONS_NOT_SAMPLED));
  }

  @Test
  public void parseEmptyTraceOptionsShouldFail() throws SpanContextParseException {
    parseFailure(
        constructHeader(TRACE_ID_BASE16, SPAN_ID_BASE10, ""),
        SpanContextParseException.class,
        "Invalid TRACE_OPTIONS");
  }

  @Test
  public void parseInvalidTraceOptionsShouldFail() throws SpanContextParseException {
    parseFailure(
        constructHeader(TRACE_ID_BASE16, SPAN_ID_BASE10) + ";O=",
        SpanContextParseException.class,
        "Invalid input");
  }

  @Test
  public void parseMissingHeaderShouldFail() throws SpanContextParseException {
    Map<String, String> headerMissing = new HashMap<String, String>();
    thrown.expect(SpanContextParseException.class);
    thrown.expectMessage("Missing X-Cloud-Trace-Context");
    cloudTraceFormat.extract(headerMissing, getter);
  }

  @Test
  public void parseEmptyHeaderShouldFail() throws SpanContextParseException {
    parseFailure("", SpanContextParseException.class, "Invalid input");
  }

  @Test
  public void parseShortTraceIdShouldFail() throws SpanContextParseException {
    parseFailure(
        constructHeader(TRACE_ID_BASE16_SHORT, SPAN_ID_BASE10, SAMPLED),
        SpanContextParseException.class,
        "Invalid input");
  }

  @Test
  public void parseLongTraceIdShouldFail() throws SpanContextParseException {
    parseFailure(
        constructHeader(TRACE_ID_BASE16_LONG, SPAN_ID_BASE10, SAMPLED),
        SpanContextParseException.class,
        "Invalid input");
  }

  @Test
  public void parseMissingTraceIdShouldFail() throws SpanContextParseException {
    parseFailure(
        constructHeader("", SPAN_ID_BASE10, SAMPLED),
        SpanContextParseException.class,
        "Invalid input");
  }

  @Test
  public void parseInvalidTraceIdShouldFail() throws SpanContextParseException {
    parseFailure(
        constructHeader(TRACE_ID_BASE16_INVALID, SPAN_ID_BASE10, SAMPLED),
        SpanContextParseException.class,
        "Invalid input");
  }

  @Test
  public void parseMissingSpanIdDelimiterShouldFail() throws SpanContextParseException {
    parseFailure(TRACE_ID_BASE16_LONG, SpanContextParseException.class, "Invalid input");
  }

  @Test
  public void parseSignedSpanIdShouldFail() throws SpanContextParseException {
    parseFailure(
        constructHeader(TRACE_ID_BASE16, SPAN_ID_BASE10_SIGNED, SAMPLED),
        SpanContextParseException.class,
        "Invalid input");
  }

  @Test
  public void parseMaxUnsignedLongSpanIdShouldSucceed() throws SpanContextParseException {
    parseSuccess(
        constructHeader(TRACE_ID_BASE16, SPAN_ID_BASE10_MAX_UNSIGNED_LONG, SAMPLED),
        SpanContext.create(TRACE_ID, SPAN_ID_MAX, TRACE_OPTIONS_SAMPLED));
  }

  @Test
  public void parseOverflowSpanIdShouldFail() throws SpanContextParseException {
    parseFailure(
        constructHeader(TRACE_ID_BASE16, SPAN_ID_BASE10_OVERFLOW, SAMPLED),
        SpanContextParseException.class,
        "Invalid input");
  }

  @Test
  public void parseInvalidSpanIdShouldFail() throws SpanContextParseException {
    parseFailure(
        constructHeader(TRACE_ID_BASE16, SPAN_ID_BASE10_INVALID, SAMPLED),
        SpanContextParseException.class,
        "Invalid input");
  }

  @Test
  public void parseMissingSpanIdShouldFail() throws SpanContextParseException {
    parseFailure(
        constructHeader(TRACE_ID_BASE16, "", SAMPLED),
        SpanContextParseException.class,
        "Invalid input");
  }

  @Test
  public void fieldsShouldMatch() {
    assertThat(cloudTraceFormat.fields()).containsExactly(HEADER_NAME);
  }
}
