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

import com.google.common.testing.EqualsTester;
import io.opencensus.internal.StringUtil;
import java.util.Arrays;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link Measure}
 */
@RunWith(JUnit4.class)
public final class MeasureTest {

  @Ignore  // TODO: determine whether there are any restrictions on measure names.
  @Test
  public void testNameMaxLength() {
    char[] name = new char[Measure.MAX_LENGTH];
    char[] truncName = new char[Measure.MAX_LENGTH + 10];
    Arrays.fill(name, 'n');
    Arrays.fill(truncName, 'n');
    assertThat(makeSimpleMeasure(new String(name)).getName())
        .isEqualTo(makeSimpleMeasure(new String(truncName)).getName());
  }

  @Ignore  // TODO: determine whether there are any restrictions on measure names.
  @Test
  public void testNameBadChar() {
    assertThat(makeSimpleMeasure("\2ab\3cd").getName())
        .isEqualTo(StringUtil.UNPRINTABLE_CHAR_SUBSTITUTE + "ab"
            + StringUtil.UNPRINTABLE_CHAR_SUBSTITUTE + "cd");
  }

  @Test
  public void testDoubleMeasureComponents() {
    Measure measurement = Measure.DoubleMeasure.create(
        "Foo",
        "The description of Foo",
        "Mbit/s");
    assertThat(measurement.getName()).isEqualTo("Foo");
    assertThat(measurement.getDescription()).isEqualTo("The description of Foo");
    assertThat(measurement.getUnit()).isEqualTo("Mbit/s");
  }

  @Test
  public void testLongMeasureComponents() {
    Measure measurement = Measure.LongMeasure.create(
        "Bar",
        "The description of Bar",
        "1");
    assertThat(measurement.getName()).isEqualTo("Bar");
    assertThat(measurement.getDescription()).isEqualTo("The description of Bar");
    assertThat(measurement.getUnit()).isEqualTo("1");
  }

  @Test
  public void testDoubleMeasureEquals() {
    new EqualsTester()
        .addEqualityGroup(
            Measure.DoubleMeasure.create(
                "name",
                "description",
                "bit/s"),
            Measure.DoubleMeasure.create(
                "name",
                "description",
                "bit/s"))
        .addEqualityGroup(
            Measure.DoubleMeasure.create(
                "name",
                "description 2",
                "bit/s"))
        .testEquals();
  }

  @Test
  public void testLongMeasureEquals() {
    new EqualsTester()
        .addEqualityGroup(
            Measure.LongMeasure.create(
                "name",
                "description",
                "bit/s"),
            Measure.LongMeasure.create(
                "name",
                "description",
                "bit/s"))
        .addEqualityGroup(
            Measure.LongMeasure.create(
                "name",
                "description 2",
                "bit/s"))
        .testEquals();
  }

  private static final Measure makeSimpleMeasure(String name) {
    return Measure.DoubleMeasure.create(name, name + " description", "1");
  }
}
