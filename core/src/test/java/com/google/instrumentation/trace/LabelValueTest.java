/*
 * Copyright 2017, Google Inc.
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

package com.google.instrumentation.trace;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link LabelValue}. */
@RunWith(JUnit4.class)
public class LabelValueTest {
  @Test
  public void stringLabelValue() {
    LabelValue label = LabelValue.stringLabelValue("MyStringLabelValue");
    assertThat(label.getStringValue()).isEqualTo("MyStringLabelValue");
    assertThat(label.getBooleanValue()).isNull();
    assertThat(label.getLongValue()).isNull();
  }

  @Test
  public void booleanLabelValue() {
    LabelValue label = LabelValue.booleanLabelValue(true);
    assertThat(label.getStringValue()).isNull();
    assertThat(label.getBooleanValue()).isTrue();
    assertThat(label.getLongValue()).isNull();
  }

  @Test
  public void longLabelValue() {
    LabelValue label = LabelValue.longLabelValue(123456L);
    assertThat(label.getStringValue()).isNull();
    assertThat(label.getBooleanValue()).isNull();
    assertThat(label.getLongValue()).isEqualTo(123456L);
  }

  @Test
  public void labelValue_EqualsAndHashCode() {
    EqualsTester tester = new EqualsTester();
    tester.addEqualityGroup(
        LabelValue.stringLabelValue("MyStringLabelValue"),
        LabelValue.stringLabelValue("MyStringLabelValue"));
    tester.addEqualityGroup(LabelValue.stringLabelValue("MyStringLabelDiffValue"));
    tester.addEqualityGroup(LabelValue.booleanLabelValue(true), LabelValue.booleanLabelValue(true));
    tester.addEqualityGroup(LabelValue.booleanLabelValue(false));
    tester.addEqualityGroup(LabelValue.longLabelValue(123456L), LabelValue.longLabelValue(123456L));
    tester.addEqualityGroup(LabelValue.longLabelValue(1234567L));
    tester.testEquals();
  }

  @Test
  public void labelValue_ToString() {
    LabelValue label = LabelValue.stringLabelValue("MyStringLabelValue");
    assertThat(label.toString()).contains("string");
    assertThat(label.toString()).contains("MyStringLabelValue");
    label = LabelValue.booleanLabelValue(true);
    assertThat(label.toString()).contains("boolean");
    assertThat(label.toString()).contains("true");
    label = LabelValue.longLabelValue(123456L);
    assertThat(label.toString()).contains("long");
    assertThat(label.toString()).contains("123456");
  }
}
