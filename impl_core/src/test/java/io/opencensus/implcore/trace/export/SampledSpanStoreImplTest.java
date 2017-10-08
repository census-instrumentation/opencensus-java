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

package io.opencensus.implcore.trace.export;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.common.Duration;
import io.opencensus.common.Timestamp;
import io.opencensus.implcore.trace.SpanImpl;
import io.opencensus.implcore.trace.SpanImpl.StartEndHandler;
import io.opencensus.testing.common.TestClock;
import io.opencensus.trace.EndSpanOptions;
import io.opencensus.trace.Span;
import io.opencensus.trace.Span.Options;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.Status;
import io.opencensus.trace.Status.CanonicalCode;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.export.SampledSpanStore.ErrorFilter;
import io.opencensus.trace.export.SampledSpanStore.LatencyBucketBoundaries;
import io.opencensus.trace.export.SampledSpanStore.LatencyFilter;
import io.opencensus.trace.export.SampledSpanStore.PerSpanNameSummary;
import io.opencensus.trace.export.SpanData;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link SampledSpanStoreImpl}. */
@RunWith(JUnit4.class)
public class SampledSpanStoreImplTest {
  private static final String REGISTERED_SPAN_NAME = "MySpanName/1";
  private static final String NOT_REGISTERED_SPAN_NAME = "MySpanName/2";
  private static final long NUM_NANOS_PER_SECOND = TimeUnit.SECONDS.toNanos(1);
  private final Random random = new Random(1234);
  private final SpanContext sampledSpanContext =
      SpanContext.create(
          TraceId.generateRandomId(random),
          SpanId.generateRandomId(random),
          TraceOptions.builder().setIsSampled(true).build());
  private final SpanContext notSampledSpanContext =
      SpanContext.create(
          TraceId.generateRandomId(random), SpanId.generateRandomId(random), TraceOptions.DEFAULT);
  private final SpanId parentSpanId = SpanId.generateRandomId(random);
  private final EnumSet<Options> recordSpanOptions = EnumSet.of(Options.RECORD_EVENTS);
  private final TestClock testClock = TestClock.create(Timestamp.create(12345, 54321));
  private final SampledSpanStoreImpl sampleStore = new SampledSpanStoreImpl();
  private final StartEndHandler startEndHandler =
      new StartEndHandler() {
        @Override
        public void onStart(SpanImpl span) {
          // Do nothing.
        }

        @Override
        public void onEnd(SpanImpl span) {
          sampleStore.considerForSampling(span);
        }
      };

  @Before
  public void setUp() {
    sampleStore.registerSpanNamesForCollection(Collections.singletonList(REGISTERED_SPAN_NAME));
  }

  private SpanImpl createSampledSpan(String spanName) {
    return SpanImpl.startSpan(
        sampledSpanContext,
        recordSpanOptions,
        spanName,
        parentSpanId,
        false,
        TraceParams.DEFAULT,
        false,
        startEndHandler,
        null,
        testClock);
  }

  private SpanImpl createSampledSpanWithRegisterName(String spanName) {
    return SpanImpl.startSpan(
        sampledSpanContext,
        recordSpanOptions,
        spanName,
        parentSpanId,
        false,
        TraceParams.DEFAULT,
        true,
        startEndHandler,
        null,
        testClock);
  }

  private SpanImpl createNotSampledSpan(String spanName) {
    return SpanImpl.startSpan(
        notSampledSpanContext,
        recordSpanOptions,
        spanName,
        parentSpanId,
        false,
        TraceParams.DEFAULT,
        false,
        startEndHandler,
        null,
        testClock);
  }

  private void addSpanNameToAllLatencyBuckets(String spanName) {
    for (LatencyBucketBoundaries boundaries : LatencyBucketBoundaries.values()) {
      Span sampledSpan = createSampledSpan(spanName);
      Span notSampledSpan = createNotSampledSpan(spanName);
      if (boundaries.getLatencyLowerNs() < NUM_NANOS_PER_SECOND) {
        testClock.advanceTime(Duration.create(0, (int) boundaries.getLatencyLowerNs()));
      } else {
        testClock.advanceTime(
            Duration.create(
                boundaries.getLatencyLowerNs() / NUM_NANOS_PER_SECOND,
                (int) (boundaries.getLatencyLowerNs() % NUM_NANOS_PER_SECOND)));
      }
      sampledSpan.end();
      notSampledSpan.end();
    }
  }

  private void addSpanNameToAllErrorBuckets(String spanName) {
    for (CanonicalCode code : CanonicalCode.values()) {
      if (code != CanonicalCode.OK) {
        Span sampledSpan = createSampledSpan(spanName);
        Span notSampledSpan = createNotSampledSpan(spanName);
        testClock.advanceTime(Duration.create(0, 1000));
        sampledSpan.end(EndSpanOptions.builder().setStatus(code.toStatus()).build());
        notSampledSpan.end(EndSpanOptions.builder().setStatus(code.toStatus()).build());
      }
    }
  }

  @Test
  public void addSpansWithRegisteredNamesInAllLatencyBuckets() {
    addSpanNameToAllLatencyBuckets(REGISTERED_SPAN_NAME);
    Map<String, PerSpanNameSummary> perSpanNameSummary =
        sampleStore.getSummary().getPerSpanNameSummary();
    assertThat(perSpanNameSummary.size()).isEqualTo(1);
    Map<LatencyBucketBoundaries, Integer> latencyBucketsSummaries =
        perSpanNameSummary.get(REGISTERED_SPAN_NAME).getNumbersOfLatencySampledSpans();
    assertThat(latencyBucketsSummaries.size()).isEqualTo(LatencyBucketBoundaries.values().length);
    for (Map.Entry<LatencyBucketBoundaries, Integer> it : latencyBucketsSummaries.entrySet()) {
      assertThat(it.getValue()).isEqualTo(2);
    }
  }

  @Test
  public void addSpansWithoutRegisteredNamesInAllLatencyBuckets() {
    addSpanNameToAllLatencyBuckets(NOT_REGISTERED_SPAN_NAME);
    Map<String, PerSpanNameSummary> perSpanNameSummary =
        sampleStore.getSummary().getPerSpanNameSummary();
    assertThat(perSpanNameSummary.size()).isEqualTo(1);
    assertThat(perSpanNameSummary.containsKey(NOT_REGISTERED_SPAN_NAME)).isFalse();
  }

  @Test
  public void registerUnregisterAndListSpanNames() {
    assertThat(sampleStore.getRegisteredSpanNamesForCollection())
        .containsExactly(REGISTERED_SPAN_NAME);
    sampleStore.registerSpanNamesForCollection(Collections.singletonList(NOT_REGISTERED_SPAN_NAME));
    assertThat(sampleStore.getRegisteredSpanNamesForCollection())
        .containsExactly(REGISTERED_SPAN_NAME, NOT_REGISTERED_SPAN_NAME);
    sampleStore.unregisterSpanNamesForCollection(
        Collections.singletonList(NOT_REGISTERED_SPAN_NAME));
    assertThat(sampleStore.getRegisteredSpanNamesForCollection())
        .containsExactly(REGISTERED_SPAN_NAME);
  }

  @Test
  public void registerSpanNamesViaSpanBuilderOption() {
    assertThat(sampleStore.getRegisteredSpanNamesForCollection())
        .containsExactly(REGISTERED_SPAN_NAME);
    createSampledSpanWithRegisterName(NOT_REGISTERED_SPAN_NAME).end();
    assertThat(sampleStore.getRegisteredSpanNamesForCollection())
        .containsExactly(REGISTERED_SPAN_NAME, NOT_REGISTERED_SPAN_NAME);
  }

  @Test
  public void addSpansWithRegisteredNamesInAllErrorBuckets() {
    addSpanNameToAllErrorBuckets(REGISTERED_SPAN_NAME);
    Map<String, PerSpanNameSummary> perSpanNameSummary =
        sampleStore.getSummary().getPerSpanNameSummary();
    assertThat(perSpanNameSummary.size()).isEqualTo(1);
    Map<CanonicalCode, Integer> errorBucketsSummaries =
        perSpanNameSummary.get(REGISTERED_SPAN_NAME).getNumbersOfErrorSampledSpans();
    assertThat(errorBucketsSummaries.size()).isEqualTo(CanonicalCode.values().length - 1);
    for (Map.Entry<CanonicalCode, Integer> it : errorBucketsSummaries.entrySet()) {
      assertThat(it.getValue()).isEqualTo(2);
    }
  }

  @Test
  public void addSpansWithoutRegisteredNamesInAllErrorBuckets() {
    addSpanNameToAllErrorBuckets(NOT_REGISTERED_SPAN_NAME);
    Map<String, PerSpanNameSummary> perSpanNameSummary =
        sampleStore.getSummary().getPerSpanNameSummary();
    assertThat(perSpanNameSummary.size()).isEqualTo(1);
    assertThat(perSpanNameSummary.containsKey(NOT_REGISTERED_SPAN_NAME)).isFalse();
  }

  @Test
  public void getErrorSampledSpans() {
    SpanImpl span = createSampledSpan(REGISTERED_SPAN_NAME);
    testClock.advanceTime(Duration.create(0, 1000));
    span.end(EndSpanOptions.builder().setStatus(Status.CANCELLED).build());
    Collection<SpanData> samples =
        sampleStore.getErrorSampledSpans(
            ErrorFilter.create(REGISTERED_SPAN_NAME, CanonicalCode.CANCELLED, 0));
    assertThat(samples.size()).isEqualTo(1);
    assertThat(samples.contains(span.toSpanData())).isTrue();
  }

  @Test
  public void getErrorSampledSpans_MaxSpansToReturn() {
    SpanImpl span1 = createSampledSpan(REGISTERED_SPAN_NAME);
    testClock.advanceTime(Duration.create(0, 1000));
    span1.end(EndSpanOptions.builder().setStatus(Status.CANCELLED).build());
    // Advance time to allow other spans to be sampled.
    testClock.advanceTime(Duration.create(5, 0));
    SpanImpl span2 = createSampledSpan(REGISTERED_SPAN_NAME);
    testClock.advanceTime(Duration.create(0, 1000));
    span2.end(EndSpanOptions.builder().setStatus(Status.CANCELLED).build());
    Collection<SpanData> samples =
        sampleStore.getErrorSampledSpans(
            ErrorFilter.create(REGISTERED_SPAN_NAME, CanonicalCode.CANCELLED, 1));
    assertThat(samples.size()).isEqualTo(1);
    // No order guaranteed so one of the spans should be in the list.
    assertThat(samples).containsAnyOf(span1.toSpanData(), span2.toSpanData());
  }

  @Test
  public void getErrorSampledSpans_NullCode() {
    SpanImpl span1 = createSampledSpan(REGISTERED_SPAN_NAME);
    testClock.advanceTime(Duration.create(0, 1000));
    span1.end(EndSpanOptions.builder().setStatus(Status.CANCELLED).build());
    SpanImpl span2 = createSampledSpan(REGISTERED_SPAN_NAME);
    testClock.advanceTime(Duration.create(0, 1000));
    span2.end(EndSpanOptions.builder().setStatus(Status.UNKNOWN).build());
    Collection<SpanData> samples =
        sampleStore.getErrorSampledSpans(ErrorFilter.create(REGISTERED_SPAN_NAME, null, 0));
    assertThat(samples.size()).isEqualTo(2);
    assertThat(samples).containsExactly(span1.toSpanData(), span2.toSpanData());
  }

  @Test
  public void getErrorSampledSpans_NullCode_MaxSpansToReturn() {
    SpanImpl span1 = createSampledSpan(REGISTERED_SPAN_NAME);
    testClock.advanceTime(Duration.create(0, 1000));
    span1.end(EndSpanOptions.builder().setStatus(Status.CANCELLED).build());
    SpanImpl span2 = createSampledSpan(REGISTERED_SPAN_NAME);
    testClock.advanceTime(Duration.create(0, 1000));
    span2.end(EndSpanOptions.builder().setStatus(Status.UNKNOWN).build());
    Collection<SpanData> samples =
        sampleStore.getErrorSampledSpans(ErrorFilter.create(REGISTERED_SPAN_NAME, null, 1));
    assertThat(samples.size()).isEqualTo(1);
    assertThat(samples).containsAnyOf(span1.toSpanData(), span2.toSpanData());
  }

  @Test
  public void getLatencySampledSpans() {
    SpanImpl span = createSampledSpan(REGISTERED_SPAN_NAME);
    testClock.advanceTime(Duration.create(0, (int) TimeUnit.MICROSECONDS.toNanos(20)));
    span.end();
    Collection<SpanData> samples =
        sampleStore.getLatencySampledSpans(
            LatencyFilter.create(
                REGISTERED_SPAN_NAME,
                TimeUnit.MICROSECONDS.toNanos(15),
                TimeUnit.MICROSECONDS.toNanos(25),
                0));
    assertThat(samples.size()).isEqualTo(1);
    assertThat(samples.contains(span.toSpanData())).isTrue();
  }

  @Test
  public void getLatencySampledSpans_ExclusiveUpperBound() {
    SpanImpl span = createSampledSpan(REGISTERED_SPAN_NAME);
    testClock.advanceTime(Duration.create(0, (int) TimeUnit.MICROSECONDS.toNanos(20)));
    span.end();
    Collection<SpanData> samples =
        sampleStore.getLatencySampledSpans(
            LatencyFilter.create(
                REGISTERED_SPAN_NAME,
                TimeUnit.MICROSECONDS.toNanos(15),
                TimeUnit.MICROSECONDS.toNanos(20),
                0));
    assertThat(samples.size()).isEqualTo(0);
  }

  @Test
  public void getLatencySampledSpans_InclusiveLowerBound() {
    SpanImpl span = createSampledSpan(REGISTERED_SPAN_NAME);
    testClock.advanceTime(Duration.create(0, (int) TimeUnit.MICROSECONDS.toNanos(20)));
    span.end();
    Collection<SpanData> samples =
        sampleStore.getLatencySampledSpans(
            LatencyFilter.create(
                REGISTERED_SPAN_NAME,
                TimeUnit.MICROSECONDS.toNanos(20),
                TimeUnit.MICROSECONDS.toNanos(25),
                0));
    assertThat(samples.size()).isEqualTo(1);
    assertThat(samples.contains(span.toSpanData())).isTrue();
  }

  @Test
  public void getLatencySampledSpans_QueryBetweenMultipleBuckets() {
    SpanImpl span1 = createSampledSpan(REGISTERED_SPAN_NAME);
    testClock.advanceTime(Duration.create(0, (int) TimeUnit.MICROSECONDS.toNanos(20)));
    span1.end();
    // Advance time to allow other spans to be sampled.
    testClock.advanceTime(Duration.create(5, 0));
    SpanImpl span2 = createSampledSpan(REGISTERED_SPAN_NAME);
    testClock.advanceTime(Duration.create(0, (int) TimeUnit.MICROSECONDS.toNanos(200)));
    span2.end();
    Collection<SpanData> samples =
        sampleStore.getLatencySampledSpans(
            LatencyFilter.create(
                REGISTERED_SPAN_NAME,
                TimeUnit.MICROSECONDS.toNanos(15),
                TimeUnit.MICROSECONDS.toNanos(250),
                0));
    assertThat(samples).containsExactly(span1.toSpanData(), span2.toSpanData());
  }

  @Test
  public void getLatencySampledSpans_MaxSpansToReturn() {
    SpanImpl span1 = createSampledSpan(REGISTERED_SPAN_NAME);
    testClock.advanceTime(Duration.create(0, (int) TimeUnit.MICROSECONDS.toNanos(20)));
    span1.end();
    // Advance time to allow other spans to be sampled.
    testClock.advanceTime(Duration.create(5, 0));
    SpanImpl span2 = createSampledSpan(REGISTERED_SPAN_NAME);
    testClock.advanceTime(Duration.create(0, (int) TimeUnit.MICROSECONDS.toNanos(200)));
    span2.end();
    Collection<SpanData> samples =
        sampleStore.getLatencySampledSpans(
            LatencyFilter.create(
                REGISTERED_SPAN_NAME,
                TimeUnit.MICROSECONDS.toNanos(15),
                TimeUnit.MICROSECONDS.toNanos(250),
                1));
    assertThat(samples.size()).isEqualTo(1);
    assertThat(samples.contains(span1.toSpanData())).isTrue();
  }

  @Test
  public void ignoreNegativeSpanLatency() {
    SpanImpl span = createSampledSpan(REGISTERED_SPAN_NAME);
    testClock.advanceTime(Duration.create(0, (int) TimeUnit.MICROSECONDS.toNanos(-20)));
    span.end();
    Collection<SpanData> samples =
        sampleStore.getLatencySampledSpans(
            LatencyFilter.create(REGISTERED_SPAN_NAME, 0, Long.MAX_VALUE, 0));
    assertThat(samples.size()).isEqualTo(0);
  }
}
