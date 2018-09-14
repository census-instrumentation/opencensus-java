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

package io.opencensus.implcore.metrics.export;

import com.google.common.base.Preconditions;
import io.opencensus.metrics.MetricProducer;
import io.opencensus.metrics.export.MetricProducerManager;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.concurrent.ThreadSafe;

/** Implementation of {@link MetricProducerManager}. */
@ThreadSafe
public final class MetricProducerManagerImpl extends MetricProducerManager {

  private volatile Set<MetricProducer> metricProducers =
      Collections.unmodifiableSet(new LinkedHashSet<MetricProducer>());

  @Override
  public synchronized void add(MetricProducer metricProducer) {
    Preconditions.checkNotNull(metricProducer, "metricProducer");
    // Updating the set of MetricProducers happens under a lock to avoid multiple add or remove
    // operations to happen in the same time.
    Set<MetricProducer> newMetricProducers = new LinkedHashSet<MetricProducer>(metricProducers);
    if (!newMetricProducers.add(metricProducer)) {
      // The element already present, no need to update the current set of MetricProducers.
      return;
    }
    metricProducers = Collections.unmodifiableSet(newMetricProducers);
  }

  @Override
  public synchronized void remove(MetricProducer metricProducer) {
    Preconditions.checkNotNull(metricProducer, "metricProducer");
    // Updating the set of MetricProducers happens under a lock to avoid multiple add or remove
    // operations to happen in the same time.
    Set<MetricProducer> newMetricProducers = new LinkedHashSet<MetricProducer>(metricProducers);
    if (!newMetricProducers.remove(metricProducer)) {
      // The element not present, no need to update the current set of MetricProducers.
      return;
    }
    metricProducers = Collections.unmodifiableSet(newMetricProducers);
  }

  @Override
  public Set<MetricProducer> getAllMetricProducer() {
    return metricProducers;
  }
}
