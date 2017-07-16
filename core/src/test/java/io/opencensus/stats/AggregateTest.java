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
import io.opencensus.stats.Aggregate.Count;
import io.opencensus.stats.Aggregate.Histogram;
import io.opencensus.stats.Aggregate.Mean;
import io.opencensus.stats.Aggregate.Range;
import io.opencensus.stats.Aggregate.StdDev;
import io.opencensus.stats.Aggregate.Sum;
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
  public void testCreateHistogram() {
    Histogram histogram = Histogram.create(0, 0, 0);
    assertThat(new ArrayList<Long>(histogram.get())).isEqualTo(Arrays.asList(0L, 0L, 0L));
  }

  @Test
  public void testNullBucketCounts() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("bucket counts should not be null.");
    Histogram.create(null);
  }

  @Test
  public void testRangeMinIsGreaterThanMax() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("max should be greater or equal to min.");
    Range.create(10.0, 0.0);
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            Sum.create(10.0),
            Sum.create(10.0))
        .addEqualityGroup(
            Count.create(40),
            Count.create(40))
        .addEqualityGroup(
            Histogram.create(new long[]{0, 10, 0}),
            Histogram.create(new long[]{0, 10, 0}))
        .addEqualityGroup(
            Range.create(-1.0, 1.0),
            Range.create(-1.0, 1.0))
        .addEqualityGroup(
            Mean.create(5.0),
            Mean.create(5.0))
        .addEqualityGroup(
            StdDev.create(23.3),
            StdDev.create(23.3))
        .testEquals();
  }

  @Test
  public void testMatchAndGet() {
    List<Aggregate> aggregations = Arrays.asList(
        Sum.create(10.0),
        Count.create(40),
        Histogram.create(new long[]{0, 10, 0}),
        Range.create(-1.0, 1.0),
        Mean.create(5.0),
        StdDev.create(23.3));

    final List<Object> actual = new ArrayList<Object>();
    for (Aggregate aggregation : aggregations) {
      aggregation.match(
          new Function<Sum, Void>() {
            @Override
            public Void apply(Sum arg) {
              actual.add(arg.get());
              return null;
            }
          },
          new Function<Count, Void>() {
            @Override
            public Void apply(Count arg) {
              actual.add(arg.get());
              return null;
            }
          },
          new Function<Histogram, Void>() {
            @Override
            public Void apply(Histogram arg) {
              actual.add(arg.get());
              return null;
            }
          },
          new Function<Range, Void>() {
            @Override
            public Void apply(Range arg) {
              actual.add(arg.getMin());
              actual.add(arg.getMax());
              return null;
            }
          },
          new Function<Mean, Void>() {
            @Override
            public Void apply(Mean arg) {
              actual.add(arg.get());
              return null;
            }
          },
          new Function<StdDev, Void>() {
            @Override
            public Void apply(StdDev arg) {
              actual.add(arg.get());
              return null;
            }
          },
          Functions.<Void>throwIllegalArgumentException());
    }

    assertThat(actual).isEqualTo(
        Arrays.asList(10.0, 40L, Arrays.asList(0L, 10L, 0L), -1.0, 1.0, 5.0, 23.3));
  }
}
