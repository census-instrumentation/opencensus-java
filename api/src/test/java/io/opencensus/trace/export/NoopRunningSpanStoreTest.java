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

import java.util.Collection;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link NoopRunningSpanStore}. */
@RunWith(JUnit4.class)
public final class NoopRunningSpanStoreTest {

  private final RunningSpanStore runningSpanStore =
      ExportComponent.newNoopExportComponent().getRunningSpanStore();

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void noopRunningSpanStore_GetSummary() {
    RunningSpanStore.Summary summary = runningSpanStore.getSummary();
    assertThat(summary.getPerSpanNameSummary()).isEmpty();
  }

  @Test
  public void noopRunningSpanStore_GetRunningSpans_DisallowsNull() {
    thrown.expect(NullPointerException.class);
    runningSpanStore.getRunningSpans(null);
  }

  @Test
  public void noopRunningSpanStore_GetRunningSpans() {
    Collection<SpanData> runningSpans =
        runningSpanStore.getRunningSpans(RunningSpanStore.Filter.create("TestSpan", 0));
    assertThat(runningSpans).isEmpty();
  }
}
