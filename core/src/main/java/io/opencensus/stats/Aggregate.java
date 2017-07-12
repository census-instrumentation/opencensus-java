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
      Function<? super AggregateSum, T> p0,
      Function<? super AggregateCount, T> p1,
      Function<? super AggregateHistogram, T> p2,
      Function<? super AggregateRange, T> p3,
      Function<? super AggregateMean, T> p4,
      Function<? super AggregateStdDev, T> p5);

  /** The sum value of aggregated {@code MeasureValue}s. */
  @Immutable
  @AutoValue
  public abstract static class AggregateSum extends Aggregate {

    AggregateSum() {
    }

    static AggregateSum create(double sum) {
      return new AutoValue_Aggregate_AggregateSum(sum);
    }

    /**
     * Returns the aggregated sum.
     *
     * @return the aggregated sum.
     */
    public abstract double get();

    @Override
    public final <T> T match(
        Function<? super AggregateSum, T> p0,
        Function<? super AggregateCount, T> p1,
        Function<? super AggregateHistogram, T> p2,
        Function<? super AggregateRange, T> p3,
        Function<? super AggregateMean, T> p4,
        Function<? super AggregateStdDev, T> p5) {
      return p0.apply(this);
    }
  }

  /** The count value of aggregated {@code MeasureValue}s. */
  @Immutable
  @AutoValue
  public abstract static class AggregateCount extends Aggregate {

    AggregateCount() {
    }

    static AggregateCount create(long count) {
      return new AutoValue_Aggregate_AggregateCount(count);
    }

    /**
     * Returns the aggregated count.
     *
     * @return the aggregated count.
     */
    public abstract long get();

    @Override
    public final <T> T match(
        Function<? super AggregateSum, T> p0,
        Function<? super AggregateCount, T> p1,
        Function<? super AggregateHistogram, T> p2,
        Function<? super AggregateRange, T> p3,
        Function<? super AggregateMean, T> p4,
        Function<? super AggregateStdDev, T> p5) {
      return p1.apply(this);
    }
  }

  /** The histogram of aggregated {@code MeasureValue}s. */
  @Immutable
  @AutoValue
  public abstract static class AggregateHistogram extends Aggregate {

    AggregateHistogram() {
    }

    static AggregateHistogram create(long[] bucketCounts) {
      List<Long> boxedBucketCounts = new ArrayList<Long>();
      for (long bucketCount : bucketCounts) {
        boxedBucketCounts.add(bucketCount);
      }
      return new AutoValue_Aggregate_AggregateHistogram(
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
        Function<? super AggregateSum, T> p0,
        Function<? super AggregateCount, T> p1,
        Function<? super AggregateHistogram, T> p2,
        Function<? super AggregateRange, T> p3,
        Function<? super AggregateMean, T> p4,
        Function<? super AggregateStdDev, T> p5) {
      return p2.apply(this);
    }
  }

  /** The range of aggregated {@code MeasureValue}s. */
  @Immutable
  @AutoValue
  public abstract static class AggregateRange extends Aggregate {

    AggregateRange() {
    }

    static AggregateRange create(Range range) {
      return new AutoValue_Aggregate_AggregateRange(range);
    }

    /**
     * Returns the aggregated {@code Range}.
     *
     * @return the aggregated {@code Range}.
     */
    public abstract Range get();

    @Override
    public final <T> T match(
        Function<? super AggregateSum, T> p0,
        Function<? super AggregateCount, T> p1,
        Function<? super AggregateHistogram, T> p2,
        Function<? super AggregateRange, T> p3,
        Function<? super AggregateMean, T> p4,
        Function<? super AggregateStdDev, T> p5) {
      return p3.apply(this);
    }
  }

  /** The mean value of aggregated {@code MeasureValue}s. */
  @Immutable
  @AutoValue
  public abstract static class AggregateMean extends Aggregate {

    AggregateMean() {
    }

    static AggregateMean create(double mean) {
      return new AutoValue_Aggregate_AggregateMean(mean);
    }

    /**
     * Returns the aggregated mean.
     *
     * @return the aggregated mean.
     */
    public abstract double get();

    @Override
    public final <T> T match(
        Function<? super AggregateSum, T> p0,
        Function<? super AggregateCount, T> p1,
        Function<? super AggregateHistogram, T> p2,
        Function<? super AggregateRange, T> p3,
        Function<? super AggregateMean, T> p4,
        Function<? super AggregateStdDev, T> p5) {
      return p4.apply(this);
    }
  }

  /** The standard deviation value of aggregated {@code MeasureValue}s. */
  @Immutable
  @AutoValue
  public abstract static class AggregateStdDev extends Aggregate {

    AggregateStdDev() {
    }

    static AggregateStdDev create(double stdDev) {
      return new AutoValue_Aggregate_AggregateStdDev(stdDev);
    }

    /**
     * Returns the aggregated sum of squared deviations.
     *
     * @return the aggregated sum of squared deviations.
     */
    public abstract double get();

    @Override
    public final <T> T match(
        Function<? super AggregateSum, T> p0,
        Function<? super AggregateCount, T> p1,
        Function<? super AggregateHistogram, T> p2,
        Function<? super AggregateRange, T> p3,
        Function<? super AggregateMean, T> p4,
        Function<? super AggregateStdDev, T> p5) {
      return p5.apply(this);
    }
  }

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
    static io.opencensus.stats.DistributionAggregate.Range create(double min, double max) {
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
