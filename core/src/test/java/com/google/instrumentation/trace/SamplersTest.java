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

import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Samplers}. */
@RunWith(JUnit4.class)
public class SamplersTest {
  @Test
  public void alwaysSampleSampler_AlwaysReturnTrue() {
    TraceId traceId = new TraceId(2, 3);
    SpanId parentSpanId = new SpanId(17);
    SpanId spanId = new SpanId(42);
    // Traced parent.
    assertThat(
            Samplers.alwaysSample()
                .shouldSample(
                    new SpanContext(traceId, parentSpanId, new TraceOptions(TraceOptions.IS_SAMPLED)),
                    false,
                    traceId,
                    spanId,
                    "Another name",
                    Collections.emptyList()))
        .isTrue();
    // Untraced parent.
    assertThat(
            Samplers.alwaysSample()
                .shouldSample(
                    new SpanContext(traceId, parentSpanId, new TraceOptions(0)),
                    false,
                    traceId,
                    spanId,
                    "Yet another name",
                    Collections.emptyList()))
        .isTrue();
  }

  @Test
  public void alwaysSampleSampler_ToString() {
    assertThat(Samplers.alwaysSample().toString()).isEqualTo("AlwaysSampleSampler");
  }

  @Test
  public void neverSampleSampler_AlwaysReturnFalse() {
    TraceId traceId = new TraceId(4, 5);
    SpanId parentSpanId = new SpanId(19);
    SpanId spanId = new SpanId(42);
    // Traced parent.
    assertThat(
            Samplers.neverSample()
                .shouldSample(
                    new SpanContext(traceId, parentSpanId, new TraceOptions(TraceOptions.IS_SAMPLED)),
                    false,
                    traceId,
                    spanId,
                    "bar",
                    Collections.emptyList()))
        .isFalse();
    // Untraced parent.
    assertThat(
            Samplers.neverSample()
                .shouldSample(
                    new SpanContext(traceId, parentSpanId, new TraceOptions(0)),
                    false,
                    traceId,
                    spanId,
                    "quux",
                    Collections.emptyList()))
        .isFalse();
  }

  @Test
  public void neverSampleSampler_ToString() {
    assertThat(Samplers.neverSample().toString()).isEqualTo("NeverSampleSampler");
  }
}
