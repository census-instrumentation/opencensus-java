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
import java.util.List;

public abstract class LongGaugeMetric<T> implements Gauge {

  public abstract DataPoint<T> addDataPoint(List<LabelValue> labelValues);

  public abstract DataPoint<T> addDataPoint(
      List<LabelValue> labelValues, T obj, ToLongFunction<T> function);

  public abstract DataPoint<T> getDefaultDataPoint();

  public abstract static class DataPoint<T> {

    /*
     * Increment the value by 1.
     */
    public abstract void inc();

    /*
     * Increment the value by the given amount.
     */
    public abstract void inc(long amt);

    /*
     * Decrement the value by 1.
     */
    public abstract void dec();

    /*
     * Decrement the value by the given amount.
     */
    public abstract void dec(long amt);

    /*
     * Set to the given value.
     */
    public abstract void set(long val);

    /*
     * Get the value
     */
    public abstract long get();
  }
}
