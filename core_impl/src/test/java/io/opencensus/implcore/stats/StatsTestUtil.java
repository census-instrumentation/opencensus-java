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

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.Iterables;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.AggregationData;
import io.opencensus.stats.AggregationData.CountData;
import io.opencensus.stats.AggregationData.HistogramData;
import io.opencensus.stats.AggregationData.MeanData;
import io.opencensus.stats.AggregationData.RangeData;
import io.opencensus.stats.AggregationData.StdDevData;
import io.opencensus.stats.AggregationData.SumData;
import io.opencensus.tags.TagValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Stats test utilities. */
final class StatsTestUtil {

  private StatsTestUtil() {}

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
      MutableAggregation mutableAggregation = MutableViewData.createMutableAggregation(aggregation);
      for (double value : values) {
        mutableAggregation.add(value);
      }
      aggregationDataList.add(MutableViewData.createAggregationData(mutableAggregation));
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
   */
  static void assertAggregationMapEquals(
      Map<? extends List<? extends TagValue>, List<AggregationData>> actual,
      Map<? extends List<? extends TagValue>, List<AggregationData>> expected,
      double tolerance) {
    assertThat(actual.keySet()).containsExactlyElementsIn(expected.keySet());
    for (List<? extends TagValue> tagValues : actual.keySet()) {
      assertAggregationDataListEquals(expected.get(tagValues), actual.get(tagValues), tolerance);
    }
  }

  /**
   * Compare the expected and actual list of {@code AggregationData} within the given tolerance.
   *
   * @param expectedValue the expected list of {@code AggregationData}.
   * @param actualValue the actual list of {@code AggregationData}.
   * @param tolerance the tolerance used for {@code double} comparison.
   */
  static void assertAggregationDataListEquals(
      List<AggregationData> expectedValue, List<AggregationData> actualValue,
      final double tolerance) {
    assertThat(expectedValue.size()).isEqualTo(actualValue.size());
    for (int i = 0; i < expectedValue.size(); i++) {
      final AggregationData actual = actualValue.get(i);
      AggregationData expected = expectedValue.get(i);
      expected.match(
          new Function<SumData, Void>() {
            @Override
            public Void apply(SumData arg) {
              assertThat(actual).isInstanceOf(SumData.class);
              assertThat(((SumData) actual).getSum()).isWithin(tolerance).of(arg.getSum());
              return null;
            }
          },
          new Function<CountData, Void>() {
            @Override
            public Void apply(CountData arg) {
              assertThat(actual).isInstanceOf(CountData.class);
              assertThat(((CountData) actual).getCount()).isEqualTo(arg.getCount());
              return null;
            }
          },
          new Function<HistogramData, Void>() {
            @Override
            public Void apply(HistogramData arg) {
              assertThat(actual).isInstanceOf(HistogramData.class);
              assertThat(removeTrailingZeros(((HistogramData) actual).getBucketCounts()))
                  .isEqualTo(removeTrailingZeros(arg.getBucketCounts()));
              return null;
            }
          },
          new Function<RangeData, Void>() {
            @Override
            public Void apply(RangeData arg) {
              assertThat(actual).isInstanceOf(RangeData.class);
              assertRangeDataEquals((RangeData) actual, arg, tolerance);
              return null;
            }
          },
          new Function<MeanData, Void>() {
            @Override
            public Void apply(MeanData arg) {
              assertThat(actual).isInstanceOf(MeanData.class);
              assertThat(((MeanData) actual).getMean()).isWithin(tolerance).of(arg.getMean());
              return null;
            }
          },
          new Function<StdDevData, Void>() {
            @Override
            public Void apply(StdDevData arg) {
              assertThat(actual).isInstanceOf(StdDevData.class);
              assertThat(((StdDevData) actual).getStdDev()).isWithin(tolerance).of(arg.getStdDev());
              return null;
            }
          },
          Functions.<Void>throwIllegalArgumentException());
    }
  }

  // Compare the expected and actual RangeData within the given tolerance.
  private static void assertRangeDataEquals(
      RangeData actual, RangeData expected, double tolerance) {
    if (expected.getMax() == Double.NEGATIVE_INFINITY
        && expected.getMin() == Double.POSITIVE_INFINITY) {
      assertThat(actual.getMax()).isNegativeInfinity();
      assertThat(actual.getMin()).isPositiveInfinity();
    } else {
      assertThat(actual.getMax()).isWithin(tolerance).of(expected.getMax());
      assertThat(actual.getMin()).isWithin(tolerance).of(expected.getMin());
    }
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
