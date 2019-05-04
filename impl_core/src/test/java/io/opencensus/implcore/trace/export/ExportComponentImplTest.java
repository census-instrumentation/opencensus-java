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

import io.opencensus.implcore.internal.SimpleEventQueue;
import io.opencensus.trace.export.ExportComponent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link ExportComponentImpl}. */
@RunWith(JUnit4.class)
public class ExportComponentImplTest {
  private final ExportComponent exportComponentWithInProcess =
      ExportComponentImpl.createWithInProcessStores(new SimpleEventQueue());
  private final ExportComponent exportComponentWithoutInProcess =
      ExportComponentImpl.createWithoutInProcessStores(new SimpleEventQueue());

  @Test
  public void implementationOfSpanExporter() {
    assertThat(exportComponentWithInProcess.getSpanExporter()).isInstanceOf(SpanExporterImpl.class);
  }

  @Test
  public void implementationOfActiveSpans() {
    assertThat(exportComponentWithInProcess.getRunningSpanStore())
        .isInstanceOf(InProcessRunningSpanStore.class);
    assertThat(exportComponentWithoutInProcess.getRunningSpanStore())
        .isInstanceOf(InProcessRunningSpanStore.class);
  }

  @Test
  public void implementationOfSampledSpanStore() {
    assertThat(exportComponentWithInProcess.getSampledSpanStore())
        .isInstanceOf(InProcessSampledSpanStoreImpl.class);
    assertThat(exportComponentWithoutInProcess.getSampledSpanStore())
        .isInstanceOf(SampledSpanStoreImpl.getNoopSampledSpanStoreImpl().getClass());
  }
}
