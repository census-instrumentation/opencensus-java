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
import static io.opencensus.implcore.stats.MutableViewData.ZERO_TIMESTAMP;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.common.Timestamp;
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.AggregationData;
import io.opencensus.stats.AggregationData.CountData;
import io.opencensus.stats.AggregationData.DistributionData;
import io.opencensus.stats.AggregationData.LastValueDataDouble;
import io.opencensus.stats.AggregationData.LastValueDataLong;
import io.opencensus.stats.AggregationData.MeanData;
import io.opencensus.stats.AggregationData.SumDataDouble;
import io.opencensus.stats.AggregationData.SumDataLong;
import io.opencensus.stats.Measure;
import io.opencensus.stats.View;
import io.opencensus.stats.ViewData;
import io.opencensus.stats.ViewData.AggregationWindowData;
import io.opencensus.stats.ViewData.AggregationWindowData.CumulativeData;
import io.opencensus.stats.ViewData.AggregationWindowData.IntervalData;
import io.opencensus.tags.Tag;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagContextBuilder.TagScope;
import io.opencensus.tags.TagValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;

/** Stats test utilities. */
final class StatsTestUtil {

  private static final Timestamp EMPTY = Timestamp.create(0, 0);

  private StatsTestUtil() {}

  /**
   * Creates an {@link AggregationData} by adding the given sequence of values, based on the
   * definition of the given {@link Aggregation}.
   *
   * @param aggregation the {@code Aggregation} to apply the values to.
   * @param values the values to add to the {@code MutableAggregation}s.
   * @return an {@code AggregationData}.
   */
  static AggregationData createAggregationData(
      Aggregation aggregation, Measure measure, double... values) {
    MutableAggregation mutableAggregation =
        RecordUtils.createMutableAggregation(aggregation, measure);
    for (double value : values) {
      mutableAggregation.add(value, Collections.<String, String>emptyMap(), EMPTY);
    }
    return mutableAggregation.toAggregationData();
  }

  /**
   * Compare the actual and expected AggregationMap within the given tolerance.
   *
   * @param expected the expected map.
   * @param actual the actual mapping from {@code List<TagValue>} to {@code AggregationData}.
   * @param tolerance the tolerance used for {@code double} comparison.
   */
  static void assertAggregationMapEquals(
      Map<? extends List<? extends TagValue>, ? extends AggregationData> actual,
      Map<? extends List<? extends TagValue>, ? extends AggregationData> expected,
      double tolerance) {
    assertThat(actual.keySet()).containsExactlyElementsIn(expected.keySet());
    for (Entry<? extends List<? extends TagValue>, ? extends AggregationData> entry :
        actual.entrySet()) {
      assertAggregationDataEquals(expected.get(entry.getKey()), entry.getValue(), tolerance);
    }
  }

  /**
   * Compare the expected and actual {@code AggregationData} within the given tolerance.
   *
   * @param expected the expected {@code AggregationData}.
   * @param actual the actual {@code AggregationData}.
   * @param tolerance the tolerance used for {@code double} comparison.
   */
  static void assertAggregationDataEquals(
      AggregationData expected, final AggregationData actual, final double tolerance) {
    expected.match(
        new Function<SumDataDouble, Void>() {
          @Override
          public Void apply(SumDataDouble arg) {
            assertThat(actual).isInstanceOf(SumDataDouble.class);
            assertThat(((SumDataDouble) actual).getSum()).isWithin(tolerance).of(arg.getSum());
            return null;
          }
        },
        new Function<SumDataLong, Void>() {
          @Override
          public Void apply(SumDataLong arg) {
            assertThat(actual).isInstanceOf(SumDataLong.class);
            assertThat(((SumDataLong) actual).getSum()).isEqualTo(arg.getSum());
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
        new Function<DistributionData, Void>() {
          @Override
          public Void apply(DistributionData arg) {
            assertThat(actual).isInstanceOf(DistributionData.class);
            assertDistributionDataEquals(arg, (DistributionData) actual, tolerance);
            return null;
          }
        },
        new Function<LastValueDataDouble, Void>() {
          @Override
          public Void apply(LastValueDataDouble arg) {
            assertThat(actual).isInstanceOf(LastValueDataDouble.class);
            assertThat(((LastValueDataDouble) actual).getLastValue())
                .isWithin(tolerance)
                .of(arg.getLastValue());
            return null;
          }
        },
        new Function<LastValueDataLong, Void>() {
          @Override
          public Void apply(LastValueDataLong arg) {
            assertThat(actual).isInstanceOf(LastValueDataLong.class);
            assertThat(((LastValueDataLong) actual).getLastValue()).isEqualTo(arg.getLastValue());
            return null;
          }
        },
        new Function<AggregationData, Void>() {
          @Override
          public Void apply(AggregationData arg) {
            if (arg instanceof MeanData) {
              assertThat(actual).isInstanceOf(MeanData.class);
              assertThat(((MeanData) actual).getMean())
                  .isWithin(tolerance)
                  .of(((MeanData) arg).getMean());
              return null;
            }
            throw new IllegalArgumentException("Unknown Aggregation.");
          }
        });
  }

  // Create an empty ViewData with the given View.
  static ViewData createEmptyViewData(View view) {
    return ViewData.create(
        view,
        Collections.<List<TagValue>, AggregationData>emptyMap(),
        view.getWindow()
            .match(
                Functions.<AggregationWindowData>returnConstant(
                    CumulativeData.create(ZERO_TIMESTAMP, ZERO_TIMESTAMP)),
                Functions.<AggregationWindowData>returnConstant(
                    IntervalData.create(ZERO_TIMESTAMP)),
                Functions.<AggregationWindowData>throwAssertionError()));
  }

  // Compare the expected and actual DistributionData within the given tolerance.
  private static void assertDistributionDataEquals(
      DistributionData expected, DistributionData actual, double tolerance) {
    assertThat(actual.getMean()).isWithin(tolerance).of(expected.getMean());
    assertThat(actual.getCount()).isEqualTo(expected.getCount());
    assertThat(actual.getMean()).isWithin(tolerance).of(expected.getMean());
    assertThat(actual.getSumOfSquaredDeviations())
        .isWithin(tolerance)
        .of(expected.getSumOfSquaredDeviations());

    assertThat(removeTrailingZeros(actual.getBucketCounts()))
        .isEqualTo(removeTrailingZeros(expected.getBucketCounts()));
  }

  @Nullable
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

  static final class SimpleTagContext extends TagContext {
    private final List<Tag> tags;

    SimpleTagContext(Tag... tags) {
      this.tags = Collections.unmodifiableList(Lists.newArrayList(tags));
    }

    @Override
    protected Iterator<Tag> getIterator() {
      return tags.iterator();
    }

    @Override
    public TagScope getTagScope() {
      return TagScope.LOCAL;
    }
  }
}
