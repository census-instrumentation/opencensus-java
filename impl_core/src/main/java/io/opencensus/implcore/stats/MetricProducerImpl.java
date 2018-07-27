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

package io.opencensus.implcore.stats;

import io.opencensus.metrics.Metric;
import io.opencensus.metrics.MetricProducer;
import java.util.Collection;
import javax.annotation.concurrent.ThreadSafe;

/** Implementation of {@link MetricProducer}. */
@ThreadSafe
public final class MetricProducerImpl extends MetricProducer {

  private final StatsManager statsManager;

  MetricProducerImpl(StatsManager statsManager) {
    this.statsManager = statsManager;
  }

  @Override
  public Collection<Metric> getMetrics() {
    return statsManager.getMetrics();
  }
}
