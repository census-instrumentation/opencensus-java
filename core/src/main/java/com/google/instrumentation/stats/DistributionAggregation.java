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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

// TODO(songya) remove all the Deprecated variables and methods.
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
public final class DistributionAggregation {
  /**
   * Constructs a new {@link DistributionAggregation}.
   */
  public static final DistributionAggregation create(
      Distribution distribution, List<Tag> tags) {
    return new DistributionAggregation(distribution, tags, null);
  }

  /**
   * Constructs a new {@link DistributionAggregation} with the optional {@code bucketCount}s.
   */
  public static final DistributionAggregation create(
      Distribution distribution, List<Tag> tags, List<Long> bucketCounts) {
    return new DistributionAggregation(
        distribution, tags, Collections.unmodifiableList(new ArrayList<Long>(bucketCounts)));
  }

  /**
   * Constructs a new {@link DistributionAggregation}.
   */
  @Deprecated
  public static final DistributionAggregation create(
      long count, double mean, double sum, Range range, List<Tag> tags) {
    return new DistributionAggregation(count, mean, sum, range, tags, null);
  }

  /**
   * Constructs a new {@link DistributionAggregation} with the optional {@code bucketCount}s.
   */
  @Deprecated
  public static final DistributionAggregation create(
      long count, double mean, double sum, Range range, List<Tag> tags, List<Long> bucketCounts) {
    return new DistributionAggregation(count, mean, sum, range, tags,
        Collections.unmodifiableList(new ArrayList<Long>(bucketCounts)));
  }

  /**
   * {@link Tag}s associated with this {@link DistributionAggregation}.
   *
   * <p>Note: The returned list is unmodifiable, attempts to update it will throw an
   * UnsupportedOperationException.
   */
  public final List<Tag> getTags() {
    return tags;
  }

  /**
   * The number of values in the population. Must be non-negative.
   */
  public long getCountFromDistribution() {
    return distribution.getCount();
  }

  /**
   * The arithmetic mean of the values in the population. If {@link #getCount()} is zero then this
   * value must also be zero.
   */
  public double getMeanFromDistribution() {
    return distribution.getMean();
  }

  /**
   * The sum of the values in the population.  If {@link #getCount()} is zero then this values must
   * also be zero.
   */
  public double getSumFromDistribution() {
    return distribution.getSum();
  }

  /**
   * The range of the population values. If {@link #getCount()} is zero then this returned range is
   * implementation-dependent.
   */
  public Distribution.Range getRangeFromDistribution() {
    return distribution.getRange();
  }

  /**
   * The number of values in the population. Must be non-negative.
   */
  @Deprecated
  public long getCount() {
    return count;
  }

  /**
   * The arithmetic mean of the values in the population. If {@link #getCount()} is zero then this
   * value must also be zero.
   */
  @Deprecated
  public double getMean() {
    return mean;
  }

  /**
   * The sum of the values in the population.  If {@link #getCount()} is zero then this values must
   * also be zero.
   */
  @Deprecated
  public double getSum() {
    return sum;
  }

  /**
   * The range of the population values. If {@link #getCount()} is zero then this returned range is
   * implementation-dependent.
   */
  @Deprecated
  public Range getRange() {
    return range;
  }

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
  public List<Long> getBucketCounts() {
    return bucketCounts;
  }

  @Deprecated private final long count;
  @Deprecated private final double mean;
  @Deprecated private final double sum;
  @Deprecated private final Range range;

  private final Distribution distribution;
  private final List<Tag> tags;
  private final List<Long> bucketCounts;

  private DistributionAggregation(
      Distribution distribution, List<Tag> tags,
      @Nullable List<Long> bucketCounts) {
    this.count = 0;
    this.mean = 0;
    this.sum = 0;
    this.range = Range.create(0, 0);
    this.distribution = distribution;
    this.tags = tags;
    this.bucketCounts = bucketCounts;
  }

  @Deprecated
  private DistributionAggregation(
      long count, double mean, double sum, Range range, List<Tag> tags,
      @Nullable List<Long> bucketCounts) {
    this.count = count;
    this.mean = mean;
    this.sum = sum;
    this.range = range;
    this.distribution = Distribution.create(MutableDistribution.create());
    this.tags = tags;
    this.bucketCounts = bucketCounts;
  }

  /**
   * Describes a range of population values.
   */
  @Deprecated
  public static final class Range {
    /**
     * Constructs a new {@link Range}.
     */
    @Deprecated
    public static final Range create(double min, double max) {
      return new Range(min, max);
    }

    /**
     * The minimum of the population values.
     */
    @Deprecated
    public double getMin() {
      return min;
    }

    /**
     * The maximum of the population values.
     */
    @Deprecated
    public double getMax() {
      return max;
    }

    private double min;
    private double max;

    private Range(double min, double max) {
      this.min = min;
      this.max = max;
    }
  }
}
