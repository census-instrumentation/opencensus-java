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
import io.opencensus.common.Functions;
import io.opencensus.common.Timestamp;
import io.opencensus.implcore.stats.MutableAggregation.MutableCount;
import io.opencensus.implcore.stats.MutableAggregation.MutableDistribution;
import io.opencensus.implcore.stats.MutableAggregation.MutableLastValue;
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

  @Rule public ExpectedException thrown = ExpectedException.none();

  private static final double TOLERANCE = 1e-6;
  private static final BucketBoundaries BUCKET_BOUNDARIES =
      BucketBoundaries.create(Arrays.asList(-10.0, 0.0, 10.0));
  private static final Timestamp TIMESTAMP = Timestamp.create(60, 0);

  @Test
  public void testCreateEmpty() {
    assertThat(MutableSum.create().getSum()).isWithin(TOLERANCE).of(0);
    assertThat(MutableCount.create().getCount()).isEqualTo(0);
    assertThat(MutableMean.create().getMean()).isWithin(TOLERANCE).of(0);
    assertThat(MutableLastValue.create().getLastValue()).isNaN();

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
    MutableDistribution noBoundaries = MutableDistribution.create(BucketBoundaries.create(buckets));
    assertThat(noBoundaries.getBucketCounts().length).isEqualTo(1);
    assertThat(noBoundaries.getBucketCounts()[0]).isEqualTo(0);
  }

  @Test
  public void testAdd() {
    List<MutableAggregation> aggregations =
        Arrays.asList(
            MutableSum.create(),
            MutableCount.create(),
            MutableMean.create(),
            MutableDistribution.create(BUCKET_BOUNDARIES),
            MutableLastValue.create());

    List<Double> values = Arrays.asList(-1.0, 1.0, -5.0, 20.0, 5.0);

    for (double value : values) {
      for (MutableAggregation aggregation : aggregations) {
        aggregation.add(value, null, TIMESTAMP);
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
              assertThat(arg.getBucketCounts()).isEqualTo(new long[] {0, 2, 2, 1});
              return null;
            }
          },
          new Function<MutableLastValue, Void>() {
            @Override
            public Void apply(MutableLastValue arg) {
              assertThat(arg.getLastValue()).isWithin(TOLERANCE).of(5.0);
              return null;
            }
          });
    }
  }

  @Test
  public void testMatch() {
    List<MutableAggregation> aggregations =
        Arrays.asList(
            MutableSum.create(),
            MutableCount.create(),
            MutableMean.create(),
            MutableDistribution.create(BUCKET_BOUNDARIES),
            MutableLastValue.create());

    List<String> actual = new ArrayList<String>();
    for (MutableAggregation aggregation : aggregations) {
      actual.add(
          aggregation.match(
              Functions.returnConstant("SUM"),
              Functions.returnConstant("COUNT"),
              Functions.returnConstant("MEAN"),
              Functions.returnConstant("DISTRIBUTION"),
              Functions.returnConstant("LASTVALUE")));
    }

    assertThat(actual)
        .isEqualTo(Arrays.asList("SUM", "COUNT", "MEAN", "DISTRIBUTION", "LASTVALUE"));
  }

  @Test
  public void testCombine_SumCountMean() {
    // combine() for Mutable Sum, Count and Mean will pick up fractional stats
    List<MutableAggregation> aggregations1 =
        Arrays.asList(MutableSum.create(), MutableCount.create(), MutableMean.create());
    List<MutableAggregation> aggregations2 =
        Arrays.asList(MutableSum.create(), MutableCount.create(), MutableMean.create());

    for (double val : Arrays.asList(-1.0, -5.0)) {
      for (MutableAggregation aggregation : aggregations1) {
        aggregation.add(val, null, TIMESTAMP);
      }
    }
    for (double val : Arrays.asList(10.0, 50.0)) {
      for (MutableAggregation aggregation : aggregations2) {
        aggregation.add(val, null, TIMESTAMP);
      }
    }

    List<MutableAggregation> combined =
        Arrays.asList(MutableSum.create(), MutableCount.create(), MutableMean.create());
    double fraction1 = 1.0;
    double fraction2 = 0.6;
    for (int i = 0; i < combined.size(); i++) {
      combined.get(i).combine(aggregations1.get(i), fraction1);
      combined.get(i).combine(aggregations2.get(i), fraction2);
    }

    assertThat(((MutableSum) combined.get(0)).getSum()).isWithin(TOLERANCE).of(30);
    assertThat(((MutableCount) combined.get(1)).getCount()).isEqualTo(3);
    assertThat(((MutableMean) combined.get(2)).getMean()).isWithin(TOLERANCE).of(10);
  }

  @Test
  public void testCombine_Distribution() {
    // combine() for Mutable Distribution will ignore fractional stats
    MutableDistribution distribution1 = MutableDistribution.create(BUCKET_BOUNDARIES);
    MutableDistribution distribution2 = MutableDistribution.create(BUCKET_BOUNDARIES);
    MutableDistribution distribution3 = MutableDistribution.create(BUCKET_BOUNDARIES);

    for (double val : Arrays.asList(5.0, -5.0)) {
      distribution1.add(val, null, TIMESTAMP);
    }
    for (double val : Arrays.asList(10.0, 20.0)) {
      distribution2.add(val, null, TIMESTAMP);
    }
    for (double val : Arrays.asList(-10.0, 15.0, -15.0, -20.0)) {
      distribution3.add(val, null, TIMESTAMP);
    }

    MutableDistribution combined = MutableDistribution.create(BUCKET_BOUNDARIES);
    combined.combine(distribution1, 1.0); // distribution1 will be combined
    combined.combine(distribution2, 0.6); // distribution2 will be ignored
    verifyMutableDistribution(combined, 0, 2, -5, 5, 50.0, new long[] {0, 1, 1, 0}, TOLERANCE);

    combined.combine(distribution2, 1.0); // distribution2 will be combined
    verifyMutableDistribution(combined, 7.5, 4, -5, 20, 325.0, new long[] {0, 1, 1, 2}, TOLERANCE);

    combined.combine(distribution3, 1.0); // distribution3 will be combined
    verifyMutableDistribution(combined, 0, 8, -20, 20, 1500.0, new long[] {2, 2, 1, 3}, TOLERANCE);
  }

  private static void verifyMutableDistribution(
      MutableDistribution mutableDistribution,
      double mean,
      long count,
      double min,
      double max,
      double sumOfSquaredDeviations,
      long[] bucketCounts,
      double tolerance) {
    assertThat(mutableDistribution.getMean()).isWithin(tolerance).of(mean);
    assertThat(mutableDistribution.getCount()).isEqualTo(count);
    assertThat(mutableDistribution.getMin()).isWithin(tolerance).of(min);
    assertThat(mutableDistribution.getMax()).isWithin(tolerance).of(max);
    assertThat(mutableDistribution.getSumOfSquaredDeviations())
        .isWithin(tolerance)
        .of(sumOfSquaredDeviations);
    assertThat(mutableDistribution.getBucketCounts()).isEqualTo(bucketCounts);
  }
}
