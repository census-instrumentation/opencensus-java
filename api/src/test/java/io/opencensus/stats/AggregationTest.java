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
import io.opencensus.stats.Aggregation.Count;
import io.opencensus.stats.Aggregation.Distribution;
import io.opencensus.stats.Aggregation.Mean;
import io.opencensus.stats.Aggregation.Sum;
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
  public void testCreateDistribution() {
    BucketBoundaries bucketBoundaries = BucketBoundaries.create(Arrays.asList(0.1, 2.2, 33.3));
    Distribution distribution = Distribution.create(bucketBoundaries);
    assertThat(distribution.getBucketBoundaries()).isEqualTo(bucketBoundaries);
  }

  @Test
  public void testNullBucketBoundaries() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("bucketBoundaries should not be null.");
    Distribution.create(null);
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            Sum.create(),
            Sum.create())
        .addEqualityGroup(
            Count.create(),
            Count.create())
        .addEqualityGroup(
            Distribution.create(BucketBoundaries.create(Arrays.asList(-10.0, 1.0, 5.0))),
            Distribution.create(BucketBoundaries.create(Arrays.asList(-10.0, 1.0, 5.0))))
        .addEqualityGroup(
            Distribution.create(BucketBoundaries.create(Arrays.asList(0.0, 1.0, 5.0))),
            Distribution.create(BucketBoundaries.create(Arrays.asList(0.0, 1.0, 5.0))))
        .addEqualityGroup(
            Mean.create(),
            Mean.create())
        .testEquals();
  }

  @Test
  public void testMatch() {
    List<Aggregation> aggregations = Arrays.asList(
        Sum.create(),
        Count.create(),
        Mean.create(),
        Distribution.create(BucketBoundaries.create(Arrays.asList(-10.0, 1.0, 5.0))));

    List<String> actual = new ArrayList<String>();
    for (Aggregation aggregation : aggregations) {
      actual.add(aggregation.match(
          new Function<Sum, String>() {
            @Override
            public String apply(Sum arg) {
              return "SUM";
            }
          },
          new Function<Count, String>() {
            @Override
            public String apply(Count arg) {
              return "COUNT";
            }
          },
          new Function<Mean, String>() {
            @Override
            public String apply(Mean arg) {
              return "MEAN";
            }
          },
          new Function<Distribution, String>() {
            @Override
            public String apply(Distribution arg) {
              return "DISTRIBUTION";
            }
          },
          Functions.<String>throwIllegalArgumentException()));
    }

    assertThat(actual).isEqualTo(Arrays.asList("SUM", "COUNT", "MEAN", "DISTRIBUTION"));
  }
}
