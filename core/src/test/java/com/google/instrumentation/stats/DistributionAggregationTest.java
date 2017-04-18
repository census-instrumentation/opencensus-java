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

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for class {@link DistributionAggregation}.
 */
@RunWith(JUnit4.class)
public final class DistributionAggregationTest {
  @Test
  public void testDistributionAggregationWithOutBuckets() {
    DistributionAggregation aggr = DistributionAggregation.create(distribution, TAGS);

    assertThat(aggr.getCountFromDistribution()).isEqualTo(COUNT);
    assertThat(aggr.getMeanFromDistribution()).isWithin(TOLERANCE).of(MEAN);
    assertThat(aggr.getSumFromDistribution()).isWithin(TOLERANCE).of(SUM);
    assertThat(aggr.getRangeFromDistribution().getMin()).isWithin(TOLERANCE).of(MIN);
    assertThat(aggr.getRangeFromDistribution().getMax()).isWithin(TOLERANCE).of(MAX);
    assertThat(aggr.getTags()).hasSize(TAGS.size());
    for (int i = 0; i < aggr.getTags().size(); i++) {
      assertThat(aggr.getTags().get(i)).isEqualTo(TAGS.get(i));
    }
    assertThat(aggr.getBucketCounts()).isNull();
  }

  @Test
  public void testDistributionAggregationWithBuckets() {
    List<Long> buckets = Arrays.asList(2L, 2L, 2L, 2L, 2L);
    DistributionAggregation aggr = DistributionAggregation.create(distribution, TAGS, buckets);

    assertThat(aggr.getCountFromDistribution()).isEqualTo(COUNT);
    assertThat(aggr.getMeanFromDistribution()).isWithin(TOLERANCE).of(MEAN);
    assertThat(aggr.getSumFromDistribution()).isWithin(TOLERANCE).of(SUM);
    assertThat(aggr.getRangeFromDistribution().getMin()).isWithin(TOLERANCE).of(MIN);
    assertThat(aggr.getRangeFromDistribution().getMax()).isWithin(TOLERANCE).of(MAX);
    assertThat(aggr.getBucketCounts()).isNotNull();
    assertThat(aggr.getBucketCounts()).hasSize(buckets.size());
    assertThat(aggr.getTags()).hasSize(TAGS.size());
    for (int i = 0; i < aggr.getTags().size(); i++) {
      assertThat(aggr.getTags().get(i)).isEqualTo(TAGS.get(i));
    }
    for (int i = 0; i < aggr.getBucketCounts().size(); i++) {
      assertThat(aggr.getBucketCounts().get(i)).isEqualTo(buckets.get(i));
    }
  }

  private static final double TOLERANCE = 1e-5;

  private static final MutableDistribution mutableDistribution = MutableDistribution.create();
  static {
    mutableDistribution.add(1.0);
    mutableDistribution.add(2.0);
    mutableDistribution.add(6.0);
  }
  private static final Distribution distribution = Distribution.create(mutableDistribution);
  private static final int COUNT = 3;
  private static final double MEAN = 3.0;
  private static final double SUM = 9.0;
  private static final double MIN = 1.0;
  private static final double MAX = 6.0;

  private static final TagKey K1 = TagKey.create("k1");
  private static final TagKey K2 = TagKey.create("k2");

  private static final TagValue V1 = TagValue.create("v1");
  private static final TagValue V2 = TagValue.create("v2");

  private static final List<Tag> TAGS = Arrays.asList(Tag.create(K1, V1), Tag.create(K2, V2));
}
