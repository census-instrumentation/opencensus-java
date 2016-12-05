/*
 * Copyright 2016, Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.instrumentation.stats;

import com.google.instrumentation.common.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Describes data aggregations based on time intervals.
 */
public final class IntervalAggregationDescriptor {
  /**
   * Constructs a new {@link IntervalAggregationDescriptor}.
   *
   * <p> The number of internal sub-intervals to use when collecting stats for each interval. The
   * max error in interval measurements will be approximately 1/getNumSubIntervals()
   * (although in practice, this will only be approached in the presence of very large and bursty
   * workload changes), and underlying memory usage will be roughly proportional to the value of
   * this field. Must be in the range [2, 20]. A value of 5 will be used if this is unspecified.
   *
   * <p> The given {@code intervalSizes} must have at least one entry.
   */
  public static IntervalAggregationDescriptor create(
      int numSubIntervals, List<Duration> intervalSizes) {
    if (numSubIntervals < 2 || numSubIntervals > 20) {
      throw new IllegalArgumentException(
          "The number of subintervals must be in the range [2, 20].");
    }
    if (intervalSizes.isEmpty()) {
      throw new IllegalArgumentException("There must be at least one interval size.");
    }
    return new IntervalAggregationDescriptor(
        numSubIntervals,
        Collections.unmodifiableList(new ArrayList<Duration>(intervalSizes)));
  }

  /**
   * Constructs a new {@link IntervalAggregationDescriptor} with the number of sub intervals set
   * to the default value of 5.
   */
  public static IntervalAggregationDescriptor create(List<Duration> intervalSizes) {
    return create(5, intervalSizes);
  }

  /**
   * The number of sub intervals.
   */

  public int getNumSubintervals() {
    return numSubIntervals;
  }

  /**
   * The time intervals to record for the aggregation.
   *
   * <p>Note: The returned list is unmodifiable, attempts to update it will throw an
   * UnsupportedOperationException.
   */
  public List<Duration> getIntervalSizes() {
    return intervalSizes;
  }

  private final int numSubIntervals;
  private final List<Duration> intervalSizes;

  private IntervalAggregationDescriptor(int numSubIntervals, List<Duration> intervalSizes) {
    this.numSubIntervals = numSubIntervals;
    this.intervalSizes = intervalSizes;
  }
}
