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

import com.google.auto.value.AutoValue;
import java.util.ArrayList;
import java.util.Collections;
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
@AutoValue
public abstract class Distribution {

  Distribution() {}

  /**
   * Constructs a {@link Distribution} with the same contents as a {@link MutableDistribution}.
   *
   * @param distribution the {@code MutableDistribution} to be copied.
   * @return a {@code Distribution} with the same contents as the given {@link MutableDistribution}.
   */
  public static Distribution create(MutableDistribution distribution) {
    long[] counts = distribution.getBucketCountsArray();
    List<Long> bucketCounts = counts != null ? longArrayToImmutableList(counts) : null;
    return new AutoValue_Distribution(
        distribution.getCount(),
        distribution.getSum(),
        Range.create(distribution.getRange()),
        distribution.getBucketBoundaries(),
        bucketCounts);
  }

  private static List<Long> longArrayToImmutableList(long[] array) {
    List<Long> boxedBucketCounts = new ArrayList<Long>(array.length);
    for (long bucketCount : array) {
      boxedBucketCounts.add(bucketCount);
    }
    return Collections.unmodifiableList(boxedBucketCounts);
  }

  /**
   * The number of values in the population. Must be non-negative.
   *
   * @return The number of values in the population.
   */
  public abstract long getCount();

  /**
   * The arithmetic mean of the values in the population. If {@link #getCount()} is zero then this
   * value will be NaN.
   *
   * @return The arithmetic mean of all values in the population.
   */
  public final double getMean() {
    return getSum() / getCount();
  }

  /**
   * The sum of the values in the population. If {@link #getCount()} is zero then this value must
   * also be zero.
   *
   * @return The sum of values in the population.
   */
  public abstract double getSum();

  /**
   * The range of the population values. If {@link #getCount()} is zero then this returned range is
   * implementation-dependent.
   *
   * @return The {code Range} representing the range of population values.
   */
  public abstract Range getRange();

  /**
   * The optional histogram bucket boundaries used by this {@code MutableDistribution}.
   *
   * @return The bucket boundaries, or {@code null} if there is no histogram.
   */
  @Nullable
  public abstract BucketBoundaries getBucketBoundaries();

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
  public abstract List<Long> getBucketCounts();

  /** Describes a range of population values. */
  // TODO(sebright): Decide what to do when the distribution contains no values.
  @AutoValue
  public abstract static class Range {

    /** Constructs a {@code Range} from a {@link MutableDistribution.Range}. */
    public static Range create(MutableDistribution.Range range) {
      return new AutoValue_Distribution_Range(range.getMin(), range.getMax());
    }

    /**
     * The minimum of the population values.
     *
     * @return The minimum of the population values.
     */
    public abstract double getMin();

    /**
     * The maximum of the population values.
     *
     * @return The maximum of the population values.
     */
    public abstract double getMax();
  }
}
