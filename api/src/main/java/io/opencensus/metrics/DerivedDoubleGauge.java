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

import io.opencensus.common.ToDoubleFunction;
import io.opencensus.internal.Utils;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Derived Double Gauge metric, to report instantaneous measurement of a double value. Gauges can go
 * both up and down. The gauges values can be negative.
 *
 * @since 0.17
 */
@ThreadSafe
public abstract class DerivedDoubleGauge {

  /**
   * Creates a TimeSeries, that reports the value of the object after the function. This is, slightly
   * more common form of gauge is one that monitors some non-numeric object. The last argument
   * establishes the function that is used to determine the value of the gauge when the gauge is
   * collected. The number of label values must be the same to that of the label keys passed to
   * {@link MetricRegistry#addDerivedDoubleGauge}.
   *
   * @param labelValues the list of label values.
   * @param obj the state object from which the function derives a measurement.
   * @param function the function to be called.
   * @param <T> the type of the object upon which the function derives a measurement.
   * @since 0.17
   */
  public abstract <T> void createTimeSeries(
      List<LabelValue> labelValues, @Nullable T obj, ToDoubleFunction<T> function);

  /**
   * Removes the {@code TimeSeries} from gauge metric, if it is present.
   *
   * @param labelValues the list of label values.
   * @since 0.17
   */
  public abstract void removeTimeSeries(List<LabelValue> labelValues);

  /**
   * Removes all {@code TimeSeries}s from gauge metric.
   *
   * @since 0.17
   */
  public abstract void clear();

  /**
   * Returns the no-op implementation of the {@code DerivedDoubleGauge}.
   *
   * @return the no-op implementation of the {@code DerivedDoubleGauge}.
   * @since 0.17
   */
  static DerivedDoubleGauge getNoopDerivedDoubleGauge(
      String name, String description, String unit, List<LabelKey> labelKeys) {
    return NoopDerivedDoubleGauge.getInstance(name, description, unit, labelKeys);
  }

  /** No-op implementations of DoubleGauge class. */
  private static final class NoopDerivedDoubleGauge extends DerivedDoubleGauge {

    static NoopDerivedDoubleGauge getInstance(
        String name, String description, String unit, List<LabelKey> labelKeys) {
      return new NoopDerivedDoubleGauge(name, description, unit, labelKeys);
    }

    /** Creates a new {@code NoopDerivedDoubleGauge}. */
    NoopDerivedDoubleGauge(String name, String description, String unit, List<LabelKey> labelKeys) {
      Utils.checkNotNull(name, "name");
      Utils.checkNotNull(description, "description");
      Utils.checkNotNull(unit, "unit");
      Utils.checkNotNull(labelKeys, "labelKeys should not be null.");
      Utils.checkListElementNotNull(labelKeys, "labelKeys element should not be null.");
    }

    @Override
    public <T> void createTimeSeries(
        List<LabelValue> labelValues, @Nullable T obj, ToDoubleFunction<T> function) {
      Utils.checkNotNull(labelValues, "labelValues should not be null.");
      Utils.checkListElementNotNull(labelValues, "labelValues element should not be null.");
      Utils.checkNotNull(function, "function");
    }

    @Override
    public void removeTimeSeries(List<LabelValue> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues should not be null.");
      Utils.checkListElementNotNull(labelValues, "labelValues element should not be null.");
    }

    @Override
    public void clear() {}
  }
}
