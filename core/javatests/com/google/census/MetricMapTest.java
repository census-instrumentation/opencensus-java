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

package com.google.census;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Tests for {@link MetricMap}
 */
@RunWith(JUnit4.class)
public class MetricMapTest {
  @Test
  public void testOf1() {
    ImmutableList<Metric> expected = ImmutableList.of(
        new Metric(M1, 44.4));
    MetricMap metrics = MetricMap.of(M1, 44.4);
    assertEquals(expected, metrics);
  }

  @Test
  public void testOf2() {
    ImmutableList<Metric> expected = ImmutableList.of(new Metric(M1, 44.4), new Metric(M2, 66.6));
    MetricMap metrics = MetricMap.of(M1, 44.4, M2, 66.6);
    assertEquals(expected, metrics);
  }

  @Test
  public void testOf3() {
    ImmutableList<Metric> expected = ImmutableList.of(
        new Metric(M1, 44.4), new Metric(M2, 66.6), new Metric(M3, 88.8));
    MetricMap metrics = MetricMap.of(
        M1, 44.4, M2, 66.6, M3, 88.8);
    assertEquals(expected, metrics);
  }

  @Test
  public void testBuilderEmpty() {
    ImmutableList<Metric> expected = ImmutableList.of();
    MetricMap metrics = MetricMap.builder().build();
    assertEquals(expected, metrics);
  }

  @Test
  public void testBuilder() {
    ArrayList<Metric> expected = new ArrayList<Metric>(10);
    MetricMap.Builder builder = MetricMap.builder();
    for (int i = 1; i <= 10; i++) {
      expected.add(new Metric(new MetricName("m" + i), i * 11.1));
      builder.put(new MetricName("m" + i), i * 11.1);
      assertEquals(expected, builder.build());
    }
  }

  @Test
  public void testDuplicateMetricNames() {
    assertEquals(MetricMap.of(M1, 1.0, M1, 1.0), MetricMap.of(M1, 1.0));
    assertEquals(MetricMap.of(M1, 1.0, M1, 2.0), MetricMap.of(M1, 1.0));
    assertEquals(MetricMap.of(M1, 1.0, M1, 2.0, M1, 3.0), MetricMap.of(M1, 1.0));
    assertEquals(MetricMap.of(M1, 1.0, M2, 2.0, M1, 3.0), MetricMap.of(M1, 1.0, M2, 2.0));
    assertEquals(MetricMap.of(M1, 1.0, M1, 2.0, M2, 2.0), MetricMap.of(M1, 1.0, M2, 2.0));
  }

  @Test
  public void testSize() {
    MetricMap.Builder builder = MetricMap.builder();
    for (int i = 1; i <= 10; i++) {
      builder.put(new MetricName("m" + i), i * 11.1);
      assertThat(builder.build().size()).isEqualTo(i);
    }
  }

  private static final MetricName M1 = new MetricName("m1");
  private static final MetricName M2 = new MetricName("m2");
  private static final MetricName M3 = new MetricName("m3");

  private static void assertEquals(Iterable<Metric> expected, Iterable<Metric> actual) {
    Iterator<Metric> e = expected.iterator();
    Iterator<Metric> a = actual.iterator();
    while (e.hasNext() && a.hasNext()) {
      Metric expectedMetric = e.next();
      Metric actualMetric = a.next();
      assertThat(expectedMetric.getName()).isEqualTo(actualMetric.getName());
      assertThat(expectedMetric.getValue()).isWithin(0.00000001).of(actualMetric.getValue());
    }
    assertThat(e.hasNext()).isFalse();
    assertThat(a.hasNext()).isFalse();
  }
}
