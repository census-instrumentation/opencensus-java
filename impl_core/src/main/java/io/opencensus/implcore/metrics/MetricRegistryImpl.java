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
import io.opencensus.metrics.Gauge;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LongGaugeMetric;
import io.opencensus.metrics.MetricRegistry;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricProducer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Implementation of {@link MetricRegistry}. */
public final class MetricRegistryImpl extends MetricRegistry {
  private final RegisteredMeters registeredMeters;
  private final MetricProducer metricProducer;

  MetricRegistryImpl(Clock clock) {
    registeredMeters = new RegisteredMeters();
    metricProducer = new MetricProducerForRegistry(registeredMeters, clock);
  }

  @Override

  public <T> LongGaugeMetric<T> addLongGaugeMetric(
      String name, String description, String unit, List<LabelKey> labelKeys) {
    checkNotNull(labelKeys, "labelKeys");

    LongGaugeMetric<T> longGaugeMetric =
        new LongGaugeMetricImp<T>(
            checkNotNull(name, "name"),
            checkNotNull(description, "description"),
            checkNotNull(unit, "unit"),
            Collections.unmodifiableList(labelKeys));

    return longGaugeMetric;
  }

  @Override
  public <T> DoubleGaugeMetric<T> addDoubleGaugeMetric(
      String name, String description, String unit, List<LabelKey> labelKeys) {
    checkNotNull(labelKeys, "labelKeys");

    DoubleGaugeMetric<T> doubleGaugeMetric =
        new DoubleGaugeMetricImp<T>(
            checkNotNull(name, "name"),
            checkNotNull(description, "description"),
            checkNotNull(unit, "unit"),
            Collections.unmodifiableList(labelKeys));

    return doubleGaugeMetric;
  }

  private static final class RegisteredMeters {
    private volatile Set<Gauge> registeredGauges = Collections.emptySet();

    private Set<Gauge> getRegisteredMeters() {
      return registeredGauges;
    }

    private synchronized void registerMeter(Gauge gauge) {
      Set<Gauge> newGaguesList = new LinkedHashSet<Gauge>(registeredGauges);
      newGaguesList.add(gauge);
      registeredGauges = Collections.unmodifiableSet(newGaguesList);
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
      Set<Gauge> gauges = registeredMeters.getRegisteredMeters();
      if (gauges.isEmpty()) {
        return Collections.emptyList();
      }
      ArrayList<Metric> metrics = new ArrayList<Metric>();
      for (Gauge gauge : gauges) {
        metrics.add(gauge.getMetric(clock));
      }
      return metrics;
    }
  }

  MetricProducer getMetricProducer() {
    return metricProducer;
  }
}
