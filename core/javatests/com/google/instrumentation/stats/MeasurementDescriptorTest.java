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
    assertThat(makeSimpleDescriptor(new String(name)).getName())
        .isEqualTo(makeSimpleDescriptor(new String(truncName)).getName());
  }

  @Test
  public void testNameBadChar() {
    assertThat(makeSimpleDescriptor("\2ab\3cd").getName())
        .isEqualTo(StringUtil.UNPRINTABLE_CHAR_SUBSTITUTE + "ab"
                 + StringUtil.UNPRINTABLE_CHAR_SUBSTITUTE + "cd");
  }

  @Test
  public void testComponents() {
    MeasurementDescriptor measurement = MeasurementDescriptor.create(
        "Foo",
        "The description of Foo",
        MeasurementUnit.create(
            6,
            Arrays.asList(new BasicUnit[] { BasicUnit.BITS }),
            Arrays.asList(new BasicUnit[] { BasicUnit.SECS })));
    assertThat(measurement.getName()).isEqualTo("Foo");
    assertThat(measurement.getDescription()).isEqualTo("The description of Foo");
    assertThat(measurement.getUnit().getPower10()).isEqualTo(6);
    assertThat(measurement.getUnit().getNumerators().size()).isEqualTo(1);
    assertThat(measurement.getUnit().getNumerators().get(0)).isEqualTo(BasicUnit.BITS);
    assertThat(measurement.getUnit().getDenominators().size()).isEqualTo(1);
    assertThat(measurement.getUnit().getDenominators().get(0)).isEqualTo(BasicUnit.SECS);
  }

  private static final MeasurementDescriptor makeSimpleDescriptor(String name) {
    return MeasurementDescriptor.create(
        name,
        name + " description",
        MeasurementUnit.create(1, Arrays.asList(new BasicUnit[] { BasicUnit.SCALAR })));
  }
}
