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

package io.opencensus.stats;

import static com.google.common.base.Preconditions.checkNotNull;

import io.opencensus.common.Function;

import java.util.Arrays;

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
   * Applies the given match function to the underlying data type.
   */
  abstract <T> T match(
      Function<? super MutableSum, T> p0,
      Function<? super MutableCount, T> p1,
      Function<? super MutableHistogram, T> p2,
      Function<? super MutableRange, T> p3,
      Function<? super MutableMean, T> p4,
      Function<? super MutableStdDev, T> p5);

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
        Function<? super MutableHistogram, T> p2,
        Function<? super MutableRange, T> p3,
        Function<? super MutableMean, T> p4,
        Function<? super MutableStdDev, T> p5) {
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
        Function<? super MutableHistogram, T> p2,
        Function<? super MutableRange, T> p3,
        Function<? super MutableMean, T> p4,
        Function<? super MutableStdDev, T> p5) {
      return p1.apply(this);
    }
  }

  /** Calculate histogram on aggregated {@code MeasureValue}s. */
  static final class MutableHistogram extends MutableAggregation {

    private final BucketBoundaries bucketBoundaries;
    private final long[] bucketCounts;

    private MutableHistogram(BucketBoundaries bucketBoundaries) {
      this.bucketBoundaries = bucketBoundaries;
      this.bucketCounts = new long[bucketBoundaries.getBoundaries().size() + 1];
    }

    /**
     * Construct a {@code MutableHistogram}.
     *
     * @return an empty {@code MutableHistogram}.
     */
    static MutableHistogram create(BucketBoundaries bucketBoundaries) {
      checkNotNull(bucketBoundaries, "bucketBoundaries should not be null.");
      return new MutableHistogram(bucketBoundaries);
    }

    @Override
    void add(double value) {
      for (int i = 0; i < bucketBoundaries.getBoundaries().size(); i++) {
        if (value < bucketBoundaries.getBoundaries().get(i)) {
          bucketCounts[i]++;
          return;
        }
      }
      bucketCounts[bucketCounts.length - 1]++;
    }

    /**
     * Returns the aggregated bucket count.
     *
     * @return the aggregated bucket count.
     */
    long[] getBucketCounts() {
      return Arrays.copyOf(bucketCounts, bucketCounts.length);
    }

    @Override
    final <T> T match(
        Function<? super MutableSum, T> p0,
        Function<? super MutableCount, T> p1,
        Function<? super MutableHistogram, T> p2,
        Function<? super MutableRange, T> p3,
        Function<? super MutableMean, T> p4,
        Function<? super MutableStdDev, T> p5) {
      return p2.apply(this);
    }
  }

  /** Calculate range on aggregated {@code MeasureValue}s. */
  static final class MutableRange extends MutableAggregation {

    // Initial "impossible" values, that will get reset as soon as first value is added.
    private double min = Double.POSITIVE_INFINITY;
    private double max = Double.NEGATIVE_INFINITY;

    private MutableRange() {
    }

    /**
     * Construct a {@code MutableRange}.
     *
     * @return an empty {@code MutableRange}.
     */
    static MutableRange create() {
      return new MutableRange();
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
    @Override
    void add(double value) {
      if (value < min) {
        min = value;
      }
      if (value > max) {
        max = value;
      }
    }

    @Override
    final <T> T match(
        Function<? super MutableSum, T> p0,
        Function<? super MutableCount, T> p1,
        Function<? super MutableHistogram, T> p2,
        Function<? super MutableRange, T> p3,
        Function<? super MutableMean, T> p4,
        Function<? super MutableStdDev, T> p5) {
      return p3.apply(this);
    }
  }

  /** Calculate mean on aggregated {@code MeasureValue}s. */
  static final class MutableMean extends MutableAggregation {

    private double mean = 0.0;
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
      double deltaFromMean = value - mean;
      mean += deltaFromMean / count;
    }

    /**
     * Returns the aggregated mean.
     *
     * @return the aggregated mean.
     */
    double getMean() {
      return mean;
    }

    @Override
    final <T> T match(
        Function<? super MutableSum, T> p0,
        Function<? super MutableCount, T> p1,
        Function<? super MutableHistogram, T> p2,
        Function<? super MutableRange, T> p3,
        Function<? super MutableMean, T> p4,
        Function<? super MutableStdDev, T> p5) {
      return p4.apply(this);
    }
  }

  /** Calculate standard deviation on aggregated {@code MeasureValue}s. */
  static final class MutableStdDev extends MutableAggregation {

    private double sumOfSquaredDeviations = 0.0;
    private double mean = 0.0;
    private long count = 0;

    private MutableStdDev() {
    }

    /**
     * Construct a {@code MutableStdDev}.
     *
     * @return an empty {@code MutableStdDev}.
     */
    static MutableStdDev create() {
      return new MutableStdDev();
    }

    /**
     * Update the sum of squared deviations from the mean with the given value. For values
     * x_i this is Sum[i=1..n]((x_i - mean)^2)
     *
     * <p>Computed using Welfords method (see
     * https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance, or Knuth, "The Art of
     * Computer Programming", Vol. 2, page 323, 3rd edition)
     */
    @Override
    void add(double value) {
      count++;
      double deltaFromMean = value - mean;
      mean += deltaFromMean / count;
      double deltaFromMean2 = value - mean;
      sumOfSquaredDeviations += deltaFromMean * deltaFromMean2;
    }

    /**
     * Returns the aggregated standard deviations.
     *
     * <p>If count is zero then this value will also be zero.
     *
     * @return the aggregated standard deviations.
     */
    double getStdDev() {
      return count == 0 ? 0 : Math.sqrt(sumOfSquaredDeviations / count);
    }

    @Override
    final <T> T match(
        Function<? super MutableSum, T> p0,
        Function<? super MutableCount, T> p1,
        Function<? super MutableHistogram, T> p2,
        Function<? super MutableRange, T> p3,
        Function<? super MutableMean, T> p4,
        Function<? super MutableStdDev, T> p5) {
      return p5.apply(this);
    }
  }
}
