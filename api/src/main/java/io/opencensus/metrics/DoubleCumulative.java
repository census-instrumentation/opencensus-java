/*
 * Copyright 2019, OpenCensus Authors
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
 * Double Cumulative metric, to report instantaneous measurement of a double value. Cumulative
 * values can go up or stay the same, but can never go down. Cumulative values cannot be negative.
 *
 * <p>Example 1: Create a Cumulative with default labels.
 *
 * <pre>{@code
 * class YourClass {
 *
 *   private static final MetricRegistry metricRegistry = Metrics.getMetricRegistry();
 *
 *   List<LabelKey> labelKeys = Arrays.asList(LabelKey.create("Name", "desc"));
 *
 *   DoubleCumulative cumulative = metricRegistry.addDoubleCumulative("processed_jobs",
 *                       "Processed jobs", "1", labelKeys);
 *
 *   // It is recommended to keep a reference of a point for manual operations.
 *   DoublePoint defaultPoint = cumulative.getDefaultTimeSeries();
 *
 *   void doWork() {
 *      // Your code here.
 *      defaultPoint.add(10);
 *   }
 *
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
 *   List<LabelKey> labelKeys = Arrays.asList(LabelKey.create("Name", "desc"));
 *   List<LabelValue> labelValues = Arrays.asList(LabelValue.create("Inbound"));
 *
 *   DoubleCumulative cumulative = metricRegistry.addDoubleCumulative("processed_jobs",
 *                       "Processed jobs", "1", labelKeys);
 *
 *   // It is recommended to keep a reference of a point for manual operations.
 *   DoublePoint inboundPoint = cumulative.getOrCreateTimeSeries(labelValues);
 *
 *   void doSomeWork() {
 *      // Your code here.
 *      inboundPoint.set(15);
 *   }
 *
 * }
 * }</pre>
 *
 * @since 0.21
 */
@ThreadSafe
public abstract class DoubleCumulative {

  /**
   * Creates a {@code TimeSeries} and returns a {@code DoublePoint} if the specified {@code
   * labelValues} is not already associated with this cumulative, else returns an existing {@code
   * DoublePoint}.
   *
   * <p>It is recommended to keep a reference to the DoublePoint instead of always calling this
   * method for manual operations.
   *
   * @param labelValues the list of label values. The number of label values must be the same to
   *     that of the label keys passed to {@link MetricRegistry#addDoubleCumulative}.
   * @return a {@code DoublePoint} the value of single cumulative.
   * @throws NullPointerException if {@code labelValues} is null OR any element of {@code
   *     labelValues} is null.
   * @throws IllegalArgumentException if number of {@code labelValues}s are not equal to the label
   *     keys.
   * @since 0.21
   */
  public abstract DoublePoint getOrCreateTimeSeries(List<LabelValue> labelValues);

  /**
   * Returns a {@code DoublePoint} for a cumulative with all labels not set, or default labels.
   *
   * @return a {@code DoublePoint} for a cumulative with all labels not set, or default labels.
   * @since 0.21
   */
  public abstract DoublePoint getDefaultTimeSeries();

  /**
   * Removes the {@code TimeSeries} from the cumulative metric, if it is present. i.e. references to
   * previous {@code DoublePoint} objects are invalid (not part of the metric).
   *
   * @param labelValues the list of label values.
   * @throws NullPointerException if {@code labelValues} is null or any element of {@code
   *     labelValues} is null.
   * @since 0.21
   */
  public abstract void removeTimeSeries(List<LabelValue> labelValues);

  /**
   * Removes all {@code TimeSeries} from the cumulative metric. i.e. references to all previous
   * {@code DoublePoint} objects are invalid (not part of the metric).
   *
   * @since 0.21
   */
  public abstract void clear();

  /**
   * Returns the no-op implementation of the {@code DoubleCumulative}.
   *
   * @return the no-op implementation of the {@code DoubleCumulative}.
   * @since 0.21
   */
  static DoubleCumulative newNoopDoubleCumulative(
      String name, String description, String unit, List<LabelKey> labelKeys) {
    return NoopDoubleCumulative.create(name, description, unit, labelKeys);
  }

  /**
   * The value of a single point in the Cumulative.TimeSeries.
   *
   * @since 0.21
   */
  public abstract static class DoublePoint {

    /**
     * Adds the given value to the current value. The values can be negative.
     *
     * @param amt the value to add
     * @since 0.21
     */
    public abstract void add(double amt);

    /**
     * Sets the given value.
     *
     * @param val the new value.
     * @since 0.21
     */
    public abstract void set(double val);
  }

  /** No-op implementations of DoubleCumulative class. */
  private static final class NoopDoubleCumulative extends DoubleCumulative {
    private final int labelKeysSize;

    static NoopDoubleCumulative create(
        String name, String description, String unit, List<LabelKey> labelKeys) {
      return new NoopDoubleCumulative(name, description, unit, labelKeys);
    }

    /** Creates a new {@code NoopDoublePoint}. */
    NoopDoubleCumulative(String name, String description, String unit, List<LabelKey> labelKeys) {
      Utils.checkNotNull(name, "name");
      Utils.checkNotNull(description, "description");
      Utils.checkNotNull(unit, "unit");
      Utils.checkListElementNotNull(Utils.checkNotNull(labelKeys, "labelKeys"), "labelKey");
      labelKeysSize = labelKeys.size();
    }

    @Override
    public NoopDoublePoint getOrCreateTimeSeries(List<LabelValue> labelValues) {
      Utils.checkListElementNotNull(Utils.checkNotNull(labelValues, "labelValues"), "labelValue");
      Utils.checkArgument(
          labelKeysSize == labelValues.size(), "Label Keys and Label Values don't have same size.");
      return NoopDoublePoint.INSTANCE;
    }

    @Override
    public NoopDoublePoint getDefaultTimeSeries() {
      return NoopDoublePoint.INSTANCE;
    }

    @Override
    public void removeTimeSeries(List<LabelValue> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues");
    }

    @Override
    public void clear() {}

    /** No-op implementations of DoublePoint class. */
    private static final class NoopDoublePoint extends DoublePoint {
      private static final NoopDoublePoint INSTANCE = new NoopDoublePoint();

      private NoopDoublePoint() {}

      @Override
      public void add(double amt) {}

      @Override
      public void set(double val) {}
    }
  }
}
