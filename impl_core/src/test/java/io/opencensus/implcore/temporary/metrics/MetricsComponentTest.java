/*
 * Copyright 2018, OpenCensus Authors
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

package io.opencensus.implcore.temporary.metrics;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.implcore.temporary.metrics.export.ExportComponent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link MetricsComponent}. */
@RunWith(JUnit4.class)
public class MetricsComponentTest {
  @Test
  public void defaultExportComponent() {
    assertThat(MetricsComponent.newNoopMetricsComponent().getExportComponent())
        .isInstanceOf(ExportComponent.newNoopExportComponent().getClass());
  }

  @Test
  public void defaultMetricRegistry() {
    assertThat(MetricsComponent.newNoopMetricsComponent().getMetricRegistry())
        .isInstanceOf(MetricRegistry.newNoopMetricRegistry().getClass());
  }
}
