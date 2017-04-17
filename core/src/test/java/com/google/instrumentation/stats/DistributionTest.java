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

package com.google.instrumentation.stats;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link com.google.instrumentation.stats.Distribution}. */
@RunWith(JUnit4.class)
public class DistributionTest {
  private static final double TOLERANCE = 1e-6;

  @Test
  public void testDistributionWithoutBoundaries() {
    MutableDistribution mDistribution = MutableDistribution.create();
    mDistribution.add(1.0);
    mDistribution.add(2.0);
    Distribution distribution = Distribution.create(mDistribution);
    assertThat(distribution.getBucketBoundaries()).isNull();
    assertThat(distribution.getBucketCounts()).isNull();
    assertThat(distribution.getCount()).isEqualTo(2L);
    assertThat(distribution.getMean()).isWithin(TOLERANCE).of(1.5);
    assertThat(distribution.getSum()).isWithin(TOLERANCE).of(3.0);
    assertThat(distribution.getRange().getMin()).isWithin(TOLERANCE).of(1.0);
    assertThat(distribution.getRange().getMax()).isWithin(TOLERANCE).of(2.0);
  }

  @Test
  public void testDistributionWithBoundaries() {
    BucketBoundaries boundaries = BucketBoundaries.create(Arrays.asList(-10.0, 10.0));
    MutableDistribution mDistribution = MutableDistribution.create(boundaries);
    mDistribution.add(2.0);
    mDistribution.add(100.0);
    mDistribution.add(-6);
    Distribution distribution = Distribution.create(mDistribution);
    assertThat(distribution.getBucketBoundaries()).isEqualTo(boundaries);
    assertThat(distribution.getBucketCounts()).containsExactly(0L, 2L, 1L).inOrder();
    assertThat(distribution.getCount()).isEqualTo(3L);
    assertThat(distribution.getMean()).isWithin(TOLERANCE).of(32.0);
    assertThat(distribution.getSum()).isWithin(TOLERANCE).of(96.0);
    assertThat(distribution.getRange().getMin()).isWithin(TOLERANCE).of(-6.0);
    assertThat(distribution.getRange().getMax()).isWithin(TOLERANCE).of(100.0);
  }

  @Test(expected = NullPointerException.class)
  public void disallowCreatingDistributionFromNull() {
    Distribution.create(null);
  }

  @Test(expected = NullPointerException.class)
  public void disallowCreatingRangeFromNull() {
    Distribution.Range.create(null);
  }

  @Test
  public void testDistributionEquals() {
    BucketBoundaries boundaries = BucketBoundaries.create(Arrays.asList(-10.0, 10.0));
    MutableDistribution mDistribution = MutableDistribution.create(boundaries);
    mDistribution.add(1);
    mDistribution.add(-1);
    Distribution distribution1 = Distribution.create(mDistribution);
    Distribution distribution2 = Distribution.create(mDistribution);
    mDistribution.add(0);
    Distribution distribution3 = Distribution.create(mDistribution);
    new EqualsTester()
        .addEqualityGroup(distribution1, distribution2)
        .addEqualityGroup(distribution3)
        .testEquals();
  }
}
