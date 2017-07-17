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

import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.stats.MutableAggregation.MutableAggregationCount;
import io.opencensus.stats.MutableAggregation.MutableAggregationHistogram;
import io.opencensus.stats.MutableAggregation.MutableAggregationMean;
import io.opencensus.stats.MutableAggregation.MutableAggregationRange;
import io.opencensus.stats.MutableAggregation.MutableAggregationStdDev;
import io.opencensus.stats.MutableAggregation.MutableAggregationSum;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link io.opencensus.stats.MutableAggregation}. */
@RunWith(JUnit4.class)
public class MutableAggregationTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private static final double TOLERANCE = 1e-6;

  @Test
  public void testCreateEmpty() {
    BucketBoundaries bucketBoundaries = BucketBoundaries.create(Arrays.asList(0.1, 2.2, 33.3));

    assertThat(MutableAggregationSum.create().getSum()).isWithin(TOLERANCE).of(0);
    assertThat(MutableAggregationCount.create().getCount()).isEqualTo(0);
    assertThat(MutableAggregationHistogram.create(bucketBoundaries).getBucketCounts())
        .isEqualTo(new long[4]);
    assertThat(MutableAggregationRange.create().getMin()).isPositiveInfinity();
    assertThat(MutableAggregationRange.create().getMax()).isNegativeInfinity();
    assertThat(MutableAggregationMean.create().getMean()).isWithin(TOLERANCE).of(0);
    assertThat(MutableAggregationStdDev.create().getStdDev()).isWithin(TOLERANCE).of(0);
  }

  @Test
  public void testNullBucketBoundaries() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("bucketBoundaries should not be null.");
    MutableAggregationHistogram.create(null);
  }

  @Test
  public void testUnsortedBoundaries() throws Exception {
    List<Double> buckets = Arrays.asList(0.0, 1.0, 1.0);
    thrown.expect(IllegalArgumentException.class);
    MutableAggregationHistogram.create(BucketBoundaries.create(buckets));
  }

  @Test
  public void testNoBoundaries() {
    List<Double> buckets = Arrays.asList();
    MutableAggregationHistogram noBoundaries =
        MutableAggregationHistogram.create(BucketBoundaries.create(buckets));
    assertThat(noBoundaries.getBucketCounts().length).isEqualTo(1);
    assertThat(noBoundaries.getBucketCounts()[0]).isEqualTo(0);
  }

  @Test
  public void testAdd() {
    List<MutableAggregation> aggregations = Arrays.asList(
        MutableAggregationSum.create(),
        MutableAggregationCount.create(),
        MutableAggregationHistogram.create(
            BucketBoundaries.create(Arrays.asList(-10.0, 0.0, 10.0))),
        MutableAggregationRange.create(),
        MutableAggregationMean.create(),
        MutableAggregationStdDev.create());

    List<Double> values = Arrays.asList(-1.0, 1.0, -5.0, 20.0, 5.0);

    for (double value : values) {
      for (MutableAggregation aggregation : aggregations) {
        aggregation.add(value);
      }
    }

    for (MutableAggregation aggregation : aggregations) {
      aggregation.match(
          new Function<MutableAggregationSum, Void>() {
            @Override
            public Void apply(MutableAggregationSum arg) {
              assertThat(arg.getSum()).isWithin(TOLERANCE).of(20.0);
              return null;
            }
          },
          new Function<MutableAggregationCount, Void>() {
            @Override
            public Void apply(MutableAggregationCount arg) {
              assertThat(arg.getCount()).isEqualTo(5);
              return null;
            }
          },
          new Function<MutableAggregationHistogram, Void>() {
            @Override
            public Void apply(MutableAggregationHistogram arg) {
              assertThat(arg.getBucketCounts()).isEqualTo(new long[]{0, 2, 2, 1});
              return null;
            }
          },
          new Function<MutableAggregationRange, Void>() {
            @Override
            public Void apply(MutableAggregationRange arg) {
              assertThat(arg.getMin()).isWithin(TOLERANCE).of(-5.0);
              assertThat(arg.getMax()).isWithin(TOLERANCE).of(20.0);
              return null;
            }
          },
          new Function<MutableAggregationMean, Void>() {
            @Override
            public Void apply(MutableAggregationMean arg) {
              assertThat(arg.getMean()).isWithin(TOLERANCE).of(4.0);
              return null;
            }
          },
          new Function<MutableAggregationStdDev, Void>() {
            @Override
            public Void apply(MutableAggregationStdDev arg) {
              assertThat(arg.getStdDev()).isWithin(TOLERANCE).of(Math.sqrt(372 / 5.0));
              return null;
            }
          },
          Functions.<Void>throwIllegalArgumentException());
    }
  }

  @Test
  public void testMatch() {
    List<MutableAggregation> aggregations = Arrays.asList(
        MutableAggregationSum.create(),
        MutableAggregationCount.create(),
        MutableAggregationHistogram.create(
            BucketBoundaries.create(Arrays.asList(-10.0, 1.0, 5.0))),
        MutableAggregationRange.create(),
        MutableAggregationMean.create(),
        MutableAggregationStdDev.create());

    List<String> actual = new ArrayList<String>();
    for (MutableAggregation aggregation : aggregations) {
      actual.add(aggregation.match(
          new Function<MutableAggregationSum, String>() {
            @Override
            public String apply(MutableAggregationSum arg) {
              return "SUM";
            }
          },
          new Function<MutableAggregationCount, String>() {
            @Override
            public String apply(MutableAggregationCount arg) {
              return "COUNT";
            }
          },
          new Function<MutableAggregationHistogram, String>() {
            @Override
            public String apply(MutableAggregationHistogram arg) {
              return "HISTOGRAM";
            }
          },
          new Function<MutableAggregationRange, String>() {
            @Override
            public String apply(MutableAggregationRange arg) {
              return "RANGE";
            }
          },
          new Function<MutableAggregationMean, String>() {
            @Override
            public String apply(MutableAggregationMean arg) {
              return "MEAN";
            }
          },
          new Function<MutableAggregationStdDev, String>() {
            @Override
            public String apply(MutableAggregationStdDev arg) {
              return "STDDEV";
            }
          },
          Functions.<String>throwIllegalArgumentException()));
    }

    assertThat(actual)
        .isEqualTo(Arrays.asList("SUM", "COUNT", "HISTOGRAM", "RANGE", "MEAN", "STDDEV"));
  }
}
