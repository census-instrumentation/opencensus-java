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
import io.opencensus.implcore.internal.Utils;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LongGauge;
import io.opencensus.metrics.MetricRegistry;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricProducer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Implementation of {@link MetricRegistry}. */
public final class MetricRegistryImpl extends MetricRegistry {
  private final RegisteredMeters registeredMeters;
  private final MetricProducer metricProducer;

  MetricRegistryImpl(Clock clock) {
    registeredMeters = new RegisteredMeters();
    metricProducer = new MetricProducerForRegistry(registeredMeters, clock);
  }

  @Override
  public LongGauge addLongGauge(
      String name, String description, String unit, List<LabelKey> labelKeys) {
    Utils.checkListElementNotNull(
        checkNotNull(labelKeys, "labelKeys should not be null."),
        "labelKeys element should not be null.");
    LongGaugeImpl longGaugeMetric =
        new LongGaugeImpl(
            checkNotNull(name, "name"),
            checkNotNull(description, "description"),
            checkNotNull(unit, "unit"),
            Collections.unmodifiableList(new ArrayList<LabelKey>(labelKeys)));
    registeredMeters.registerMeter(name, longGaugeMetric);
    return longGaugeMetric;
  }

  private static final class RegisteredMeters {
    private volatile Map<String, Meter> registeredMeters = Collections.emptyMap();

    private Map<String, Meter> getRegisteredMeters() {
      return registeredMeters;
    }

    private synchronized void registerMeter(String meterName, Meter meter) {
      Meter existingMeter = registeredMeters.get(meterName);
      if (existingMeter != null) {
        // TODO(mayurkale): Allow users to register the same Meter multiple times without exception.
        throw new IllegalArgumentException(
            "A different metric with the same name already registered.");
      }

      Map<String, Meter> registeredMetersCopy = new LinkedHashMap<String, Meter>(registeredMeters);
      registeredMetersCopy.put(meterName, meter);
      registeredMeters = Collections.unmodifiableMap(registeredMetersCopy);
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
      // Get a snapshot of the current registered meters.
      Map<String, Meter> meters = registeredMeters.getRegisteredMeters();
      if (meters.isEmpty()) {
        return Collections.emptyList();
      }

      List<Metric> metrics = new ArrayList<Metric>(meters.size());
      for (Map.Entry<String, Meter> entry : meters.entrySet()) {
        Metric metric = entry.getValue().getMetric(clock);
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
