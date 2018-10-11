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
 * Double Gauge metric, to report instantaneous measurement of a double value. Gauges can go both up
 * and down. The gauges values can be negative. For example, Free memory or Total memory. See {@link
 * io.opencensus.metrics.MetricRegistry} for an example of its use.
 *
 * <p>Example 1: Create a Gauge with default labels.
 *
 * <pre>{@code
 * class YourClass {
 *
 *   private static final MetricRegistry metricRegistry = Metrics.getMetricRegistry();
 *
 *   List<LabelKey> labelKeys = Arrays.asList(LabelKey.create("Name", "desc"));
 *   List<LabelValue> labelValues = Arrays.asList(LabelValue.create("Inbound"));
 *
 *   DoubleGauge requestQueue = metricRegistry.addDoubleGauge(
 *       "queue_size", "Pending jobs in a queue", "1", labelKeys);
 *
 *   void doWork() {
 *      // add values
 *      requestQueue.getDefaultTimeSeries().add(10);
 *
 *      // Or, you can also use DoublePoint objects to add/set values.
 *      DoublePoint defaultPoint = requestQueue.getDefaultTimeSeries();
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
 *   DoubleGauge requestQueue = metricRegistry.addDoubleGauge(
 *       "queue_size", "Pending jobs in a queue", "1", labelKeys);
 *
 *   void doSomeWork() {
 *      // Your code here.
 *      requestQueue.getOrCreateTimeSeries(labelValues).set(15);
 *   }
 *
 * }
 * }</pre>
 *
 * @since 0.17
 */
@ThreadSafe
public abstract class DoubleGauge {

  /**
   * Creates a {@code TimeSeries} and returns a {@code DoublePoint}, which is part of the
   * TimeSeries. This is more convenient form when you want to manually increase and decrease values
   * as per your service requirements. The number of label values must be the same to that of the
   * label keys passed to {@link MetricRegistry#addDoubleGauge}.
   *
   * <p>It is strongly recommended to keep a reference to the DoublePoint instead of always calling
   * this method.
   *
   * @param labelValues the list of label values.
   * @return a {@code DoublePoint} the value of single gauge.
   * @since 0.17
   */
  public abstract DoublePoint getOrCreateTimeSeries(List<LabelValue> labelValues);

  /**
   * Returns a {@code DoublePoint} for a gauge with all labels not set, or default labels.
   *
   * @return a {@code DoublePoint} the value of default gauge.
   * @since 0.17
   */
  public abstract DoublePoint getDefaultTimeSeries();

  /**
   * Removes the {@code TimeSeries} from the gauge metric, if it is present. i.e. references to
   * previous {@code DoublePoint} objects are invalid (not part of the metric).
   *
   * @param labelValues the list of label values.
   * @since 0.17
   */
  public abstract void removeTimeSeries(List<LabelValue> labelValues);

  /**
   * References to all previous {@code DoublePoint} objects are invalid (not part of the metric).
   *
   * @since 0.17
   */
  public abstract void clear();

  /**
   * Returns the no-op implementation of the {@code DoubleGauge}.
   *
   * @return the no-op implementation of the {@code DoubleGauge}.
   * @since 0.17
   */
  static DoubleGauge getNoopDoubleGauge(
      String name, String description, String unit, List<LabelKey> labelKeys) {
    return NoopDoubleGauge.getInstance(name, description, unit, labelKeys);
  }

  /**
   * The value of a single Gauge.
   *
   * @since 0.17
   */
  public abstract static class DoublePoint {

    /**
     * Adds the given value to the current value. The values can be negative.
     *
     * @param amt the value to add
     * @since 0.17
     */
    public abstract void add(double amt);

    /**
     * Sets the given value.
     *
     * @param val the new value.
     * @since 0.17
     */
    public abstract void set(double val);
  }

  /** No-op implementations of DoubleGauge class. */
  private static final class NoopDoubleGauge extends DoubleGauge {

    static NoopDoubleGauge getInstance(
        String name, String description, String unit, List<LabelKey> labelKeys) {
      return new NoopDoubleGauge(name, description, unit, labelKeys);
    }

    /** Creates a new {@code NoopDoubleGauge}. */
    NoopDoubleGauge(String name, String description, String unit, List<LabelKey> labelKeys) {
      Utils.checkNotNull(name, "name");
      Utils.checkNotNull(description, "description");
      Utils.checkNotNull(unit, "unit");
      Utils.checkNotNull(labelKeys, "labelKeys should not be null.");
      Utils.checkListElementNotNull(labelKeys, "labelKeys element should not be null.");
    }

    @Override
    public NoopDoublePoint getOrCreateTimeSeries(List<LabelValue> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues should not be null.");
      Utils.checkListElementNotNull(labelValues, "labelValues element should not be null.");
      return NoopDoublePoint.getInstance();
    }

    @Override
    public NoopDoublePoint getDefaultTimeSeries() {
      return NoopDoublePoint.getInstance();
    }

    @Override
    public void removeTimeSeries(List<LabelValue> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues should not be null.");
      Utils.checkListElementNotNull(labelValues, "labelValues element should not be null.");
    }

    @Override
    public void clear() {}

    /** No-op implementations of DoublePoint class. */
    private static final class NoopDoublePoint extends DoublePoint {
      private static final NoopDoublePoint INSTANCE = new NoopDoublePoint();

      private NoopDoublePoint() {}

      /**
       * Returns a {@code NoopDoublePoint}.
       *
       * @return a {@code NoopDoublePoint}.
       */
      static NoopDoublePoint getInstance() {
        return INSTANCE;
      }

      @Override
      public void add(double amt) {}

      @Override
      public void set(double val) {}
    }
  }
}
