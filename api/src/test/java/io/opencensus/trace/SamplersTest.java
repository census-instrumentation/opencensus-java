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

import io.opencensus.trace.base.SpanId;
import io.opencensus.trace.base.TraceId;
import io.opencensus.trace.base.TraceOptions;
import java.util.Collections;
import java.util.Random;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Samplers}. */
@RunWith(JUnit4.class)
public class SamplersTest {
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

  private final void probabilitySampler_AlwaysReturnTrueForSampled(Sampler sampler) {
    final int numSamples = 100; // Number of traces for which to generate sampling decisions.
    for (int i = 0; i < numSamples; i++) {
      assertThat(
              sampler.shouldSample(
                  sampledSpanContext,
                  false,
                  TraceId.generateRandomId(random),
                  spanId,
                  "bar",
                  Collections.<Span>emptyList()))
          .isTrue();
    }
  }

  private final void probabilitySampler_SamplesWithProbabilityForUnsampled(
      Sampler sampler, double probability) {
    final int numSamples = 1000; // Number of traces for which to generate sampling decisions.
    int count = 0; // Count of spans with sampling enabled
    for (int i = 0; i < numSamples; i++) {
      if (sampler.shouldSample(
          notSampledSpanContext,
          false,
          TraceId.generateRandomId(random),
          spanId,
          "bar",
          Collections.<Span>emptyList())) {
        count++;
      }
    }
    double proportionSampled = (double) count / numSamples;
    // Allow for a large amount of slop (+/- 10%) in number of sampled traces, to avoid flakiness.
    assertThat(proportionSampled < probability + 0.1 && proportionSampled > probability - 0.1)
        .isTrue();
  }

  @Test
  public void probabilitySamper_SamplesWithProbability() {
    final Sampler neverSample = Samplers.probabilitySampler(0.0);
    probabilitySampler_AlwaysReturnTrueForSampled(neverSample);
    probabilitySampler_SamplesWithProbabilityForUnsampled(neverSample, 0.0);
    final Sampler alwaysSample = Samplers.probabilitySampler(1.0);
    probabilitySampler_AlwaysReturnTrueForSampled(alwaysSample);
    probabilitySampler_SamplesWithProbabilityForUnsampled(alwaysSample, 1.0);
    final Sampler fiftyPercentSample = Samplers.probabilitySampler(0.5);
    probabilitySampler_AlwaysReturnTrueForSampled(fiftyPercentSample);
    probabilitySampler_SamplesWithProbabilityForUnsampled(fiftyPercentSample, 0.5);
    final Sampler twentyPercentSample = Samplers.probabilitySampler(0.2);
    probabilitySampler_AlwaysReturnTrueForSampled(twentyPercentSample);
    probabilitySampler_SamplesWithProbabilityForUnsampled(twentyPercentSample, 0.2);
    final Sampler twoThirdsSample = Samplers.probabilitySampler(2.0 / 3.0);
    probabilitySampler_AlwaysReturnTrueForSampled(twoThirdsSample);
    probabilitySampler_SamplesWithProbabilityForUnsampled(twoThirdsSample, 2.0 / 3.0);
  }

  @Test
  public void probabilitySampler_ToString() {
    assertThat((Samplers.probabilitySampler(0.5)).toString()).contains("0.5");
  }
}
