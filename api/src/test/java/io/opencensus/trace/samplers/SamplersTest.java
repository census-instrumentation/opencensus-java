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

package io.opencensus.trace.samplers;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.trace.Sampler;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import java.util.Collections;
import java.util.Random;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Samplers}. */
@RunWith(JUnit4.class)
public class SamplersTest {
  private static final String SPAN_NAME = "MySpanName";
  private static final int NUM_SAMPLE_TRIES = 1000;
  private final Random random = new Random(1234);
  private final TraceId traceId = TraceId.generateRandomId(random);
  private final SpanId parentSpanId = SpanId.generateRandomId(random);
  private final SpanId spanId = SpanId.generateRandomId(random);
  private final SpanContext sampledSpanContext =
      SpanContext.create(traceId, parentSpanId, TraceOptions.builder().setIsSampled().build());
  private final SpanContext notSampledSpanContext =
      SpanContext.create(traceId, parentSpanId, TraceOptions.DEFAULT);

  @Test
  public void alwaysSampleSampler_AlwaysReturnTrue() {
    // Sampled parent.
    assertThat(
            Samplers.alwaysSample()
                .shouldSample(
                    sampledSpanContext,
                    false,
                    traceId,
                    spanId,
                    "Another name",
                    Collections.<Span>emptyList()))
        .isTrue();
    // Not sampled parent.
    assertThat(
            Samplers.alwaysSample()
                .shouldSample(
                    notSampledSpanContext,
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
    // Sampled parent.
    assertThat(
            Samplers.neverSample()
                .shouldSample(
                    sampledSpanContext,
                    false,
                    traceId,
                    spanId,
                    "bar",
                    Collections.<Span>emptyList()))
        .isFalse();
    // Not sampled parent.
    assertThat(
            Samplers.neverSample()
                .shouldSample(
                    notSampledSpanContext,
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

  @Test(expected = IllegalArgumentException.class)
  public void probabilitySampler_outOfRangeHighProbability() {
    Samplers.probabilitySampler(1.01);
  }

  @Test(expected = IllegalArgumentException.class)
  public void probabilitySampler_outOfRangeLowProbability() {
    Samplers.probabilitySampler(-0.00001);
  }

  // Applies the given sampler to NUM_SAMPLE_TRIES random traceId/spanId pairs.
  private static void assertSamplerSamplesWithProbability(
      Sampler sampler, SpanContext parent, double probability) {
    Random random = new Random(1234);
    int count = 0; // Count of spans with sampling enabled
    for (int i = 0; i < NUM_SAMPLE_TRIES; i++) {
      if (sampler.shouldSample(
          parent,
          false,
          TraceId.generateRandomId(random),
          SpanId.generateRandomId(random),
          SPAN_NAME,
          Collections.<Span>emptyList())) {
        count++;
      }
    }
    double proportionSampled = (double) count / NUM_SAMPLE_TRIES;
    // Allow for a large amount of slop (+/- 10%) in number of sampled traces, to avoid flakiness.
    assertThat(proportionSampled < probability + 0.1 && proportionSampled > probability - 0.1)
        .isTrue();
  }

  @Test
  public void probabilitySampler_differentProbabilities() {
    final Sampler neverSample = Samplers.probabilitySampler(0.0);
    assertSamplerSamplesWithProbability(neverSample, sampledSpanContext, 0.0);
    final Sampler alwaysSample = Samplers.probabilitySampler(1.0);
    assertSamplerSamplesWithProbability(alwaysSample, sampledSpanContext, 1.0);
    final Sampler fiftyPercentSample = Samplers.probabilitySampler(0.5);
    assertSamplerSamplesWithProbability(fiftyPercentSample, sampledSpanContext, 0.5);
    final Sampler twentyPercentSample = Samplers.probabilitySampler(0.2);
    assertSamplerSamplesWithProbability(twentyPercentSample, sampledSpanContext, 0.2);
    final Sampler twoThirdsSample = Samplers.probabilitySampler(2.0 / 3.0);
    assertSamplerSamplesWithProbability(twoThirdsSample, sampledSpanContext, 2.0 / 3.0);
  }

  @Test
  public void probabilitySampler_ToString() {
    assertThat((Samplers.probabilitySampler(0.5)).toString()).contains("0.5");
  }
}
