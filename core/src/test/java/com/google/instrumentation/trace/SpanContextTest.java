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
  private static final SpanContext first =
      new SpanContext(new TraceId(0, 10), 30, TraceOptions.getDefault());
  private static final SpanContext second =
      new SpanContext(new TraceId(0, 20), 40, new TraceOptions(TraceOptions.IS_SAMPLED));

  @Test
  public void invalidSpanContext() {
    assertThat(SpanContext.getInvalid().getTraceId()).isEqualTo(TraceId.getInvalid());
    assertThat(SpanContext.getInvalid().getSpanId()).isEqualTo(0);
    assertThat(SpanContext.getInvalid().getTraceOptions()).isEqualTo(TraceOptions.getDefault());
  }

  @Test
  public void isValid() {
    assertThat(SpanContext.getInvalid().isValid()).isFalse();
    assertThat(new SpanContext(new TraceId(0, 10), 0, TraceOptions.getDefault()).isValid())
        .isFalse();
    assertThat(new SpanContext(TraceId.getInvalid(), 10, TraceOptions.getDefault()).isValid())
        .isFalse();
    assertThat(first.isValid()).isTrue();
    assertThat(second.isValid()).isTrue();
  }

  @Test
  public void getTraceId() {
    assertThat(first.getTraceId()).isEqualTo(new TraceId(0, 10));
    assertThat(second.getTraceId()).isEqualTo(new TraceId(0, 20));
  }

  @Test
  public void getSpanId() {
    assertThat(first.getSpanId()).isEqualTo(30);
    assertThat(second.getSpanId()).isEqualTo(40);
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
        first, new SpanContext(new TraceId(0, 10), 30, TraceOptions.getDefault()));
    tester.addEqualityGroup(
        second, new SpanContext(new TraceId(0, 20), 40, new TraceOptions(TraceOptions.IS_SAMPLED)));
    tester.testEquals();
  }

  @Test
  public void spanContext_ToString() {
    assertThat(first.toString()).contains(new TraceId(0, 10).toString());
    assertThat(first.toString()).contains("spanId=000000000000001e");
    assertThat(first.toString()).contains(TraceOptions.getDefault().toString());
    assertThat(second.toString()).contains(new TraceId(0, 20).toString());
    assertThat(second.toString()).contains("spanId=0000000000000028");
    assertThat(second.toString()).contains(new TraceOptions(TraceOptions.IS_SAMPLED).toString());
  }
}
