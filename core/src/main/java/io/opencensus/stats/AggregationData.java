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
 * {@link AggregationData} is the result of applying a given {@link Aggregation} to a set of
 * {@code MeasureValue}s.
 *
 * <p>{@link AggregationData} currently supports 6 types of basic aggregation values:
 * <ul>
 *   <li>SumData
 *   <li>CountData
 *   <li>HistogramData
 *   <li>RangeData
 *   <li>MeanData
 *   <li>StdDevData (standard deviation)
 * </ul>
 *
 * <p>{@link ViewData} will contain one or more {@link AggregationData}s, corresponding to its
 * {@link Aggregation} definition in {@link View}.
 */
@Immutable
public abstract class AggregationData {

  private AggregationData() {
  }

  /**
   * Applies the given match function to the underlying data type.
   */
  public abstract <T> T match(
      Function<? super SumData, T> p0,
      Function<? super CountData, T> p1,
      Function<? super HistogramData, T> p2,
      Function<? super RangeData, T> p3,
      Function<? super MeanData, T> p4,
      Function<? super StdDevData, T> p5,
      Function<? super AggregationData, T> defaultFunction);

  /** The sum value of aggregated {@code MeasureValue}s. */
  @Immutable
  public abstract static class SumData extends AggregationData {

    private SumData() {
    }

    @Override
    public final <T> T match(
        Function<? super SumData, T> p0,
        Function<? super CountData, T> p1,
        Function<? super HistogramData, T> p2,
        Function<? super RangeData, T> p3,
        Function<? super MeanData, T> p4,
        Function<? super StdDevData, T> p5,
        Function<? super AggregationData, T> defaultFunction) {
      return p0.apply(this);
    }

    /** Applies the given match function to the underlying data type. */
    public abstract <T> T match(
        Function<? super SumDataDouble, T> p0,
        Function<? super SumDataLong, T> p1,
        Function<? super SumData, T> defaultFunction);

    /** The double sum value on aggregated values of type {@code MeasureDouble}. */
    @Immutable
    @AutoValue
    public abstract static class SumDataDouble extends SumData {

      SumDataDouble() {
      }

      static SumDataDouble create(double sum) {
        return new AutoValue_AggregationData_SumData_SumDataDouble(sum);
      }

      /**
       * Returns the aggregated sum.
       *
       * @return the aggregated sum.
       */
      public abstract double getSum();

      @Override
      public final <T> T match(
          Function<? super SumDataDouble, T> p0,
          Function<? super SumDataLong, T> p1,
          Function<? super SumData, T> defaultFunction) {
        return p0.apply(this);
      }
    }

    /** The long sum value on aggregated values of type {@code MeasureLong}. */
    @Immutable
    @AutoValue
    public abstract static class SumDataLong extends SumData {

      SumDataLong() {
      }

      static SumDataLong create(long sum) {
        return new AutoValue_AggregationData_SumData_SumDataLong(sum);
      }

      /**
       * Returns the aggregated sum.
       *
       * @return the aggregated sum.
       */
      public abstract long getSum();

      @Override
      public final <T> T match(
          Function<? super SumDataDouble, T> p0,
          Function<? super SumDataLong, T> p1,
          Function<? super SumData, T> defaultFunction) {
        return p1.apply(this);
      }
    }
  }

  /** The count value of aggregated {@code MeasureValue}s. */
  @Immutable
  @AutoValue
  public abstract static class CountData extends AggregationData {

    CountData() {
    }

    static CountData create(long count) {
      return new AutoValue_AggregationData_CountData(count);
    }

    /**
     * Returns the aggregated count.
     *
     * @return the aggregated count.
     */
    public abstract long getCount();

    @Override
    public final <T> T match(
        Function<? super SumData, T> p0,
        Function<? super CountData, T> p1,
        Function<? super HistogramData, T> p2,
        Function<? super RangeData, T> p3,
        Function<? super MeanData, T> p4,
        Function<? super StdDevData, T> p5,
        Function<? super AggregationData, T> defaultFunction) {
      return p1.apply(this);
    }
  }

  /** The histogram of aggregated {@code MeasureValue}s. */
  @Immutable
  @AutoValue
  public abstract static class HistogramData extends AggregationData {

    HistogramData() {
    }

    static HistogramData create(long... bucketCountDatas) {
      checkNotNull(bucketCountDatas, "bucket counts should not be null.");
      List<Long> boxedBucketCountDatas = new ArrayList<Long>();
      for (long bucketCountData : bucketCountDatas) {
        boxedBucketCountDatas.add(bucketCountData);
      }
      return new AutoValue_AggregationData_HistogramData(
          Collections.unmodifiableList(boxedBucketCountDatas));
    }

    /**
     * Returns the aggregated bucket counts. The returned list is immutable, trying to update it
     * will throw an {@code UnsupportedOperationException}.
     *
     * @return the aggregated bucket counts.
     */
    public abstract List<Long> getBucketCounts();

    @Override
    public final <T> T match(
        Function<? super SumData, T> p0,
        Function<? super CountData, T> p1,
        Function<? super HistogramData, T> p2,
        Function<? super RangeData, T> p3,
        Function<? super MeanData, T> p4,
        Function<? super StdDevData, T> p5,
        Function<? super AggregationData, T> defaultFunction) {
      return p2.apply(this);
    }
  }

  /** The range of aggregated {@code MeasureValue}s. */
  @Immutable
  public abstract static class RangeData extends AggregationData {

    private RangeData() {
    }

    @Override
    public final <T> T match(
        Function<? super SumData, T> p0,
        Function<? super CountData, T> p1,
        Function<? super HistogramData, T> p2,
        Function<? super RangeData, T> p3,
        Function<? super MeanData, T> p4,
        Function<? super StdDevData, T> p5,
        Function<? super AggregationData, T> defaultFunction) {
      return p3.apply(this);
    }

    /** Applies the given match function to the underlying data type. */
    public abstract <T> T match(
        Function<? super RangeDataDouble, T> p0,
        Function<? super RangeDataLong, T> p1,
        Function<? super RangeData, T> defaultFunction);

    /** The range on aggregated values of type {@code MeasureDouble}. */
    @Immutable
    @AutoValue
    public abstract static class RangeDataDouble extends RangeData {

      RangeDataDouble() {
      }

      static RangeDataDouble create(double min, double max) {
        if (min != Double.POSITIVE_INFINITY || max != Double.NEGATIVE_INFINITY) {
          checkArgument(min <= max, "max should be greater or equal to min.");
        }
        return new AutoValue_AggregationData_RangeData_RangeDataDouble(min, max);
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

      @Override
      public final <T> T match(
          Function<? super RangeDataDouble, T> p0,
          Function<? super RangeDataLong, T> p1,
          Function<? super RangeData, T> defaultFunction) {
        return p0.apply(this);
      }
    }

    /** The range on aggregated values of type {@code MeasureLong}. */
    @Immutable
    @AutoValue
    public abstract static class RangeDataLong extends RangeData {

      RangeDataLong() {
      }

      static RangeDataLong create(long min, long max) {
        if (min != Long.MAX_VALUE || max != Long.MIN_VALUE) {
          checkArgument(min <= max, "max should be greater or equal to min.");
        }
        return new AutoValue_AggregationData_RangeData_RangeDataLong(min, max);
      }

      /**
       * Returns the minimum of the population values.
       *
       * @return the minimum of the population values.
       */
      public abstract long getMin();

      /**
       * Returns the maximum of the population values.
       *
       * @return the maximum of the population values.
       */
      public abstract long getMax();

      @Override
      public final <T> T match(
          Function<? super RangeDataDouble, T> p0,
          Function<? super RangeDataLong, T> p1,
          Function<? super RangeData, T> defaultFunction) {
        return p1.apply(this);
      }
    }
  }

  /** The mean value of aggregated {@code MeasureValue}s. */
  @Immutable
  @AutoValue
  public abstract static class MeanData extends AggregationData {

    MeanData() {
    }

    static MeanData create(double mean) {
      return new AutoValue_AggregationData_MeanData(mean);
    }

    /**
     * Returns the aggregated mean.
     *
     * @return the aggregated mean.
     */
    public abstract double getMean();

    @Override
    public final <T> T match(
        Function<? super SumData, T> p0,
        Function<? super CountData, T> p1,
        Function<? super HistogramData, T> p2,
        Function<? super RangeData, T> p3,
        Function<? super MeanData, T> p4,
        Function<? super StdDevData, T> p5,
        Function<? super AggregationData, T> defaultFunction) {
      return p4.apply(this);
    }
  }

  /** The standard deviation value of aggregated {@code MeasureValue}s. */
  @Immutable
  @AutoValue
  public abstract static class StdDevData extends AggregationData {

    StdDevData() {
    }

    static StdDevData create(double stdDev) {
      return new AutoValue_AggregationData_StdDevData(stdDev);
    }

    /**
     * Returns the aggregated standard deviation.
     *
     * @return the aggregated standard deviation.
     */
    public abstract double getStdDev();

    @Override
    public final <T> T match(
        Function<? super SumData, T> p0,
        Function<? super CountData, T> p1,
        Function<? super HistogramData, T> p2,
        Function<? super RangeData, T> p3,
        Function<? super MeanData, T> p4,
        Function<? super StdDevData, T> p5,
        Function<? super AggregationData, T> defaultFunction) {
      return p5.apply(this);
    }
  }
}
