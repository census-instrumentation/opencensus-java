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

import com.google.common.collect.ImmutableList;
import io.opencensus.stats.MeasurementDescriptor.BasicUnit;
import io.opencensus.stats.MeasurementDescriptor.MeasurementUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link MeasurementMap}
 */
@RunWith(JUnit4.class)
public class MeasurementMapTest {
  @Test
  public void testOf1() {
    ImmutableList<MeasurementValue> expected = ImmutableList.of(MeasurementValue.create(M1, 44.4));
    MeasurementMap metrics = MeasurementMap.of(M1, 44.4);
    assertEquals(expected, metrics);
  }

  @Test
  public void testOf2() {
    ImmutableList<MeasurementValue> expected = ImmutableList.of(
        MeasurementValue.create(M1, 44.4), MeasurementValue.create(M2, 66.6));
    MeasurementMap metrics = MeasurementMap.of(M1, 44.4, M2, 66.6);
    assertEquals(expected, metrics);
  }

  @Test
  public void testOf3() {
    ImmutableList<MeasurementValue> expected = ImmutableList.of(
        MeasurementValue.create(M1, 44.4),
        MeasurementValue.create(M2, 66.6),
        MeasurementValue.create(M3, 88.8));
    MeasurementMap metrics = MeasurementMap.of(
        M1, 44.4, M2, 66.6, M3, 88.8);
    assertEquals(expected, metrics);
  }

  @Test
  public void testBuilderEmpty() {
    ImmutableList<MeasurementValue> expected = ImmutableList.of();
    MeasurementMap metrics = MeasurementMap.builder().build();
    assertEquals(expected, metrics);
  }

  @Test
  public void testBuilder() {
    ArrayList<MeasurementValue> expected = new ArrayList<MeasurementValue>(10);
    MeasurementMap.Builder builder = MeasurementMap.builder();
    for (int i = 1; i <= 10; i++) {
      expected.add(MeasurementValue.create(makeSimpleMeasurement("m" + i), i * 11.1));
      builder.put(makeSimpleMeasurement("m" + i), i * 11.1);
      assertEquals(expected, builder.build());
    }
  }

  @Test
  public void testDuplicateMeasurementDescriptors() {
    assertEquals(MeasurementMap.of(M1, 1.0, M1, 1.0), MeasurementMap.of(M1, 1.0));
    assertEquals(MeasurementMap.of(M1, 1.0, M1, 2.0), MeasurementMap.of(M1, 1.0));
    assertEquals(MeasurementMap.of(M1, 1.0, M1, 2.0, M1, 3.0), MeasurementMap.of(M1, 1.0));
    assertEquals(MeasurementMap.of(M1, 1.0, M2, 2.0, M1, 3.0), MeasurementMap.of(M1, 1.0, M2, 2.0));
    assertEquals(MeasurementMap.of(M1, 1.0, M1, 2.0, M2, 2.0), MeasurementMap.of(M1, 1.0, M2, 2.0));
  }

  @Test
  public void testSize() {
    MeasurementMap.Builder builder = MeasurementMap.builder();
    for (int i = 1; i <= 10; i++) {
      builder.put(makeSimpleMeasurement("m" + i), i * 11.1);
      assertThat(builder.build()).hasSize(i);
    }
  }

  private static final MeasurementUnit simpleMeasurementUnit =
      MeasurementUnit.create(1, Arrays.asList(BasicUnit.SCALAR));
  private static final MeasurementDescriptor M1 = makeSimpleMeasurement("m1");
  private static final MeasurementDescriptor M2 = makeSimpleMeasurement("m2");
  private static final MeasurementDescriptor M3 = makeSimpleMeasurement("m3");

  private static final MeasurementDescriptor makeSimpleMeasurement(String measurement) {
    return MeasurementDescriptor.create(
        measurement, measurement + " description", simpleMeasurementUnit);
  }

  private static void assertEquals(
      Iterable<MeasurementValue> expected, Iterable<MeasurementValue> actual) {
    Iterator<MeasurementValue> e = expected.iterator();
    Iterator<MeasurementValue> a = actual.iterator();
    while (e.hasNext() && a.hasNext()) {
      MeasurementValue expectedMeasurement = e.next();
      MeasurementValue actualMeasurement = a.next();
      assertThat(expectedMeasurement.getMeasurement().getMeasurementDescriptorName())
          .isEqualTo(actualMeasurement.getMeasurement().getMeasurementDescriptorName());
      assertThat(expectedMeasurement.getValue())
          .isWithin(0.00000001).of(actualMeasurement.getValue());
    }
    assertThat(e.hasNext()).isFalse();
    assertThat(a.hasNext()).isFalse();
  }
}
