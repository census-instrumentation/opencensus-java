/*
 * Copyright 2016-17, OpenCensus Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.implcore.stats;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.Lists;
import io.opencensus.stats.Measure;
import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.stats.Measure.MeasureLong;
import io.opencensus.stats.Measurement;
import io.opencensus.stats.Measurement.MeasurementDouble;
import io.opencensus.stats.Measurement.MeasurementLong;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link MeasureMapInternal}. */
@RunWith(JUnit4.class)
public class MeasureMapInternalTest {

  @Test
  public void testPutDouble() {
    MeasureMapInternal metrics = MeasureMapInternal.builder().put(M1, 44.4).build();
    assertContains(metrics, MeasurementDouble.create(M1, 44.4));
  }

  @Test
  public void testPutLong() {
    MeasureMapInternal metrics = MeasureMapInternal.builder().put(M3, 9999L).put(M4, 8888L).build();
    assertContains(metrics,
        MeasurementLong.create(M3, 9999L),
        MeasurementLong.create(M4, 8888L));
  }

  @Test
  public void testCombination() {
    MeasureMapInternal metrics =
        MeasureMapInternal.builder().put(M1, 44.4).put(M2, 66.6).put(M3, 9999L).put(M4, 8888L)
            .build();
    assertContains(metrics,
        MeasurementDouble.create(M1, 44.4),
        MeasurementDouble.create(M2, 66.6),
        MeasurementLong.create(M3, 9999L),
        MeasurementLong.create(M4, 8888L));
  }

  @Test
  public void testBuilderEmpty() {
    MeasureMapInternal metrics = MeasureMapInternal.builder().build();
    assertContains(metrics);
  }

  @Test
  public void testBuilder() {
    ArrayList<Measurement> expected = new ArrayList<Measurement>(10);
    MeasureMapInternal.Builder builder = MeasureMapInternal.builder();
    for (int i = 1; i <= 10; i++) {
      expected
          .add(MeasurementDouble.create(makeSimpleMeasureDouble("m" + i), i * 11.1));
      builder.put(makeSimpleMeasureDouble("m" + i), i * 11.1);
      assertContains(builder.build(), expected.toArray(new Measurement[i]));
    }
  }

  @Test
  public void testDuplicateMeasureDoubles() {
    assertContains(MeasureMapInternal.builder().put(M1, 1.0).put(M1, 2.0).build(),
        MeasurementDouble.create(M1, 2.0));
    assertContains(MeasureMapInternal.builder().put(M1, 1.0).put(M1, 2.0).put(M1, 3.0).build(),
        MeasurementDouble.create(M1, 3.0));
    assertContains(MeasureMapInternal.builder().put(M1, 1.0).put(M2, 2.0).put(M1, 3.0).build(),
        MeasurementDouble.create(M1, 3.0),
        MeasurementDouble.create(M2, 2.0));
    assertContains(MeasureMapInternal.builder().put(M1, 1.0).put(M1, 2.0).put(M2, 2.0).build(),
        MeasurementDouble.create(M1, 2.0),
        MeasurementDouble.create(M2, 2.0));
  }

  @Test
  public void testDuplicateMeasureLongs() {
    assertContains(MeasureMapInternal.builder().put(M3, 100L).put(M3, 100L).build(),
        MeasurementLong.create(M3, 100L));
    assertContains(MeasureMapInternal.builder().put(M3, 100L).put(M3, 200L).put(M3, 300L).build(),
        MeasurementLong.create(M3, 300L));
    assertContains(MeasureMapInternal.builder().put(M3, 100L).put(M4, 200L).put(M3, 300L).build(),
        MeasurementLong.create(M3, 300L),
        MeasurementLong.create(M4, 200L));
    assertContains(MeasureMapInternal.builder().put(M3, 100L).put(M3, 200L).put(M4, 200L).build(),
        MeasurementLong.create(M3, 200L),
        MeasurementLong.create(M4, 200L));
  }

  @Test
  public void testDuplicateMeasures() {
    assertContains(MeasureMapInternal.builder().put(M3, 100L).put(M1, 1.0).put(M3, 300L).build(),
        MeasurementLong.create(M3, 300L),
        MeasurementDouble.create(M1, 1.0));
    assertContains(MeasureMapInternal.builder().put(M2, 2.0).put(M3, 100L).put(M2, 3.0).build(),
        MeasurementDouble.create(M2, 3.0),
        MeasurementLong.create(M3, 100L));
  }

  private static final MeasureDouble M1 = makeSimpleMeasureDouble("m1");
  private static final MeasureDouble M2 = makeSimpleMeasureDouble("m2");
  private static final MeasureLong M3 = makeSimpleMeasureLong("m3");
  private static final MeasureLong M4 = makeSimpleMeasureLong("m4");

  private static MeasureDouble makeSimpleMeasureDouble(String measure) {
    return Measure.MeasureDouble.create(measure, measure + " description", "1");
  }

  private static MeasureLong makeSimpleMeasureLong(String measure) {
    return Measure.MeasureLong.create(measure, measure + " description", "1");
  }

  private static void assertContains(MeasureMapInternal metrics, Measurement... measurements) {
    assertThat(Lists.newArrayList(metrics.iterator())).containsExactly((Object[]) measurements);
  }
}
