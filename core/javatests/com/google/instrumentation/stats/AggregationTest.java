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
import com.google.instrumentation.common.Function;
import com.google.instrumentation.stats.Aggregation.DistributionAggregation;
import com.google.instrumentation.stats.Aggregation.DistributionAggregation.Range;
import com.google.instrumentation.stats.Aggregation.IntervalAggregation;
import com.google.instrumentation.stats.Aggregation.IntervalAggregation.Interval;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for class {@link Aggregation}.
 */
@RunWith(JUnit4.class)
public final class AggregationTest {
  @Test
  public void testDistributionAggregationWithRange() {
    List<Long> buckets = Arrays.asList(new Long[] { 2L, 2L, 2L, 2L, 2L });
    DistributionAggregation aggr = DistributionAggregation.create(tags, 10, 5.0, 30.0,
        Range.create(1.0, 5.0), buckets);

    assertThat(aggr.getTags().size()).isEqualTo(tags.size());
    for (int i = 0; i < aggr.getTags().size(); i++) {
      assertThat(isEqualTo(aggr.getTags().get(i), tags.get(i))).isTrue();
    }
    assertThat(aggr.getCount()).isEqualTo(10);
    assertThat(aggr.getMean()).isEqualTo(5.0);
    assertThat(aggr.getSum()).isEqualTo(30.0);
    assertThat(aggr.getRange()).isNotNull();
    assertThat(aggr.getRange().getMin()).isEqualTo(1.0);
    assertThat(aggr.getRange().getMax()).isEqualTo(5.0);
    assertThat(aggr.getBucketCounts().size()).isEqualTo(buckets.size());
    for (int i = 0; i < aggr.getBucketCounts().size(); i++) {
      assertThat(aggr.getBucketCounts().get(i)).isEqualTo(buckets.get(i));
    }
  }

  @Test
  public void testDistributionAggregationWithoutRange() {
    List<Long> buckets = Arrays.asList(new Long[] { 2L, 2L, 2L, 2L, 2L });
    DistributionAggregation aggr = DistributionAggregation.create(tags, 10, 5.0, 30.0, buckets);

    assertThat(aggr.getTags().size()).isEqualTo(tags.size());
    for (int i = 0; i < aggr.getTags().size(); i++) {
      assertThat(isEqualTo(aggr.getTags().get(i), tags.get(i))).isTrue();
    }
    assertThat(aggr.getCount()).isEqualTo(10);
    assertThat(aggr.getMean()).isEqualTo(5.0);
    assertThat(aggr.getSum()).isEqualTo(30.0);
    assertThat(aggr.getRange()).isNull();
    assertThat(aggr.getBucketCounts().size()).isEqualTo(buckets.size());
    for (int i = 0; i < aggr.getBucketCounts().size(); i++) {
      assertThat(aggr.getBucketCounts().get(i)).isEqualTo(buckets.get(i));
    }
  }

  @Test
  public void testIntervalAggregation() {
    List<Interval> intervals =  Arrays.asList(new Interval[] {
          Interval.create(Duration.fromMillis(10), 100, 1000),
          Interval.create(Duration.fromMillis(11), 101, 1001)
        });
    IntervalAggregation aggr = IntervalAggregation.create(tags, intervals);

    assertThat(aggr.getTags().size()).isEqualTo(tags.size());
    for (int i = 0; i < aggr.getTags().size(); i++) {
      assertThat(isEqualTo(aggr.getTags().get(i), tags.get(i))).isTrue();
    }
    assertThat(aggr.getIntervals().size()).isEqualTo(intervals.size());
    for (int i = 0; i < aggr.getIntervals().size(); i++) {
      assertThat(isEqualTo(aggr.getIntervals().get(i), intervals.get(i))).isTrue();
    }
  }

  @Test
  public void testDistributionAggregationMatch() {
    List<Long> buckets = Arrays.asList(new Long[] { 2L, 2L, 2L, 2L, 2L });
    final Aggregation aggr = DistributionAggregation.create(tags, 10, 5.0, 30.0,
        Range.create(1.0, 5.0), buckets);
    assertThat(aggr.match(
        new Function<DistributionAggregation, Boolean> () {
          @Override public Boolean apply(DistributionAggregation dAggr) {
            return (dAggr == aggr);
          }},
        new Function<IntervalAggregation, Boolean> () {
          @Override public Boolean apply(IntervalAggregation iAggr) {
            return false;
          }
        })).isTrue();
  }

  @Test
  public void testIntervalAggregationMatch() {
    List<Interval> intervals =  Arrays.asList(new Interval[] {
          Interval.create(Duration.fromMillis(10), 100, 1000),
          Interval.create(Duration.fromMillis(11), 101, 1001)
        });
    final Aggregation aggr = IntervalAggregation.create(tags, intervals);
    assertThat(aggr.match(
        new Function<DistributionAggregation, Boolean> () {
          @Override public Boolean apply(DistributionAggregation dAggr) {
            return false;
          }
        },
        new Function<IntervalAggregation, Boolean> () {
          @Override public Boolean apply(IntervalAggregation iAggr) {
            return (iAggr == aggr);
          }
        })).isTrue();
  }

  // tag keys
  private static final TagKey K1 = new TagKey("k1");
  private static final TagKey K2 = new TagKey("k2");

  // tag values
  private static final TagValue V1 = new TagValue("v1");
  private static final TagValue V2 = new TagValue("v2");

  // tags
  private static final List<Tag> tags =
      Arrays.asList(new Tag[] { Tag.create(K1, V1), Tag.create(K2, V2) });

  private static final boolean isEqualTo(Interval interval1, Interval interval2) {
    return interval1.getIntervalSize().equals(interval2.getIntervalSize()) &&
        interval1.getCount() == interval2.getCount() &&
        interval1.getMean() == interval2.getMean();
  }

  private static final boolean isEqualTo(Tag tag1, Tag tag2) {
    return tag1.getKey().equals(tag2.getKey()) && tag1.getValue().equals(tag2.getValue());
  }
}
