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

package com.google.instrumentation.stats;

import static com.google.common.truth.Truth.assertThat;

import com.google.instrumentation.common.Duration;
import com.google.instrumentation.stats.IntervalAggregation.Interval;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for class {@link IntervalAggregation}.
 */
@RunWith(JUnit4.class)
public final class IntervalAggregationTest {
  @Test
  public void testIntervalAggregation() {
    List<Interval> intervals =  Arrays.asList(new Interval[] {
          Interval.create(Duration.fromMillis(10), 100, 1000),
          Interval.create(Duration.fromMillis(11), 101, 1001)
        });
    IntervalAggregation aggr = IntervalAggregation.create(TAGS, intervals);

    assertThat(aggr.getTags()).hasSize(TAGS.size());
    for (int i = 0; i < aggr.getTags().size(); i++) {
      assertThat(aggr.getTags().get(i)).isEqualTo(TAGS.get(i));
    }
    assertThat(aggr.getIntervals()).hasSize(intervals.size());
    for (int i = 0; i < aggr.getIntervals().size(); i++) {
      assertThat(aggr.getIntervals().get(i)).isEqualTo(intervals.get(i));
    }
  }

  private static final TagKey K1 = new TagKey("k1");
  private static final TagKey K2 = new TagKey("k2");

  private static final TagValue V1 = new TagValue("v1");
  private static final TagValue V2 = new TagValue("v2");

  private static final List<Tag> TAGS =
      Arrays.asList(new Tag[] { Tag.create(K1, V1), Tag.create(K2, V2) });
}
