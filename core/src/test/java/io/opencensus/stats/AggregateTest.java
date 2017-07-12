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
import io.opencensus.stats.Aggregate.AggregateCount;
import io.opencensus.stats.Aggregate.AggregateHistogram;
import io.opencensus.stats.Aggregate.AggregateMean;
import io.opencensus.stats.Aggregate.AggregateRange;
import io.opencensus.stats.Aggregate.AggregateStdDev;
import io.opencensus.stats.Aggregate.AggregateSum;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link io.opencensus.stats.Aggregate}. */
@RunWith(JUnit4.class)
public class AggregateTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testCreateAggregateHistogram() {
    AggregateHistogram histogram = AggregateHistogram.create(0, 0, 0);
    assertThat(new ArrayList<Long>(histogram.get())).isEqualTo(Arrays.asList(0L, 0L, 0L));
  }

  @Test
  public void testNullBucketCounts() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("bucket counts should not be null.");
    AggregateHistogram.create(null);
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            AggregateSum.create(10.0),
            AggregateSum.create(10.0))
        .addEqualityGroup(
            AggregateCount.create(40),
            AggregateCount.create(40))
        .addEqualityGroup(
            AggregateHistogram.create(new long[]{0, 10, 0}),
            AggregateHistogram.create(new long[]{0, 10, 0}))
        .addEqualityGroup(
            AggregateRange.create(Range.create(-1.0, 1.0)),
            AggregateRange.create(Range.create(-1.0, 1.0)))
        .addEqualityGroup(
            AggregateMean.create(5.0),
            AggregateMean.create(5.0))
        .addEqualityGroup(
            AggregateStdDev.create(23.3),
            AggregateStdDev.create(23.3))
        .testEquals();
  }

  @Test
  public void testMatchAndGet() {
    List<Aggregate> aggregations = Arrays.asList(
        AggregateSum.create(10.0),
        AggregateCount.create(40),
        AggregateHistogram.create(new long[]{0, 10, 0}),
        AggregateRange.create(Range.create(-1.0, 1.0)),
        AggregateMean.create(5.0),
        AggregateStdDev.create(23.3));

    final List<Object> actual = new ArrayList<Object>();
    for (Aggregate aggregation : aggregations) {
      aggregation.match(
          new Function<AggregateSum, Void>() {
            @Override
            public Void apply(AggregateSum arg) {
              actual.add(arg.get());
              return null;
            }
          },
          new Function<AggregateCount, Void>() {
            @Override
            public Void apply(AggregateCount arg) {
              actual.add(arg.get());
              return null;
            }
          },
          new Function<AggregateHistogram, Void>() {
            @Override
            public Void apply(AggregateHistogram arg) {
              actual.add(arg.get());
              return null;
            }
          },
          new Function<AggregateRange, Void>() {
            @Override
            public Void apply(AggregateRange arg) {
              actual.add(arg.get());
              return null;
            }
          },
          new Function<AggregateMean, Void>() {
            @Override
            public Void apply(AggregateMean arg) {
              actual.add(arg.get());
              return null;
            }
          },
          new Function<AggregateStdDev, Void>() {
            @Override
            public Void apply(AggregateStdDev arg) {
              actual.add(arg.get());
              return null;
            }
          }
      );
    }

    assertThat(actual).isEqualTo(
        Arrays.asList(10.0, 40L, Arrays.asList(0L, 10L, 0L), Range.create(-1.0, 1.0), 5.0, 23.3));
  }
}
