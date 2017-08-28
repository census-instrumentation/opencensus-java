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

package io.opencensus.contrib.zpages;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import io.opencensus.trace.Status.CanonicalCode;
import io.opencensus.trace.export.RunningSpanStore;
import io.opencensus.trace.export.SampledSpanStore;
import io.opencensus.trace.export.SampledSpanStore.LatencyBucketBoundaries;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link TracezZPageHandler}. */
@RunWith(JUnit4.class)
public class TracezZPageHandlerTest {
  private static final String ACTIVE_SPAN_NAME = "TestActiveSpan";
  private static final String SAMPLED_SPAN_NAME = "TestSampledSpan";
  private static final String ACTIVE_SAMPLED_SPAN_NAME = "TestActiveAndSampledSpan";
  @Mock private RunningSpanStore runningSpanStore;
  @Mock private SampledSpanStore sampledSpanStore;
  RunningSpanStore.Summary runningSpanStoreSummary;
  SampledSpanStore.Summary sampledSpanStoreSummary;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    Map<String, RunningSpanStore.PerSpanNameSummary> runningSummaryMap = new HashMap<>();
    runningSummaryMap.put(ACTIVE_SPAN_NAME, RunningSpanStore.PerSpanNameSummary.create(3));
    runningSummaryMap.put(ACTIVE_SAMPLED_SPAN_NAME, RunningSpanStore.PerSpanNameSummary.create(5));
    runningSpanStoreSummary = RunningSpanStore.Summary.create(runningSummaryMap);
    Map<LatencyBucketBoundaries, Integer> numbersOfLatencySampledSpans = new HashMap<>();
    numbersOfLatencySampledSpans.put(LatencyBucketBoundaries.MILLIx1_MILLIx10, 3);
    numbersOfLatencySampledSpans.put(LatencyBucketBoundaries.MICROSx10_MICROSx100, 7);
    Map<CanonicalCode, Integer> numbersOfErrorSampledSpans = new HashMap<>();
    numbersOfErrorSampledSpans.put(CanonicalCode.CANCELLED, 2);
    numbersOfErrorSampledSpans.put(CanonicalCode.DEADLINE_EXCEEDED, 5);
    Map<String, SampledSpanStore.PerSpanNameSummary> sampledSummaryMap = new HashMap<>();
    sampledSummaryMap.put(
        SAMPLED_SPAN_NAME,
        SampledSpanStore.PerSpanNameSummary.create(
            numbersOfLatencySampledSpans, numbersOfErrorSampledSpans));
    sampledSummaryMap.put(
        ACTIVE_SAMPLED_SPAN_NAME,
        SampledSpanStore.PerSpanNameSummary.create(
            numbersOfLatencySampledSpans, numbersOfErrorSampledSpans));
    sampledSpanStoreSummary = SampledSpanStore.Summary.create(sampledSummaryMap);
  }

  @Test
  public void emitSummaryTableForEachSpan() {
    OutputStream output = new ByteArrayOutputStream();
    TracezZPageHandler tracezZPageHandler =
        TracezZPageHandler.create(runningSpanStore, sampledSpanStore);
    when(runningSpanStore.getSummary()).thenReturn(runningSpanStoreSummary);
    when(sampledSpanStore.getSummary()).thenReturn(sampledSpanStoreSummary);
    tracezZPageHandler.emitHtml(Collections.emptyMap(), output);
    assertThat(output.toString()).contains(ACTIVE_SPAN_NAME);
    assertThat(output.toString()).contains(SAMPLED_SPAN_NAME);
    assertThat(output.toString()).contains(ACTIVE_SAMPLED_SPAN_NAME);
  }

  @Test
  public void linksForActiveRequests_InSummaryTable() {
    OutputStream output = new ByteArrayOutputStream();
    TracezZPageHandler tracezZPageHandler =
        TracezZPageHandler.create(runningSpanStore, sampledSpanStore);
    when(runningSpanStore.getSummary()).thenReturn(runningSpanStoreSummary);
    when(sampledSpanStore.getSummary()).thenReturn(sampledSpanStoreSummary);
    tracezZPageHandler.emitHtml(Collections.emptyMap(), output);
    // 3 active requests
    assertThat(output.toString()).contains("href='?zspanname=TestActiveSpan&ztype=0&zsubtype=0'>3");
    // No active links
    assertThat(output.toString())
        .doesNotContain("href='?zspanname=TestSampledSpan&ztype=0&zsubtype=0'");
    // 5 active requests
    assertThat(output.toString())
        .contains("href='?zspanname=TestActiveAndSampledSpan&ztype=0&zsubtype=0'>5");
  }

  @Test
  public void linksForSampledRequests_InSummaryTable() {
    OutputStream output = new ByteArrayOutputStream();
    TracezZPageHandler tracezZPageHandler =
        TracezZPageHandler.create(runningSpanStore, sampledSpanStore);
    when(runningSpanStore.getSummary()).thenReturn(runningSpanStoreSummary);
    when(sampledSpanStore.getSummary()).thenReturn(sampledSpanStoreSummary);
    tracezZPageHandler.emitHtml(Collections.emptyMap(), output);
    // No sampled links (ztype=1);
    assertThat(output.toString()).doesNotContain("href=\"?zspanname=TestActiveSpan&ztype=1");
    // Links for 7 samples [10us, 100us) and 3 samples [1ms, 10ms);
    assertThat(output.toString())
        .contains("href='?zspanname=TestSampledSpan&ztype=1&zsubtype=1'>7");
    assertThat(output.toString())
        .contains("href='?zspanname=TestSampledSpan&ztype=1&zsubtype=3'>3");
    // Links for 7 samples [10us, 100us) and 3 samples [1ms, 10ms);
    assertThat(output.toString())
        .contains("href='?zspanname=TestActiveAndSampledSpan&ztype=1&zsubtype=1'>7");
    assertThat(output.toString())
        .contains("href='?zspanname=TestActiveAndSampledSpan&ztype=1&zsubtype=3'>3");
  }

  @Test
  public void linksForFailedRequests_InSummaryTable() {
    OutputStream output = new ByteArrayOutputStream();
    TracezZPageHandler tracezZPageHandler =
        TracezZPageHandler.create(runningSpanStore, sampledSpanStore);
    when(runningSpanStore.getSummary()).thenReturn(runningSpanStoreSummary);
    when(sampledSpanStore.getSummary()).thenReturn(sampledSpanStoreSummary);
    tracezZPageHandler.emitHtml(Collections.emptyMap(), output);
    // No sampled links (ztype=1);
    assertThat(output.toString()).doesNotContain("href=\"?zspanname=TestActiveSpan&ztype=2");
    // Links for 7 errors 2 CANCELLED + 5 DEADLINE_EXCEEDED;
    assertThat(output.toString())
        .contains("href='?zspanname=TestSampledSpan&ztype=2&zsubtype=0'>7");
    // Links for 7 errors 2 CANCELLED + 5 DEADLINE_EXCEEDED;
    assertThat(output.toString())
        .contains("href='?zspanname=TestActiveAndSampledSpan&ztype=2&zsubtype=0'>7");
  }

  // TODO(bdrutu): Add tests for latency.
  // TODO(bdrutu): Add tests for samples/running/errors.
}
