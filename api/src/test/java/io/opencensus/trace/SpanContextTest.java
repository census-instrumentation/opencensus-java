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

package io.opencensus.trace;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import io.opencensus.trace.base.SpanId;
import io.opencensus.trace.base.TraceId;
import io.opencensus.trace.base.TraceOptions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link SpanContext}. */
@RunWith(JUnit4.class)
public class SpanContextTest {
  private static final byte[] firstTraceIdBytes =
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 'a'};
  private static final byte[] secondTraceIdBytes =
      new byte[] {0, 0, 0, 0, 0, 0, 0, '0', 0, 0, 0, 0, 0, 0, 0, 0};
  private static final byte[] firstSpanIdBytes = new byte[] {0, 0, 0, 0, 0, 0, 0, 'a'};
  private static final byte[] secondSpanIdBytes = new byte[] {'0', 0, 0, 0, 0, 0, 0, 0};
  private static final SpanContext first =
      SpanContext.create(
          TraceId.fromBytes(firstTraceIdBytes),
          SpanId.fromBytes(firstSpanIdBytes),
          TraceOptions.DEFAULT);
  private static final SpanContext second =
      SpanContext.create(
          TraceId.fromBytes(secondTraceIdBytes),
          SpanId.fromBytes(secondSpanIdBytes),
          TraceOptions.builder().setIsSampled().build());

  @Test
  public void invalidSpanContext() {
    assertThat(SpanContext.INVALID.getTraceId()).isEqualTo(TraceId.INVALID);
    assertThat(SpanContext.INVALID.getSpanId()).isEqualTo(SpanId.INVALID);
    assertThat(SpanContext.INVALID.getTraceOptions()).isEqualTo(TraceOptions.DEFAULT);
  }

  @Test
  public void isValid() {
    assertThat(SpanContext.INVALID.isValid()).isFalse();
    assertThat(
            SpanContext.create(
                    TraceId.fromBytes(firstTraceIdBytes), SpanId.INVALID, TraceOptions.DEFAULT)
                .isValid())
        .isFalse();
    assertThat(
            SpanContext.create(
                    TraceId.INVALID, SpanId.fromBytes(firstSpanIdBytes), TraceOptions.DEFAULT)
                .isValid())
        .isFalse();
    assertThat(first.isValid()).isTrue();
    assertThat(second.isValid()).isTrue();
  }

  @Test
  public void getTraceId() {
    assertThat(first.getTraceId()).isEqualTo(TraceId.fromBytes(firstTraceIdBytes));
    assertThat(second.getTraceId()).isEqualTo(TraceId.fromBytes(secondTraceIdBytes));
  }

  @Test
  public void getSpanId() {
    assertThat(first.getSpanId()).isEqualTo(SpanId.fromBytes(firstSpanIdBytes));
    assertThat(second.getSpanId()).isEqualTo(SpanId.fromBytes(secondSpanIdBytes));
  }

  @Test
  public void getTraceOptions() {
    assertThat(first.getTraceOptions()).isEqualTo(TraceOptions.DEFAULT);
    assertThat(second.getTraceOptions()).isEqualTo(TraceOptions.builder().setIsSampled().build());
  }

  @Test
  public void spanContext_EqualsAndHashCode() {
    EqualsTester tester = new EqualsTester();
    tester.addEqualityGroup(
        first,
        SpanContext.create(
            TraceId.fromBytes(firstTraceIdBytes),
            SpanId.fromBytes(firstSpanIdBytes),
            TraceOptions.DEFAULT));
    tester.addEqualityGroup(
        second,
        SpanContext.create(
            TraceId.fromBytes(secondTraceIdBytes),
            SpanId.fromBytes(secondSpanIdBytes),
            TraceOptions.builder().setIsSampled().build()));
    tester.testEquals();
  }

  @Test
  public void spanContext_ToString() {
    assertThat(first.toString()).contains(TraceId.fromBytes(firstTraceIdBytes).toString());
    assertThat(first.toString()).contains(SpanId.fromBytes(firstSpanIdBytes).toString());
    assertThat(first.toString()).contains(TraceOptions.DEFAULT.toString());
    assertThat(second.toString()).contains(TraceId.fromBytes(secondTraceIdBytes).toString());
    assertThat(second.toString()).contains(SpanId.fromBytes(secondSpanIdBytes).toString());
    assertThat(second.toString())
        .contains(TraceOptions.builder().setIsSampled().build().toString());
  }
}
