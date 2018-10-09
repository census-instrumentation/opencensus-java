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
 *   DoubleGauge totalMemory = metricRegistry.addDoubleGauge(
 *       "Total_memory", "Total CPU Memory", "1", new ArrayList<LabelKey>());
 *
 *   Point defaultPoint = totalMemory.getDefaultTimeSeries();
 *
 *   void doWork() {
 *      defaultPoint.inc();
 *      // Your code here.
 *      defaultPoint.dec();
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
 *   List<LabelKey> keys = Collections.singletonList(LabelKey.create("Type","desc"));
 *   DoubleGauge totalMemory = metricRegistry.addDoubleGauge(
 *       "Total_Memory", "Total CPU Memory", "1", keys);
 *
 *   List<LabelValue> usedMemory = Collections.singletonList(LabelValue.create("Used"));
 *   Point point = totalMemory.getOrCreateTimeSeries(usedMemory);
 *
 *   void doSomeWork() {
 *      point.set(12.5);
 *      // Your code here.
 *      point.dec();
 *   }
 * }
 * }</pre>
 *
 * @since 0.17
 */
@ThreadSafe
public abstract class DoubleGauge {

  /**
   * Creates a TimeSeries and returns a {@code Point}, which is part of the TimeSeries. This is more
   * convenient form when you can want to manually increase and decrease values as per your service
   * requirements. The number of label values must be the same to that of the label keys passed to
   * {@link MetricRegistry#addDoubleGauge}.
   *
   * @param labelValues the list of label values.
   * @return a {@code Point} the value of single gauge.
   * @since 0.17
   */
  public abstract Point getOrCreateTimeSeries(List<LabelValue> labelValues);

  /**
   * Returns a {@code Point} for a gauge with all labels not set, or default labels.
   *
   * @return a {@code Point} the value of default gauge.
   * @since 0.17
   */
  public abstract Point getDefaultTimeSeries();

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
  public abstract static class Point {

    /**
     * Increments the gauge value by one.
     *
     * @since 0.17
     */
    public abstract void inc();

    /**
     * Increments the gauge by the given amount.
     *
     * @param amt amount to add to the gauge.
     * @since 0.17
     */
    public abstract void inc(double amt);

    /**
     * Decrements the gauge value by one.
     *
     * @since 0.17
     */
    public abstract void dec();

    /**
     * Decrements the gauge by the given amount.
     *
     * @param amt amount to subtract from the gauge.
     * @since 0.17
     */
    public abstract void dec(double amt);

    /**
     * Sets the gauge to the given value.
     *
     * @param val to assign to the gauge.
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
    public NoopPoint getOrCreateTimeSeries(List<LabelValue> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues should not be null.");
      Utils.checkListElementNotNull(labelValues, "labelValues element should not be null.");
      return NoopPoint.getInstance();
    }

    @Override
    public NoopPoint getDefaultTimeSeries() {
      return NoopPoint.getInstance();
    }

    @Override
    public void removeTimeSeries(List<LabelValue> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues should not be null.");
      Utils.checkListElementNotNull(labelValues, "labelValues element should not be null.");
    }

    @Override
    public void clear() {}

    /** No-op implementations of Point class. */
    private static final class NoopPoint extends Point {
      private static final NoopPoint INSTANCE = new NoopPoint();

      private NoopPoint() {}

      /**
       * Returns a {@code NoopPoint}.
       *
       * @return a {@code NoopPoint}.
       */
      static NoopPoint getInstance() {
        return INSTANCE;
      }

      @Override
      public void inc() {}

      @Override
      public void inc(double amt) {}

      @Override
      public void dec() {}

      @Override
      public void dec(double amt) {}

      @Override
      public void set(double val) {}
    }
  }
}
