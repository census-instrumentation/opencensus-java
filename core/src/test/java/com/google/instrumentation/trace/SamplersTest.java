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
import java.util.Random;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Samplers}. */
@RunWith(JUnit4.class)
public class SamplersTest {
  @Test
  public void alwaysSampleSampler_AlwaysReturnTrue() {
    Random random = new Random(1234);
    TraceId traceId = TraceId.generateRandomId(random);
    SpanId parentSpanId = SpanId.generateRandomId(random);
    SpanId spanId = SpanId.generateRandomId(random);
    // Traced parent.
    assertThat(
            Samplers.alwaysSample()
                .shouldSample(
                    new SpanContext(
                        traceId, parentSpanId, TraceOptions.builder().setIsSampled().build()),
                    false,
                    traceId,
                    spanId,
                    "Another name",
                    Collections.<Span>emptyList()))
        .isTrue();
    // Untraced parent.
    assertThat(
            Samplers.alwaysSample()
                .shouldSample(
                    new SpanContext(traceId, parentSpanId, TraceOptions.DEFAULT),
                    false,
                    traceId,
                    spanId,
                    "Yet another name",
                    Collections.<Span>emptyList()))
        .isTrue();
  }

  @Test
  public void alwaysSampleSampler_ToString() {
    assertThat(Samplers.alwaysSample().toString()).isEqualTo("AlwaysSampleSampler");
  }

  @Test
  public void neverSampleSampler_AlwaysReturnFalse() {
    Random random = new Random(1234);
    TraceId traceId = TraceId.generateRandomId(random);
    SpanId parentSpanId = SpanId.generateRandomId(random);
    SpanId spanId = SpanId.generateRandomId(random);
    // Traced parent.
    assertThat(
            Samplers.neverSample()
                .shouldSample(
                    new SpanContext(
                        traceId, parentSpanId, TraceOptions.builder().setIsSampled().build()),
                    false,
                    traceId,
                    spanId,
                    "bar",
                    Collections.<Span>emptyList()))
        .isFalse();
    // Untraced parent.
    assertThat(
            Samplers.neverSample()
                .shouldSample(
                    new SpanContext(traceId, parentSpanId, TraceOptions.DEFAULT),
                    false,
                    traceId,
                    spanId,
                    "quux",
                    Collections.<Span>emptyList()))
        .isFalse();
  }

  @Test
  public void neverSampleSampler_ToString() {
    assertThat(Samplers.neverSample().toString()).isEqualTo("NeverSampleSampler");
  }
}
