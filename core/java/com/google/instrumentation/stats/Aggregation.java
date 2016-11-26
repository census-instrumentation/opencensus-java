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
import com.google.instrumentation.common.Function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

/**
 * An {@link Aggregation} is a series of individual measurements.
 *
 * <p> Each measurments can be one of either:
 * <ul>
 * <li> {@link DistributionAggregation}
 * <li> {@link IntervalAggregation}
 * </ul>
 */
public abstract class Aggregation {
  /**
   * {@link Tag}s associated with this {@link Aggregation}.
   *
   * <p>Note: The returned list is unmodifiable, attempts to update it will throw an
   * UnsupportedOperationException.
   */
  public final List<Tag> getTags() {
    return tags;
  }

  /**
   * Applies the given match function to the underlying data type.
   */
  public abstract <T> T match(
      Function<DistributionAggregation, T> p0,
      Function<IntervalAggregation, T> p1);

  private List<Tag> tags;

  // Seals the tagged union.
  private Aggregation(List<Tag> tags) {
    this.tags = Collections.unmodifiableList(new ArrayList<Tag>(tags));
  }

  /**
   * An {@link Aggregation} based on distributions.
   *
   * <p> A distribution contains summary statistics for a population of values and, optionally, a
   * histogram representing the distribution of those values across a specified set of histogram
   * buckets, as defined in {@link DistributionAggregationDescriptor#getBucketBoundaries()}.
   *
   * <p>Although not forbidden, it is generally a bad idea to include non-finite values (infinities
   * or NaNs) in the population of values, as this will render the {@code mean} meaningless.
   */
  public static final class DistributionAggregation extends Aggregation {
    /**
     * Constructs a new {@link DistributionAggregation} with the optional {@link Range}.
     */
    public static final DistributionAggregation create(
        List<Tag> tags, long count, double mean, double sum, Range range, List<Long> bucketCounts) {
      return new DistributionAggregation(tags, count, mean, sum, range, bucketCounts);
    }

    /**
     * Constructs a new {@link DistributionAggregation} without the optional {@link Range}.
     */
    public static final DistributionAggregation create(
        List<Tag> tags, long count, double mean, double sum, List<Long> bucketCounts) {
      return new DistributionAggregation(tags, count, mean, sum, null, bucketCounts);
    }

    /**
     * The number of values in the population. Must be non-negative.
     */
    public long getCount() {
      return count;
    }

    /**
     * The arithmetic mean of the values in the population. If {@link getCount()} is zero then this
     * value must also be zero.
     */
    public double getMean() {
      return mean;
    }

    /**
     * The sum of the values in the population.  If {@link getCount()} is zero then this values must
     * also be zero.
     */
    public double getSum() {
      return sum;
    }

    /**
     * The range of the population values. If {@link getCount()} is zero then this method will
     * return null.
     */
    @Nullable
    public Range getRange() {
      return range;
    }

    /**
     * A Distribution may optionally contain a histogram of the values in the population. The
     * histogram is given in {@link getBucketCounts()} as counts of values that fall into one of a
     * sequence of non-overlapping buckets, described by
     * {@link DistributionAggregationDescriptor#getBucketBoundaries()}.
     * The sum of the values in {@link getBucketCounts()} must equal the value in
     * {@link getCount()}.
     *
     * <p>Bucket counts are given in order under the numbering scheme described
     * above (the underflow bucket has number 0; the finite buckets, if any,
     * have numbers 1 through N-2; the overflow bucket has number N-1).
     *
     * <p>The size of {@link getBucketCounts()} must be no greater than N as defined in
     * {@link DistributionAggregationDescriptor#getBucketBoundaries()}.
     *
     * <p>Any suffix of trailing buckets containing only zero may be omitted.
     */
    public List<Long> getBucketCounts() {
      return bucketCounts;
    }

    @Override
    public <T> T match(
        Function<DistributionAggregation, T> p0,
        Function<IntervalAggregation, T> p1) {
      return p0.apply(this);
    }

    private final long count;
    private final double mean;
    private final double sum;
    private final Range range;
    private final List<Long> bucketCounts;

    private DistributionAggregation(
        List<Tag> tags, long count, double mean, double sum, @Nullable Range range,
        List<Long> bucketCounts) {
      super(tags);
      this.count = count;
      this.mean = mean;
      this.sum = sum;
      this.range = range;
      this.bucketCounts = Collections.unmodifiableList(new ArrayList<Long>(bucketCounts));
    }

    /**
     * Describes a range of population values.
     */
    public static final class Range {
      /**
       * Constructs a new {@link Range}.
       */
      public static final Range create(double min, double max) {
        return new Range(min, max);
      }

      /**
       * The minimum of the population values.
       */
      public double getMin() {
        return min;
      }

      /**
       * The maximum of the population values.
       */
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

  /**
   * Contains summary stats over various time intervals.
   */
  public static final class IntervalAggregation extends Aggregation {
    /**
     * Constructs new {@link IntervalAggregation}.
     */
    public static final IntervalAggregation create(List<Tag> tags, List<Interval> intervals) {
      return new IntervalAggregation(tags, intervals);
    }

    /**
     * Sequence of intervals for this aggregation.
     */
    public List<Interval> getIntervals() {
      return intervals;
    }

    @Override
    public <T> T match(
        Function<DistributionAggregation, T> p0,
        Function<IntervalAggregation, T> p1) {
      return p1.apply(this);
    }

    private final List<Interval> intervals;

    private IntervalAggregation(List<Tag> tags, List<Interval> intervals) {
      super(tags);
      this.intervals = Collections.unmodifiableList(new ArrayList<Interval>(intervals));
    }

    /**
     * Summary statistic over a single time interval.
     */
    public static final class Interval {
      /**
       * Constructs a new {@link Interval}.
       *
       * <p>Note: {@code intervalSize} must be positive otherwise behavior is unspecified.
       */
      public static Interval create(Duration intervalSize, long count, double mean) {
        return new Interval(intervalSize, count, mean);
      }

      /**
       * The interval duration.
       */
      public Duration getIntervalSize() {
        return intervalSize;
      }

      /**
       * The number of measurements in this interval.
       */
      public long getCount() {
        return count;
      }

      /**
       * The arithmetic mean of all measurements in the interval.
       */
      public double getMean() {
        return mean;
      }

      private final Duration intervalSize;
      private final long count;
      private final double mean;

      private Interval(Duration intervalSize, long count, double mean) {
        this.intervalSize = intervalSize;
        this.count = count;
        this.mean = mean;
      }
    }
  }
}
