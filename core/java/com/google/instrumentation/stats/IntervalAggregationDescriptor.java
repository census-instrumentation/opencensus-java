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
   * Constructs a new {@link IntervalAggregationDescriptor}. The given {@code intervalSizes} must
   * have at least one entry.
   */
  public static IntervalAggregationDescriptor create(List<Duration> intervalSizes) {
    if (intervalSizes.isEmpty()) {
      throw new IllegalArgumentException("There must be at least one interval size.");
    }
    return new IntervalAggregationDescriptor(
        Collections.unmodifiableList(new ArrayList<Duration>(intervalSizes)));
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

  private final List<Duration> intervalSizes;

  private IntervalAggregationDescriptor(List<Duration> intervalSizes) {
    this.intervalSizes = intervalSizes;
  }
}
