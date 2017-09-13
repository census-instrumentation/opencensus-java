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
import io.opencensus.stats.AggregationData.MeanData;
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

  private static final double TOLERANCE = 1e-6;

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testCreateDistributionData() {
    DistributionData distributionData = DistributionData.create(1, 1, 1, 1, 0, 0, 1, 0);
    assertThat(distributionData.getMean()).isWithin(TOLERANCE).of(1);
    assertThat(distributionData.getCount()).isEqualTo(1);
    assertThat(distributionData.getMean()).isWithin(TOLERANCE).of(1);
    assertThat(distributionData.getMin()).isWithin(TOLERANCE).of(1);
    assertThat(distributionData.getMax()).isWithin(TOLERANCE).of(1);
    assertThat(distributionData.getSumOfSquaredDeviations()).isWithin(TOLERANCE).of(0);
    assertThat(distributionData.getBucketCounts()).containsExactly(0L, 1L, 0L).inOrder();
  }

  @Test
  public void preventNullBucketCountData() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("bucket counts should not be null.");
    DistributionData.create(1, 1, 1, 1, 0, null);
  }

  @Test
  public void preventMinIsGreaterThanMax() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("max should be greater or equal to min.");
    DistributionData.create(1, 1, 10, 1, 0, 0, 1, 0);
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
            DistributionData.create(10, 10, 1, 1, 0,0, 10, 0),
            DistributionData.create(10, 10, 1, 1, 0,0, 10, 0))
        .addEqualityGroup(
            DistributionData.create(110, 110, 1, 1, 0,0, 10, 100))
        .addEqualityGroup(
            MeanData.create(5.0, 1),
            MeanData.create(5.0, 1))
        .addEqualityGroup(
            MeanData.create(-5.0, 1),
            MeanData.create(-5.0, 1))
        .testEquals();
  }

  @Test
  public void testMatchAndGet() {
    List<AggregationData> aggregations = Arrays.asList(
        SumData.create(10.0),
        CountData.create(40),
        MeanData.create(5.0, 1),
        DistributionData.create(1, 1, 1, 1, 0, 0, 10, 0));

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
          new Function<MeanData, Void>() {
            @Override
            public Void apply(MeanData arg) {
              actual.add(arg.getMean());
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
          Functions.<Void>throwIllegalArgumentException());
    }

    assertThat(actual).isEqualTo(
        Arrays.asList(10.0, 40L, 5.0, Arrays.asList(0L, 10L, 0L)));
  }
}
