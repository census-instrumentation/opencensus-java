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

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.stats.AggregationData.CountData;
import io.opencensus.stats.AggregationData.DistributionData;
import io.opencensus.stats.AggregationData.LastValueDataDouble;
import io.opencensus.stats.AggregationData.LastValueDataLong;
import io.opencensus.stats.AggregationData.MeanData;
import io.opencensus.stats.AggregationData.SumDataDouble;
import io.opencensus.stats.AggregationData.SumDataLong;
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

  private static final double TOLERANCE = 1e-6;

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testCreateDistributionData() {
    DistributionData distributionData =
        DistributionData.create(7.7, 10, 1.1, 9.9, 32.2, Arrays.asList(4L, 1L, 5L));
    assertThat(distributionData.getMean()).isWithin(TOLERANCE).of(7.7);
    assertThat(distributionData.getCount()).isEqualTo(10);
    assertThat(distributionData.getMin()).isWithin(TOLERANCE).of(1.1);
    assertThat(distributionData.getMax()).isWithin(TOLERANCE).of(9.9);
    assertThat(distributionData.getSumOfSquaredDeviations()).isWithin(TOLERANCE).of(32.2);
    assertThat(distributionData.getBucketCounts()).containsExactly(4L, 1L, 5L).inOrder();
  }

  @Test
  public void preventNullBucketCountList() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("bucket counts should not be null.");
    DistributionData.create(1, 1, 1, 1, 0, null);
  }

  @Test
  public void preventNullBucket() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("bucket should not be null.");
    DistributionData.create(1, 1, 1, 1, 0, Arrays.asList(0L, 1L, null));
  }

  @Test
  public void preventMinIsGreaterThanMax() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("max should be greater or equal to min.");
    DistributionData.create(1, 1, 10, 1, 0, Arrays.asList(0L, 1L, 0L));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(SumDataDouble.create(10.0), SumDataDouble.create(10.0))
        .addEqualityGroup(SumDataDouble.create(20.0), SumDataDouble.create(20.0))
        .addEqualityGroup(SumDataLong.create(20), SumDataLong.create(20))
        .addEqualityGroup(CountData.create(40), CountData.create(40))
        .addEqualityGroup(CountData.create(80), CountData.create(80))
        .addEqualityGroup(
            DistributionData.create(10, 10, 1, 1, 0, Arrays.asList(0L, 10L, 0L)),
            DistributionData.create(10, 10, 1, 1, 0, Arrays.asList(0L, 10L, 0L)))
        .addEqualityGroup(DistributionData.create(10, 10, 1, 1, 0, Arrays.asList(0L, 10L, 100L)))
        .addEqualityGroup(DistributionData.create(110, 10, 1, 1, 0, Arrays.asList(0L, 10L, 0L)))
        .addEqualityGroup(DistributionData.create(10, 110, 1, 1, 0, Arrays.asList(0L, 10L, 0L)))
        .addEqualityGroup(DistributionData.create(10, 10, -1, 1, 0, Arrays.asList(0L, 10L, 0L)))
        .addEqualityGroup(DistributionData.create(10, 10, 1, 5, 0, Arrays.asList(0L, 10L, 0L)))
        .addEqualityGroup(DistributionData.create(10, 10, 1, 1, 55.5, Arrays.asList(0L, 10L, 0L)))
        .addEqualityGroup(MeanData.create(5.0, 1), MeanData.create(5.0, 1))
        .addEqualityGroup(MeanData.create(-5.0, 1), MeanData.create(-5.0, 1))
        .addEqualityGroup(LastValueDataDouble.create(20.0), LastValueDataDouble.create(20.0))
        .addEqualityGroup(LastValueDataLong.create(20), LastValueDataLong.create(20))
        .testEquals();
  }

  @Test
  public void testMatchAndGet() {
    List<AggregationData> aggregations =
        Arrays.asList(
            SumDataDouble.create(10.0),
            SumDataLong.create(100000000),
            CountData.create(40),
            DistributionData.create(1, 1, 1, 1, 0, Arrays.asList(0L, 10L, 0L)),
            LastValueDataDouble.create(20.0),
            LastValueDataLong.create(200000000L));

    final List<Object> actual = new ArrayList<Object>();
    for (AggregationData aggregation : aggregations) {
      aggregation.match(
          new Function<SumDataDouble, Void>() {
            @Override
            public Void apply(SumDataDouble arg) {
              actual.add(arg.getSum());
              return null;
            }
          },
          new Function<SumDataLong, Void>() {
            @Override
            public Void apply(SumDataLong arg) {
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
          new Function<DistributionData, Void>() {
            @Override
            public Void apply(DistributionData arg) {
              actual.add(arg.getBucketCounts());
              return null;
            }
          },
          new Function<LastValueDataDouble, Void>() {
            @Override
            public Void apply(LastValueDataDouble arg) {
              actual.add(arg.getLastValue());
              return null;
            }
          },
          new Function<LastValueDataLong, Void>() {
            @Override
            public Void apply(LastValueDataLong arg) {
              actual.add(arg.getLastValue());
              return null;
            }
          },
          Functions.<Void>throwIllegalArgumentException());
    }

    assertThat(actual)
        .containsExactly(10.0, 100000000L, 40L, Arrays.asList(0L, 10L, 0L), 20.0, 200000000L)
        .inOrder();
  }
}
