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

import com.google.common.testing.EqualsTester;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.stats.AggregationData.CountData;
import io.opencensus.stats.AggregationData.HistogramData;
import io.opencensus.stats.AggregationData.MeanData;
import io.opencensus.stats.AggregationData.RangeData;
import io.opencensus.stats.AggregationData.StdDevData;
import io.opencensus.stats.AggregationData.SumData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link io.opencensus.stats.AggregationData}. */
@RunWith(JUnit4.class)
public class AggregationDataTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testCreateHistogramData() {
    HistogramData histogram = HistogramData.create(0, 0, 0);
    assertThat(histogram.getBucketCounts()).containsExactly(0L, 0L, 0L).inOrder();
  }

  @Test
  public void testNullBucketCountData() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("bucket counts should not be null.");
    HistogramData.create(null);
  }

  @Test
  public void testRangeDataMinIsGreaterThanMax() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("max should be greater or equal to min.");
    RangeData.create(10.0, 0.0);
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            SumData.create(10.0),
            SumData.create(10.0))
        .addEqualityGroup(
            SumData.create(20.0),
            SumData.create(20.0))
        .addEqualityGroup(
            CountData.create(40),
            CountData.create(40))
        .addEqualityGroup(
            CountData.create(80),
            CountData.create(80))
        .addEqualityGroup(
            HistogramData.create(new long[]{0, 10, 0}),
            HistogramData.create(new long[]{0, 10, 0}))
        .addEqualityGroup(
            HistogramData.create(new long[]{0, 10, 100}),
            HistogramData.create(new long[]{0, 10, 100}))
        .addEqualityGroup(
            RangeData.create(-1.0, 1.0),
            RangeData.create(-1.0, 1.0))
        .addEqualityGroup(
            RangeData.create(-5.0, 1.0),
            RangeData.create(-5.0, 1.0))
        .addEqualityGroup(
            MeanData.create(5.0),
            MeanData.create(5.0))
        .addEqualityGroup(
            MeanData.create(-5.0),
            MeanData.create(-5.0))
        .addEqualityGroup(
            StdDevData.create(23.3),
            StdDevData.create(23.3))
        .addEqualityGroup(
            StdDevData.create(-23.3),
            StdDevData.create(-23.3))
        .testEquals();
  }

  @Test
  public void testMatchAndGet() {
    List<AggregationData> aggregations = Arrays.asList(
        SumData.create(10.0),
        CountData.create(40),
        HistogramData.create(new long[]{0, 10, 0}),
        RangeData.create(-1.0, 1.0),
        MeanData.create(5.0),
        StdDevData.create(23.3));

    final List<Object> actual = new ArrayList<Object>();
    for (AggregationData aggregation : aggregations) {
      aggregation.match(
          new Function<SumData, Void>() {
            @Override
            public Void apply(SumData arg) {
              actual.add(arg.getSum());
              return null;
            }
          },
          new Function<CountData, Void>() {
            @Override
            public Void apply(CountData arg) {
              actual.add(arg.getCount());
              return null;
            }
          },
          new Function<HistogramData, Void>() {
            @Override
            public Void apply(HistogramData arg) {
              actual.add(arg.getBucketCounts());
              return null;
            }
          },
          new Function<RangeData, Void>() {
            @Override
            public Void apply(RangeData arg) {
              actual.add(arg.getMin());
              actual.add(arg.getMax());
              return null;
            }
          },
          new Function<MeanData, Void>() {
            @Override
            public Void apply(MeanData arg) {
              actual.add(arg.getMean());
              return null;
            }
          },
          new Function<StdDevData, Void>() {
            @Override
            public Void apply(StdDevData arg) {
              actual.add(arg.getStdDev());
              return null;
            }
          },
          Functions.<Void>throwIllegalArgumentException());
    }

    assertThat(actual).isEqualTo(
        Arrays.asList(10.0, 40L, Arrays.asList(0L, 10L, 0L), -1.0, 1.0, 5.0, 23.3));
  }
}
