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

package com.google.instrumentation.stats;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import com.google.instrumentation.stats.MeasurementDescriptor.BasicUnit;
import com.google.instrumentation.stats.MeasurementDescriptor.MeasurementUnit;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link MeasurementDescriptor}
 */
@RunWith(JUnit4.class)
public final class MeasurementDescriptorTest {
  @Test
  public void testNameMaxLength() {
    char[] name = new char[MeasurementDescriptor.MAX_LENGTH];
    char[] truncName = new char[MeasurementDescriptor.MAX_LENGTH + 10];
    Arrays.fill(name, 'n');
    Arrays.fill(truncName, 'n');
    assertThat(makeSimpleDescriptor(new String(name)).toString())
        .isEqualTo(makeSimpleDescriptor(new String(truncName)).toString());
  }

  @Test
  public void testNameBadChar() {
    assertThat(makeSimpleDescriptor("\2ab\3cd").toString())
        .isEqualTo(StringUtil.UNPRINTABLE_CHAR_SUBSTITUTE + "ab"
                 + StringUtil.UNPRINTABLE_CHAR_SUBSTITUTE + "cd");
  }

  @Test
  public void testComponents() {
    MeasurementDescriptor measurement = new MeasurementDescriptor(
        "Foo",
        "The description of Foo",
        new MeasurementUnit(
            6,
            Arrays.asList(new BasicUnit[] { BasicUnit.BITS }),
            Arrays.asList(new BasicUnit[] { BasicUnit.SECS })));
    assertThat(measurement.name).isEqualTo("Foo");
    assertThat(measurement.description).isEqualTo("The description of Foo");
    assertThat(measurement.unit.power10).isEqualTo(6);
    assertThat(measurement.unit.numerators.size()).isEqualTo(1);
    assertThat(measurement.unit.numerators.get(0)).isEqualTo(BasicUnit.BITS);
    assertThat(measurement.unit.denominators.size()).isEqualTo(1);
    assertThat(measurement.unit.denominators.get(0)).isEqualTo(BasicUnit.SECS);
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(makeSimpleDescriptor("foo"), makeSimpleDescriptor("foo"))
        .addEqualityGroup(makeSimpleDescriptor("bar"))
        .testEquals();
  }

  @Test
  public void testToString() {
    assertThat(makeSimpleDescriptor("foo").toString()).isEqualTo("foo");
    assertThat(makeSimpleDescriptor("bar").toString()).isEqualTo("bar");
  }

  private static final MeasurementDescriptor makeSimpleDescriptor(String name) {
    return new MeasurementDescriptor(name, name + " description", simpleMeasurementUnit);
  }

  private static final MeasurementUnit simpleMeasurementUnit =
      new MeasurementUnit(1, Arrays.asList(new BasicUnit[] { BasicUnit.SCALAR }));
}
