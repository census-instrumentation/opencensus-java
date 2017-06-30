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
import io.opencensus.stats.Measure.DoubleMeasure;
import java.util.ArrayList;
import java.util.Iterator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link MeasureMap}
 */
@RunWith(JUnit4.class)
public class MeasureMapTest {

  @Test
  public void testOf1() {
    ImmutableList<Measurement> expected = ImmutableList.of(
        (Measurement) Measurement.DoubleMeasurement.create(M1, 44.4));
    MeasureMap metrics = MeasureMap.of(M1, 44.4);
    assertEquals(expected, metrics);
  }

  @Test
  public void testOf2() {
    ImmutableList<Measurement> expected = ImmutableList.of(
        (Measurement) Measurement.DoubleMeasurement.create(M1, 44.4),
        Measurement.DoubleMeasurement.create(M2, 66.6));
    MeasureMap metrics = MeasureMap.of(M1, 44.4, M2, 66.6);
    assertEquals(expected, metrics);
  }

  @Test
  public void testOf3() {
    ImmutableList<Measurement> expected = ImmutableList.of(
        (Measurement) Measurement.DoubleMeasurement.create(M1, 44.4),
        Measurement.DoubleMeasurement.create(M2, 66.6),
        Measurement.DoubleMeasurement.create(M3, 88.8));
    MeasureMap metrics = MeasureMap.of(
        M1, 44.4, M2, 66.6, M3, 88.8);
    assertEquals(expected, metrics);
  }

  @Test
  public void testBuilderEmpty() {
    ImmutableList<Measurement> expected = ImmutableList.of();
    MeasureMap metrics = MeasureMap.builder().build();
    assertEquals(expected, metrics);
  }

  @Test
  public void testBuilder() {
    ArrayList<Measurement> expected = new ArrayList<Measurement>(10);
    MeasureMap.Builder builder = MeasureMap.builder();
    for (int i = 1; i <= 10; i++) {
      expected
          .add(Measurement.DoubleMeasurement.create(makeSimpleDoubleMeasure("m" + i), i * 11.1));
      builder.put(makeSimpleDoubleMeasure("m" + i), i * 11.1);
      assertEquals(expected, builder.build());
    }
  }

  @Test
  public void testDuplicateMeasures() {
    assertEquals(MeasureMap.of(M1, 1.0, M1, 1.0), MeasureMap.of(M1, 1.0));
    assertEquals(MeasureMap.of(M1, 1.0, M1, 2.0), MeasureMap.of(M1, 1.0));
    assertEquals(MeasureMap.of(M1, 1.0, M1, 2.0, M1, 3.0), MeasureMap.of(M1, 1.0));
    assertEquals(MeasureMap.of(M1, 1.0, M2, 2.0, M1, 3.0), MeasureMap.of(M1, 1.0, M2, 2.0));
    assertEquals(MeasureMap.of(M1, 1.0, M1, 2.0, M2, 2.0), MeasureMap.of(M1, 1.0, M2, 2.0));
  }

  @Test
  public void testSize() {
    MeasureMap.Builder builder = MeasureMap.builder();
    for (int i = 1; i <= 10; i++) {
      builder.put(makeSimpleDoubleMeasure("m" + i), i * 11.1);
      assertThat(builder.build()).hasSize(i);
    }
  }

  private static final DoubleMeasure M1 = makeSimpleDoubleMeasure("m1");
  private static final DoubleMeasure M2 = makeSimpleDoubleMeasure("m2");
  private static final DoubleMeasure M3 = makeSimpleDoubleMeasure("m3");

  private static final DoubleMeasure makeSimpleDoubleMeasure(String measure) {
    return Measure.DoubleMeasure.create(
        measure, measure + " description", "1");
  }

  private static void assertEquals(
      Iterable<Measurement> expected, Iterable<Measurement> actual) {
    Iterator<Measurement> e = expected.iterator();
    Iterator<Measurement> a = actual.iterator();
    while (e.hasNext() && a.hasNext()) {
      Measurement expectedMeasurement = e.next();
      Measurement actualMeasurement = a.next();
      assertThat(expectedMeasurement.getMeasure().getName())
          .isEqualTo(actualMeasurement.getMeasure().getName());
      assertThat(expectedMeasurement.getValue()).isEqualTo(actualMeasurement.getValue());
    }
    assertThat(e.hasNext()).isFalse();
    assertThat(a.hasNext()).isFalse();
  }
}
