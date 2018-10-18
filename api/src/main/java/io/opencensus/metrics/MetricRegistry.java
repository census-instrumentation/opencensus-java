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

package io.opencensus.metrics;

import io.opencensus.common.ExperimentalApi;
import io.opencensus.common.ToLongFunction;
import io.opencensus.internal.Utils;
import java.util.List;

/**
 * Creates and manages your application's set of metrics. The default implementation of this creates
 * a {@link io.opencensus.metrics.export.MetricProducer} and registers it to the global {@link
 * io.opencensus.metrics.export.MetricProducerManager}.
 *
 * @since 0.17
 */
@ExperimentalApi
public abstract class MetricRegistry {
  /**
   * Builds a new long gauge to be added to the registry. This is more convenient form when you want
   * to manually increase and decrease values as per your service requirements.
   *
   * @param name the name of the metric.
   * @param description the description of the metric.
   * @param unit the unit of the metric.
   * @param labelKeys the list of the label keys.
   * @throws NullPointerException if {@code labelKeys} is null OR any element of {@code labelKeys}
   *     is null OR {@code name}, {@code description}, {@code unit} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @since 0.17
   */
  @ExperimentalApi
  public abstract LongGauge addLongGauge(
      String name, String description, String unit, List<LabelKey> labelKeys);

  /**
   * Builds a new double gauge to be added to the registry. This is more convenient form when you
   * want to manually increase and decrease values as per your service requirements.
   *
   * @param name the name of the metric.
   * @param description the description of the metric.
   * @param unit the unit of the metric.
   * @param labelKeys the list of the label keys.
   * @throws NullPointerException if {@code labelKeys} is null OR any element of {@code labelKeys}
   *     is null OR {@code name}, {@code description}, {@code unit} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @since 0.17
   */
  @ExperimentalApi
  public abstract DoubleGauge addDoubleGauge(
      String name, String description, String unit, List<LabelKey> labelKeys);

  /**
   * Builds a new derived long gauge to be added to the registry. This is more convenient form when
   * you want to define a gauge by executing a {@link ToLongFunction} on an object.
   *
   * @param name the name of the metric.
   * @param description the description of the metric.
   * @param unit the unit of the metric.
   * @param labelKeys the list of the label keys.
   * @throws NullPointerException if {@code labelKeys} is null OR any element of {@code labelKeys}
   *     is null OR {@code name}, {@code description}, {@code unit} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @since 0.17
   */
  @ExperimentalApi
  public abstract DerivedLongGauge addDerivedLongGauge(
      String name, String description, String unit, List<LabelKey> labelKeys);

  static MetricRegistry newNoopMetricRegistry() {
    return new NoopMetricRegistry();
  }

  private static final class NoopMetricRegistry extends MetricRegistry {

    @Override
    public LongGauge addLongGauge(
        String name, String description, String unit, List<LabelKey> labelKeys) {
      Utils.checkListElementNotNull(
          Utils.checkNotNull(labelKeys, "labelKeys"), "labelKey element should not be null.");
      return LongGauge.newNoopLongGauge(
          Utils.checkNotNull(name, "name"),
          Utils.checkNotNull(description, "description"),
          Utils.checkNotNull(unit, "unit"),
          labelKeys);
    }

    @Override
    public DoubleGauge addDoubleGauge(
        String name, String description, String unit, List<LabelKey> labelKeys) {
      Utils.checkListElementNotNull(
          Utils.checkNotNull(labelKeys, "labelKeys"), "labelKey element should not be null.");
      return DoubleGauge.newNoopDoubleGauge(
          Utils.checkNotNull(name, "name"),
          Utils.checkNotNull(description, "description"),
          Utils.checkNotNull(unit, "unit"),
          labelKeys);
    }

    @Override
    public DerivedLongGauge addDerivedLongGauge(
        String name, String description, String unit, List<LabelKey> labelKeys) {
      Utils.checkListElementNotNull(
          Utils.checkNotNull(labelKeys, "labelKeys"), "labelKey element should not be null.");
      return DerivedLongGauge.newNoopDerivedLongGauge(
          Utils.checkNotNull(name, "name"),
          Utils.checkNotNull(description, "description"),
          Utils.checkNotNull(unit, "unit"),
          labelKeys);
    }
  }
}
