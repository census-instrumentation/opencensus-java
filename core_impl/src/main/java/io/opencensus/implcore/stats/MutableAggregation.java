/*
 * Copyright 2017, OpenCensus Authors
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

package io.opencensus.implcore.stats;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import io.opencensus.common.Function;
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.BucketBoundaries;

/** Mutable version of {@link Aggregation} that supports adding values. */
abstract class MutableAggregation {

  private MutableAggregation() {
  }

  /**
   * Put a new value into the MutableAggregation.
   *
   * @param value new value to be added to population
   */
  abstract void add(double value);

  /**
   * Combine the internal values of this MutableAggregation and value of the given
   * MutableAggregation, with the given fraction. Then set the internal value of this
   * MutableAggregation to the combined value.
   * 
   * @param other the other {@code MutableAggregation}. The type of this and other {@code
   *     MutableAggregation} must match.
   * @param fraction the fraction that the value in other {@code MutableAggregation} should
   *     contribute. Must be within [0.0, 1.0].
   */
  abstract void combine(MutableAggregation other, double fraction);

  /**
   * Applies the given match function to the underlying data type.
   */
  abstract <T> T match(
      Function<? super MutableSum, T> p0,
      Function<? super MutableCount, T> p1,
      Function<? super MutableMean, T> p2,
      Function<? super MutableDistribution, T> p3);

  /** Calculate sum on aggregated {@code MeasureValue}s. */
  static final class MutableSum extends MutableAggregation {

    private double sum = 0.0;

    private MutableSum() {
    }

    /**
     * Construct a {@code MutableSum}.
     *
     * @return an empty {@code MutableSum}.
     */
    static MutableSum create() {
      return new MutableSum();
    }

    @Override
    void add(double value) {
      sum += value;
    }

    @Override
    void combine(MutableAggregation other, double fraction) {
      checkArgument(other instanceof MutableSum, "MutableSum expected.");
      this.sum += fraction * ((MutableSum) other).getSum();
    }

    /**
     * Returns the aggregated sum.
     *
     * @return the aggregated sum.
     */
    double getSum() {
      return sum;
    }

    @Override
    final <T> T match(
        Function<? super MutableSum, T> p0,
        Function<? super MutableCount, T> p1,
        Function<? super MutableMean, T> p2,
        Function<? super MutableDistribution, T> p3) {
      return p0.apply(this);
    }
  }

  /** Calculate count on aggregated {@code MeasureValue}s. */
  static final class MutableCount extends MutableAggregation {

    private long count = 0;

    private MutableCount() {
    }

    /**
     * Construct a {@code MutableCount}.
     *
     * @return an empty {@code MutableCount}.
     */
    static MutableCount create() {
      return new MutableCount();
    }

    @Override
    void add(double value) {
      count++;
    }

    @Override
    void combine(MutableAggregation other, double fraction) {
      checkArgument(other instanceof MutableCount, "MutableCount expected.");
      this.count += Math.round(fraction * ((MutableCount) other).getCount());
    }

    /**
     * Returns the aggregated count.
     *
     * @return the aggregated count.
     */
    long getCount() {
      return count;
    }

    @Override
    final <T> T match(
        Function<? super MutableSum, T> p0,
        Function<? super MutableCount, T> p1,
        Function<? super MutableMean, T> p2,
        Function<? super MutableDistribution, T> p3) {
      return p1.apply(this);
    }
  }

  /** Calculate mean on aggregated {@code MeasureValue}s. */
  static final class MutableMean extends MutableAggregation {

    private double sum = 0.0;
    private long count = 0;

    private MutableMean() {
    }

    /**
     * Construct a {@code MutableMean}.
     *
     * @return an empty {@code MutableMean}.
     */
    static MutableMean create() {
      return new MutableMean();
    }

    @Override
    void add(double value) {
      count++;
      sum += value;
    }

    @Override
    void combine(MutableAggregation other, double fraction) {
      checkArgument(other instanceof MutableMean, "MutableMean expected.");
      MutableMean mutableMean = (MutableMean) other;
      this.count += Math.round(mutableMean.count * fraction);
      this.sum += mutableMean.sum * fraction;
    }

    /**
     * Returns the aggregated mean.
     *
     * @return the aggregated mean.
     */
    double getMean() {
      return getCount() == 0 ? 0 : getSum() / getCount();
    }

    /**
     * Returns the aggregated count.
     *
     * @return the aggregated count.
     */
    long getCount() {
      return count;
    }

    /**
     * Returns the aggregated sum.
     *
     * @return the aggregated sum.
     */
    double getSum() {
      return sum;
    }

    @Override
    final <T> T match(
        Function<? super MutableSum, T> p0,
        Function<? super MutableCount, T> p1,
        Function<? super MutableMean, T> p2,
        Function<? super MutableDistribution, T> p3) {
      return p2.apply(this);
    }
  }

  /** Calculate distribution stats on aggregated {@code MeasureValue}s. */
  static final class MutableDistribution extends MutableAggregation {

    private double mean = 0.0;
    private long count = 0;
    private double sumOfSquaredDeviations = 0.0;

    // Initial "impossible" values, that will get reset as soon as first value is added.
    private double min = Double.POSITIVE_INFINITY;
    private double max = Double.NEGATIVE_INFINITY;

    private final BucketBoundaries bucketBoundaries;
    private final long[] bucketCounts;

    private MutableDistribution(BucketBoundaries bucketBoundaries) {
      this.bucketBoundaries = bucketBoundaries;
      this.bucketCounts = new long[bucketBoundaries.getBoundaries().size() + 1];
    }

    /**
     * Construct a {@code MutableDistribution}.
     *
     * @return an empty {@code MutableDistribution}.
     */
    static MutableDistribution create(BucketBoundaries bucketBoundaries) {
      checkNotNull(bucketBoundaries, "bucketBoundaries should not be null.");
      return new MutableDistribution(bucketBoundaries);
    }

    @Override
    void add(double value) {
      count++;

      /*
       * Update the sum of squared deviations from the mean with the given value. For values
       * x_i this is Sum[i=1..n]((x_i - mean)^2)
       *
       * Computed using Welfords method (see
       * https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance, or Knuth, "The Art of
       * Computer Programming", Vol. 2, page 323, 3rd edition)
       */
      double deltaFromMean = value - mean;
      mean += deltaFromMean / count;
      double deltaFromMean2 = value - mean;
      sumOfSquaredDeviations += deltaFromMean * deltaFromMean2;

      if (value < min) {
        min = value;
      }
      if (value > max) {
        max = value;
      }

      for (int i = 0; i < bucketBoundaries.getBoundaries().size(); i++) {
        if (value < bucketBoundaries.getBoundaries().get(i)) {
          bucketCounts[i]++;
          return;
        }
      }
      bucketCounts[bucketCounts.length - 1]++;
    }

    // We don't compute fractional MutableDistribution, it's either whole or none.
    @Override
    void combine(MutableAggregation other, double fraction) {
      checkArgument(other instanceof MutableDistribution, "MutableDistribution expected.");
      if (fraction != 1) {
        return;
      }

      MutableDistribution mutableDistribution = (MutableDistribution) other;
      checkArgument(this.bucketBoundaries.equals(mutableDistribution.bucketBoundaries),
          "Bucket boundaries should match.");

      double sum = this.mean * this.count;
      this.count += mutableDistribution.count;
      this.mean = (sum + mutableDistribution.count * mutableDistribution.mean) / this.count;
      this.min = mutableDistribution.min < this.min ? mutableDistribution.min : this.min;
      this.max = mutableDistribution.max > this.max ? mutableDistribution.max : this.max;
      // TODO(songya): how to calculate combined sum of squared deviations?

      long[] bucketCounts = mutableDistribution.getBucketCounts();
      for (int i = 0; i < bucketCounts.length; i++) {
        this.bucketCounts[i] += bucketCounts[i];
      }
    }

    double getMean() {
      return mean;
    }

    long getCount() {
      return count;
    }

    double getMin() {
      return min;
    }

    double getMax() {
      return max;
    }

    // Returns the aggregated sum of squared deviations.
    double getSumOfSquaredDeviations() {
      return sumOfSquaredDeviations;
    }

    long[] getBucketCounts() {
      return bucketCounts;
    }

    @Override
    final <T> T match(
        Function<? super MutableSum, T> p0,
        Function<? super MutableCount, T> p1,
        Function<? super MutableMean, T> p2,
        Function<? super MutableDistribution, T> p3) {
      return p3.apply(this);
    }
  }
}
