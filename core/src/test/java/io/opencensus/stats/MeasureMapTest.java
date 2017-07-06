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
    MeasureMap metrics = MeasureMap.builder().set(M1, 44.4).build();
    assertContains(metrics, Measurement.DoubleMeasurement.create(M1, 44.4));
  }

  @Test
  public void testPutLong() {
    MeasureMap metrics = MeasureMap.builder().set(M3, 9999L).set(M4, 8888L).build();
    assertContains(metrics,
        Measurement.LongMeasurement.create(M3, 9999L),
        Measurement.LongMeasurement.create(M4, 8888L));
  }

  @Test
  public void testCombination() {
    MeasureMap metrics =
        MeasureMap.builder().set(M1, 44.4).set(M2, 66.6).set(M3, 9999L).set(M4, 8888L).build();
    assertContains(metrics,
        Measurement.DoubleMeasurement.create(M1, 44.4),
        Measurement.DoubleMeasurement.create(M2, 66.6),
        Measurement.LongMeasurement.create(M3, 9999L),
        Measurement.LongMeasurement.create(M4, 8888L));
  }

  @Test
  public void testBuilderEmpty() {
    MeasureMap metrics = MeasureMap.builder().build();
    assertContains(metrics);
  }

  @Test
  public void testBuilder() {
    ArrayList<Measurement> expected = new ArrayList<Measurement>(10);
    MeasureMap.Builder builder = MeasureMap.builder();
    for (int i = 1; i <= 10; i++) {
      expected
          .add(Measurement.DoubleMeasurement.create(makeSimpleDoubleMeasure("m" + i), i * 11.1));
      builder.set(makeSimpleDoubleMeasure("m" + i), i * 11.1);
      assertContains(builder.build(), expected.toArray(new Measurement[i]));
    }
  }

  @Test
  public void testDuplicateDoubleMeasures() {
    assertContains(MeasureMap.builder().set(M1, 1.0).set(M1, 2.0).build(), MEASUREMENT_1);
    assertContains(MeasureMap.builder().set(M1, 1.0).set(M1, 2.0).set(M1, 3.0).build(), MEASUREMENT_1);
    assertContains(MeasureMap.builder().set(M1, 1.0).set(M2, 2.0).set(M1, 3.0).build(), MEASUREMENT_1,
        MEASUREMENT_2);
    assertContains(MeasureMap.builder().set(M1, 1.0).set(M1, 2.0).set(M2, 2.0).build(), MEASUREMENT_1,
        MEASUREMENT_2);
  }

  @Test
  public void testDuplicateLongMeasures() {
    assertContains(MeasureMap.builder().set(M3, 100L).set(M3, 100L).build(), MEASUREMENT_3);
    assertContains(MeasureMap.builder().set(M3, 100L).set(M3, 200L).set(M3, 300L).build(),
        MEASUREMENT_3);
    assertContains(MeasureMap.builder().set(M3, 100L).set(M4, 200L).set(M3, 300L).build(),
        MEASUREMENT_3, MEASUREMENT_4);
    assertContains(MeasureMap.builder().set(M3, 100L).set(M3, 200L).set(M4, 200L).build(),
        MEASUREMENT_3, MEASUREMENT_4);
  }

  @Test
  public void testDuplicateMeasures() {
    assertContains(MeasureMap.builder().set(M3, 100L).set(M1, 1.0).set(M3, 300L).build(),
        MEASUREMENT_3, MEASUREMENT_1);
    assertContains(MeasureMap.builder().set(M2, 2.0).set(M3, 100L).set(M2, 3.0).build(),
        MEASUREMENT_2, MEASUREMENT_3);
  }

  private static final DoubleMeasure M1 = makeSimpleDoubleMeasure("m1");
  private static final DoubleMeasure M2 = makeSimpleDoubleMeasure("m2");
  private static final LongMeasure M3 = makeSimpleLongMeasure("m3");
  private static final LongMeasure M4 = makeSimpleLongMeasure("m4");

  private static final Measurement MEASUREMENT_1 = Measurement.DoubleMeasurement.create(M1, 1.0);
  private static final Measurement MEASUREMENT_2 = Measurement.DoubleMeasurement.create(M2, 2.0);
  private static final Measurement MEASUREMENT_3 = Measurement.LongMeasurement.create(M3, 100L);
  private static final Measurement MEASUREMENT_4 = Measurement.LongMeasurement.create(M4, 200L);

  private static DoubleMeasure makeSimpleDoubleMeasure(String measure) {
    return Measure.DoubleMeasure.create(measure, measure + " description", "1");
  }

  private static LongMeasure makeSimpleLongMeasure(String measure) {
    return Measure.LongMeasure.create(measure, measure + " description", "1");
  }

  private static void assertContains(MeasureMap metrics, Measurement... measurements) {
    assertThat(Lists.newArrayList(metrics.iterator())).containsExactly((Object[]) measurements);
  }
}
