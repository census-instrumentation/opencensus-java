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

package io.opencensus.temporary.metrics;

import io.opencensus.common.ExperimentalApi;
import io.opencensus.common.ToDoubleFunction;
import io.opencensus.common.ToLongFunction;
import io.opencensus.internal.Utils;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;

/**
 * Creates and manages your application's set of metrics. Exporters use the metric registry to
 * iterate over the set of metrics instrumenting your application, and then further export each
 * metric to the backend of choice.
 *
 * @since 0.16
 */
@ExperimentalApi
public abstract class MetricRegistry extends MetricProducer {
  /**
   * Build a new long gauge to be added to the registry.
   *
   * <p>Must be called only once.
   *
   * @param name the name of the metric.
   * @param description the description of the metric.
   * @param unit the unit of the metric.
   * @param obj the function argument.
   * @param function the function to be called.
   * @since 0.16 @ExperimentalApi
   */
  public abstract <T> void addLongGauge(
      String name,
      String description,
      String unit,
      LinkedHashMap<LabelKey, LabelValue> labels,
      T obj,
      ToLongFunction<T> function);

  /**
   * Build a new double gauge to be added to the registry.
   *
   * <p>Must be called only once.
   *
   * @param name the name of the metric.
   * @param description the description of the metric.
   * @param unit the unit of the metric.
   * @param obj the function argument.
   * @param function the function to be called.
   * @since 0.16 @ExperimentalApi
   */
  public abstract <T> void addDoubleGauge(
      String name,
      String description,
      String unit,
      LinkedHashMap<LabelKey, LabelValue> labels,
      T obj,
      ToDoubleFunction<T> function);

  static MetricRegistry newNoopMetricRegistry() {
    return new NoopMetricRegistry();
  }

  private static final class NoopMetricRegistry extends MetricRegistry {

    @Override
    public <T> void addLongGauge(
        String name,
        String description,
        String unit,
        LinkedHashMap<LabelKey, LabelValue> labels,
        T obj,
        ToLongFunction<T> function) {
      Utils.checkNotNull(name, "name");
      Utils.checkNotNull(description, "description");
      Utils.checkNotNull(unit, "unit");
      Utils.checkNotNull(labels, "labels");
      Utils.checkNotNull(function, "function");
    }

    @Override
    public <T> void addDoubleGauge(
        String name,
        String description,
        String unit,
        LinkedHashMap<LabelKey, LabelValue> labels,
        T obj,
        ToDoubleFunction<T> function) {
      Utils.checkNotNull(name, "name");
      Utils.checkNotNull(description, "description");
      Utils.checkNotNull(unit, "unit");
      Utils.checkNotNull(labels, "labels");
      Utils.checkNotNull(function, "function");
    }

    @Override
    public Collection<Metric> getMetrics() {
      return Collections.emptyList();
    }
  }
}
