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
import io.opencensus.stats.Aggregation.AggregationCount;
import io.opencensus.stats.Aggregation.AggregationHistogram;
import io.opencensus.stats.Aggregation.AggregationMean;
import io.opencensus.stats.Aggregation.AggregationRange;
import io.opencensus.stats.Aggregation.AggregationStdDev;
import io.opencensus.stats.Aggregation.AggregationSum;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link io.opencensus.stats.Aggregation}. */
@RunWith(JUnit4.class)
public class AggregationTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testCreateAggregationHistogram() {
    BucketBoundaries bucketBoundaries = BucketBoundaries.create(Arrays.asList(0.1, 2.2, 33.3));
    AggregationHistogram histogram = AggregationHistogram.create(bucketBoundaries);
    assertThat(histogram.getBucketBoundaries()).isEqualTo(bucketBoundaries);
  }

  @Test
  public void testNullBucketBoundaries() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("bucketBoundaries should not be null.");
    AggregationHistogram.create(null);
  }

  @Test
  public void testUnsortedBoundaries() throws Exception {
    List<Double> buckets = Arrays.asList(0.0, 1.0, 1.0);
    thrown.expect(IllegalArgumentException.class);
    AggregationHistogram.create(BucketBoundaries.create(buckets));
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            AggregationSum.create(),
            AggregationSum.create())
        .addEqualityGroup(
            AggregationCount.create(),
            AggregationCount.create())
        .addEqualityGroup(
            AggregationHistogram.create(BucketBoundaries.create(Arrays.asList(-10.0, 1.0, 5.0))),
            AggregationHistogram.create(BucketBoundaries.create(Arrays.asList(-10.0, 1.0, 5.0))))
        .addEqualityGroup(
            AggregationRange.create(),
            AggregationRange.create())
        .addEqualityGroup(
            AggregationMean.create(),
            AggregationMean.create())
        .addEqualityGroup(
            AggregationStdDev.create(),
            AggregationStdDev.create())
        .testEquals();
  }

  @Test
  public void testMatch() {
    List<Aggregation> aggregations = Arrays.asList(
        AggregationSum.create(),
        AggregationCount.create(),
        AggregationHistogram.create(BucketBoundaries.create(Arrays.asList(-10.0, 1.0, 5.0))),
        AggregationRange.create(),
        AggregationMean.create(),
        AggregationStdDev.create());

    List<String> actual = new ArrayList<String>();
    for (Aggregation aggregation : aggregations) {
      actual.add(aggregation.match(
          new Function<AggregationSum, String>() {
            @Override
            public String apply(AggregationSum arg) {
              return "SUM";
            }
          },
          new Function<AggregationCount, String>() {
            @Override
            public String apply(AggregationCount arg) {
              return "COUNT";
            }
          },
          new Function<AggregationHistogram, String>() {
            @Override
            public String apply(AggregationHistogram arg) {
              return "HISTOGRAM";
            }
          },
          new Function<AggregationRange, String>() {
            @Override
            public String apply(AggregationRange arg) {
              return "RANGE";
            }
          },
          new Function<AggregationMean, String>() {
            @Override
            public String apply(AggregationMean arg) {
              return "MEAN";
            }
          },
          new Function<AggregationStdDev, String>() {
            @Override
            public String apply(AggregationStdDev arg) {
              return "STDDEV";
            }
          },
          Functions.<String>throwIllegalArgumentException()));
    }

    assertThat(actual)
        .isEqualTo(Arrays.asList("SUM", "COUNT", "HISTOGRAM", "RANGE", "MEAN", "STDDEV"));
  }
}
