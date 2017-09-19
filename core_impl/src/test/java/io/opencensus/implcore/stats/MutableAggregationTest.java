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

import io.opencensus.common.Function;
import io.opencensus.implcore.stats.MutableAggregation.MutableCount;
import io.opencensus.implcore.stats.MutableAggregation.MutableDistribution;
import io.opencensus.implcore.stats.MutableAggregation.MutableMean;
import io.opencensus.implcore.stats.MutableAggregation.MutableSum;
import io.opencensus.stats.BucketBoundaries;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link io.opencensus.implcore.stats.MutableAggregation}. */
@RunWith(JUnit4.class)
public class MutableAggregationTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private static final double TOLERANCE = 1e-6;

  @Test
  public void testCreateEmpty() {
    assertThat(MutableSum.create().getSum()).isWithin(TOLERANCE).of(0);
    assertThat(MutableCount.create().getCount()).isEqualTo(0);
    assertThat(MutableMean.create().getMean()).isWithin(TOLERANCE).of(0);

    BucketBoundaries bucketBoundaries = BucketBoundaries.create(Arrays.asList(0.1, 2.2, 33.3));
    MutableDistribution mutableDistribution = MutableDistribution.create(bucketBoundaries);
    assertThat(mutableDistribution.getMean()).isWithin(TOLERANCE).of(0);
    assertThat(mutableDistribution.getCount()).isEqualTo(0);
    assertThat(mutableDistribution.getMin()).isPositiveInfinity();
    assertThat(mutableDistribution.getMax()).isNegativeInfinity();
    assertThat(mutableDistribution.getSumOfSquaredDeviations()).isWithin(TOLERANCE).of(0);
    assertThat(mutableDistribution.getBucketCounts()).isEqualTo(new long[4]);
  }

  @Test
  public void testNullBucketBoundaries() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("bucketBoundaries should not be null.");
    MutableDistribution.create(null);
  }

  @Test
  public void testNoBoundaries() {
    List<Double> buckets = Arrays.asList();
    MutableDistribution noBoundaries =
        MutableDistribution.create(BucketBoundaries.create(buckets));
    assertThat(noBoundaries.getBucketCounts().length).isEqualTo(1);
    assertThat(noBoundaries.getBucketCounts()[0]).isEqualTo(0);
  }

  @Test
  public void testAdd() {
    List<MutableAggregation> aggregations = Arrays.asList(
        MutableSum.create(),
        MutableCount.create(),
        MutableMean.create(),
        MutableDistribution.create(
            BucketBoundaries.create(Arrays.asList(-10.0, 0.0, 10.0))));

    List<Double> values = Arrays.asList(-1.0, 1.0, -5.0, 20.0, 5.0);

    for (double value : values) {
      for (MutableAggregation aggregation : aggregations) {
        aggregation.add(value);
      }
    }

    for (MutableAggregation aggregation : aggregations) {
      aggregation.match(
          new Function<MutableSum, Void>() {
            @Override
            public Void apply(MutableSum arg) {
              assertThat(arg.getSum()).isWithin(TOLERANCE).of(20.0);
              return null;
            }
          },
          new Function<MutableCount, Void>() {
            @Override
            public Void apply(MutableCount arg) {
              assertThat(arg.getCount()).isEqualTo(5);
              return null;
            }
          },
          new Function<MutableMean, Void>() {
            @Override
            public Void apply(MutableMean arg) {
              assertThat(arg.getMean()).isWithin(TOLERANCE).of(4.0);
              return null;
            }
          },
          new Function<MutableDistribution, Void>() {
            @Override
            public Void apply(MutableDistribution arg) {
              assertThat(arg.getBucketCounts()).isEqualTo(new long[]{0, 2, 2, 1});
              return null;
            }
          });
    }
  }

  @Test
  public void testMatch() {
    List<MutableAggregation> aggregations = Arrays.asList(
        MutableSum.create(),
        MutableCount.create(),
        MutableMean.create(),
        MutableDistribution.create(
            BucketBoundaries.create(Arrays.asList(-10.0, 1.0, 5.0))));

    List<String> actual = new ArrayList<String>();
    for (MutableAggregation aggregation : aggregations) {
      actual.add(aggregation.match(
          new Function<MutableSum, String>() {
            @Override
            public String apply(MutableSum arg) {
              return "SUM";
            }
          },
          new Function<MutableCount, String>() {
            @Override
            public String apply(MutableCount arg) {
              return "COUNT";
            }
          },
          new Function<MutableMean, String>() {
            @Override
            public String apply(MutableMean arg) {
              return "MEAN";
            }
          },
          new Function<MutableDistribution, String>() {
            @Override
            public String apply(MutableDistribution arg) {
              return "DISTRIBUTION";
            }
          }));
    }

    assertThat(actual)
        .isEqualTo(Arrays.asList("SUM", "COUNT", "MEAN", "DISTRIBUTION"));
  }

  @Test
  public void testCombine() {
    List<MutableAggregation> aggregations1 = Arrays.asList(
        MutableSum.create(),
        MutableCount.create(),
        MutableMean.create(),
        MutableDistribution.create(BucketBoundaries.create(Arrays.asList(-10.0, 0.0, 10.0))));
    List<MutableAggregation> aggregations2 = Arrays.asList(
        MutableSum.create(),
        MutableCount.create(),
        MutableMean.create(),
        MutableDistribution.create(BucketBoundaries.create(Arrays.asList(-10.0, 0.0, 10.0))));

    List<Double> valueList1 = Arrays.asList(-1.0, -5.0);
    List<Double> valueList2 = Arrays.asList(10.0, 50.0);

    for (double val : valueList1) {
      for (MutableAggregation aggregation : aggregations1) {
        aggregation.add(val);
      }
    }
    for (double val : valueList2) {
      for (MutableAggregation aggregation : aggregations2) {
        aggregation.add(val);
      }
    }

    List<MutableAggregation> combined = Arrays.asList(
        MutableSum.create(),
        MutableCount.create(),
        MutableMean.create(),
        MutableDistribution.create(BucketBoundaries.create(Arrays.asList(-10.0, 0.0, 10.0))));
    double fraction1 = 1.0;
    double fraction2 = 0.6;
    for (int i = 0; i < combined.size(); i++) {
      combined.get(i).combine(aggregations1.get(i), fraction1);
      combined.get(i).combine(aggregations2.get(i), fraction2);
    }

    assertThat(((MutableSum) combined.get(0)).getSum()).isWithin(TOLERANCE).of(30);
    assertThat(((MutableCount) combined.get(1)).getCount()).isEqualTo(3);
    assertThat(((MutableMean) combined.get(2)).getMean()).isWithin(TOLERANCE).of(10);

    MutableDistribution mutableDistribution = (MutableDistribution) combined.get(3);
    assertThat(mutableDistribution.getMean()).isWithin(TOLERANCE).of(-3);
    assertThat(mutableDistribution.getCount()).isEqualTo(2);
    assertThat(mutableDistribution.getMin()).isWithin(TOLERANCE).of(-5);
    assertThat(mutableDistribution.getMax()).isWithin(TOLERANCE).of(-1);
    //    assertThat(mutableDistribution.getSumOfSquaredDeviations()).isWithin(TOLERANCE).of();
    assertThat(mutableDistribution.getBucketCounts()).isEqualTo(new long[]{0, 2, 0, 0});
  }
}
