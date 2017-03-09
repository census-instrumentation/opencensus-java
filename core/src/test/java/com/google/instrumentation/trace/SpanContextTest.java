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

import com.google.common.testing.EqualsTester;
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
      new SpanContext(
          TraceId.fromBytes(firstTraceIdBytes),
          SpanId.fromBytes(firstSpanIdBytes),
          TraceOptions.getDefault());
  private static final SpanContext second =
      new SpanContext(
          TraceId.fromBytes(secondTraceIdBytes),
          SpanId.fromBytes(secondSpanIdBytes),
          new TraceOptions(TraceOptions.IS_SAMPLED));

  @Test
  public void invalidSpanContext() {
    assertThat(SpanContext.INVALID.getTraceId()).isEqualTo(TraceId.INVALID);
    assertThat(SpanContext.INVALID.getSpanId()).isEqualTo(SpanId.INVALID);
    assertThat(SpanContext.INVALID.getTraceOptions()).isEqualTo(TraceOptions.getDefault());
  }

  @Test
  public void isValid() {
    assertThat(SpanContext.INVALID.isValid()).isFalse();
    assertThat(
            new SpanContext(
                    TraceId.fromBytes(firstTraceIdBytes), SpanId.INVALID, TraceOptions.getDefault())
                .isValid())
        .isFalse();
    assertThat(
            new SpanContext(
                    TraceId.INVALID, SpanId.fromBytes(firstSpanIdBytes), TraceOptions.getDefault())
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
    assertThat(first.getTraceOptions()).isEqualTo(TraceOptions.getDefault());
    assertThat(second.getTraceOptions()).isEqualTo(new TraceOptions(TraceOptions.IS_SAMPLED));
  }

  @Test
  public void spanContext_EqualsAndHashCode() {
    EqualsTester tester = new EqualsTester();
    tester.addEqualityGroup(
        first,
        new SpanContext(
            TraceId.fromBytes(firstTraceIdBytes),
            SpanId.fromBytes(firstSpanIdBytes),
            TraceOptions.getDefault()));
    tester.addEqualityGroup(
        second,
        new SpanContext(
            TraceId.fromBytes(secondTraceIdBytes),
            SpanId.fromBytes(secondSpanIdBytes),
            new TraceOptions(TraceOptions.IS_SAMPLED)));
    tester.testEquals();
  }

  @Test
  public void spanContext_ToString() {
    assertThat(first.toString()).contains(TraceId.fromBytes(firstTraceIdBytes).toString());
    assertThat(first.toString()).contains(SpanId.fromBytes(firstSpanIdBytes).toString());
    assertThat(first.toString()).contains(TraceOptions.getDefault().toString());
    assertThat(second.toString()).contains(TraceId.fromBytes(secondTraceIdBytes).toString());
    assertThat(second.toString()).contains(SpanId.fromBytes(secondSpanIdBytes).toString());
    assertThat(second.toString()).contains(new TraceOptions(TraceOptions.IS_SAMPLED).toString());
  }
}
