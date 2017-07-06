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

import com.google.common.collect.Lists;
import io.opencensus.stats.Measure.DoubleMeasure;
import io.opencensus.stats.Measure.LongMeasure;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link MeasureMap} */
@RunWith(JUnit4.class)
public class MeasureMapTest {

  @Test
  public void testPutDouble() {
    MeasureMap metrics = MeasureMap.builder().put(M1, 44.4).build();
    assertEquals(metrics, Measurement.DoubleMeasurement.create(M1, 44.4));
  }

  @Test
  public void testPutLong() {
    MeasureMap metrics = MeasureMap.builder().put(M3, 9999L).put(M4, 8888L).build();
    assertEquals(metrics,
        Measurement.LongMeasurement.create(M3, 9999L),
        Measurement.LongMeasurement.create(M4, 8888L));
  }

  @Test
  public void testCombination() {
    MeasureMap metrics =
        MeasureMap.builder().put(M1, 44.4).put(M2, 66.6).put(M3, 9999L).put(M4, 8888L).build();
    assertEquals(metrics,
        Measurement.DoubleMeasurement.create(M1, 44.4),
        Measurement.DoubleMeasurement.create(M2, 66.6),
        Measurement.LongMeasurement.create(M3, 9999L),
        Measurement.LongMeasurement.create(M4, 8888L));
  }

  @Test
  public void testBuilderEmpty() {
    MeasureMap metrics = MeasureMap.builder().build();
    assertEquals(metrics);
  }

  @Test
  public void testBuilder() {
    ArrayList<Measurement> expected = new ArrayList<Measurement>(10);
    MeasureMap.Builder builder = MeasureMap.builder();
    for (int i = 1; i <= 10; i++) {
      expected
          .add(Measurement.DoubleMeasurement.create(makeSimpleDoubleMeasure("m" + i), i * 11.1));
      builder.put(makeSimpleDoubleMeasure("m" + i), i * 11.1);
      assertThat(Lists.newArrayList(builder.build().iterator()))
          .containsExactlyElementsIn(expected);
      assertEquals(builder.build(), expected.toArray(new Measurement[i]));
    }
  }

  @Test
  public void testDuplicateDoubleMeasures() {
    assertEquals(MeasureMap.builder().put(M1, 1.0).put(M1, 2.0).build(), measurement1);
    assertEquals(MeasureMap.builder().put(M1, 1.0).put(M1, 2.0).put(M1, 3.0).build(), measurement1);
    assertEquals(MeasureMap.builder().put(M1, 1.0).put(M2, 2.0).put(M1, 3.0).build(), measurement1,
        measurement2);
    assertEquals(MeasureMap.builder().put(M1, 1.0).put(M1, 2.0).put(M2, 2.0).build(), measurement1,
        measurement2);
  }

  @Test
  public void testDuplicateLongMeasures() {
    assertEquals(MeasureMap.builder().put(M3, 100L).put(M3, 100L).build(), measurement3);
    assertEquals(MeasureMap.builder().put(M3, 100L).put(M3, 200L).put(M3, 300L).build(),
        measurement3);
    assertEquals(MeasureMap.builder().put(M3, 100L).put(M4, 200L).put(M3, 300L).build(),
        measurement3, measurement4);
    assertEquals(MeasureMap.builder().put(M3, 100L).put(M3, 200L).put(M4, 200L).build(),
        measurement3, measurement4);
  }

  @Test
  public void testDuplicateMeasures() {
    assertEquals(MeasureMap.builder().put(M3, 100L).put(M1, 1.0).put(M3, 300L).build(),
        measurement3, measurement1);
    assertEquals(MeasureMap.builder().put(M2, 2.0).put(M3, 100L).put(M2, 3.0).build(),
        measurement2, measurement3);
  }

  private static final DoubleMeasure M1 = makeSimpleDoubleMeasure("m1");
  private static final DoubleMeasure M2 = makeSimpleDoubleMeasure("m2");
  private static final LongMeasure M3 = makeSimpleLongMeasure("m3");
  private static final LongMeasure M4 = makeSimpleLongMeasure("m4");

  private static final Measurement measurement1 = Measurement.DoubleMeasurement.create(M1, 1.0);
  private static final Measurement measurement2 = Measurement.DoubleMeasurement.create(M2, 2.0);
  private static final Measurement measurement3 = Measurement.LongMeasurement.create(M3, 100L);
  private static final Measurement measurement4 = Measurement.LongMeasurement.create(M4, 200L);

  private static DoubleMeasure makeSimpleDoubleMeasure(String measure) {
    return Measure.DoubleMeasure.create(
        measure, measure + " description", "1");
  }

  private static LongMeasure makeSimpleLongMeasure(String measure) {
    return Measure.LongMeasure.create(
        measure, measure + " description", "1");
  }

  private static void assertEquals(MeasureMap metrics, Measurement... measurements) {
    assertThat(Lists.newArrayList(metrics.iterator()))
        .containsExactly((Object[]) measurements);
  }
}
