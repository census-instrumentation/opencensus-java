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

package io.opencensus.impl.metrics;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.implcore.metrics.MetricRegistryImpl;
import io.opencensus.implcore.metrics.export.ExportComponentImpl;
import io.opencensus.metrics.Metrics;
import io.opencensus.metrics.MetricsComponent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Test for accessing the {@link MetricsComponent} through the {@link Metrics} class. */
@RunWith(JUnit4.class)
public class MetricsTest {

  @Test
  public void getExportComponent() {
    assertThat(Metrics.getExportComponent()).isInstanceOf(ExportComponentImpl.class);
  }

  @Test
  public void getMetricRegistry() {
    assertThat(Metrics.getMetricRegistry()).isInstanceOf(MetricRegistryImpl.class);
  }
}
