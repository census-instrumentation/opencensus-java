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

import static com.google.common.base.Preconditions.checkNotNull;
import static io.opencensus.implcore.internal.Utils.checkListElementNotNull;

import io.opencensus.common.Clock;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LongGauge;
import io.opencensus.metrics.MetricRegistry;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricProducer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

/** Implementation of {@link MetricRegistry}. */
public final class MetricRegistryImpl extends MetricRegistry {
  private final RegisteredMeters registeredMeters;
  private final MetricProducer metricProducer;
  private volatile Set<String> registeredMetrics = Collections.emptySet();

  MetricRegistryImpl(Clock clock) {
    registeredMeters = new RegisteredMeters();
    metricProducer = new MetricProducerForRegistry(registeredMeters, clock);
  }

  @Override
  public LongGauge addLongGauge(
      String name, String description, String unit, List<LabelKey> labelKeys) {
    checkNotNull(name, "name");
    checkNotNull(labelKeys, "labelKeys should not be null.");
    checkListElementNotNull(labelKeys, "labelKeys element should not be null.");
    checkDuplicateMetric(name, "A different metric with the same name is already registered.");
    LongGaugeImpl longGaugeMetric =
        new LongGaugeImpl(
            name,
            checkNotNull(description, "description"),
            checkNotNull(unit, "unit"),
            Collections.unmodifiableList(labelKeys));
    registeredMeters.registerMeter(longGaugeMetric);
    return longGaugeMetric;
  }

  private synchronized void checkDuplicateMetric(String metricName, @Nullable String errorMessage) {
    // Updating the set of RegisteredMetrics happens under a lock to avoid multiple add operations
    // to happen in the same time.
    Set<String> newRegisteredMetrics = new LinkedHashSet<String>(registeredMetrics);
    if (!newRegisteredMetrics.add(metricName)) {
      throw new IllegalArgumentException(errorMessage);
    }
    registeredMetrics = Collections.unmodifiableSet(newRegisteredMetrics);
  }

  private static final class RegisteredMeters {
    private volatile Set<Meter> registeredGauges = Collections.emptySet();

    private Set<Meter> getRegisteredMeters() {
      return registeredGauges;
    }

    private synchronized void registerMeter(Meter meter) {
      Set<Meter> newGaugesList = new LinkedHashSet<Meter>(registeredGauges);
      newGaugesList.add(meter);
      registeredGauges = Collections.unmodifiableSet(newGaugesList);
    }
  }

  private static final class MetricProducerForRegistry extends MetricProducer {
    private final RegisteredMeters registeredMeters;
    private final Clock clock;

    private MetricProducerForRegistry(RegisteredMeters registeredMeters, Clock clock) {
      this.registeredMeters = registeredMeters;
      this.clock = clock;
    }

    @Override
    public Collection<Metric> getMetrics() {
      // Get a snapshot of the current registered gauges.
      Set<Meter> gaguges = registeredMeters.getRegisteredMeters();
      if (gaguges.isEmpty()) {
        return Collections.emptyList();
      }
      ArrayList<Metric> metrics = new ArrayList<Metric>();
      for (Meter gauge : gaguges) {
        Metric metric = gauge.getMetric(clock);
        if (metric != null) {
          metrics.add(metric);
        }
      }
      return metrics;
    }
  }

  MetricProducer getMetricProducer() {
    return metricProducer;
  }
}
