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

/**
 * {@link Aggregation} is the process of combining a certain set of {@code MeasureValue}s
 * for a given {@code Measure} into an {@code Aggregate}.
 *
 * <p>{@link Aggregation} currently supports 6 types of basic aggregation:
 * <ul>
 *   <li>SUM
 *   <li>COUNT
 *   <li>HISTOGRAM
 *   <li>RANGE
 *   <li>MEAN
 *   <li>STDDEV (standard deviation)
 * </ul>
 *
 * <p>When creating a {@link View}, one or more {@link Aggregation} needs to be specified as how to
 * aggregate {@code MeasureValue}s.
 */
public abstract class Aggregation {

  private Aggregation() {
  }

  /**
   * Put a new value into the Aggregation.
   *
   * @param value new value to be added to population
   */
  public abstract void add(double value);

  /**
   * Applies the given match function to the underlying data type.
   */
  public abstract <T> T match(
      Function<? super AggregationSum, T> p0,
      Function<? super AggregationCount, T> p1,
      Function<? super AggregationHistogram, T> p2,
      Function<? super AggregationRange, T> p3,
      Function<? super AggregationMean, T> p4,
      Function<? super AggregationStdDev, T> p5);

  /** Calculate sum on aggregated {@code MeasureValue}s. */
  public static final class AggregationSum extends Aggregation {

    private double sum = 0.0;

    private AggregationSum() {
    }

    /**
     * Construct a {@code AggregationSum}.
     *
     * @return an empty {@code AggregationSum}.
     */
    public static AggregationSum create() {
      return new AggregationSum();
    }

    @Override
    public void add(double value) {
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
    public <T> T match(
        Function<? super AggregationSum, T> p0,
        Function<? super AggregationCount, T> p1,
        Function<? super AggregationHistogram, T> p2,
        Function<? super AggregationRange, T> p3,
        Function<? super AggregationMean, T> p4,
        Function<? super AggregationStdDev, T> p5) {
      return p0.apply(this);
    }
  }

  /** Calculate count on aggregated {@code MeasureValue}s. */
  public static final class AggregationCount extends Aggregation {

    private long count = 0;

    private AggregationCount() {
    }

    /**
     * Construct a {@code AggregationCount}.
     *
     * @return an empty {@code AggregationCount}.
     */
    public static AggregationCount create() {
      return new AggregationCount();
    }

    @Override
    public void add(double value) {
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
    public <T> T match(
        Function<? super AggregationSum, T> p0,
        Function<? super AggregationCount, T> p1,
        Function<? super AggregationHistogram, T> p2,
        Function<? super AggregationRange, T> p3,
        Function<? super AggregationMean, T> p4,
        Function<? super AggregationStdDev, T> p5) {
      return p1.apply(this);
    }
  }

  /** Calculate histogram on aggregated {@code MeasureValue}s. */
  public static final class AggregationHistogram extends Aggregation {

    private final BucketBoundaries bucketBoundaries;
    private final long[] bucketCounts;

    private AggregationHistogram(BucketBoundaries bucketBoundaries) {
      this.bucketBoundaries = bucketBoundaries;
      this.bucketCounts = new long[bucketBoundaries.getBoundaries().size() + 1];
    }

    public static AggregationHistogram create(BucketBoundaries bucketBoundaries) {
      checkNotNull(bucketBoundaries, "bucketBoundaries should not be null.");
      return new AggregationHistogram(bucketBoundaries);
    }

    @Override
    public void add(double value) {
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
    public <T> T match(
        Function<? super AggregationSum, T> p0,
        Function<? super AggregationCount, T> p1,
        Function<? super AggregationHistogram, T> p2,
        Function<? super AggregationRange, T> p3,
        Function<? super AggregationMean, T> p4,
        Function<? super AggregationStdDev, T> p5) {
      return p2.apply(this);
    }
  }

  /** Calculate range on aggregated {@code MeasureValue}s. */
  public static final class AggregationRange extends Aggregation {

    private final Range range;

    private AggregationRange() {
      range = Range.create();
    }

    /**
     * Construct a {@code AggregationRange}.
     *
     * @return an empty {@code AggregationRange}.
     */
    public static AggregationRange create() {
      return new AggregationRange();
    }

    @Override
    public void add(double value) {
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
    public <T> T match(
        Function<? super AggregationSum, T> p0,
        Function<? super AggregationCount, T> p1,
        Function<? super AggregationHistogram, T> p2,
        Function<? super AggregationRange, T> p3,
        Function<? super AggregationMean, T> p4,
        Function<? super AggregationStdDev, T> p5) {
      return p3.apply(this);
    }
  }

  /** Calculate mean on aggregated {@code MeasureValue}s. */
  public static final class AggregationMean extends Aggregation {

    private double mean = 0.0;
    private long count = 0;

    private AggregationMean() {
    }

    /**
     * Construct a {@code AggregationMean}.
     *
     * @return an empty {@code AggregationMean}.
     */
    public static AggregationMean create() {
      return new AggregationMean();
    }

    @Override
    public void add(double value) {
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
    public <T> T match(
        Function<? super AggregationSum, T> p0,
        Function<? super AggregationCount, T> p1,
        Function<? super AggregationHistogram, T> p2,
        Function<? super AggregationRange, T> p3,
        Function<? super AggregationMean, T> p4,
        Function<? super AggregationStdDev, T> p5) {
      return p4.apply(this);
    }
  }

  /** Calculate standard deviation on aggregated {@code MeasureValue}s. */
  public static final class AggregationStdDev extends Aggregation {

    private double sumOfSquaredDeviations = 0.0;
    private double mean = 0.0;
    private long count = 0;

    private AggregationStdDev() {
    }

    /**
     * Construct a {@code AggregationStdDev}.
     *
     * @return an empty {@code AggregationStdDev}.
     */
    public static AggregationStdDev create() {
      return new AggregationStdDev();
    }

    @Override
    public void add(double value) {
      count++;
      double deltaFromMean = value - mean;
      mean += deltaFromMean / count;
      double deltaFromMean2 = value - mean;
      sumOfSquaredDeviations += deltaFromMean * deltaFromMean2;
    }

    /**
     * Returns the aggregated sum of squared deviations.
     *
     * @return the aggregated sum of squared deviations.
     */
    double get() {
      return sumOfSquaredDeviations;
    }

    @Override
    public <T> T match(
        Function<? super AggregationSum, T> p0,
        Function<? super AggregationCount, T> p1,
        Function<? super AggregationHistogram, T> p2,
        Function<? super AggregationRange, T> p3,
        Function<? super AggregationMean, T> p4,
        Function<? super AggregationStdDev, T> p5) {
      return p5.apply(this);
    }
  }

  /**
   * Describes a range of population values.
   *
   * <p>Mutable version of {@link Aggregate.Range}.
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
