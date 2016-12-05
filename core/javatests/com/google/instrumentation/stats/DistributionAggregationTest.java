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

import com.google.instrumentation.stats.DistributionAggregation.Range;

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
    DistributionAggregation aggr = DistributionAggregation.create(10, 5.0, 30.0,
        Range.create(1.0, 5.0), TAGS);

    assertThat(aggr.getCount()).isEqualTo(10);
    assertThat(aggr.getMean()).isEqualTo(5.0);
    assertThat(aggr.getSum()).isEqualTo(30.0);
    assertThat(aggr.getRange().getMin()).isEqualTo(1.0);
    assertThat(aggr.getRange().getMax()).isEqualTo(5.0);
    assertThat(aggr.getTags()).hasSize(TAGS.size());
    for (int i = 0; i < aggr.getTags().size(); i++) {
      assertThat(aggr.getTags().get(i)).isEqualTo(TAGS.get(i));
    }
    assertThat(aggr.getBucketCounts()).isNull();
  }

  @Test
  public void testDistributionAggregationWithBuckets() {
    List<Long> buckets = Arrays.asList(2L, 2L, 2L, 2L, 2L);
    DistributionAggregation aggr = DistributionAggregation.create(10, 5.0, 30.0,
        Range.create(1.0, 5.0), TAGS, buckets);

    assertThat(aggr.getCount()).isEqualTo(10);
    assertThat(aggr.getMean()).isEqualTo(5.0);
    assertThat(aggr.getSum()).isEqualTo(30.0);
    assertThat(aggr.getRange().getMin()).isEqualTo(1.0);
    assertThat(aggr.getRange().getMax()).isEqualTo(5.0);
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

  private static final TagKey K1 = new TagKey("k1");
  private static final TagKey K2 = new TagKey("k2");

  private static final TagValue V1 = new TagValue("v1");
  private static final TagValue V2 = new TagValue("v2");

  private static final List<Tag> TAGS = Arrays.asList(Tag.create(K1, V1), Tag.create(K2, V2));
}
