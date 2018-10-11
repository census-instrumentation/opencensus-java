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

import io.opencensus.internal.Utils;
import java.util.List;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Long Gauge metric, to report instantaneous measurement of an int64 value. Gauges can go both up
 * and down. The gauges values can be negative. For example, the number of pending jobs in the
 * queue. See {@link io.opencensus.metrics.MetricRegistry} for an example of its use.
 *
 * <p>Example 1: Create a Gauge with default labels.
 *
 * <pre>{@code
 * class YourClass {
 *
 *   private static final MetricRegistry metricRegistry = Metrics.getMetricRegistry();
 *
 *   List<LabelKey> labelKeys = Arrays.asList(LabelKey.create("Name", "desc"));
 *   LongGauge gauge = metricRegistry.addLongGauge(
 *       "queue_size", "Pending jobs in a queue", "1", labelKeys);
 *
 *   void doWork() {
 *      // add values
 *      gauge.getDefaultTimeSeries().add(10);
 *
 *      // Or, you can also use LongPoint objects to add/set values.
 *      LongPoint defaultPoint = gauge.getDefaultTimeSeries();
 *      defaultPoint.set(100);
 *   }
 * }
 *
 * }</pre>
 *
 * <p>Example 2: You can also use labels(keys and values) to track different types of metric.
 *
 * <pre>{@code
 * class YourClass {
 *
 *   private static final MetricRegistry metricRegistry = Metrics.getMetricRegistry();
 *
 *   List<LabelKey> labelKeys = Arrays.asList(LabelKey.create("Name", "desc"));
 *   List<LabelValue> labelValues = Arrays.asList(LabelValue.create("Inbound"));
 *
 *   LongGauge gauge = metricRegistry.addLongGauge(
 *       "queue_size", "Pending jobs in a queue", "1", labelKeys);
 *
 *   void doSomeWork() {
 *      // Your code here.
 *      gauge.getOrCreateTimeSeries(labelValues).set(15);
 *   }
 *
 * }
 * }</pre>
 *
 * @since 0.17
 */
@ThreadSafe
public abstract class LongGauge {

  /**
   * Creates a {@code TimeSeries} and returns a {@code LongPoint}, which is part of the TimeSeries.
   * This is more convenient form when you want to manually increase and decrease values as per your
   * service requirements. The number of label values must be the same to that of the label keys
   * passed to {@link MetricRegistry#addLongGauge}.
   *
   * <p>It is strongly recommended to keep a reference to the LongPoint instead of always calling
   * this method for manual operations.
   *
   * @param labelValues the list of label values.
   * @return a {@code LongPoint} the value of single gauge.
   * @throws NullPointerException if {@code labelValues} is null OR element of {@code labelValues}
   *     is null.
   * @throws IllegalArgumentException if number of {@code labelValues}s are not equal to the label
   *     keys passed to {@link MetricRegistry#addLongGauge}.
   * @since 0.17
   */
  public abstract LongPoint getOrCreateTimeSeries(List<LabelValue> labelValues);

  /**
   * Returns a {@code LongPoint} for a gauge with all labels not set, or default labels.
   *
   * @return a {@code LongPoint} the value of default gauge.
   * @since 0.17
   */
  public abstract LongPoint getDefaultTimeSeries();

  /**
   * Removes the {@code TimeSeries} from the gauge metric, if it is present. i.e. references to
   * previous {@code LongPoint} objects are invalid (not part of the metric).
   *
   * @param labelValues the list of label values.
   * @throws NullPointerException if {@code labelValues} is null or element of {@code labelValues}
   *     is null.
   * @since 0.17
   */
  public abstract void removeTimeSeries(List<LabelValue> labelValues);

  /**
   * References to all previous {@code LongPoint} objects are invalid (not part of the metric).
   *
   * @since 0.17
   */
  public abstract void clear();

  /**
   * Returns the no-op implementation of the {@code LongGauge}.
   *
   * @return the no-op implementation of the {@code LongGauge}.
   * @since 0.17
   */
  static LongGauge getNoopLongGauge(
      String name, String description, String unit, List<LabelKey> labelKeys) {
    return NoopLongGauge.getInstance(name, description, unit, labelKeys);
  }

  /**
   * The value of a single Gauge.
   *
   * @since 0.17
   */
  public abstract static class LongPoint {

    /**
     * Adds the given value to the current value. The values can be negative.
     *
     * @param amt the value to add
     * @since 0.17
     */
    public abstract void add(long amt);

    /**
     * Sets the given value.
     *
     * @param val the new value.
     * @since 0.17
     */
    public abstract void set(long val);
  }

  /** No-op implementations of LongGauge class. */
  private static final class NoopLongGauge extends LongGauge {

    static NoopLongGauge getInstance(
        String name, String description, String unit, List<LabelKey> labelKeys) {
      return new NoopLongGauge(name, description, unit, labelKeys);
    }

    /** Creates a new {@code NoopLongPoint}. */
    NoopLongGauge(String name, String description, String unit, List<LabelKey> labelKeys) {
      Utils.checkNotNull(name, "name");
      Utils.checkNotNull(description, "description");
      Utils.checkNotNull(unit, "unit");
      Utils.checkNotNull(labelKeys, "labelKeys should not be null.");
      Utils.checkListElementNotNull(labelKeys, "labelKeys element should not be null.");
    }

    @Override
    public NoopLongPoint getOrCreateTimeSeries(List<LabelValue> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues should not be null.");
      Utils.checkListElementNotNull(labelValues, "labelValues element should not be null.");
      return NoopLongPoint.getInstance();
    }

    @Override
    public NoopLongPoint getDefaultTimeSeries() {
      return NoopLongPoint.getInstance();
    }

    @Override
    public void removeTimeSeries(List<LabelValue> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues should not be null.");
      Utils.checkListElementNotNull(labelValues, "labelValues element should not be null.");
    }

    @Override
    public void clear() {}

    /** No-op implementations of LongPoint class. */
    private static final class NoopLongPoint extends LongPoint {
      private static final NoopLongPoint INSTANCE = new NoopLongPoint();

      private NoopLongPoint() {}

      /**
       * Returns a {@code NoopLongPoint}.
       *
       * @return a {@code NoopLongPoint}.
       */
      static NoopLongPoint getInstance() {
        return INSTANCE;
      }

      @Override
      public void add(long amt) {}

      @Override
      public void set(long val) {}
    }
  }
}
