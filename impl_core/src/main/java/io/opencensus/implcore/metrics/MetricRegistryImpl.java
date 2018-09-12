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
import io.opencensus.common.ToDoubleFunction;
import io.opencensus.common.ToLongFunction;
import io.opencensus.implcore.metrics.Gauge.DoubleGauge;
import io.opencensus.implcore.metrics.Gauge.LongGauge;
import io.opencensus.temporary.metrics.LabelKey;
import io.opencensus.temporary.metrics.LabelValue;
import io.opencensus.temporary.metrics.Metric;
import io.opencensus.temporary.metrics.MetricRegistry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/** Implementation of {@link MetricRegistry}. */
public final class MetricRegistryImpl extends MetricRegistry {
  private final Clock clock;
  private volatile Set<Gauge> registeredGauges = Collections.emptySet();

  MetricRegistryImpl(Clock clock) {
    this.clock = clock;
  }

  @Override
  public <T> void addLongGauge(
      String name,
      String description,
      String unit,
      LinkedHashMap<LabelKey, LabelValue> labels,
      T obj,
      ToLongFunction<T> function) {
    checkNotNull(labels, "labels");
    registerGauge(
        new LongGauge<T>(
            checkNotNull(name, "name"),
            checkNotNull(description, "description"),
            checkNotNull(unit, "unit"),
            Collections.unmodifiableList(new ArrayList<LabelKey>(labels.keySet())),
            Collections.unmodifiableList(new ArrayList<LabelValue>(labels.values())),
            obj,
            checkNotNull(function, "function")));
  }

  @Override
  public <T> void addDoubleGauge(
      String name,
      String description,
      String unit,
      LinkedHashMap<LabelKey, LabelValue> labels,
      T obj,
      ToDoubleFunction<T> function) {
    checkNotNull(labels, "labels");
    registerGauge(
        new DoubleGauge<T>(
            checkNotNull(name, "name"),
            checkNotNull(description, "description"),
            checkNotNull(unit, "unit"),
            Collections.unmodifiableList(new ArrayList<LabelKey>(labels.keySet())),
            Collections.unmodifiableList(new ArrayList<LabelValue>(labels.values())),
            obj,
            checkNotNull(function, "function")));
  }

  @Override
  public Collection<Metric> getMetrics() {
    // Get a snapshot of the current registered gauges.
    Set<Gauge> gaguges = registeredGauges;
    if (gaguges.isEmpty()) {
      return Collections.emptyList();
    }
    ArrayList<Metric> metrics = new ArrayList<Metric>();
    for (Gauge gauge : gaguges) {
      metrics.add(gauge.getMetric(clock));
    }
    return metrics;
  }

  private synchronized void registerGauge(Gauge gauge) {
    Set<Gauge> newGaguesList = new LinkedHashSet<Gauge>(registeredGauges);
    newGaguesList.add(gauge);
    registeredGauges = Collections.unmodifiableSet(newGaguesList);
  }
}
