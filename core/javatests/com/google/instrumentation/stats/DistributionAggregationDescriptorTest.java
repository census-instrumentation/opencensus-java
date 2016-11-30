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
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link DistributionAggregationDescriptor}
 */
@RunWith(JUnit4.class)
public final class DistributionAggregationDescriptorTest {
  @Test
  public void testDistributionAggregationDescriptorEmpty() {
    DistributionAggregationDescriptor dDescriptor = DistributionAggregationDescriptor.create();
    assertThat(dDescriptor.getBucketBoundaries()).isNull();
  }

  @Test
  public void testDistributionAggregationDescriptor() {
    Double[] buckets = new Double[] { 0.1, 2.2, 33.3 };
    DistributionAggregationDescriptor dDescriptor =
        DistributionAggregationDescriptor.create(Arrays.asList(buckets));
    assertThat(dDescriptor.getBucketBoundaries()).isNotNull();
    assertThat(dDescriptor.getBucketBoundaries().size()).isEqualTo(3);
    for (int i = 0; i < buckets.length; i++) {
      assertThat(dDescriptor.getBucketBoundaries().get(i))
          .isWithin(0.00000001).of(buckets[i]);
    }
  }
}
