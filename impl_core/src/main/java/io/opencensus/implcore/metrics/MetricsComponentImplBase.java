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

package io.opencensus.implcore.metrics;

import io.opencensus.common.Clock;
import io.opencensus.implcore.metrics.export.ExportComponentImpl;
import io.opencensus.temporary.metrics.MetricRegistry;
import io.opencensus.temporary.metrics.MetricsComponent;

/** Implementation of {@link MetricsComponent}. */
public class MetricsComponentImplBase extends MetricsComponent {

  private final ExportComponentImpl exportComponent;
  private final MetricRegistryImpl metricRegistry;

  @Override
  public ExportComponentImpl getExportComponent() {
    return exportComponent;
  }

  @Override
  public MetricRegistry getMetricRegistry() {
    return metricRegistry;
  }

  protected MetricsComponentImplBase(Clock clock) {
    exportComponent = new ExportComponentImpl();
    metricRegistry = new MetricRegistryImpl(clock);
  }
}
