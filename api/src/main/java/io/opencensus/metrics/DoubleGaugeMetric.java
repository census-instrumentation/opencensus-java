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
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Double Gauge metric, to report instantaneous measurement of a double value. Gauges can go both up
 * and down. The gauges values can be negative. For example, Free memory or Total memory. See {@link
 * io.opencensus.metrics.MetricRegistry} for an example of its use.
 *
 * <p>Example 1: Create a Gauge without a labels
 *
 * <pre>{@code
 * class YourClass {
 *
 *   private static final MetricRegistry metricRegistry = Metrics.getMetricRegistry();
 *   DoubleGaugeMetric totalMemory = metricRegistry.addDoubleGaugeMetric(
 *       "Total_memory", "Total CPU Memory", "1", new ArrayList<LabelKey>());
 *
 *   Point defaultPoint = totalMemory.getDefaultPoint();
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
 *   DoubleGaugeMetric totalMemory = metricRegistry.addDoubleGaugeMetric(
 *       "Total_Memory", "Total CPU Memory", "1", keys);
 *
 *   List<LabelValue> usedMemory = Collections.singletonList(LabelValue.create("Used"));
 *   Point point = totalMemory.addPoint(usedMemory);
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
public abstract class DoubleGaugeMetric {

  /**
   * Adds and returns new Point. This is more convenient form when you can want to manually increase
   * and decrease values as per your service requirements. The number of label values must be the
   * same to that of the label keys passed to {@link MetricRegistry#addDoubleGaugeMetric}.
   *
   * @param labelValues the list of label values.
   * @return Point the value of single gauge
   * @since 0.17
   */
  public abstract Point addPoint(List<LabelValue> labelValues);

  /**
   * Adds and returns new Point that reports the value of the object after the function. This is ,
   * slightly more common form of gauge is one that monitors some non-numeric object. The last
   * argument establishes the function that is used to determine the value of the gauge when the
   * gauge is collected. The number of label values must be the same to that of the label keys
   * passed to {@link MetricRegistry#addDoubleGaugeMetric}.
   *
   * @param labelValues the list of label values.
   * @param obj the state object from which the function derives a measurement.
   * @param function the function to be called.
   * @param <T> the type of the object upon which the function derives a measurement.
   * @since 0.17
   */
  public abstract <T> void addPoint(
      List<LabelValue> labelValues, @Nullable T obj, ToDoubleFunction<T> function);

  /**
   * Returns a Point for a gauge without labels.
   *
   * @return Point the value of default gauge
   * @since 0.17
   */
  public abstract Point getDefaultPoint();

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
     * @param amt Amount to add to the gauge.
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
     * @param amt Amount to subtract from the gauge.
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
}
