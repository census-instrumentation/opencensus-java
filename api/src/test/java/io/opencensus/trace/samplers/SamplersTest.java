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

import io.opencensus.trace.NoopSpan;
import io.opencensus.trace.Sampler;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
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
      SpanContext.create(traceId, parentSpanId, TraceOptions.builder().setIsSampled(true).build());
  private final SpanContext notSampledSpanContext =
      SpanContext.create(traceId, parentSpanId, TraceOptions.DEFAULT);
  private final Span sampledSpan =
      new NoopSpan(sampledSpanContext, EnumSet.of(Span.Options.RECORD_EVENTS));

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
      Sampler sampler, SpanContext parent, List<Span> parentLinks, double probability) {
    Random random = new Random(1234);
    int count = 0; // Count of spans with sampling enabled
    for (int i = 0; i < NUM_SAMPLE_TRIES; i++) {
      if (sampler.shouldSample(
          parent,
          false,
          TraceId.generateRandomId(random),
          SpanId.generateRandomId(random),
          SPAN_NAME,
          parentLinks)) {
        count++;
      }
    }
    double proportionSampled = (double) count / NUM_SAMPLE_TRIES;
    // Allow for a large amount of slop (+/- 10%) in number of sampled traces, to avoid flakiness.
    assertThat(proportionSampled < probability + 0.1 && proportionSampled > probability - 0.1)
        .isTrue();
  }

  @Test
  public void probabilitySampler_DifferentProbabilities_NotSampledParent() {
    final Sampler neverSample = Samplers.probabilitySampler(0.0);
    assertSamplerSamplesWithProbability(
        neverSample, notSampledSpanContext, Collections.<Span>emptyList(), 0.0);
    final Sampler alwaysSample = Samplers.probabilitySampler(1.0);
    assertSamplerSamplesWithProbability(
        alwaysSample, notSampledSpanContext, Collections.<Span>emptyList(), 1.0);
    final Sampler fiftyPercentSample = Samplers.probabilitySampler(0.5);
    assertSamplerSamplesWithProbability(
        fiftyPercentSample, notSampledSpanContext, Collections.<Span>emptyList(), 0.5);
    final Sampler twentyPercentSample = Samplers.probabilitySampler(0.2);
    assertSamplerSamplesWithProbability(
        twentyPercentSample, notSampledSpanContext, Collections.<Span>emptyList(), 0.2);
    final Sampler twoThirdsSample = Samplers.probabilitySampler(2.0 / 3.0);
    assertSamplerSamplesWithProbability(
        twoThirdsSample, notSampledSpanContext, Collections.<Span>emptyList(), 2.0 / 3.0);
  }

  @Test
  public void probabilitySampler_DifferentProbabilities_SampledParent() {
    final Sampler neverSample = Samplers.probabilitySampler(0.0);
    assertSamplerSamplesWithProbability(
        neverSample, sampledSpanContext, Collections.<Span>emptyList(), 1.0);
    final Sampler alwaysSample = Samplers.probabilitySampler(1.0);
    assertSamplerSamplesWithProbability(
        alwaysSample, sampledSpanContext, Collections.<Span>emptyList(), 1.0);
    final Sampler fiftyPercentSample = Samplers.probabilitySampler(0.5);
    assertSamplerSamplesWithProbability(
        fiftyPercentSample, sampledSpanContext, Collections.<Span>emptyList(), 1.0);
    final Sampler twentyPercentSample = Samplers.probabilitySampler(0.2);
    assertSamplerSamplesWithProbability(
        twentyPercentSample, sampledSpanContext, Collections.<Span>emptyList(), 1.0);
    final Sampler twoThirdsSample = Samplers.probabilitySampler(2.0 / 3.0);
    assertSamplerSamplesWithProbability(
        twoThirdsSample, sampledSpanContext, Collections.<Span>emptyList(), 1.0);
  }

  @Test
  public void probabilitySampler_DifferentProbabilities_SampledParentLink() {
    final Sampler neverSample = Samplers.probabilitySampler(0.0);
    assertSamplerSamplesWithProbability(
        neverSample, notSampledSpanContext, Arrays.asList(sampledSpan), 1.0);
    final Sampler alwaysSample = Samplers.probabilitySampler(1.0);
    assertSamplerSamplesWithProbability(
        alwaysSample, notSampledSpanContext, Arrays.asList(sampledSpan), 1.0);
    final Sampler fiftyPercentSample = Samplers.probabilitySampler(0.5);
    assertSamplerSamplesWithProbability(
        fiftyPercentSample, notSampledSpanContext, Arrays.asList(sampledSpan), 1.0);
    final Sampler twentyPercentSample = Samplers.probabilitySampler(0.2);
    assertSamplerSamplesWithProbability(
        twentyPercentSample, notSampledSpanContext, Arrays.asList(sampledSpan), 1.0);
    final Sampler twoThirdsSample = Samplers.probabilitySampler(2.0 / 3.0);
    assertSamplerSamplesWithProbability(
        twoThirdsSample, notSampledSpanContext, Arrays.asList(sampledSpan), 1.0);
  }

  @Test
  public void probabilitySampler_SampleBasedOnTraceId() {
    final Sampler defaultProbability = Samplers.probabilitySampler(0.0001);
    // This traceId will not be sampled by the ProbabilitySampler because the first 8 bytes as long
    // is not less than probability * Long.MAX_VALUE;
    TraceId notSampledtraceId =
        TraceId.fromBytes(
            new byte[] {
              (byte) 0x8F,
              (byte) 0xFF,
              (byte) 0xFF,
              (byte) 0xFF,
              (byte) 0xFF,
              (byte) 0xFF,
              (byte) 0xFF,
              (byte) 0xFF,
              0,
              0,
              0,
              0,
              0,
              0,
              0,
              0
            });
    assertThat(
            defaultProbability.shouldSample(
                null,
                false,
                notSampledtraceId,
                SpanId.generateRandomId(random),
                SPAN_NAME,
                Collections.<Span>emptyList()))
        .isFalse();
    // This traceId will be sampled by the ProbabilitySampler because the first 8 bytes as long
    // is less than probability * Long.MAX_VALUE;
    TraceId sampledtraceId =
        TraceId.fromBytes(
            new byte[] {
              (byte) 0x00,
              (byte) 0x00,
              (byte) 0xFF,
              (byte) 0xFF,
              (byte) 0xFF,
              (byte) 0xFF,
              (byte) 0xFF,
              (byte) 0xFF,
              0,
              0,
              0,
              0,
              0,
              0,
              0,
              0
            });
    assertThat(
            defaultProbability.shouldSample(
                null,
                false,
                sampledtraceId,
                SpanId.generateRandomId(random),
                SPAN_NAME,
                Collections.<Span>emptyList()))
        .isTrue();
  }

  @Test
  public void probabilitySampler_getDescription() {
    assertThat(Samplers.probabilitySampler(0.5).getDescription())
        .isEqualTo(String.format("ProbabilitySampler{%.6f}", 0.5));
  }

  @Test
  public void probabilitySampler_ToString() {
    assertThat(Samplers.probabilitySampler(0.5).toString()).contains("0.5");
  }
}
