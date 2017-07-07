/*
 * Copyright 2016, Google Inc.
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

import io.opencensus.common.Duration;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link IntervalAggregation}
 */
@RunWith(JUnit4.class)
public final class IntervalAggregationTest {
  @Test
  public void testIntervalAggregation() {
    Duration[] intervals =
        new Duration[] { Duration.fromMillis(1), Duration.fromMillis(22), Duration.fromMillis(333)};
    IntervalAggregation intervalAggregation =
        IntervalAggregation.create(12, Arrays.asList(intervals));
    assertThat(intervalAggregation.getNumSubIntervals()).isEqualTo(12);
    assertThat(intervalAggregation.getIntervalSizes()).isNotNull();
    assertThat(intervalAggregation.getIntervalSizes()).hasSize(intervals.length);
    for (int i = 0; i < intervals.length; i++) {
      assertThat(intervalAggregation.getIntervalSizes().get(i)).isEqualTo(intervals[i]);
    }
  }

  @Test
  public void testIntervalAggregationWithDefaultNumSubIntervals() {
    assertThat(
        IntervalAggregation.create(
            Arrays.asList(Duration.fromMillis(1))).getNumSubIntervals())
        .isEqualTo(5);
  }

  @Test
  public void testIntervalAggregationNumSubIntervalsRange() {
    assertThat(
        IntervalAggregation.create(
            2, Arrays.asList(Duration.fromMillis(1))).getNumSubIntervals())
        .isEqualTo(2);
    assertThat(
        IntervalAggregation.create(
            20, Arrays.asList(Duration.fromMillis(1))).getNumSubIntervals())
        .isEqualTo(20);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIntervalAggregationLowNumSubIntervals() {
    IntervalAggregation.create(1, Arrays.asList(Duration.fromMillis(1)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIntervalAggregationHighNumSubIntervals() {
    IntervalAggregation.create(21, Arrays.asList(Duration.fromMillis(1)));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testIntervalAggregationEmptyIntervalSizes() {
    IntervalAggregation.create(Arrays.asList(new Duration[] { }));
  }
}
