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

package io.opencensus.stats;

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
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
 * <p>Although not forbidden, it is generally a bad idea to include non-finite values (infinities or
 * NaNs) in the population of values, as this will render the {@code mean} meaningless.
 */
@Immutable
@AutoValue
public abstract class DistributionAggregate {
  /**
   * Constructs a {@code DistributionAggregate} without bucket counts.
   *
   * @param count the number of values in the population. It must be non-negative.
   * @param mean the arithmetic mean of the values. If {@code count} is zero then this value must
   *     also be zero.
   * @param sum the sum of the values. If {@code count} is zero then this value must also be zero.
   * @param range the range of the values.
   * @param tags the {@code Tag}s associated with the {@code DistributionAggregate}.
   * @return a {@code DistributionAggregate} without bucket counts.
   */
  public static DistributionAggregate create(
      long count, double mean, double sum, Range range, List<Tag> tags) {
    return createInternal(count, mean, sum, range, tags, null);
  }

  /**
   * Constructs a {@code DistributionAggregate} with bucket counts.
   *
   * @param count the number of values in the population. It must be non-negative.
   * @param mean the arithmetic mean of the values. If {@code count} is zero then this value must
   *     also be zero.
   * @param sum the sum of the values. If {@code count} is zero then this value must also be zero.
   * @param range the range of the values.
   * @param tags the {@code Tag}s associated with the {@code DistributionAggregate}.
   * @param bucketCounts the bucket counts for the histogram associated with the {@code
   *     DistributionAggregate}.
   * @return a {@code DistributionAggregate} with bucket counts.
   */
  public static DistributionAggregate create(
      long count, double mean, double sum, Range range, List<Tag> tags, List<Long> bucketCounts) {
    return createInternal(
        count,
        mean,
        sum,
        range,
        tags,
        Collections.unmodifiableList(new ArrayList<Long>(bucketCounts)));
  }

  private static DistributionAggregate createInternal(
      long count, double mean, double sum, Range range, List<Tag> tags, List<Long> bucketCounts) {
    Preconditions.checkArgument(count >= 0, "Count must be non-negative.");
    if (count == 0) {
      Preconditions.checkArgument(mean == 0, "Mean must be 0 when the count is 0.");
      Preconditions.checkArgument(sum == 0, "Sum must be 0 when the count is 0.");
    }
    return new AutoValue_DistributionAggregate(count, mean, sum, range, tags, bucketCounts);
  }

  /**
   * Returns the number of values in the population.
   *
   * @return the number of values in the population.
   */
  public abstract long getCount();

  /**
   * Returns the arithmetic mean of the values in the population.
   *
   * @return the arithmetic mean of the values in the population.
   */
  public abstract double getMean();

  /**
   * Returns the sum of the values in the population.
   *
   * @return the sum of the values in the population.
   */
  public abstract double getSum();

  /**
   * Returns the range of the population values. If {@link #getCount()} is zero then the returned
   * range is implementation-dependent.
   *
   * @return the range of the population values.
   */
  public abstract Range getRange();

  /**
   * Returns the {@code Tag}s associated with this {@code DistributionAggregate}.
   *
   * @return the {@code Tag}s associated with this {@code DistributionAggregate}.
   */
  public abstract List<Tag> getTags();

  /**
   * Returns the bucket counts, or {@code null} if this {@code DistributionAggregate}'s associated
   * {@link DistributionAggregationDescriptor} has no buckets.
   *
   * <p>A Distribution may contain a histogram of the values in the population. The histogram is
   * given in {@link #getBucketCounts()} as counts of values that fall into one of a sequence of
   * non-overlapping buckets, described by {@link
   * DistributionAggregationDescriptor#getBucketBoundaries()}. The sum of the values in {@link
   * #getBucketCounts()} must equal the value in {@link #getCount()}.
   *
   * <p>Bucket counts are given in order under the numbering scheme described in the link above (the
   * underflow bucket has number 0; the finite buckets, if any, have numbers 1 through N-2; the
   * overflow bucket has number N-1).
   *
   * <p>The size of {@link #getBucketCounts()} must be no greater than N as defined in {@link
   * DistributionAggregationDescriptor#getBucketBoundaries()}.
   *
   * <p>Any suffix of trailing buckets containing only zero may be omitted.
   *
   * <p>{@link #getBucketCounts()} will return null iff the associated {@link
   * DistributionAggregationDescriptor#getBucketBoundaries()} returns null.
   *
   * @return the bucket counts, or {@code null} if this {@code DistributionAggregate}'s associated
   *     {@link DistributionAggregationDescriptor} has no buckets.
   */
  @Nullable
  public abstract List<Long> getBucketCounts();

  /** The range of a population's values. */
  @Immutable
  @AutoValue
  public abstract static class Range {
    /**
     * Returns a {@code Range} with the given bounds.
     *
     * @param min the minimum of the population values.
     * @param max the maximum of the population values.
     * @return a {@code Range} with the given bounds.
     */
    public static Range create(double min, double max) {
      return new AutoValue_DistributionAggregate_Range(min, max);
    }

    /**
     * Returns the minimum of the population values.
     *
     * @return the minimum of the population values.
     */
    public abstract double getMin();

    /**
     * Returns the maximum of the population values.
     *
     * @return the maximum of the population values.
     */
    public abstract double getMax();
  }
}
