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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Labels}. */
@RunWith(JUnit4.class)
public class LabelsTest {
  @Test
  public void putStringLabel() {
    Labels labels =
        Labels.builder().putStringLabel("MyStringLabelKey", "MyStringLabelValue").build();
    assertThat(labels.getAll().size()).isEqualTo(1);
    assertThat(labels.getAll().containsKey("MyStringLabelKey")).isTrue();
    assertThat(labels.getLabelValue("MyStringLabelKey"))
        .isEqualTo(LabelValue.stringLabelValue("MyStringLabelValue"));
  }

  @Test(expected = NullPointerException.class)
  public void putStringLabel_NullKey() {
    Labels.builder().putStringLabel(null, "MyStringLabelValue").build();
  }

  @Test(expected = NullPointerException.class)
  public void putStringLabel_NullValue() {
    Labels.builder().putStringLabel("MyStringLabelKey", null).build();
  }

  @Test
  public void putBooleanLabel() {
    Labels labels = Labels.builder().putBooleanLabel("MyBooleanLabelKey", true).build();
    assertThat(labels.getAll().size()).isEqualTo(1);
    assertThat(labels.getAll().containsKey("MyBooleanLabelKey")).isTrue();
    assertThat(labels.getLabelValue("MyBooleanLabelKey"))
        .isEqualTo(LabelValue.booleanLabelValue(true));
  }

  @Test(expected = NullPointerException.class)
  public void putBooleanLabel_NullKey() {
    Labels.builder().putBooleanLabel(null, true).build();
  }

  @Test
  public void putLongLabel() {
    Labels labels = Labels.builder().putLongLabel("MyLongLabelKey", 123L).build();
    assertThat(labels.getAll().size()).isEqualTo(1);
    assertThat(labels.getAll().containsKey("MyLongLabelKey")).isTrue();
    assertThat(labels.getLabelValue("MyLongLabelKey")).isEqualTo(LabelValue.longLabelValue(123L));
  }

  @Test(expected = NullPointerException.class)
  public void putLongLabel_NullKey() {
    Labels.builder().putLongLabel(null, 123L).build();
  }

  @Test(expected = NullPointerException.class)
  public void getLabelValue_NullKey() {
    Labels.builder()
        .putStringLabel("MyStringLabelKey", "MyStringLabelValue")
        .build()
        .getLabelValue(null);
  }

  @Test
  public void putMultipleLabels() {
    Labels labels =
        Labels.builder()
            .putStringLabel("MyStringLabelKey", "MyStringLabelValue")
            .putBooleanLabel("MyBooleanLabelKey", true)
            .putLongLabel("MyLongLabelKey", 123L)
            .build();
    assertThat(labels.getAll().size()).isEqualTo(3);
    assertThat(labels.getAll().containsKey("MyStringLabelKey")).isTrue();
    assertThat(labels.getLabelValue("MyStringLabelKey"))
        .isEqualTo(LabelValue.stringLabelValue("MyStringLabelValue"));
    assertThat(labels.getAll().containsKey("MyBooleanLabelKey")).isTrue();
    assertThat(labels.getLabelValue("MyBooleanLabelKey"))
        .isEqualTo(LabelValue.booleanLabelValue(true));
    assertThat(labels.getAll().containsKey("MyLongLabelKey")).isTrue();
    assertThat(labels.getLabelValue("MyLongLabelKey")).isEqualTo(LabelValue.longLabelValue(123L));
  }

  @Test
  public void putMultipleLabel_SameKey() {
    Labels labels =
        Labels.builder()
            .putStringLabel("MyLabelKey", "MyStringLabelValue")
            .putLongLabel("MyLabelKey", 123L)
            .build();
    assertThat(labels.getAll().size()).isEqualTo(1);
    assertThat(labels.getAll().containsKey("MyLabelKey")).isTrue();
    assertThat(labels.getLabelValue("MyLabelKey")).isEqualTo(LabelValue.longLabelValue(123L));
  }

  @Test
  public void putMultipleLabels_SameType() {
    Labels labels =
        Labels.builder()
            .putLongLabel("MyLongLabelKey1", 1L)
            .putLongLabel("MyLongLabelKey12", 12L)
            .putLongLabel("MyLongLabelKey123", 123L)
            .build();
    assertThat(labels.getAll().size()).isEqualTo(3);
    assertThat(labels.getAll().containsKey("MyLongLabelKey1")).isTrue();
    assertThat(labels.getLabelValue("MyLongLabelKey1")).isEqualTo(LabelValue.longLabelValue(1L));
    assertThat(labels.getAll().containsKey("MyLongLabelKey12")).isTrue();
    assertThat(labels.getLabelValue("MyLongLabelKey12")).isEqualTo(LabelValue.longLabelValue(12L));
    assertThat(labels.getAll().containsKey("MyLongLabelKey123")).isTrue();
    assertThat(labels.getLabelValue("MyLongLabelKey123"))
        .isEqualTo(LabelValue.longLabelValue(123L));
  }

  @Test
  public void labels_ToString() {
    Labels labels =
        Labels.builder().putStringLabel("MyStringLabelKey", "MyStringLabelValue").build();
    assertThat(labels.toString()).contains("MyStringLabelKey");
    assertThat(labels.toString())
        .contains(LabelValue.stringLabelValue("MyStringLabelValue").toString());
    labels = Labels.builder().putBooleanLabel("MyBooleanLabelKey", true).build();
    assertThat(labels.toString()).contains("MyBooleanLabelKey");
    assertThat(labels.toString()).contains(LabelValue.booleanLabelValue(true).toString());
    labels = Labels.builder().putLongLabel("MyLongLabelKey", 123L).build();
    assertThat(labels.toString()).contains("MyLongLabelKey");
    assertThat(labels.toString()).contains(LabelValue.longLabelValue(123L).toString());
  }
}
