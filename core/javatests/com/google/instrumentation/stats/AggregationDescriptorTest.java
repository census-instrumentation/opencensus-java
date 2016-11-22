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
import com.google.instrumentation.common.MatchFunction;
import com.google.instrumentation.stats.AggregationDescriptor.DistributionAggregationDescriptor;
import com.google.instrumentation.stats.AggregationDescriptor.IntervalAggregationDescriptor;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link AggregationDescriptor}
 */
@RunWith(JUnit4.class)
public final class AggregationDescriptorTest {
  @Test
  public void testDistributionAggregationDescriptorEmpty() {
    DistributionAggregationDescriptor distribution = DistributionAggregationDescriptor.create();
    assertThat(distribution.getBucketBoundaries()).isNull();
  }

  @Test
  public void testDistributionAggregationDescriptor() {
    Double[] buckets = new Double[] { 0.1, 2.2, 33.3 };
    DistributionAggregationDescriptor distribution =
        DistributionAggregationDescriptor.create(Arrays.asList(buckets));
    assertThat(distribution.getBucketBoundaries()).isNotNull();
    assertThat(distribution.getBucketBoundaries().size()).isEqualTo(3);
    for (int i = 0; i < buckets.length; i++) {
      assertThat(distribution.getBucketBoundaries().get(i))
          .isWithin(0.00000001).of(buckets[i]);
    }
  }

  @Test
  public void testIntervalAggregationDescriptorEmpty() {
    IntervalAggregationDescriptor interval = IntervalAggregationDescriptor.create();
    assertThat(interval.getIntervalSizes()).isNull();
  }

  @Test
  public void testIntervalAggregationDescriptor() {
    Duration[] intervals =
        new Duration[] { Duration.fromMillis(1), Duration.fromMillis(22), Duration.fromMillis(333)};
    IntervalAggregationDescriptor interval =
         IntervalAggregationDescriptor.create(Arrays.asList(intervals));
    assertThat(interval.getIntervalSizes()).isNotNull();
    assertThat(interval.getIntervalSizes().size()).isEqualTo(intervals.length);
    for (int i = 0; i < intervals.length; i++) {
      assertThat(interval.getIntervalSizes().get(i)).isEqualTo(intervals[i]);
    }
  }

  @Test
  public void testDistributionAggregationDescriptorMatch() {
    Double[] buckets = new Double[] { 0.1, 2.2, 33.3 };
    AggregationDescriptor descriptor =
        DistributionAggregationDescriptor.create(Arrays.asList(buckets));
    assertThat(descriptor.match(
        new MatchFunction<DistributionAggregationDescriptor, Boolean> () {
          @Override public Boolean f(DistributionAggregationDescriptor d) {
            if (d.getBucketBoundaries().size() != buckets.length) {
              return false;
            }
            for (int i = 0; i < buckets.length; i++) {
              if (!d.getBucketBoundaries().get(i).equals(buckets[i])) {
                return false;
              }
            }
            return true;
          }},
        new MatchFunction<IntervalAggregationDescriptor, Boolean> () {
          @Override public Boolean f(IntervalAggregationDescriptor d) {
            return false;
          }
        })).isTrue();
  }

  @Test
  public void testIntervalAggregationDescriptorMatch() {
    final Duration[] intervals =
        new Duration[] { Duration.fromMillis(1), Duration.fromMillis(22), Duration.fromMillis(333)};
    AggregationDescriptor descriptor =
        IntervalAggregationDescriptor.create(Arrays.asList(intervals));
    assertThat(descriptor.match(
        new MatchFunction<DistributionAggregationDescriptor, Boolean> () {
          @Override public Boolean f(DistributionAggregationDescriptor d) {
            return false;
          }
        },
        new MatchFunction<IntervalAggregationDescriptor, Boolean> () {
          @Override public Boolean f(IntervalAggregationDescriptor d) {
            if (d.getIntervalSizes().size() != intervals.length) {
              return false;
            }
            for (int i = 0; i < intervals.length; i++) {
              if (!d.getIntervalSizes().get(i).equals(intervals[i])) {
                return false;
              }
            }
            return true;
          }
        })).isTrue();
  }
}
