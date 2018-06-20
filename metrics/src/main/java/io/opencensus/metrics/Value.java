/*
 * Copyright 2018, OpenCensus Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.metrics;

import com.google.auto.value.AutoValue;
import io.opencensus.common.ExperimentalApi;
import io.opencensus.common.Function;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * The actual point value for a {@link Point}.
 *
 * <p>Currently there are three types of {@link Value}:
 *
 * <ul>
 *   <li>{@link DoubleValue}
 *   <li>{@link LongValue}
 *   <li>{@link DistributionValue}
 * </ul>
 *
 * <p>Each {@link Point} contains exactly one of the three {@link Value} types.
 *
 * @since 0.16
 */
@ExperimentalApi
@Immutable
public abstract class Value {

  Value() {}

  /**
   * Applies the given match function to the underlying data type.
   *
   * @since 0.16
   */
  public abstract <T> T match(
      Function<? super DoubleValue, T> p0,
      Function<? super LongValue, T> p1,
      Function<? super DistributionValue, T> p2,
      Function<? super Value, T> defaultFunction);

  /**
   * A 64-bit double-precision floating-point {@link Value}.
   *
   * @since 0.16
   */
  @AutoValue
  @Immutable
  public abstract static class DoubleValue extends Value {

    DoubleValue() {}

    @Override
    public final <T> T match(
        Function<? super DoubleValue, T> p0,
        Function<? super LongValue, T> p1,
        Function<? super DistributionValue, T> p2,
        Function<? super Value, T> defaultFunction) {
      return p0.apply(this);
    }

    /**
     * Creates a {@link DoubleValue}.
     *
     * @param value the value in double.
     * @return a {@code DoubleValue}.
     * @since 0.16
     */
    public static DoubleValue create(double value) {
      return new AutoValue_Value_DoubleValue(value);
    }

    /**
     * Returns the double value.
     *
     * @return the double value.
     * @since 0.16
     */
    public abstract double getValue();
  }

  /**
   * A 64-bit integer {@link Value}.
   *
   * @since 0.16
   */
  @AutoValue
  @Immutable
  public abstract static class LongValue extends Value {

    LongValue() {}

    @Override
    public final <T> T match(
        Function<? super DoubleValue, T> p0,
        Function<? super LongValue, T> p1,
        Function<? super DistributionValue, T> p2,
        Function<? super Value, T> defaultFunction) {
      return p1.apply(this);
    }

    /**
     * Creates a {@link LongValue}.
     *
     * @param value the value in long.
     * @return a {@code LongValue}.
     * @since 0.16
     */
    public static LongValue create(long value) {
      return new AutoValue_Value_LongValue(value);
    }

    /**
     * Returns the long value.
     *
     * @return the long value.
     * @since 0.16
     */
    public abstract long getValue();
  }

  /**
   * {@link DistributionValue} contains summary statistics for a population of values. It optionally
   * contains a histogram representing the distribution of those values across a set of buckets.
   *
   * @since 0.16
   */
  @AutoValue
  @Immutable
  public abstract static class DistributionValue extends Value {

    DistributionValue() {}

    @Override
    public final <T> T match(
        Function<? super DoubleValue, T> p0,
        Function<? super LongValue, T> p1,
        Function<? super DistributionValue, T> p2,
        Function<? super Value, T> defaultFunction) {
      return p2.apply(this);
    }

    /**
     * Creates a {@link DistributionValue}.
     *
     * @param mean mean of the population values.
     * @param count count of the population values.
     * @param sumOfSquaredDeviations sum of squared deviations of the population values.
     * @param range {@link Range} of the population values.
     * @param bucketBoundaries bucket boundaries of a histogram.
     * @param buckets {@link Bucket}s of a histogram.
     * @return a {@code DistributionValue}.
     * @since 0.16
     */
    public static DistributionValue create(
        double mean,
        long count,
        double sumOfSquaredDeviations,
        Range range,
        List<Double> bucketBoundaries,
        List<Bucket> buckets) {
      Utils.checkArgument(count >= 0, "count should be non-negative.");
      return new AutoValue_Value_DistributionValue(
          mean,
          count,
          sumOfSquaredDeviations,
          range,
          copyBucketBounds(bucketBoundaries),
          copyBucketCount(buckets));
    }

    private static List<Double> copyBucketBounds(List<Double> bucketBoundaries) {
      Utils.checkNotNull(bucketBoundaries, "bucketBoundaries list should not be null.");
      List<Double> bucketBoundariesCopy = new ArrayList<Double>(bucketBoundaries); // Deep copy.
      // Check if sorted.
      if (bucketBoundariesCopy.size() > 1) {
        double lower = bucketBoundariesCopy.get(0);
        for (int i = 1; i < bucketBoundariesCopy.size(); i++) {
          double next = bucketBoundariesCopy.get(i);
          Utils.checkArgument(lower < next, "bucket boundaries not sorted.");
          lower = next;
        }
      }
      return Collections.unmodifiableList(bucketBoundariesCopy);
    }

    private static List<Bucket> copyBucketCount(List<Bucket> buckets) {
      Utils.checkNotNull(buckets, "bucket list should not be null.");
      List<Bucket> bucketsCopy = new ArrayList<Bucket>(buckets);
      for (Bucket bucket : bucketsCopy) {
        Utils.checkNotNull(bucket, "bucket should not be null.");
      }
      return Collections.unmodifiableList(bucketsCopy);
    }

    /**
     * Returns the aggregated mean.
     *
     * @return the aggregated mean.
     * @since 0.16
     */
    public abstract double getMean();

    /**
     * Returns the aggregated count.
     *
     * @return the aggregated count.
     * @since 0.16
     */
    public abstract long getCount();

    /**
     * Returns the aggregated sum of squared deviations.
     *
     * <p>The sum of squared deviations from the mean of the values in the population. For values
     * x_i this is:
     *
     * <p>Sum[i=1..n]((x_i - mean)^2)
     *
     * <p>Knuth, "The Art of Computer Programming", Vol. 2, page 323, 3rd edition describes
     * Welford's method for accumulating this sum in one pass.
     *
     * <p>If count is zero then this field must be zero.
     *
     * @return the aggregated sum of squared deviations.
     * @since 0.16
     */
    public abstract double getSumOfSquaredDeviations();

    /**
     * Returns the {@link Range} of the population values.
     *
     * @return the {@code Range} of the population values.
     * @since 0.16
     */
    public abstract Range getRange();

    /**
     * Returns the bucket boundaries of this distribution.
     *
     * <p>The bucket boundaries for that histogram are described by bucket_bounds. This defines
     * size(bucket_bounds) + 1 (= N) buckets. The boundaries for bucket index i are:
     *
     * <ul>
     *   <li>{@code (-infinity, bucket_bounds[i]) for i == 0}
     *   <li>{@code [bucket_bounds[i-1], bucket_bounds[i]) for 0 < i < N-2}
     *   <li>{@code [bucket_bounds[i-1], +infinity) for i == N-1}
     * </ul>
     *
     * <p>i.e. an underflow bucket (number 0), zero or more finite buckets (1 through N - 2, and an
     * overflow bucket (N - 1), with inclusive lower bounds and exclusive upper bounds.
     *
     * <p>If bucket_bounds has no elements (zero size), then there is no histogram associated with
     * the Distribution. If bucket_bounds has only one element, there are no finite buckets, and
     * that single element is the common boundary of the overflow and underflow buckets. The values
     * must be monotonically increasing.
     *
     * <p>The returned list is immutable, an {@link UnsupportedOperationException} will be thrown
     * when trying to modify it.
     *
     * @return the bucket boundaries of this distribution.
     * @since 0.16
     */
    public abstract List<Double> getBucketBoundaries();

    /**
     * Returns the the aggregated histogram {@link Bucket}s. The returned list is immutable, trying
     * to update it will throw an {@code UnsupportedOperationException}.
     *
     * @return the the aggregated histogram buckets.
     * @since 0.16
     */
    public abstract List<Bucket> getBuckets();

    /**
     * The range of the population values.
     *
     * @since 0.16
     */
    @AutoValue
    @Immutable
    public abstract static class Range {

      Range() {}

      /**
       * Creates a {@link Range}.
       *
       * @param min the minimum of the population values.
       * @param max the maximum of the population values.
       * @return a {@code Range}.
       * @since 0.16
       */
      public static Range create(double min, double max) {
        if (min != Double.POSITIVE_INFINITY || max != Double.NEGATIVE_INFINITY) {
          Utils.checkArgument(min <= max, "max should be greater or equal to min.");
        }
        return new AutoValue_Value_DistributionValue_Range(min, max);
      }

      /**
       * Returns the minimum of the population values.
       *
       * @return the minimum of the population values.
       * @since 0.16
       */
      public abstract double getMin();

      /**
       * Returns the maximum of the population values.
       *
       * @return the maximum of the population values.
       * @since 0.16
       */
      public abstract double getMax();
    }

    /**
     * The histogram bucket of the population values.
     *
     * @since 0.16
     */
    @AutoValue
    @Immutable
    public abstract static class Bucket {

      Bucket() {}

      /**
       * Creates a {@link Bucket}.
       *
       * @param count the number of values in each bucket of the histogram.
       * @return a {@code Bucket}.
       * @since 0.16
       */
      public static Bucket create(long count) {
        Utils.checkArgument(count >= 0, "bucket count should be non-negative.");
        return new AutoValue_Value_DistributionValue_Bucket(count);
      }

      /**
       * Returns the number of values in each bucket of the histogram.
       *
       * @return the number of values in each bucket of the histogram.
       * @since 0.16
       */
      public abstract long getCount();

      // TODO(songya): add support for exemplars.
    }
  }

  // TODO(songya): Add support for Summary type.
  // This is an aggregation that produces percentiles directly.
}
