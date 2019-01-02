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

package io.opencensus.exporter.metrics.util;

import io.opencensus.metrics.export.Metric;
import java.util.Collection;

/** Abstract class that represents a metric exporter. */
public abstract class MetricExporter {

  /**
   * Exports the list of given {@link Metric}.
   *
   * @param metrics the list of {@link Metric} to be exported.
   * @since 0.19
   */
  public abstract void export(Collection<Metric> metrics);
}
