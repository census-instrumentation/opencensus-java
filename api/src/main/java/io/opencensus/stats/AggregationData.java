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
 * {@link AggregationData} is the result of applying a given {@link Aggregation} to a set of {@code
 * MeasureValue}s.
 *
 * <p>{@link AggregationData} currently supports 4 types of basic aggregation values:
 *
 * <ul>
 *   <li>SumDataDouble
 *   <li>SumDataLong
 *   <li>CountData
 *   <li>DistributionData
 * </ul>
 *
 * <p>{@link ViewData} will contain one {@link AggregationData}, corresponding to its {@link
 * Aggregation} definition in {@link View}.
 *
 * @since 0.8
 */
@Immutable
public abstract class AggregationData {

  private AggregationData() {}

  /**
   * Applies the given match function to the underlying data type.
   *
   * @since 0.8
   * @deprecated in favor of {@link #match(Function, Function, Function, Function, Function)}.
   */
  @Deprecated
  public abstract <T> T match(
      Function<? super SumDataDouble, T> p0,
      Function<? super SumDataLong, T> p1,
      Function<? super CountData, T> p2,
      Function<? super MeanData, T> p3,
      Function<? super DistributionData, T> p4,
      Function<? super AggregationData, T> defaultFunction);

  /**
   * Applies the given match function to the underlying data type.
   *
   * @since 0.13
   */
  @SuppressWarnings("InconsistentOverloads")
  public abstract <T> T match(
      Function<? super SumDataDouble, T> p0,
      Function<? super SumDataLong, T> p1,
      Function<? super CountData, T> p2,
      Function<? super DistributionData, T> p3,
      Function<? super AggregationData, T> defaultFunction);

  /**
   * The sum value of aggregated {@code MeasureValueDouble}s.
   *
   * @since 0.8
   */
  @Immutable
  @AutoValue
  public abstract static class SumDataDouble extends AggregationData {

    SumDataDouble() {}

    /**
     * Creates a {@code SumDataDouble}.
     *
     * @param sum the aggregated sum.
     * @return a {@code SumDataDouble}.
     * @since 0.8
     */
    public static SumDataDouble create(double sum) {
      return new AutoValue_AggregationData_SumDataDouble(sum);
    }

    /**
     * Returns the aggregated sum.
     *
     * @return the aggregated sum.
     * @since 0.8
     */
    public abstract double getSum();

    @Override
    public final <T> T match(
        Function<? super SumDataDouble, T> p0,
        Function<? super SumDataLong, T> p1,
        Function<? super CountData, T> p2,
        Function<? super MeanData, T> p3,
        Function<? super DistributionData, T> p4,
        Function<? super AggregationData, T> defaultFunction) {
      return p0.apply(this);
    }

    @Override
    public final <T> T match(
        Function<? super SumDataDouble, T> p0,
        Function<? super SumDataLong, T> p1,
        Function<? super CountData, T> p2,
        Function<? super DistributionData, T> p3,
        Function<? super AggregationData, T> defaultFunction) {
      return p0.apply(this);
    }
  }

  /**
   * The sum value of aggregated {@code MeasureValueLong}s.
   *
   * @since 0.8
   */
  @Immutable
  @AutoValue
  public abstract static class SumDataLong extends AggregationData {

    SumDataLong() {}

    /**
     * Creates a {@code SumDataLong}.
     *
     * @param sum the aggregated sum.
     * @return a {@code SumDataLong}.
     * @since 0.8
     */
    public static SumDataLong create(long sum) {
      return new AutoValue_AggregationData_SumDataLong(sum);
    }

    /**
     * Returns the aggregated sum.
     *
     * @return the aggregated sum.
     * @since 0.8
     */
    public abstract long getSum();

    @Override
    public final <T> T match(
        Function<? super SumDataDouble, T> p0,
        Function<? super SumDataLong, T> p1,
        Function<? super CountData, T> p2,
        Function<? super MeanData, T> p3,
        Function<? super DistributionData, T> p4,
        Function<? super AggregationData, T> defaultFunction) {
      return p1.apply(this);
    }

    @Override
    public final <T> T match(
        Function<? super SumDataDouble, T> p0,
        Function<? super SumDataLong, T> p1,
        Function<? super CountData, T> p2,
        Function<? super DistributionData, T> p3,
        Function<? super AggregationData, T> defaultFunction) {
      return p1.apply(this);
    }
  }

  /**
   * The count value of aggregated {@code MeasureValue}s.
   *
   * @since 0.8
   */
  @Immutable
  @AutoValue
  public abstract static class CountData extends AggregationData {

    CountData() {}

    /**
     * Creates a {@code CountData}.
     *
     * @param count the aggregated count.
     * @return a {@code CountData}.
     * @since 0.8
     */
    public static CountData create(long count) {
      return new AutoValue_AggregationData_CountData(count);
    }

    /**
     * Returns the aggregated count.
     *
     * @return the aggregated count.
     * @since 0.8
     */
    public abstract long getCount();

    @Override
    public final <T> T match(
        Function<? super SumDataDouble, T> p0,
        Function<? super SumDataLong, T> p1,
        Function<? super CountData, T> p2,
        Function<? super MeanData, T> p3,
        Function<? super DistributionData, T> p4,
        Function<? super AggregationData, T> defaultFunction) {
      return p2.apply(this);
    }

    @Override
    public final <T> T match(
        Function<? super SumDataDouble, T> p0,
        Function<? super SumDataLong, T> p1,
        Function<? super CountData, T> p2,
        Function<? super DistributionData, T> p3,
        Function<? super AggregationData, T> defaultFunction) {
      return p2.apply(this);
    }
  }

  /**
   * The mean value of aggregated {@code MeasureValue}s.
   *
   * @since 0.8
   * @deprecated since 0.13, use {@link DistributionData} instead.
   */
  @Immutable
  @AutoValue
  @Deprecated
  @AutoValue.CopyAnnotations
  public abstract static class MeanData extends AggregationData {

    MeanData() {}

    /**
     * Creates a {@code MeanData}.
     *
     * @param mean the aggregated mean.
     * @param count the aggregated count.
     * @return a {@code MeanData}.
     * @since 0.8
     */
    public static MeanData create(double mean, long count) {
      return new AutoValue_AggregationData_MeanData(mean, count);
    }

    /**
     * Returns the aggregated mean.
     *
     * @return the aggregated mean.
     * @since 0.8
     */
    public abstract double getMean();

    /**
     * Returns the aggregated count.
     *
     * @return the aggregated count.
     * @since 0.8
     */
    public abstract long getCount();

    @Override
    public final <T> T match(
        Function<? super SumDataDouble, T> p0,
        Function<? super SumDataLong, T> p1,
        Function<? super CountData, T> p2,
        Function<? super MeanData, T> p3,
        Function<? super DistributionData, T> p4,
        Function<? super AggregationData, T> defaultFunction) {
      return p3.apply(this);
    }

    @Override
    public final <T> T match(
        Function<? super SumDataDouble, T> p0,
        Function<? super SumDataLong, T> p1,
        Function<? super CountData, T> p2,
        Function<? super DistributionData, T> p3,
        Function<? super AggregationData, T> defaultFunction) {
      return defaultFunction.apply(this);
    }
  }

  /**
   * The distribution stats of aggregated {@code MeasureValue}s. Distribution stats include mean,
   * count, histogram, min, max and sum of squared deviations.
   *
   * @since 0.8
   */
  @Immutable
  @AutoValue
  public abstract static class DistributionData extends AggregationData {

    DistributionData() {}

    /**
     * Creates a {@code DistributionData}.
     *
     * @param mean mean value.
     * @param count count value.
     * @param min min value.
     * @param max max value.
     * @param sumOfSquaredDeviations sum of squared deviations.
     * @param bucketCounts histogram bucket counts.
     * @return a {@code DistributionData}.
     * @since 0.8
     */
    public static DistributionData create(
        double mean,
        long count,
        double min,
        double max,
        double sumOfSquaredDeviations,
        List<Long> bucketCounts) {
      if (min != Double.POSITIVE_INFINITY || max != Double.NEGATIVE_INFINITY) {
        checkArgument(min <= max, "max should be greater or equal to min.");
      }

      checkNotNull(bucketCounts, "bucket counts should not be null.");
      List<Long> bucketCountsCopy = Collections.unmodifiableList(new ArrayList<Long>(bucketCounts));
      for (Long bucket : bucketCountsCopy) {
        checkNotNull(bucket, "bucket should not be null.");
      }

      return new AutoValue_AggregationData_DistributionData(
          mean, count, min, max, sumOfSquaredDeviations, bucketCountsCopy);
    }

    /**
     * Returns the aggregated mean.
     *
     * @return the aggregated mean.
     * @since 0.8
     */
    public abstract double getMean();

    /**
     * Returns the aggregated count.
     *
     * @return the aggregated count.
     * @since 0.8
     */
    public abstract long getCount();

    /**
     * Returns the minimum of the population values.
     *
     * @return the minimum of the population values.
     * @since 0.8
     */
    public abstract double getMin();

    /**
     * Returns the maximum of the population values.
     *
     * @return the maximum of the population values.
     * @since 0.8
     */
    public abstract double getMax();

    /**
     * Returns the aggregated sum of squared deviations.
     *
     * @return the aggregated sum of squared deviations.
     * @since 0.8
     */
    public abstract double getSumOfSquaredDeviations();

    /**
     * Returns the aggregated bucket counts. The returned list is immutable, trying to update it
     * will throw an {@code UnsupportedOperationException}.
     *
     * @return the aggregated bucket counts.
     * @since 0.8
     */
    public abstract List<Long> getBucketCounts();

    @Override
    public final <T> T match(
        Function<? super SumDataDouble, T> p0,
        Function<? super SumDataLong, T> p1,
        Function<? super CountData, T> p2,
        Function<? super MeanData, T> p3,
        Function<? super DistributionData, T> p4,
        Function<? super AggregationData, T> defaultFunction) {
      return p4.apply(this);
    }

    @Override
    public final <T> T match(
        Function<? super SumDataDouble, T> p0,
        Function<? super SumDataLong, T> p1,
        Function<? super CountData, T> p2,
        Function<? super DistributionData, T> p3,
        Function<? super AggregationData, T> defaultFunction) {
      return p3.apply(this);
    }
  }
}
