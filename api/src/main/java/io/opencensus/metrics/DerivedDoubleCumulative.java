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
import java.lang.ref.WeakReference;
import java.util.List;
import javax.annotation.concurrent.ThreadSafe;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

/**
 * Derived Double Cumulative metric, to report cumulative measurement of a double value. Cumulative
 * values can go up or stay the same, but can never go down. The cumulative values cannot be
 * negative.
 *
 * <p>Example: Create a Cumulative with an object and a callback function.
 *
 * <pre>{@code
 * class YourClass {
 *
 *   private static final MetricRegistry metricRegistry = Metrics.getMetricRegistry();
 *
 *   List<LabelKey> labelKeys = Arrays.asList(LabelKey.create("Name", "desc"));
 *   List<LabelValue> labelValues = Arrays.asList(LabelValue.create("Inbound"));
 *
 *   DerivedDoubleCumulative cumulative = metricRegistry.addDerivedDoubleCumulative(
 *       "processed_jobs", "Processed jobs in a queue", "1", labelKeys);
 *
 *   QueueManager queueManager = new QueueManager();
 *   cumulative.createTimeSeries(labelValues, queueManager,
 *         new ToDoubleFunction<QueueManager>() {
 *           {@literal @}Override
 *           public double applyAsDouble(QueueManager queue) {
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
 * @since 0.21
 */
@ThreadSafe
public abstract class DerivedDoubleCumulative {
  /**
   * Creates a {@code TimeSeries}. The value of a single point in the TimeSeries is observed from a
   * callback function. This function is invoked whenever metrics are collected, meaning the
   * reported value is up-to-date. It keeps a {@link WeakReference} to the object and it is the
   * user's responsibility to manage the lifetime of the object.
   *
   * @param labelValues the list of label values.
   * @param obj the state object from which the function derives a measurement.
   * @param function the function to be called.
   * @param <T> the type of the object upon which the function derives a measurement.
   * @throws NullPointerException if {@code labelValues} is null OR any element of {@code
   *     labelValues} is null OR {@code function} is null.
   * @throws IllegalArgumentException if different time series with the same labels already exists
   *     OR if number of {@code labelValues}s are not equal to the label keys.
   * @since 0.21
   */
  public abstract <T> void createTimeSeries(
      List<LabelValue> labelValues,
      /*@Nullable*/ T obj,
      ToDoubleFunction</*@Nullable*/ T> function);

  /**
   * Removes the {@code TimeSeries} from the cumulative metric, if it is present.
   *
   * @param labelValues the list of label values.
   * @throws NullPointerException if {@code labelValues} is null.
   * @since 0.21
   */
  public abstract void removeTimeSeries(List<LabelValue> labelValues);

  /**
   * Removes all {@code TimeSeries} from the cumulative metric.
   *
   * @since 0.21
   */
  public abstract void clear();

  /**
   * Returns the no-op implementation of the {@code DerivedDoubleCumulative}.
   *
   * @return the no-op implementation of the {@code DerivedDoubleCumulative}.
   * @since 0.21
   */
  static DerivedDoubleCumulative newNoopDerivedDoubleCumulative(
      String name, String description, String unit, List<LabelKey> labelKeys) {
    return NoopDerivedDoubleCumulative.create(name, description, unit, labelKeys);
  }

  /** No-op implementations of DerivedDoubleCumulative class. */
  private static final class NoopDerivedDoubleCumulative extends DerivedDoubleCumulative {
    private final int labelKeysSize;

    static NoopDerivedDoubleCumulative create(
        String name, String description, String unit, List<LabelKey> labelKeys) {
      return new NoopDerivedDoubleCumulative(name, description, unit, labelKeys);
    }

    /** Creates a new {@code NoopDerivedDoubleCumulative}. */
    NoopDerivedDoubleCumulative(
        String name, String description, String unit, List<LabelKey> labelKeys) {
      Utils.checkNotNull(name, "name");
      Utils.checkNotNull(description, "description");
      Utils.checkNotNull(unit, "unit");
      Utils.checkListElementNotNull(Utils.checkNotNull(labelKeys, "labelKeys"), "labelKey");
      labelKeysSize = labelKeys.size();
    }

    @Override
    public <T> void createTimeSeries(
        List<LabelValue> labelValues,
        /*@Nullable*/ T obj,
        ToDoubleFunction</*@Nullable*/ T> function) {
      Utils.checkListElementNotNull(Utils.checkNotNull(labelValues, "labelValues"), "labelValue");
      Utils.checkArgument(
          labelKeysSize == labelValues.size(), "Label Keys and Label Values don't have same size.");
      Utils.checkNotNull(function, "function");
    }

    @Override
    public void removeTimeSeries(List<LabelValue> labelValues) {
      Utils.checkNotNull(labelValues, "labelValues");
    }

    @Override
    public void clear() {}
  }
}
