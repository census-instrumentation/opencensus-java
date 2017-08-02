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

import com.google.common.collect.Iterables;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.stats.AggregationData.CountData;
import io.opencensus.stats.AggregationData.HistogramData;
import io.opencensus.stats.AggregationData.MeanData;
import io.opencensus.stats.AggregationData.RangeData;
import io.opencensus.stats.AggregationData.StdDevData;
import io.opencensus.stats.AggregationData.SumData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    List<AggregationData> aggregationDataList = new ArrayList<AggregationData>(aggregations.size());
    for (Aggregation aggregation : aggregations) {
      MutableAggregation mAggregation = MutableViewData.createMutableAggregation(aggregation);
      for (double value : values) {
        mAggregation.add(value);
      }
      aggregationDataList.add(MutableViewData.createAggregationData(mAggregation));
    }
    return aggregationDataList;
  }

  /**
   * Compare the actual and expected AggregationMap within the given tolerance.
   *
   * @param expected the expected map.
   * @param actual the actual mapping from {@code List<TagValue>} to
   *     {@code List<AggregationData>}.
   * @param tolerance the tolerance used for {@code double} comparison.
   * @throws AssertionError if the AggregationMap does not contain exactly the keys and values.
   */
  static void assertAggregationMapEquals(
      Map<List<TagValue>, List<AggregationData>> actual,
      Map<List<TagValue>, List<AggregationData>> expected,
      double tolerance) {
    assertThat(actual.keySet()).containsExactlyElementsIn(expected.keySet());
    for (List<TagValue> tagValues : actual.keySet()) {
      assertAggregationDataListEquals(expected.get(tagValues), actual.get(tagValues), tolerance);
    }
  }

  /**
   * Compare the expected and actual list of {@code AggregationData} within the given tolerance.
   *
   * @param expectedValue the expected list of {@code AggregationData}.
   * @param actualValue the actual list of {@code AggregationData}.
   * @param tolerance the tolerance used for {@code double} comparison.
   * @throws AssertionError if the AggregationMap does not contain exactly the keys and values.
   */
  static void assertAggregationDataListEquals(
      List<AggregationData> expectedValue, List<AggregationData> actualValue,
      final double tolerance) {
    assertThat(expectedValue.size()).isEqualTo(actualValue.size());
    for (int i = 0; i < expectedValue.size(); i++) {
      AggregationData actual = actualValue.get(i);
      final AggregationData expected = expectedValue.get(i);
      actual.match(
          new Function<SumData, Void>() {
            @Override
            public Void apply(SumData arg) {
              assertAggregationDataEquals(
                  expected, arg.getSum(), null, null, null, null, null, null, tolerance);
              return null;
            }
          },
          new Function<CountData, Void>() {
            @Override
            public Void apply(CountData arg) {
              assertAggregationDataEquals(
                  expected, null, arg.getCount(), null, null, null, null, null, tolerance);
              return null;
            }
          },
          new Function<HistogramData, Void>() {
            @Override
            public Void apply(HistogramData arg) {
              assertAggregationDataEquals(
                  expected, null, null, arg.getBucketCounts(), null, null, null, null, tolerance);
              return null;
            }
          },
          new Function<RangeData, Void>() {
            @Override
            public Void apply(RangeData arg) {
              assertAggregationDataEquals(
                  expected, null, null, null, arg.getMin(), arg.getMax(), null, null, tolerance);
              return null;
            }
          },
          new Function<MeanData, Void>() {
            @Override
            public Void apply(MeanData arg) {
              assertAggregationDataEquals(
                  expected, null, null, null, null, null, arg.getMean(), null, tolerance);
              return null;
            }
          },
          new Function<StdDevData, Void>() {
            @Override
            public Void apply(StdDevData arg) {
              assertAggregationDataEquals(
                  expected, null, null, null, null, null, null, arg.getStdDev(), tolerance);
              return null;
            }
          },
          Functions.<Void>throwIllegalArgumentException());
    }
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
