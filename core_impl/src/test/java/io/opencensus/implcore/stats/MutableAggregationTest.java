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

package io.opencensus.implcore.stats;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.common.Function;
import io.opencensus.implcore.stats.MutableAggregation.MutableCount;
import io.opencensus.implcore.stats.MutableAggregation.MutableHistogram;
import io.opencensus.implcore.stats.MutableAggregation.MutableMean;
import io.opencensus.implcore.stats.MutableAggregation.MutableRange;
import io.opencensus.implcore.stats.MutableAggregation.MutableStdDev;
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
    BucketBoundaries bucketBoundaries = BucketBoundaries.create(Arrays.asList(0.1, 2.2, 33.3));

    assertThat(MutableSum.create().getSum()).isWithin(TOLERANCE).of(0);
    assertThat(MutableCount.create().getCount()).isEqualTo(0);
    assertThat(MutableHistogram.create(bucketBoundaries).getBucketCounts())
        .isEqualTo(new long[4]);
    assertThat(MutableRange.create().getMin()).isPositiveInfinity();
    assertThat(MutableRange.create().getMax()).isNegativeInfinity();
    assertThat(MutableMean.create().getMean()).isWithin(TOLERANCE).of(0);
    assertThat(MutableStdDev.create().getStdDev()).isWithin(TOLERANCE).of(0);
  }

  @Test
  public void testNullBucketBoundaries() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("bucketBoundaries should not be null.");
    MutableHistogram.create(null);
  }

  @Test
  public void testNoBoundaries() {
    List<Double> buckets = Arrays.asList();
    MutableHistogram noBoundaries =
        MutableHistogram.create(BucketBoundaries.create(buckets));
    assertThat(noBoundaries.getBucketCounts().length).isEqualTo(1);
    assertThat(noBoundaries.getBucketCounts()[0]).isEqualTo(0);
  }

  @Test
  public void testAdd() {
    List<MutableAggregation> aggregations = Arrays.asList(
        MutableSum.create(),
        MutableCount.create(),
        MutableHistogram.create(
            BucketBoundaries.create(Arrays.asList(-10.0, 0.0, 10.0))),
        MutableRange.create(),
        MutableMean.create(),
        MutableStdDev.create());

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
          new Function<MutableHistogram, Void>() {
            @Override
            public Void apply(MutableHistogram arg) {
              assertThat(arg.getBucketCounts()).isEqualTo(new long[]{0, 2, 2, 1});
              return null;
            }
          },
          new Function<MutableRange, Void>() {
            @Override
            public Void apply(MutableRange arg) {
              assertThat(arg.getMin()).isWithin(TOLERANCE).of(-5.0);
              assertThat(arg.getMax()).isWithin(TOLERANCE).of(20.0);
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
          new Function<MutableStdDev, Void>() {
            @Override
            public Void apply(MutableStdDev arg) {
              assertThat(arg.getStdDev()).isWithin(TOLERANCE).of(Math.sqrt(372 / 5.0));
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
        MutableHistogram.create(
            BucketBoundaries.create(Arrays.asList(-10.0, 1.0, 5.0))),
        MutableRange.create(),
        MutableMean.create(),
        MutableStdDev.create());

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
          new Function<MutableHistogram, String>() {
            @Override
            public String apply(MutableHistogram arg) {
              return "HISTOGRAM";
            }
          },
          new Function<MutableRange, String>() {
            @Override
            public String apply(MutableRange arg) {
              return "RANGE";
            }
          },
          new Function<MutableMean, String>() {
            @Override
            public String apply(MutableMean arg) {
              return "MEAN";
            }
          },
          new Function<MutableStdDev, String>() {
            @Override
            public String apply(MutableStdDev arg) {
              return "STDDEV";
            }
          }));
    }

    assertThat(actual)
        .isEqualTo(Arrays.asList("SUM", "COUNT", "HISTOGRAM", "RANGE", "MEAN", "STDDEV"));
  }
}
