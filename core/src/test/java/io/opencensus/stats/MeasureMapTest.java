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
import io.opencensus.common.Function;
import io.opencensus.stats.Measure.DoubleMeasure;
import io.opencensus.stats.Measure.LongMeasure;
import io.opencensus.stats.Measurement.DoubleMeasurement;
import io.opencensus.stats.Measurement.LongMeasurement;
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
  public void testPutDouble() {
    ImmutableList<Measurement> expected = ImmutableList.of(
        (Measurement) Measurement.DoubleMeasurement.create(M1, 44.4));
    MeasureMap metrics = MeasureMap.builder().put(M1, 44.4).build();
    assertEquals(expected.iterator(), metrics.iterator());
  }

  @Test
  public void testPutLong() {
    ImmutableList<Measurement> expected = ImmutableList.of(
        (Measurement) Measurement.LongMeasurement.create(M3, 9999L),
        Measurement.LongMeasurement.create(M4, 8888L));
    MeasureMap metrics = MeasureMap.builder().put(M3, 9999L).put(M4, 8888L).build();
    assertEquals(expected.iterator(), metrics.iterator());
  }

  @Test
  public void testCombination() {
    ImmutableList<Measurement> expected = ImmutableList.of(
        Measurement.DoubleMeasurement.create(M1, 44.4),
        Measurement.DoubleMeasurement.create(M2, 66.6),
        Measurement.LongMeasurement.create(M3, 9999L),
        Measurement.LongMeasurement.create(M4, 8888L));
    MeasureMap metrics =
        MeasureMap.builder().put(M1, 44.4).put(M2, 66.6).put(M3, 9999L).put(M4, 8888L).build();
    assertEquals(expected.iterator(), metrics.iterator());
  }

  @Test
  public void testBuilderEmpty() {
    ImmutableList<Measurement> expected = ImmutableList.of();
    MeasureMap metrics = MeasureMap.builder().build();
    assertEquals(expected.iterator(), metrics.iterator());
  }

  @Test
  public void testBuilder() {
    ArrayList<Measurement> expected = new ArrayList<Measurement>(10);
    MeasureMap.Builder builder = MeasureMap.builder();
    for (int i = 1; i <= 10; i++) {
      expected
          .add(Measurement.DoubleMeasurement.create(makeSimpleDoubleMeasure("m" + i), i * 11.1));
      builder.put(makeSimpleDoubleMeasure("m" + i), i * 11.1);
      assertEquals(expected.iterator(), builder.build().iterator());
    }
  }

  @Test
  public void testDuplicateMeasures() {
    // DoubleMeasure
    MeasureMap metrics1 = MeasureMap.builder().put(M1, 1.0).build();
    MeasureMap metrics2 = MeasureMap.builder().put(M1, 1.0).put(M2, 2.0).build();
    assertEquals(MeasureMap.builder().put(M1, 1.0).put(M1, 2.0).build().iterator(),
        metrics1.iterator());
    assertEquals(MeasureMap.builder().put(M1, 1.0).put(M1, 2.0).put(M1, 3.0).build().iterator(),
        metrics1.iterator());
    assertEquals(MeasureMap.builder().put(M1, 1.0).put(M2, 2.0).put(M1, 3.0).build().iterator(),
        metrics2.iterator());
    assertEquals(MeasureMap.builder().put(M1, 1.0).put(M1, 2.0).put(M2, 2.0).build().iterator(),
        metrics2.iterator());

    // LongMeasure
    MeasureMap metrics3 = MeasureMap.builder().put(M3, 100L).build();
    MeasureMap metrics4 = MeasureMap.builder().put(M3, 100L).put(M4, 200L).build();
    assertEquals(MeasureMap.builder().put(M3, 100L).put(M3, 100L).build().iterator(),
        metrics3.iterator());
    assertEquals(MeasureMap.builder().put(M3, 100L).put(M3, 200L).put(M3, 300L).build().iterator(),
        metrics3.iterator());
    assertEquals(MeasureMap.builder().put(M3, 100L).put(M4, 200L).put(M3, 300L).build().iterator(),
        metrics4.iterator());
    assertEquals(MeasureMap.builder().put(M3, 100L).put(M3, 200L).put(M4, 200L).build().iterator(),
        metrics4.iterator());

    // Mixed
    MeasureMap metrics5 = MeasureMap.builder().put(M3, 100L).put(M1, 1.0).build();
    MeasureMap metrics6 = MeasureMap.builder().put(M2, 2.0).put(M3, 200L).build();
    assertEquals(MeasureMap.builder().put(M3, 100L).put(M1, 1.0).put(M3, 300L).build().iterator(),
        metrics5.iterator());
    assertEquals(MeasureMap.builder().put(M2, 2.0).put(M3, 200L).put(M2, 3.0).build().iterator(),
        metrics6.iterator());
  }

  private static final DoubleMeasure M1 = makeSimpleDoubleMeasure("m1");
  private static final DoubleMeasure M2 = makeSimpleDoubleMeasure("m2");
  private static final LongMeasure M3 = makeSimpleLongMeasure("m3");
  private static final LongMeasure M4 = makeSimpleLongMeasure("m4");

  private static final DoubleMeasure makeSimpleDoubleMeasure(String measure) {
    return Measure.DoubleMeasure.create(
        measure, measure + " description", "1");
  }

  private static final LongMeasure makeSimpleLongMeasure(String measure) {
    return Measure.LongMeasure.create(
        measure, measure + " description", "1");
  }

  private static void assertEquals(
      Iterator<Measurement> expected, Iterator<Measurement> actual) {
    while (expected.hasNext() && actual.hasNext()) {
      Measurement expectedMeasurement = expected.next();
      Measurement actualMeasurement = actual.next();
      assertThat(expectedMeasurement.getMeasure().getName())
          .isEqualTo(actualMeasurement.getMeasure().getName());
      assertThat(expectedMeasurement.match(new GetDoubleValue(), new GetLongValue()))
          .isEqualTo(actualMeasurement.match(new GetDoubleValue(), new GetLongValue()));
    }
    assertThat(expected.hasNext()).isFalse();
    assertThat(actual.hasNext()).isFalse();
  }

  private static final class GetDoubleValue implements Function<DoubleMeasurement, Object> {

    @Override
    public Double apply(DoubleMeasurement arg) {
      return arg.getValue();
    }
  }

  private static final class GetLongValue implements Function<LongMeasurement, Object> {

    @Override
    public Long apply(LongMeasurement arg) {
      return arg.getValue();
    }
  }
}
