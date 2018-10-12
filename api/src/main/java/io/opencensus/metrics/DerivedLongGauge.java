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

import io.opencensus.common.ToLongFunction;
import io.opencensus.internal.Utils;
import java.lang.ref.WeakReference;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Derived Long Gauge metric, to report instantaneous measurement of an int64 value. Gauges can go
 * both up and down. The gauges values can be negative. See {@link
 * io.opencensus.metrics.MetricRegistry} for an example of its use.
 *
 * <p>Example: Create a Gauge with object and function.
 *
 * <pre>{@code
 * class YourClass {
 *
 *   private static final MetricRegistry metricRegistry = Metrics.getMetricRegistry();
 *
 *   List<LabelKey> labelKeys = Arrays.asList(LabelKey.create("Name", "desc"));
 *   List<LabelValue> labelValues = Arrays.asList(LabelValue.create("Inbound"));
 *
 *   DerivedLongGauge gauge = metricRegistry.addDerivedLongGauge(
 *       "queue_size", "Pending jobs in a queue", "1", labelKeys);
 *
 *   QueueManager queueManager = new QueueManager();
 *   gauge.createTimeSeries(labelValues, queueManager,
 *         new ToLongFunction<QueueManager>() {
 *           {@literal @}Override
 *           public long applyAsLong(QueueManager queue) {
 *             return queue.size();
 *           }
 *         });
 *
 *   void doWork() {
 *      // Your code here.
 *   }
 * }
 *
 * }</pre>
 *
 * @since 0.17
 */
@ThreadSafe
public abstract class DerivedLongGauge {

  /**
   * Creates a {@code TimeSeries}. This gauge is self sufficient once created, so users should never
   * need to interact with it. The value of the gauge is observed from the obj and function. The
   * last argument establishes the function that is used to determine the value of the gauge when
   * the gauge is collected. The number of label values must be the same to that of the label keys
   * passed to {@link MetricRegistry#addDerivedLongGauge}.
   *
   * @param labelValues the list of label values.
   * @param obj the state object from which the function derives a measurement. We keep a {@link
   *     WeakReference} to the object and it is the user's responsibility to manage the lifetime of
   *     the object.
   * @param function the function to be called.
   * @param <T> the type of the object upon which the function derives a measurement.
   * @throws NullPointerException if {@code labelValues} is null OR element of {@code labelValues}
   *     is null OR {@code function} is null.
   * @throws IllegalArgumentException if different time series with the same labels already exists
   *     OR if number of {@code labelValues}s are not equal to the label keys passed to {@link
   *     MetricRegistry#addDerivedLongGauge}.
   * @since 0.17
   */
  public abstract <T> void createTimeSeries(
      List<LabelValue> labelValues, @Nullable T obj, ToLongFunction<T> function);

  /**
   * Removes the {@code TimeSeries} from the gauge metric, if it is present.
   *
   * @param labelValues the list of label values.
   * @throws NullPointerException if {@code labelValues} is null or element of {@code labelValues}
   *     is null.
   * @since 0.17
   */
  public abstract void removeTimeSeries(List<LabelValue> labelValues);

  /**
   * Removes all the {@code TimeSeries}s from the gauge metric.
   *
   * @since 0.17
   */
  public abstract void clear();

  /**
   * Returns the no-op implementation of the {@code DerivedLongGauge}.
   *
   * @return the no-op implementation of the {@code DerivedLongGauge}.
   * @since 0.17
   */
  static DerivedLongGauge getNoopDerivedLongGauge(
      String name, String description, String unit, List<LabelKey> labelKeys) {
    return NoopDerivedLongGauge.getInstance(name, description, unit, labelKeys);
  }

  /** No-op implementations of DerivedLongGauge class. */
  private static final class NoopDerivedLongGauge extends DerivedLongGauge {

    static NoopDerivedLongGauge getInstance(
        String name, String description, String unit, List<LabelKey> labelKeys) {
      return new NoopDerivedLongGauge(name, description, unit, labelKeys);
    }

    /** Creates a new {@code NoopDerivedLongGauge}. */
    NoopDerivedLongGauge(String name, String description, String unit, List<LabelKey> labelKeys) {
      Utils.checkNotNull(name, "name");
      Utils.checkNotNull(description, "description");
      Utils.checkNotNull(unit, "unit");
      Utils.checkNotNull(labelKeys, "labelKeys should not be null.");
      Utils.checkListElementNotNull(labelKeys, "labelKeys element should not be null.");
    }

    @Override
    public <T> void createTimeSeries(
        List<LabelValue> labelValues, @Nullable T obj, ToLongFunction<T> function) {
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
