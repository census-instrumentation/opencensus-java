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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;

/**
 * A distribution contains summary statistics for a population of values and, optionally, a
 * histogram representing the distribution of those values across a specified set of histogram
 * buckets.
 *
 * <p>The bucket boundaries for that histogram are described by {@link getBucketBoundaries}, which
 * defines {@code getBucketBoundaries.size() + 1 (= N)} buckets. The boundaries for bucket index i
 * are:
 *
 * <ul>
 *   <li>[-infinity, bounds[i]) for i == 0
 *   <li>[bounds[i-1], bounds[i]) for 0 &lt; i &lt; N-2
 *   <li>[bounds[i-1], +infinity) for i == N-1
 * </ul>
 *
 * <p>i.e. an underflow bucket (number 0), zero or more finite buckets (1 through N - 2, and an
 * overflow bucket (N - 1), with inclusive lower bounds and exclusive upper bounds.
 *
 * <p>Note: If N = 1, there are no finite buckets and the single bucket is both the overflow and
 * underflow bucket.
 *
 * <p>Although not forbidden, it is generally a bad idea to include non-finite values (infinities or
 * NaNs) in the population of values, as this will render the {@code mean} meaningless.
 */
public final class Distribution {
  private long count = 0; // The number of values in the population.
  private double sum = 0.0; // The sum of the values in the population.
  private Range range = new Range(); // Range of values in the population.
  private final ArrayList<Double> bucketBoundaries; // Histogram boundaries; null means no histogram
  private Long[] bucketCounts; // Counts for each histogram bucket

  /**
   * Constructs a new, empty {@link Distribution}.
   *
   * @return a new, empty {@code Distribution}.
   */
  public static final Distribution create() {
    return new Distribution(null);
  }

  /**
   * Constructs a new {@link Distribution} with specified {@code bucketBoundaries}.
   *
   * @param bucketBoundaries the boundaries for the buckets in the underlying {@code Distribution}.
   * @return a new, empty {@code Distribution} with the specified boundaries.
   * @throws NullPointerException if {@code bucketBoundaries} is null.
   * @throws IllegalArgumentException if {@code bucketBoundaries} are not sorted.
   */
  public static final Distribution create(List<Double> bucketBoundaries) {
    checkNotNull(bucketBoundaries, "bucketBoundaries");
    double lower = bucketBoundaries.get(0);
    for (int i = 1; i < bucketBoundaries.size(); i++) {
      double next = bucketBoundaries.get(i);
      checkArgument(lower < next, "Bucket boundaries not sorted.");
      lower = next;
    }
    return new Distribution(bucketBoundaries);
  }

  // Returns true if the distribution has histogram buckets.
  private boolean hasBuckets() {
    return (bucketBoundaries != null);
  }

  // Construct a new Distribution with given boundaries.
  private Distribution(List<Double> bucketBoundaries) {
    this.bucketBoundaries =
        (bucketBoundaries == null) ? null : new ArrayList<Double>(bucketBoundaries.size());
    if (hasBuckets()) {
      for (Double value : bucketBoundaries) {
        this.bucketBoundaries.add(value);
      }
      bucketCounts = new Long[bucketBoundaries.size() + 1];
      for (int i = 0; i < bucketCounts.length; i++) {
        bucketCounts[i] = 0L;
      }
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
   * implementation-dependent. @ return The {code Range} representing the range of population
   * boundaries.
   */
  public Range getRange() {
    return range;
  }

  /**
   * A Distribution may optionally contain a histogram of the values in the population. The
   * histogram is given in {@link #getBucketCounts()} as counts of values that fall into one of a
   * sequence of non-overlapping buckets, described by {@link
   * DistributionAggregationDescriptor#getBucketBoundaries()}. The sum of the values in {@link
   * #getBucketCounts()} must equal the value in {@link #getCount()}.
   *
   * <p>Bucket counts are given in order under the numbering scheme described above (the underflow
   * bucket has number 0; the finite buckets, if any, have numbers 1 through N-2; the overflow
   * bucket has number N-1).
   *
   * <p>The size of {@link #getBucketCounts()} must be no greater than N as defined in {@link
   * DistributionAggregationDescriptor#getBucketBoundaries()}.
   *
   * <p>Any suffix of trailing buckets containing only zero may be omitted.
   *
   * <p>{@link #getBucketCounts()} will return null iff the associated {@link
   * DistributionAggregationDescriptor#getBucketBoundaries()} returns null.
   *
   * @return The count of population boundaries in each histogram bucket.
   */
  @Nullable
  public List<Long> getBucketCounts() {
    if (hasBuckets()) {
      return Arrays.asList(bucketCounts);
    }
    return null;
  }

  // Increment the appropriate bucket count. hasBuckets() MUST be true.
  private void putIntoBucket(double value) {
    for (int i = 0; i < bucketBoundaries.size(); i++) {
      if (value < bucketBoundaries.get(i)) {
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
  public void put(double value) {
    count++;
    sum += value;
    range.put(value);
    if (hasBuckets()) {
      putIntoBucket(value);
    }
  }

  /** Describes a range of population values. */
  public static final class Range {
    private double min = Double.POSITIVE_INFINITY;
    private double max = Double.NEGATIVE_INFINITY;

    /** Construct a new, empty {@code Range}. */
    public Range() {}

    /**
     * The minimum of the population values.
     *
     * @return The minimum of the population values.
     */
    public double getMin() {
      return min;
    }

    /**
     * The maximum of the population values.
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
    public void put(double value) {
      if (value < min) {
        min = value;
      }
      if (value > max) {
        max = value;
      }
    }
  }
}
