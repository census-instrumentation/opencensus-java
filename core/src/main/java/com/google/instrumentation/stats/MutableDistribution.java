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
import java.util.List;
import javax.annotation.Nullable;

/**
 * A distribution contains summary statistics for a population of values and, optionally, a
 * histogram representing the distribution of those values across a specified set of histogram
 * buckets.
 *
 * <p>The bucket boundaries for that histogram are described by {@link BucketBoundaries}, which
 * defines {@code BucketBoundaries.getBoundaries().size() + 1 (= N)} buckets.
 * The boundaries for bucket index i are:
 *
 * <ul>
 *   <li>[-infinity, bounds[i]) for i == 0
 *   <li>[bounds[i-1], bounds[i]) for 0 &lt; i &lt; N-1
 *   <li>[bounds[i-1], +infinity) for i == N-1
 * </ul>
 *
 * <p>i.e. an underflow bucket (number 0), zero or more finite buckets (1 through N - 2, and an
 * overflow bucket (N - 1), with inclusive lower bounds and exclusive upper bounds.
 *
 * <p>Note: If N = 1, there are no finite buckets and the single bucket is both the overflow and
 * underflow bucket.
 * TODO(songya): needs to determine whether N = 1 is valid. (Currently we don't allow N = 1.)
 *
 * <p>Although not forbidden, it is generally a bad idea to include non-finite values (infinities or
 * NaNs) in the population of values, as this will render the {@code mean} meaningless.
 */
public final class MutableDistribution {
  private long count = 0; // The number of values in the population.
  private double sum = 0.0; // The sum of the values in the population.
  private Range range = Range.create(); // Range of values in the population.
  private final BucketBoundaries bucketBoundaries; // Histogram boundaries; null means no histogram
  private long[] bucketCounts; // Counts for each histogram bucket

  /**
   * Constructs a new, empty {@link MutableDistribution}.
   *
   * @return a new, empty {@code Distribution}.
   */
  public static final MutableDistribution create() {
    return new MutableDistribution(null);
  }

  /**
   * Constructs a new {@link MutableDistribution} with specified {@code bucketBoundaryList}.
   *
   * @param bucketBoundaries the boundaries for the buckets in the underlying {@code Distribution}.
   * @return a new, empty {@code Distribution} with the specified boundaries.
   */
  public static final MutableDistribution create(BucketBoundaries bucketBoundaries) {
    checkNotNull(bucketBoundaries, "bucketBoundaries Object should not be null.");
    return new MutableDistribution(bucketBoundaries);
  }

  // Returns true if the distribution has histogram buckets.
  private boolean hasBuckets() {
    return (bucketBoundaries != null);
  }

  // Construct a new Distribution with optional bucket boundaries.
  private MutableDistribution(@Nullable BucketBoundaries bucketBoundaries) {
    this.bucketBoundaries = bucketBoundaries;
    if (hasBuckets()) {
      bucketCounts = new long[bucketBoundaries.getBoundaries().size() + 1];
    }
  }

  /**
   * The number of values in the population. Must be non-negative.
   *
   * @return The number of values in the population.
   */
  public long getCount() {
    return count;
  }

  /**
   * The arithmetic mean of the values in the population. If {@link #getCount()} is zero then this
   * value will be NaN.
   *
   * @return The arithmetic mean of all values in the population.
   */
  public double getMean() {
    return sum / count;
  }

  /**
   * The sum of the values in the population. If {@link #getCount()} is zero then this value must
   * also be zero.
   *
   * @return The sum of values in the population.
   */
  public double getSum() {
    return sum;
  }

  /**
   * The range of the population values. If {@link #getCount()} is zero then this returned range is
   * implementation-dependent.
   *
   * @return The {code Range} representing the range of population values.
   */
  public Range getRange() {
    return range;
  }

  /**
   * A Distribution may optionally contain a histogram of the values in the population. The
   * histogram is given in {@link #getBucketCounts()} as counts of values that fall into one of a
   * sequence of non-overlapping buckets, described by {@link BucketBoundaries}.
   * The sum of the values in {@link #getBucketCounts()} must equal the value in
   * {@link #getCount()}.
   *
   * <p>Bucket counts are given in order under the numbering scheme described above (the underflow
   * bucket has number 0; the finite buckets, if any, have numbers 1 through N-2; the overflow
   * bucket has number N-1).
   *
   * <p>The size of {@link #getBucketCounts()} must be no greater than N as defined in {@link
   * BucketBoundaries}.
   *
   * <p>Any suffix of trailing buckets containing only zero may be omitted.
   *
   * <p>{@link #getBucketCounts()} will return null iff the associated {@link BucketBoundaries}
   * is null.
   *
   * @return The count of population values in each histogram bucket.
   */
  @Nullable
  public List<Long> getBucketCounts() {
    if (hasBuckets()) {
      List<Long> boxedBucketCounts = new ArrayList<Long>(bucketCounts.length);
      for (long bucketCount : bucketCounts) {
        boxedBucketCounts.add(bucketCount);
      }
      return boxedBucketCounts;
    }
    return null;
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
   * Put a new value into the Distribution.
   *
   * @param value new value to be added to population
   */
  public void add(double value) {
    count++;
    sum += value;
    range.add(value);
    if (hasBuckets()) {
      putIntoBucket(value);
    }
  }

  /** Describes a range of population values. */
  public static final class Range {
    // Maintain the invariant that max should always be greater than or equal to min.
    // TODO(songya): needs to determine how we would want to initialize min and max.
    private Double min = null;
    private Double max = null;

    /** Construct a new, empty {@code Range}. */
    public static final Range create() {
      return new Range();
    }

    /**
     * The minimum of the population values. Will throw an exception if min has not been set.
     *
     * @return The minimum of the population values.
     */
    public double getMin() {
      return min;
    }

    /**
     * The maximum of the population values. Will throw an exception if max has not been set.
     *
     * @return The maximum of the population values.
     */
    public double getMax() {
      return max;
    }

    /**
     * Put a new value into the Range. Sets min and max values if appropriate.
     *
     * @param value the new value
     */
    public void add(double value) {
      if (min == null || value < min) {
        min = value;
      }
      if (max == null || value > max) {
        max = value;
      }
    }

    private Range() {}
  }
}
