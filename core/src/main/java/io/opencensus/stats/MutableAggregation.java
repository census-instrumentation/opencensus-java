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
      Function<? super MutableAggregationSum, T> p0,
      Function<? super MutableAggregationCount, T> p1,
      Function<? super MutableAggregationHistogram, T> p2,
      Function<? super MutableAggregationRange, T> p3,
      Function<? super MutableAggregationMean, T> p4,
      Function<? super MutableAggregationStdDev, T> p5,
      Function<? super Aggregate, T> defaultFunction);

  /** Calculate sum on aggregated {@code MeasureValue}s. */
  static final class MutableAggregationSum extends MutableAggregation {

    private double sum = 0.0;

    private MutableAggregationSum() {
    }

    /**
     * Construct a {@code MutableAggregationSum}.
     *
     * @return an empty {@code MutableAggregationSum}.
     */
    static MutableAggregationSum create() {
      return new MutableAggregationSum();
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
    double get() {
      return sum;
    }

    @Override
    final <T> T match(
        Function<? super MutableAggregationSum, T> p0,
        Function<? super MutableAggregationCount, T> p1,
        Function<? super MutableAggregationHistogram, T> p2,
        Function<? super MutableAggregationRange, T> p3,
        Function<? super MutableAggregationMean, T> p4,
        Function<? super MutableAggregationStdDev, T> p5,
        Function<? super Aggregate, T> defaultFunction) {
      return p0.apply(this);
    }
  }

  /** Calculate count on aggregated {@code MeasureValue}s. */
  static final class MutableAggregationCount extends MutableAggregation {

    private long count = 0;

    private MutableAggregationCount() {
    }

    /**
     * Construct a {@code MutableAggregationCount}.
     *
     * @return an empty {@code MutableAggregationCount}.
     */
    static MutableAggregationCount create() {
      return new MutableAggregationCount();
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
    long get() {
      return count;
    }

    @Override
    final <T> T match(
        Function<? super MutableAggregationSum, T> p0,
        Function<? super MutableAggregationCount, T> p1,
        Function<? super MutableAggregationHistogram, T> p2,
        Function<? super MutableAggregationRange, T> p3,
        Function<? super MutableAggregationMean, T> p4,
        Function<? super MutableAggregationStdDev, T> p5,
        Function<? super Aggregate, T> defaultFunction) {
      return p1.apply(this);
    }
  }

  /** Calculate histogram on aggregated {@code MeasureValue}s. */
  static final class MutableAggregationHistogram extends MutableAggregation {

    private final BucketBoundaries bucketBoundaries;
    private final long[] bucketCounts;

    private MutableAggregationHistogram(BucketBoundaries bucketBoundaries) {
      this.bucketBoundaries = bucketBoundaries;
      this.bucketCounts = new long[bucketBoundaries.getBoundaries().size() + 1];
    }

    /**
     * Construct a {@code MutableAggregationHistogram}.
     *
     * @return an empty {@code MutableAggregationHistogram}.
     */
    static MutableAggregationHistogram create(BucketBoundaries bucketBoundaries) {
      checkNotNull(bucketBoundaries, "bucketBoundaries should not be null.");
      return new MutableAggregationHistogram(bucketBoundaries);
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
    long[] get() {
      return bucketCounts;
    }

    @Override
    final <T> T match(
        Function<? super MutableAggregationSum, T> p0,
        Function<? super MutableAggregationCount, T> p1,
        Function<? super MutableAggregationHistogram, T> p2,
        Function<? super MutableAggregationRange, T> p3,
        Function<? super MutableAggregationMean, T> p4,
        Function<? super MutableAggregationStdDev, T> p5,
        Function<? super Aggregate, T> defaultFunction) {
      return p2.apply(this);
    }
  }

  /** Calculate range on aggregated {@code MeasureValue}s. */
  static final class MutableAggregationRange extends MutableAggregation {

    private final Range range;

    private MutableAggregationRange() {
      range = Range.create();
    }

    /**
     * Construct a {@code MutableAggregationRange}.
     *
     * @return an empty {@code MutableAggregationRange}.
     */
    static MutableAggregationRange create() {
      return new MutableAggregationRange();
    }

    @Override
    void add(double value) {
      range.add(value);
    }

    /**
     * Returns the aggregated {@code Range}.
     *
     * @return the aggregated {@code Range}.
     */
    Range get() {
      return range;
    }

    @Override
    final <T> T match(
        Function<? super MutableAggregationSum, T> p0,
        Function<? super MutableAggregationCount, T> p1,
        Function<? super MutableAggregationHistogram, T> p2,
        Function<? super MutableAggregationRange, T> p3,
        Function<? super MutableAggregationMean, T> p4,
        Function<? super MutableAggregationStdDev, T> p5,
        Function<? super Aggregate, T> defaultFunction) {
      return p3.apply(this);
    }
  }

  /** Calculate mean on aggregated {@code MeasureValue}s. */
  static final class MutableAggregationMean extends MutableAggregation {

    private double mean = 0.0;
    private long count = 0;

    private MutableAggregationMean() {
    }

    /**
     * Construct a {@code MutableAggregationMean}.
     *
     * @return an empty {@code MutableAggregationMean}.
     */
    static MutableAggregationMean create() {
      return new MutableAggregationMean();
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
    double get() {
      return mean;
    }

    @Override
    final <T> T match(
        Function<? super MutableAggregationSum, T> p0,
        Function<? super MutableAggregationCount, T> p1,
        Function<? super MutableAggregationHistogram, T> p2,
        Function<? super MutableAggregationRange, T> p3,
        Function<? super MutableAggregationMean, T> p4,
        Function<? super MutableAggregationStdDev, T> p5,
        Function<? super Aggregate, T> defaultFunction) {
      return p4.apply(this);
    }
  }

  /** Calculate standard deviation on aggregated {@code MeasureValue}s. */
  static final class MutableAggregationStdDev extends MutableAggregation {

    private double sumOfSquaredDeviations = 0.0;
    private double mean = 0.0;
    private long count = 0;

    private MutableAggregationStdDev() {
    }

    /**
     * Construct a {@code MutableAggregationStdDev}.
     *
     * @return an empty {@code MutableAggregationStdDev}.
     */
    static MutableAggregationStdDev create() {
      return new MutableAggregationStdDev();
    }

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
     * @return the aggregated standard deviations.
     */
    double get() {
      return count == 0 ? 0 : Math.sqrt(sumOfSquaredDeviations / count);
    }

    @Override
    final <T> T match(
        Function<? super MutableAggregationSum, T> p0,
        Function<? super MutableAggregationCount, T> p1,
        Function<? super MutableAggregationHistogram, T> p2,
        Function<? super MutableAggregationRange, T> p3,
        Function<? super MutableAggregationMean, T> p4,
        Function<? super MutableAggregationStdDev, T> p5,
        Function<? super Aggregate, T> defaultFunction) {
      return p5.apply(this);
    }
  }

  /**
   * Describes a range of population values.
   *
   * <p>Mutable version of {@code Range}.
   */
  static final class Range {

    // Initial "impossible" values, that will get reset as soon as first value is added.
    private double min = Double.POSITIVE_INFINITY;
    private double max = Double.NEGATIVE_INFINITY;

    /**
     * Construct a new, empty {@code Range}.
     */
    static final Range create() {
      return new Range();
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
    void add(double value) {
      if (value < min) {
        min = value;
      }
      if (value > max) {
        max = value;
      }
    }

    private Range() {
    }
  }
}
