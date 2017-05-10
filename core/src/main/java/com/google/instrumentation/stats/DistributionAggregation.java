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

import com.google.auto.value.AutoValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

// TODO(aveitch) The below class should be changed to use a Distribution as a private member.
/**
 * An aggregation of data based on distributions.
 *
 * <p>A distribution contains summary statistics for a population of values and, optionally, a
 * histogram representing the distribution of those values across a specified set of histogram
 * buckets, as defined in {@link DistributionAggregationDescriptor#getBucketBoundaries()}.
 *
 * <p>Although not forbidden, it is generally a bad idea to include non-finite values (infinities
 * or NaNs) in the population of values, as this will render the {@code mean} meaningless.
 */
@Immutable
@AutoValue
public abstract class DistributionAggregation {
  /**
   * Constructs a new {@link DistributionAggregation}.
   */
  public static DistributionAggregation create(
      long count, double mean, double sum, Range range, List<Tag> tags) {
    return createInternal(count, mean, sum, range, tags, null);
  }

  /**
   * Constructs a new {@link DistributionAggregation} with the optional {@code bucketCount}s.
   */
  public static DistributionAggregation create(
      long count, double mean, double sum, Range range, List<Tag> tags, List<Long> bucketCounts) {
    return createInternal(count, mean, sum, range, tags,
        Collections.unmodifiableList(new ArrayList<Long>(bucketCounts)));
  }

  private static DistributionAggregation createInternal(
      long count, double mean, double sum, Range range, List<Tag> tags, List<Long> bucketCounts) {
    return new AutoValue_DistributionAggregation(count, mean, sum, range, tags, bucketCounts);
  }

  /**
   * The number of values in the population. Must be non-negative.
   */
  public abstract long getCount();

  /**
   * The arithmetic mean of the values in the population. If {@link #getCount()} is zero then this
   * value must also be zero.
   */
  public abstract double getMean();

  /**
   * The sum of the values in the population.  If {@link #getCount()} is zero then this values must
   * also be zero.
   */
  public abstract double getSum();

  /**
   * The range of the population values. If {@link #getCount()} is zero then this returned range is
   * implementation-dependent.
   */
  public abstract Range getRange();

  /**
   * {@link Tag}s associated with this {@link DistributionAggregation}.
   *
   * <p>Note: The returned list is unmodifiable, attempts to update it will throw an
   * UnsupportedOperationException.
   */
  public abstract List<Tag> getTags();

  /**
   * A Distribution may optionally contain a histogram of the values in the population. The
   * histogram is given in {@link #getBucketCounts()} as counts of values that fall into one of a
   * sequence of non-overlapping buckets, described by
   * {@link DistributionAggregationDescriptor#getBucketBoundaries()}.
   * The sum of the values in {@link #getBucketCounts()} must equal the value in
   * {@link #getCount()}.
   *
   * <p>Bucket counts are given in order under the numbering scheme described
   * above (the underflow bucket has number 0; the finite buckets, if any,
   * have numbers 1 through N-2; the overflow bucket has number N-1).
   *
   * <p>The size of {@link #getBucketCounts()} must be no greater than N as defined in
   * {@link DistributionAggregationDescriptor#getBucketBoundaries()}.
   *
   * <p>Any suffix of trailing buckets containing only zero may be omitted.
   *
   * <p>{@link #getBucketCounts()} will return null iff the associated
   * {@link DistributionAggregationDescriptor#getBucketBoundaries()} returns null.
   */
  @Nullable
  public abstract List<Long> getBucketCounts();

  /**
   * Describes a range of population values.
   */
  @Immutable
  @AutoValue
  public abstract static class Range {
    /**
     * Constructs a new {@link Range}.
     */
    public static Range create(double min, double max) {
      return new AutoValue_DistributionAggregation_Range(min, max);
    }

    /**
     * The minimum of the population values.
     */
    public abstract double getMin();

    /**
     * The maximum of the population values.
     */
    public abstract double getMax();
  }
}
