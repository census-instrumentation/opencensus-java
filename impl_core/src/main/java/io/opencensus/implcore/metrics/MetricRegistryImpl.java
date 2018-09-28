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

import io.opencensus.common.Clock;
import io.opencensus.metrics.DoubleGaugeMetric;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LongGaugeMetric;
import io.opencensus.metrics.MetricRegistry;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricProducer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Implementation of {@link MetricRegistry}. */
public final class MetricRegistryImpl extends MetricRegistry {
  private final RegisteredMeters registeredMeters;
  private final MetricProducer metricProducer;
  private final Set<String> registeredMetric = new HashSet<String>();

  MetricRegistryImpl(Clock clock) {
    registeredMeters = new RegisteredMeters();
    metricProducer = new MetricProducerForRegistry(registeredMeters, clock);
  }

  @Override
  public LongGaugeMetric addLongGaugeMetric(
      String name, String description, String unit, List<LabelKey> labelKeys) {
    checkNotNull(name, "name");
    checkNotNull(labelKeys, "labelKeys");
    checkDuplicateMetric(name);

    LongGaugeMetricImpl longGaugeMetric =
        new LongGaugeMetricImpl(
            name,
            checkNotNull(description, "description"),
            checkNotNull(unit, "unit"),
            Collections.unmodifiableList(labelKeys));

    registeredMeters.registerMeter(longGaugeMetric);
    return longGaugeMetric;
  }

  @Override
  public DoubleGaugeMetric addDoubleGaugeMetric(
      String name, String description, String unit, List<LabelKey> labelKeys) {
    checkNotNull(name, "name");
    checkNotNull(labelKeys, "labelKeys");
    checkDuplicateMetric(name);

    DoubleGaugeMetricImpl doubleGaugeMetric =
        new DoubleGaugeMetricImpl(
            name,
            checkNotNull(description, "description"),
            checkNotNull(unit, "unit"),
            Collections.unmodifiableList(labelKeys));

    registeredMeters.registerMeter(doubleGaugeMetric);
    return doubleGaugeMetric;
  }

  private synchronized void checkDuplicateMetric(String metricName) {
    boolean isExists = registeredMetric.contains(metricName);
    if (isExists) {
      throw new IllegalArgumentException(
          "A different metric with the same name is already registered.");
    }
    registeredMetric.add(metricName);
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
      Set<Meter> gauges = registeredMeters.getRegisteredMeters();
      if (gauges.isEmpty()) {
        return Collections.emptyList();
      }
      ArrayList<Metric> metrics = new ArrayList<Metric>();
      for (Meter gauge : gauges) {
        metrics.add(gauge.getMetric(clock));
      }
      return metrics;
    }
  }

  MetricProducer getMetricProducer() {
    return metricProducer;
  }
}
