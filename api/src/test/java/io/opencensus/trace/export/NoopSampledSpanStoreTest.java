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

package io.opencensus.trace.export;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.Lists;
import io.opencensus.trace.Status.CanonicalCode;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link NoopSampledSpanStore}. */
@RunWith(JUnit4.class)
public final class NoopSampledSpanStoreTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();
  private static final SampledSpanStore.PerSpanNameSummary EMPTY_PER_SPAN_NAME_SUMMARY =
      SampledSpanStore.PerSpanNameSummary.create(
          Collections.<SampledSpanStore.LatencyBucketBoundaries, Integer>emptyMap(),
          Collections.<CanonicalCode, Integer>emptyMap());

  @Test
  public void noopSampledSpanStore_RegisterUnregisterAndGetSummary() {
    // should return empty before register
    SampledSpanStore sampledSpanStore =
        ExportComponent.newNoopExportComponent().getSampledSpanStore();
    SampledSpanStore.Summary summary = sampledSpanStore.getSummary();
    assertThat(summary.getPerSpanNameSummary()).isEmpty();

    // should return non-empty summaries with zero latency/error sampled spans after register
    sampledSpanStore.registerSpanNamesForCollection(
        Collections.unmodifiableList(Lists.newArrayList("TestSpan1", "TestSpan2", "TestSpan3")));
    summary = sampledSpanStore.getSummary();
    assertThat(summary.getPerSpanNameSummary())
        .containsExactly(
            "TestSpan1", EMPTY_PER_SPAN_NAME_SUMMARY,
            "TestSpan2", EMPTY_PER_SPAN_NAME_SUMMARY,
            "TestSpan3", EMPTY_PER_SPAN_NAME_SUMMARY);

    // should unregister specific spanNames
    sampledSpanStore.unregisterSpanNamesForCollection(
        Collections.unmodifiableList(Lists.newArrayList("TestSpan1", "TestSpan3")));
    summary = sampledSpanStore.getSummary();
    assertThat(summary.getPerSpanNameSummary())
        .containsExactly("TestSpan2", EMPTY_PER_SPAN_NAME_SUMMARY);
  }

  @Test
  public void noopSampledSpanStore_GetLatencySampledSpans() {
    SampledSpanStore sampledSpanStore =
        ExportComponent.newNoopExportComponent().getSampledSpanStore();
    Collection<SpanData> latencySampledSpans =
        sampledSpanStore.getLatencySampledSpans(
            SampledSpanStore.LatencyFilter.create("TestLatencyFilter", 0, 0, 0));
    assertThat(latencySampledSpans).isEmpty();
  }

  @Test
  public void noopSampledSpanStore_GetErrorSampledSpans() {
    SampledSpanStore sampledSpanStore =
        ExportComponent.newNoopExportComponent().getSampledSpanStore();
    Collection<SpanData> errorSampledSpans =
        sampledSpanStore.getErrorSampledSpans(
            SampledSpanStore.ErrorFilter.create("TestErrorFilter", null, 0));
    assertThat(errorSampledSpans).isEmpty();
  }

  @Test
  public void noopSampledSpanStore_GetRegisteredSpanNamesForCollection() {
    SampledSpanStore sampledSpanStore =
        ExportComponent.newNoopExportComponent().getSampledSpanStore();
    sampledSpanStore.registerSpanNamesForCollection(
        Collections.unmodifiableList(Lists.newArrayList("TestSpan3", "TestSpan4")));
    Set<String> registeredSpanNames = sampledSpanStore.getRegisteredSpanNamesForCollection();
    assertThat(registeredSpanNames).containsExactly("TestSpan3", "TestSpan4");
  }
}
