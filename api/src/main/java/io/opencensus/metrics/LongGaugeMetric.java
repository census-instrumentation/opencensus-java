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
import java.util.List;
import javax.annotation.Nullable;
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
 *   LongGaugeMetric jobsInQueue = metricRegistry.addLongGaugeMetric(
 *       "Queue_Size", "Number of jobs in queue", "1", new ArrayList<LabelKey>());
 *
 *   Point defaultPoint = jobsInQueue.getDefaultTimeSeries();
 *
 *   void doWork() {
 *      defaultPoint.inc();
 *      // Your code here.
 *      defaultPoint.dec();
 *   }
 * }
 * }</pre>
 *
 * <p>Example 2: You can also use labels(keys and values) to track different types of metric.
 *
 * <pre>{@code
 * class YourClass {
 *
 *   private static final MetricRegistry metricRegistry = Metrics.getMetricRegistry();
 *
 *   List<LabelKey> keys = Collections.singletonList(LabelKey.create("queue_name","desc"));
 *   LongGaugeMetric jobsInQueue = metricRegistry.addLongGaugeMetric(
 *       "Queue_Size", "Number of jobs in queue", "1", keys);
 *
 *   List<LabelValue> inboundQueue = Collections.singletonList(LabelValue.create("Inbound"));
 *   Point inboundQueuePoint = jobsInQueue.addTimeSeries(inboundQueue);
 *
 *   List<LabelValue> callbackQueue = Collections.singletonList(LabelValue.create("Callback"));
 *   Point callbackQueuePoint = jobsInQueue.addTimeSeries(callbackQueue);
 *
 *   void processInboundRequest() {
 *      inboundQueuePoint.inc();
 *      // Your code here.
 *      inboundQueuePoint.dec();
 *   }
 *
 *   void processCallbackRequest() {
 *      callbackQueuePoint.inc();
 *      // Your code here.
 *      callbackQueuePoint.dec();
 *   }
 * }
 * }</pre>
 *
 * @since 0.17
 */
@ThreadSafe
public abstract class LongGaugeMetric {

  /**
   * Adds a TimeSeries and returns a {@code Point}, which is part of the TimeSeries. This is more
   * convenient form when you can want to manually increase and decrease values as per your service
   * requirements. The number of label values must be the same to that of the label keys passed to
   * {@link MetricRegistry#addLongGaugeMetric}.
   *
   * @param labelValues the list of label values.
   * @return a {@code Point} the value of single gauge.
   * @since 0.17
   */
  public abstract Point addTimeSeries(List<LabelValue> labelValues);

  /**
   * Adds a TimeSeries, that reports the value of the object after the function. This is a self
   * sufficient gauge, slightly more common form of gauge is one that monitors some non-numeric
   * object. The last argument establishes the function that is used to determine the value of the
   * gauge when the gauge is collected. The number of label values must be the same to that of the
   * label keys passed to {@link MetricRegistry#addLongGaugeMetric}.
   *
   * @param labelValues the list of label values.
   * @param obj the state object from which the function derives a measurement.
   * @param function the function to be called.
   * @param <T> the type of the object upon which the function derives a measurement.
   * @since 0.17
   */
  public abstract <T> void addTimeSeries(
      List<LabelValue> labelValues, @Nullable T obj, ToLongFunction<T> function);

  /**
   * Returns a {@code Point} for a gauge with all labels not set, or default labels.
   *
   * @return a {@code Point} the value of default gauge.
   * @since 0.17
   */
  public abstract Point getDefaultTimeSeries();

  /**
   * Returns the no-op implementation of the {@code LongGaugeMetric}.
   *
   * @return the no-op implementation of the {@code LongGaugeMetric}.
   * @since 0.17
   */
  static LongGaugeMetric getNoopLongGaugeMetric(
      String name, String description, String unit, List<LabelKey> labelKeys) {
    return NoopLongGaugeMetric.getInstance(name, description, unit, labelKeys);
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
    public abstract void inc(long amt);

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
    public abstract void dec(long amt);

    /**
     * Sets the gauge to the given value.
     *
     * @param val to assign to the gauge.
     * @since 0.17
     */
    public abstract void set(long val);
  }

  /** No-op implementations of LongGaugeMetric class. */
  private static final class NoopLongGaugeMetric extends LongGaugeMetric {

    static NoopLongGaugeMetric getInstance(
        String name, String description, String unit, List<LabelKey> labelKeys) {
      return new NoopLongGaugeMetric(name, description, unit, labelKeys);
    }

    /** Creates a new {@code NoopLongGaugeMetric}. */
    NoopLongGaugeMetric(String name, String description, String unit, List<LabelKey> labelKeys) {
      Utils.checkNotNull(name, "name");
      Utils.checkNotNull(description, "description");
      Utils.checkNotNull(unit, "unit");
      Utils.checkNotNull(labelKeys, "labelKeys should not be null.");
      Utils.checkListElementNotNull(labelKeys, "labelKeys element should not be null.");
    }

    @Override
    public NoopPoint addTimeSeries(List<LabelValue> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues should not be null.");
      Utils.checkListElementNotNull(labelValues, "labelValues element should not be null.");
      return NoopPoint.getInstance();
    }

    @Override
    public <T> void addTimeSeries(
        List<LabelValue> labelValues, @Nullable T obj, ToLongFunction<T> function) {
      Utils.checkNotNull(labelValues, "labelValues should not be null.");
      Utils.checkListElementNotNull(labelValues, "labelValues element should not be null.");
      Utils.checkNotNull(function, "function");
    }

    @Override
    public NoopPoint getDefaultTimeSeries() {
      return NoopPoint.getInstance();
    }

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
      public void inc(long amt) {}

      @Override
      public void dec() {}

      @Override
      public void dec(long amt) {}

      @Override
      public void set(long val) {}
    }
  }
}
