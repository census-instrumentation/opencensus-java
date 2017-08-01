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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.common.collect.Iterables;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.common.Timestamp;
import io.opencensus.stats.AggregationData.CountData;
import io.opencensus.stats.AggregationData.HistogramData;
import io.opencensus.stats.AggregationData.MeanData;
import io.opencensus.stats.AggregationData.RangeData;
import io.opencensus.stats.AggregationData.StdDevData;
import io.opencensus.stats.AggregationData.SumData;
import io.opencensus.stats.MutableAggregation.MutableCount;
import io.opencensus.stats.MutableAggregation.MutableHistogram;
import io.opencensus.stats.MutableAggregation.MutableMean;
import io.opencensus.stats.MutableAggregation.MutableRange;
import io.opencensus.stats.MutableAggregation.MutableStdDev;
import io.opencensus.stats.MutableAggregation.MutableSum;
import io.opencensus.stats.ViewData.WindowData;
import io.opencensus.stats.ViewData.WindowData.CumulativeData;
import io.opencensus.stats.ViewData.WindowData.IntervalData;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/** Stats test utilities. */
final class StatsTestUtil {

  private StatsTestUtil() {}

  static StatsContextImpl createContext(
      StatsContextFactoryImpl factory, TagKey key, TagValue value) {
    return createContext(factory, Tag.create(key, value));
  }

  static StatsContextImpl createContext(
      StatsContextFactoryImpl factory, TagKey key1, TagValue value1, TagKey key2, TagValue value2) {
    return createContext(factory, Tag.create(key1, value1), Tag.create(key2, value2));
  }

  /**
   * Creates a {@code StatsContextImpl} from a factory and a list tags.
   *
   * @param factory the factory used to produce the {@code StatsContextImpl}.
   * @param tags a list of tags to add to the {@code StatsContextImpl}.
   * @return a {@code StatsContextImpl} with the given tags.
   */
  private static StatsContextImpl createContext(
      StatsContextFactoryImpl factory, Tag... tags) {
    StatsContextImpl.Builder builder = factory.getDefault().builder();
    for (Tag tag : tags) {
      builder.set(tag.getKey(), tag.getValue());
    }
    return builder.build();
  }

  /**
   * Creates a list of {@link AggregationData}s by adding the given sequence of values, based on the
   * definition of the given {@link Aggregation}s.
   *
   * @param aggregations the {@code Aggregation}s to apply the values to.
   * @param values the values to add to the {@code MutableAggregation}s.
   * @return the new {@code AggregationData}s.
   */
  static List<AggregationData> createAggregationData(
      List<Aggregation> aggregations, double... values) {
    List<MutableAggregation> mAggregations = new ArrayList<MutableAggregation>(aggregations.size());
    for (Aggregation aggregation : aggregations) {
      MutableAggregation mAggregation = MutableViewData.createMutableAggregation(aggregation);
      for (double value : values) {
        mAggregation.add(value);
      }
      mAggregations.add(mAggregation);
    }

    List<AggregationData> aggregates = new ArrayList<AggregationData>(aggregations.size());
    for (MutableAggregation mAggregation : mAggregations) {
      aggregates.add(MutableViewData.createAggregationData(mAggregation));
    }
    return aggregates;
  }

  /**
   * Compare the {@code MutableAggregation} with expected values based on its underlying type. The
   * expected values are all optional (nullable).
   *
   * @param aggregation the {@code MutableAggregation} to be verified.
   * @param sum the expected sum value, if aggregation is a {@code MutableSum}.
   * @param count the expected count value, if aggregation is a {@code MutableCount}.
   * @param bucketCounts the expected bucket counts, if aggregation is a
   *        {@code MutableHistogram}.
   * @param min the expected min value, if aggregation is a {@code MutableRange}.
   * @param max the expected max value, if aggregation is a {@code MutableRange}.
   * @param mean the expected mean value, if aggregation is a {@code MutableMean}.
   * @param stdDev the expected standard deviation, if aggregation is a
   *        {@code MutableStdDev}.
   * @param tolerance the tolerance used for {@code double} comparison.
   * @throws AssertionError if {@code MutableAggregation} doesn't match with expected value.
   */
  static void assertMutableAggregationEquals(
      MutableAggregation aggregation, final Double sum, final Long count, final long[] bucketCounts,
      final Double min, final Double max, final Double mean, final Double stdDev,
      final double tolerance) {
    aggregation.match(
        new Function<MutableSum, Void>() {
          @Override
          public Void apply(MutableSum arg) {
            assertThat(arg.getSum()).isWithin(tolerance).of(sum);
            return null;
          }
        },
        new Function<MutableCount, Void>() {
          @Override
          public Void apply(MutableCount arg) {
            assertThat(arg.getCount()).isEqualTo(count);
            return null;
          }
        },
        new Function<MutableHistogram, Void>() {
          @Override
          public Void apply(MutableHistogram arg) {
            assertThat(removeTrailingZeros(arg.getBucketCounts())).isEqualTo(
                removeTrailingZeros(bucketCounts));
            return null;
          }
        },
        new Function<MutableRange, Void>() {
          @Override
          public Void apply(MutableRange arg) {
            if (max == Double.NEGATIVE_INFINITY && min == Double.POSITIVE_INFINITY) {
              assertThat(arg.getMax()).isNegativeInfinity();
              assertThat(arg.getMin()).isPositiveInfinity();
            } else {
              assertThat(arg.getMax()).isWithin(tolerance).of(max);
              assertThat(arg.getMin()).isWithin(tolerance).of(min);
            }
            return null;
          }
        },
        new Function<MutableMean, Void>() {
          @Override
          public Void apply(MutableMean arg) {
            assertThat(arg.getMean()).isWithin(tolerance).of(mean);
            return null;
          }
        },
        new Function<MutableStdDev, Void>() {
          @Override
          public Void apply(MutableStdDev arg) {
            assertThat(arg.getStdDev()).isWithin(tolerance).of(stdDev);
            return null;
          }
        });
  }

  /**
   * Compare the {@code AggregationData} with expected values based on its underlying type. The
   * expected values are all optional (nullable).
   *
   * @param aggregate the {@code AggregationData} to be verified.
   * @param sum the expected sum value, if aggregation is a {@code SumData}.
   * @param count the expected count value, if aggregation is a {@code CountData}.
   * @param bucketCounts the expected bucket counts, if aggregation is a {@code HistogramData}.
   * @param min the expected min value, if aggregation is a {@code RangeData}.
   * @param max the expected max value, if aggregation is a {@code RangeData}.
   * @param mean the expected mean value, if aggregation is a {@code MeanData}.
   * @param stdDev the expected standard deviation, if aggregation is a {@code StdDevData}.
   * @param tolerance the tolerance used for {@code double} comparison.
   * @throws AssertionError if {@code AggregationData} doesn't match with expected value.
   */
  static void assertAggregationDataEquals(
      AggregationData aggregate, final Double sum, final Long count, final List<Long> bucketCounts,
      final Double min, final Double max, final Double mean, final Double stdDev,
      final double tolerance) {
    aggregate.match(
        new Function<SumData, Void>() {
          @Override
          public Void apply(SumData arg) {
            assertThat(arg.getSum()).isWithin(tolerance).of(sum);
            return null;
          }
        },
        new Function<CountData, Void>() {
          @Override
          public Void apply(CountData arg) {
            assertThat(arg.getCount()).isEqualTo(count);
            return null;
          }
        },
        new Function<HistogramData, Void>() {
          @Override
          public Void apply(HistogramData arg) {
            assertThat(removeTrailingZeros(arg.getBucketCounts())).isEqualTo(
                removeTrailingZeros(bucketCounts));
            return null;
          }
        },
        new Function<RangeData, Void>() {
          @Override
          public Void apply(RangeData arg) {
            if (max == Double.NEGATIVE_INFINITY && min == Double.POSITIVE_INFINITY) {
              assertThat(arg.getMax()).isNegativeInfinity();
              assertThat(arg.getMin()).isPositiveInfinity();
            } else {
              assertThat(arg.getMax()).isWithin(tolerance).of(max);
              assertThat(arg.getMin()).isWithin(tolerance).of(min);
            }
            return null;
          }
        },
        new Function<MeanData, Void>() {
          @Override
          public Void apply(MeanData arg) {
            assertThat(arg.getMean()).isWithin(tolerance).of(mean);
            return null;
          }
        },
        new Function<StdDevData, Void>() {
          @Override
          public Void apply(StdDevData arg) {
            assertThat(arg.getStdDev()).isWithin(tolerance).of(stdDev);
            return null;
          }
        },
        Functions.<Void>throwIllegalArgumentException());
  }

  /**
   * Compare the given {@code WindowData} with expected start and/or end {@code Timestamp}s.
   *
   * @param windowData the actual {@code WindowData}.
   * @param start the expected start {@code Timestamp}. Should be non-null for {@link
   * CumulativeData}, null for {@link IntervalData}.
   * @param end the expected end {@code Timestamp}.
   */
  static void assertWindowDataEquals(
      WindowData windowData, @Nullable final Timestamp start, final Timestamp end) {
    windowData.match(
        new Function<CumulativeData, Void>() {
          @Override
          public Void apply(CumulativeData windowData) {
            if (start == null) {
              fail("expected an IntervalData.");
            }
            assertThat(windowData.getStart()).isEqualTo(start);
            assertThat(windowData.getEnd()).isEqualTo(end);
            return null;
          }
        },
        new Function<IntervalData, Void>() {
          @Override
          public Void apply(IntervalData windowData) {
            if (start != null) {
              fail("expected a CumulativeData.");
            }
            assertThat(windowData.getEnd()).isEqualTo(end);
            return null;
          }
        },
        Functions.<Void>throwIllegalArgumentException());
  }

  /**
   * Remove trailing zeros and return a boxed list of {@code Long}.
   *
   * @param longs the input array of primitive type long.
   * @return a list of boxed object Long, with trailing zeros removed.
   */
  static List<Long> removeTrailingZeros(long... longs) {
    if (longs == null) {
      return null;
    }
    List<Long> boxed = new ArrayList<Long>(longs.length);
    for (long l : longs) {
      boxed.add(l);  // Boxing. Could use Arrays.stream().boxed().collect after Java 8.
    }
    return removeTrailingZeros(boxed);
  }

  private static List<Long> removeTrailingZeros(List<Long> longs) {
    if (longs == null) {
      return null;
    }
    List<Long> truncated = new ArrayList<Long>(longs);
    while (!truncated.isEmpty() && Iterables.getLast(truncated) == 0) {
      truncated.remove(truncated.size() - 1);
    }
    return truncated;
  }
}
