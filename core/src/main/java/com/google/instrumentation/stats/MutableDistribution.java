/*
 * Copyright 2017, Google Inc.
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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;

/** Mutable version of {@link Distribution}. */
final class MutableDistribution {
  private long count = 0; // The number of values in the population.
  private double sum = 0.0; // The sum of the values in the population.
  private double sumOfSquares = 0.0; // The sum of the sqaures of the values in the population
  private Range range = Range.create(); // Range of values in the population.

  @Nullable
  private final BucketBoundaries bucketBoundaries; // Histogram boundaries; null means no histogram

  @Nullable private final long[] bucketCounts; // Counts for each histogram bucket

  /**
   * Constructs a new, empty {@link MutableDistribution}.
   *
   * @return a new, empty {@code MutableDistribution}.
   */
  static final MutableDistribution create() {
    return new MutableDistribution(null);
  }

  /**
   * Constructs a new {@link MutableDistribution} with specified {@code bucketBoundaryList}.
   *
   * @param bucketBoundaries the boundaries for the buckets in the underlying {@code
   *     MutableDistribution}.
   * @return a new, empty {@code MutableDistribution} with the specified boundaries.
   */
  static final MutableDistribution create(BucketBoundaries bucketBoundaries) {
    checkNotNull(bucketBoundaries, "bucketBoundaries Object should not be null.");
    return new MutableDistribution(bucketBoundaries);
  }

  // Returns true if the distribution has histogram buckets.
  // TODO(sebright): Decide whether to expose this method.
  private boolean hasBuckets() {
    return (bucketBoundaries != null);
  }

  // Construct a new MutableDistribution with optional bucket boundaries.
  private MutableDistribution(@Nullable BucketBoundaries bucketBoundaries) {
    this.bucketBoundaries = bucketBoundaries;
    bucketCounts =
        bucketBoundaries == null ? null : new long[bucketBoundaries.getBoundaries().size() + 1];
  }

  /**
   * Returns the number of values in the population. Must be non-negative.
   *
   * @return The number of values in the population.
   */
  long getCount() {
    return count;
  }

  /**
   * The arithmetic mean of the values in the population. If {@link #getCount()} is zero then this
   * value will be NaN.
   *
   * @return The arithmetic mean of all values in the population.
   */
  double getMean() {
    return sum / count;
  }

  /**
   * The sum of the values in the population. If {@link #getCount()} is zero then this value will
   * also be zero.
   *
   * @return The sum of values in the population.
   */
  double getSum() {
    return sum;
  }

  /**
   * The range of the population values. If {@link #getCount()} is zero then this returned range is
   * implementation-dependent.
   *
   * @return The {code Range} representing the range of population values.
   */
  Range getRange() {
    return range;
  }

  /**
   * The optional histogram bucket boundaries used by this {@code MutableDistribution}.
   *
   * @return The bucket boundaries, or {@code null} if there is no histogram.
   */
  @Nullable
  BucketBoundaries getBucketBoundaries() {
    return bucketBoundaries;
  }

  /**
   * The bucket counts of the optional histogram.
   *
   * @see Distribution#getBucketCounts()
   * @return The count of population values in each histogram bucket, or {@code null} if there is no
   *     histogram.
   */
  @Nullable
  List<Long> getBucketCounts() {
    if (hasBuckets()) {
      List<Long> boxedBucketCounts = new ArrayList<Long>(bucketCounts.length);
      for (long bucketCount : bucketCounts) {
        boxedBucketCounts.add(bucketCount);
      }
      return boxedBucketCounts;
    }
    return null;
  }

  @Nullable
  long[] getInternalBucketCountsArray() {
    return hasBuckets() ? Arrays.copyOf(bucketCounts, bucketCounts.length) : null;
  }

  // Increment the appropriate bucket count. hasBuckets() MUST be true.
  private void putIntoBucket(double value) {
    for (int i = 0; i < bucketBoundaries.getBoundaries().size(); i++) {
      if (value < bucketBoundaries.getBoundaries().get(i)) {
        bucketCounts[i]++;
        return;
      }
    }
    bucketCounts[bucketCounts.length - 1]++;
  }

  /**
   * Put a new value into the distribution.
   *
   * @param value new value to be added to population
   */
  void add(double value) {
    count++;
    sum += value;
    sumOfSquares += value * value;
    range.add(value);
    if (hasBuckets()) {
      putIntoBucket(value);
    }
  }

  /** Mutable version of {@code Distribution.Range}. */
  static final class Range {
    // Initial "impossible" values, that will get reset as soon as first value is added.
    private double min = Double.POSITIVE_INFINITY;
    private double max = Double.NEGATIVE_INFINITY;

    /** Construct a new, empty {@code Range}. */
    static final Range create() {
      return new Range();
    }

    /**
     * The minimum of the population values.
     *
     * @return The minimum of the population values.
     */
    double getMin() {
      return min;
    }

    /**
     * The maximum of the population values.
     *
     * @return The maximum of the population values.
     */
    double getMax() {
      return max;
    }

    /**
     * Put a new value into the Range. Sets min and max values if appropriate.
     *
     * @param value the new value
     */
    void add(double value) {
      if (value < min) {
        min = value;
      }
      if (value > max) {
        max = value;
      }
    }

    private Range() {}
  }
}
