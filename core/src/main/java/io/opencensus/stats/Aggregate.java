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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.auto.value.AutoValue;
import io.opencensus.common.Function;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * {@link Aggregate} is the result of applying a given {@link Aggregation} to a set of
 * {@code MeasureValue}s.
 *
 * <p>{@link Aggregate} currently supports 6 types of basic aggregation:
 * <ul>
 *   <li>SUM
 *   <li>COUNT
 *   <li>HISTOGRAM
 *   <li>RANGE
 *   <li>MEAN
 *   <li>STDDEV (standard deviation)
 * </ul>
 *
 * <p>{@link ViewData} will contain one or more {@link Aggregate}s, corresponding to its
 * {@link Aggregation} definition in {@link View}.
 */
@Immutable
public abstract class Aggregate {

  private Aggregate() {
  }

  /**
   * Applies the given match function to the underlying data type.
   */
  public abstract <T> T match(
      Function<? super Sum, T> p0,
      Function<? super Count, T> p1,
      Function<? super Histogram, T> p2,
      Function<? super Range, T> p3,
      Function<? super Mean, T> p4,
      Function<? super StdDev, T> p5,
      Function<? super Aggregate, T> defaultFunction);

  /** The sum value of aggregated {@code MeasureValue}s. */
  @Immutable
  @AutoValue
  public abstract static class Sum extends Aggregate {

    Sum() {
    }

    static Sum create(double sum) {
      return new AutoValue_Aggregate_Sum(sum);
    }

    /**
     * Returns the aggregated sum.
     *
     * @return the aggregated sum.
     */
    public abstract double get();

    @Override
    public final <T> T match(
        Function<? super Sum, T> p0,
        Function<? super Count, T> p1,
        Function<? super Histogram, T> p2,
        Function<? super Range, T> p3,
        Function<? super Mean, T> p4,
        Function<? super StdDev, T> p5,
        Function<? super Aggregate, T> defaultFunction) {
      return p0.apply(this);
    }
  }

  /** The count value of aggregated {@code MeasureValue}s. */
  @Immutable
  @AutoValue
  public abstract static class Count extends Aggregate {

    Count() {
    }

    static Count create(long count) {
      return new AutoValue_Aggregate_Count(count);
    }

    /**
     * Returns the aggregated count.
     *
     * @return the aggregated count.
     */
    public abstract long get();

    @Override
    public final <T> T match(
        Function<? super Sum, T> p0,
        Function<? super Count, T> p1,
        Function<? super Histogram, T> p2,
        Function<? super Range, T> p3,
        Function<? super Mean, T> p4,
        Function<? super StdDev, T> p5,
        Function<? super Aggregate, T> defaultFunction) {
      return p1.apply(this);
    }
  }

  /** The histogram of aggregated {@code MeasureValue}s. */
  @Immutable
  @AutoValue
  public abstract static class Histogram extends Aggregate {

    Histogram() {
    }

    static Histogram create(long... bucketCounts) {
      checkNotNull(bucketCounts, "bucket counts should not be null.");
      List<Long> boxedBucketCounts = new ArrayList<Long>();
      for (long bucketCount : bucketCounts) {
        boxedBucketCounts.add(bucketCount);
      }
      return new AutoValue_Aggregate_Histogram(
          Collections.unmodifiableList(boxedBucketCounts));
    }

    /**
     * Returns the aggregated bucket counts. The returned list is immutable, trying to update it
     * will throw an {@code UnsupportedOperationException}.
     *
     * @return the aggregated bucket counts.
     */
    public abstract List<Long> get();

    @Override
    public final <T> T match(
        Function<? super Sum, T> p0,
        Function<? super Count, T> p1,
        Function<? super Histogram, T> p2,
        Function<? super Range, T> p3,
        Function<? super Mean, T> p4,
        Function<? super StdDev, T> p5,
        Function<? super Aggregate, T> defaultFunction) {
      return p2.apply(this);
    }
  }

  /** The range of aggregated {@code MeasureValue}s. */
  @Immutable
  @AutoValue
  public abstract static class Range extends Aggregate {

    Range() {
    }

    static Range create(double min, double max) {
      if (min != Double.POSITIVE_INFINITY && max != Double.NEGATIVE_INFINITY) {
        checkArgument(min <= max, "max should be greater or equal to min.");
      }
      return new AutoValue_Aggregate_Range(min, max);
    }

    /**
     * Returns the minimum of the population values.
     *
     * @return the minimum of the population values.
     */
    // TODO(songya): not v0.1, expose this method later.
    public abstract double getMin();

    /**
     * Returns the maximum of the population values.
     *
     * @return the maximum of the population values.
     */
    // TODO(songya): not v0.1, expose this method later.
    public abstract double getMax();

    @Override
    public final <T> T match(
        Function<? super Sum, T> p0,
        Function<? super Count, T> p1,
        Function<? super Histogram, T> p2,
        Function<? super Range, T> p3,
        Function<? super Mean, T> p4,
        Function<? super StdDev, T> p5,
        Function<? super Aggregate, T> defaultFunction) {
      return p3.apply(this);
    }
  }

  /** The mean value of aggregated {@code MeasureValue}s. */
  @Immutable
  @AutoValue
  public abstract static class Mean extends Aggregate {

    Mean() {
    }

    static Mean create(double mean) {
      return new AutoValue_Aggregate_Mean(mean);
    }

    /**
     * Returns the aggregated mean.
     *
     * @return the aggregated mean.
     */
    // TODO(songya): not v0.1, expose this method later.
    abstract double get();

    @Override
    public final <T> T match(
        Function<? super Sum, T> p0,
        Function<? super Count, T> p1,
        Function<? super Histogram, T> p2,
        Function<? super Range, T> p3,
        Function<? super Mean, T> p4,
        Function<? super StdDev, T> p5,
        Function<? super Aggregate, T> defaultFunction) {
      return p4.apply(this);
    }
  }

  /** The standard deviation value of aggregated {@code MeasureValue}s. */
  @Immutable
  @AutoValue
  public abstract static class StdDev extends Aggregate {

    StdDev() {
    }

    static StdDev create(double stdDev) {
      return new AutoValue_Aggregate_StdDev(stdDev);
    }

    /**
     * Returns the aggregated standard deviation.
     *
     * @return the aggregated standard deviation.
     */
    // TODO(songya): not v0.1, expose this method later.
    abstract double get();

    @Override
    public final <T> T match(
        Function<? super Sum, T> p0,
        Function<? super Count, T> p1,
        Function<? super Histogram, T> p2,
        Function<? super Range, T> p3,
        Function<? super Mean, T> p4,
        Function<? super StdDev, T> p5,
        Function<? super Aggregate, T> defaultFunction) {
      return p5.apply(this);
    }
  }
}
