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

import java.util.Arrays;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link com.google.instrumentation.stats.MutableDistribution}. */
@RunWith(JUnit4.class)
public class MutableDistributionTest {
  private static final double TOLERANCE = 1e-5;

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void testEmptyDistribution() {
    MutableDistribution empty = MutableDistribution.create();
    assertThat(empty.getCount()).isEqualTo(0);
    assertThat(empty.getSum()).isWithin(TOLERANCE).of(0.0);
    assertThat(empty.getSumSquaredDeviations()).isWithin(TOLERANCE).of(0.0);
    assertThat(empty.getMean()).isNaN();
    assertThat(empty.getRange().getMin()).isEqualTo(Double.POSITIVE_INFINITY);
    assertThat(empty.getRange().getMax()).isEqualTo(Double.NEGATIVE_INFINITY);
    assertThat(empty.getBucketCounts()).isNull();
  }

  @Test
  public void testEmptyDistributionWithBuckets() {
    List<Double> buckets = Arrays.asList(0.0, 1.0, 2.0);
    MutableDistribution empty = MutableDistribution.create(BucketBoundaries.create(buckets));
    assertThat(empty.getCount()).isEqualTo(0);
    assertThat(empty.getSum()).isWithin(TOLERANCE).of(0.0);
    assertThat(empty.getSumSquaredDeviations()).isWithin(TOLERANCE).of(0.0);
    assertThat(empty.getMean()).isNaN();
    assertThat(empty.getBucketCounts()).hasSize(4);
    for (Long element : empty.getBucketCounts()) {
      assertThat(element).isEqualTo(0);
    }
  }

  @Test
  public void testNullBoundaries() throws Exception {
    thrown.expect(NullPointerException.class);
    MutableDistribution.create(null);
  }

  @Test
  public void testUnsortedBoundaries() throws Exception {
    List<Double> buckets = Arrays.asList(0.0, 1.0, 1.0);
    thrown.expect(IllegalArgumentException.class);
    MutableDistribution.create(BucketBoundaries.create(buckets));
  }

  @Test
  public void testNoBoundaries() {
    List<Double> buckets = Arrays.asList();
    MutableDistribution noBoundaries = MutableDistribution.create(BucketBoundaries.create(buckets));
    assertThat(noBoundaries.getBucketCounts()).hasSize(1);
    assertThat(noBoundaries.getBucketCounts().get(0)).isEqualTo(0);
  }

  @Test
  public void testDistribution() {
    MutableDistribution distribution = MutableDistribution.create();
    distribution.add(1.0);
    assertThat(distribution.getCount()).isEqualTo(1);
    assertThat(distribution.getSum()).isWithin(TOLERANCE).of(1.0);
    assertThat(distribution.getSumSquaredDeviations()).isWithin(TOLERANCE).of(0.0);
    assertThat(distribution.getMean()).isWithin(TOLERANCE).of(1.0);
    assertThat(distribution.getRange().getMin()).isWithin(TOLERANCE).of(1.0);
    assertThat(distribution.getRange().getMax()).isWithin(TOLERANCE).of(1.0);
    distribution.add(1.0);
    assertThat(distribution.getCount()).isEqualTo(2);
    assertThat(distribution.getSum()).isWithin(TOLERANCE).of(2.0);
    assertThat(distribution.getSumSquaredDeviations()).isWithin(TOLERANCE).of(0.0);
    assertThat(distribution.getMean()).isWithin(TOLERANCE).of(1.0);
    assertThat(distribution.getRange().getMin()).isWithin(TOLERANCE).of(1.0);
    assertThat(distribution.getRange().getMax()).isWithin(TOLERANCE).of(1.0);
    distribution.add(7.0);
    assertThat(distribution.getCount()).isEqualTo(3);
    assertThat(distribution.getSum()).isWithin(TOLERANCE).of(9.0);
    assertThat(distribution.getSumSquaredDeviations()).isWithin(TOLERANCE).of(24.0);
    assertThat(distribution.getMean()).isWithin(TOLERANCE).of(3.0);
    assertThat(distribution.getRange().getMin()).isWithin(TOLERANCE).of(1.0);
    assertThat(distribution.getRange().getMax()).isWithin(TOLERANCE).of(7.0);
    distribution.add(-4.6);
    assertThat(distribution.getCount()).isEqualTo(4);
    assertThat(distribution.getSum()).isWithin(TOLERANCE).of(4.4);
    assertThat(distribution.getSumSquaredDeviations()).isWithin(TOLERANCE).of(67.32);
    assertThat(distribution.getMean()).isWithin(TOLERANCE).of(1.1);
    assertThat(distribution.getRange().getMin()).isWithin(TOLERANCE).of(-4.6);
    assertThat(distribution.getRange().getMax()).isWithin(TOLERANCE).of(7.0);
    assertThat(distribution.getBucketCounts()).isNull();
  }

  @Test
  public void testDistributionWithOneBoundary() {
    List<Double> buckets = Arrays.asList(5.0);
    MutableDistribution distribution = MutableDistribution.create(BucketBoundaries.create(buckets));
    assertThat(distribution.getBucketCounts()).hasSize(2);
    for (Long element : distribution.getBucketCounts()) {
      assertThat(element).isEqualTo(0);
    }
    distribution.add(1.4);
    distribution.add(5.0);
    distribution.add(-17.0);
    distribution.add(123.45);
    assertThat(distribution.getCount()).isEqualTo(4);
    assertThat(distribution.getBucketCounts().get(0)).isEqualTo(2);
    assertThat(distribution.getBucketCounts().get(1)).isEqualTo(2);
  }

  @Test
  public void testDistributionWithBoundaries() {
    List<Double> buckets = Arrays.asList(-1.0, 2.0, 5.0, 20.0);
    MutableDistribution distribution = MutableDistribution.create(BucketBoundaries.create(buckets));
    assertThat(distribution.getBucketCounts()).hasSize(5);
    for (Long element : distribution.getBucketCounts()) {
      assertThat(element).isEqualTo(0);
    }
    distribution.add(-50.0);
    distribution.add(-20.0);
    distribution.add(4.0);
    distribution.add(4.0);
    distribution.add(4.999999);
    distribution.add(5.0);
    distribution.add(5.000001);
    distribution.add(123.45);
    assertThat(distribution.getCount()).isEqualTo(8);
    assertThat(distribution.getBucketCounts().get(0)).isEqualTo(2);
    assertThat(distribution.getBucketCounts().get(1)).isEqualTo(0);
    assertThat(distribution.getBucketCounts().get(2)).isEqualTo(3);
    assertThat(distribution.getBucketCounts().get(3)).isEqualTo(2);
    assertThat(distribution.getBucketCounts().get(4)).isEqualTo(1);
  }
}
