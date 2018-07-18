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

package io.opencensus.metrics.export;

import io.opencensus.common.ExperimentalApi;
import io.opencensus.metrics.Utils;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Keeps a set of {@link MetricProducer} that is used by all exporters to
 *
 * @since 0.16
 */
@ExperimentalApi
@ThreadSafe
public final class MetricProducerManager {
  private volatile Set<MetricProducer> metricProducers =
      Collections.unmodifiableSet(new LinkedHashSet<MetricProducer>());

  /**
   * Adds the {@link MetricProducer} to the manager it is not already present.
   *
   * @param metricProducer the {@code MetricProducer} to be added to the manager.
   * @since 0.16
   */
  public synchronized void add(MetricProducer metricProducer) {
    Utils.checkNotNull(metricProducer, "metricProducer");
    // Updating the set of MetricProducers happens under a lock to avoid multiple add or remove
    // operations to happen in the same time.
    Set<MetricProducer> newMetricProducers = new LinkedHashSet<MetricProducer>(metricProducers);
    if (!newMetricProducers.add(metricProducer)) {
      // The element already present, no need to update the current set of MetricProducers.
      return;
    }
    metricProducers = Collections.unmodifiableSet(newMetricProducers);
  }

  /**
   * Removes the {@link MetricProducer} to the manager if it is present.
   *
   * @param metricProducer the {@code MetricProducer} to be removed from the manager.
   */
  public synchronized void remove(MetricProducer metricProducer) {
    Utils.checkNotNull(metricProducer, "metricProducer");
    // Updating the set of MetricProducers happens under a lock to avoid multiple add or remove
    // operations to happen in the same time.
    Set<MetricProducer> newMetricProducers = new LinkedHashSet<MetricProducer>(metricProducers);
    if (!newMetricProducers.remove(metricProducer)) {
      // The element not present, no need to update the current set of MetricProducers.
      return;
    }
    metricProducers = Collections.unmodifiableSet(newMetricProducers);
  }

  /**
   * Returns all registered {@link MetricProducer}s that should be exported.
   *
   * <p>This method should be used by any metrics exporter that automatically exports data for
   * {@code MetricProducer} registered with the {@code MetricProducerManager}.
   *
   * @return all registered {@code MetricProducer}s that should be exported.
   */
  public Set<MetricProducer> getAllMetricProducer() {
    return metricProducers;
  }

  // Package protected to allow us to possibly change this to an abstract class in the future. This
  // ensures that nobody can create an instance of this class except ExportComponent.
  MetricProducerManager() {}
}
