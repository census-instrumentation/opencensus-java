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

/** Unit tests for {@link Attributes}. */
@RunWith(JUnit4.class)
public class AttributesTest {
  @Test
  public void putStringAttribute() {
    Attributes attributes =
        Attributes.builder().putStringAttribute("MyStringAttributeKey", "MyStringAttributeValue").build();
    assertThat(attributes.getAll().size()).isEqualTo(1);
    assertThat(attributes.getAll().containsKey("MyStringAttributeKey")).isTrue();
    assertThat(attributes.getAttributeValue("MyStringAttributeKey"))
        .isEqualTo(AttributeValue.stringAttributeValue("MyStringAttributeValue"));
  }

  @Test(expected = NullPointerException.class)
  public void putStringAttribute_NullKey() {
    Attributes.builder().putStringAttribute(null, "MyStringAttributeValue").build();
  }

  @Test(expected = NullPointerException.class)
  public void putStringAttribute_NullValue() {
    Attributes.builder().putStringAttribute("MyStringAttributeKey", null).build();
  }

  @Test
  public void putBooleanAttribute() {
    Attributes attributes = Attributes.builder().putBooleanAttribute("MyBooleanAttributeKey", true).build();
    assertThat(attributes.getAll().size()).isEqualTo(1);
    assertThat(attributes.getAll().containsKey("MyBooleanAttributeKey")).isTrue();
    assertThat(attributes.getAttributeValue("MyBooleanAttributeKey"))
        .isEqualTo(AttributeValue.booleanAttributeValue(true));
  }

  @Test(expected = NullPointerException.class)
  public void putBooleanAttribute_NullKey() {
    Attributes.builder().putBooleanAttribute(null, true).build();
  }

  @Test
  public void putLongAttribute() {
    Attributes attributes = Attributes.builder().putLongAttribute("MyLongAttributeKey", 123L).build();
    assertThat(attributes.getAll().size()).isEqualTo(1);
    assertThat(attributes.getAll().containsKey("MyLongAttributeKey")).isTrue();
    assertThat(attributes.getAttributeValue("MyLongAttributeKey")).isEqualTo(AttributeValue.longAttributeValue(123L));
  }

  @Test(expected = NullPointerException.class)
  public void putLongAttribute_NullKey() {
    Attributes.builder().putLongAttribute(null, 123L).build();
  }

  @Test(expected = NullPointerException.class)
  public void getAttributeValue_NullKey() {
    Attributes.builder()
        .putStringAttribute("MyStringAttributeKey", "MyStringAttributeValue")
        .build()
        .getAttributeValue(null);
  }

  @Test
  public void putMultipleAttributes() {
    Attributes attributes =
        Attributes.builder()
            .putStringAttribute("MyStringAttributeKey", "MyStringAttributeValue")
            .putBooleanAttribute("MyBooleanAttributeKey", true)
            .putLongAttribute("MyLongAttributeKey", 123L)
            .build();
    assertThat(attributes.getAll().size()).isEqualTo(3);
    assertThat(attributes.getAll().containsKey("MyStringAttributeKey")).isTrue();
    assertThat(attributes.getAttributeValue("MyStringAttributeKey"))
        .isEqualTo(AttributeValue.stringAttributeValue("MyStringAttributeValue"));
    assertThat(attributes.getAll().containsKey("MyBooleanAttributeKey")).isTrue();
    assertThat(attributes.getAttributeValue("MyBooleanAttributeKey"))
        .isEqualTo(AttributeValue.booleanAttributeValue(true));
    assertThat(attributes.getAll().containsKey("MyLongAttributeKey")).isTrue();
    assertThat(attributes.getAttributeValue("MyLongAttributeKey")).isEqualTo(AttributeValue.longAttributeValue(123L));
  }

  @Test
  public void putMultipleAttribute_SameKey() {
    Attributes attributes =
        Attributes.builder()
            .putStringAttribute("MyAttributeKey", "MyStringAttributeValue")
            .putLongAttribute("MyAttributeKey", 123L)
            .build();
    assertThat(attributes.getAll().size()).isEqualTo(1);
    assertThat(attributes.getAll().containsKey("MyAttributeKey")).isTrue();
    assertThat(attributes.getAttributeValue("MyAttributeKey")).isEqualTo(AttributeValue.longAttributeValue(123L));
  }

  @Test
  public void putMultipleAttributes_SameType() {
    Attributes attributes =
        Attributes.builder()
            .putLongAttribute("MyLongAttributeKey1", 1L)
            .putLongAttribute("MyLongAttributeKey12", 12L)
            .putLongAttribute("MyLongAttributeKey123", 123L)
            .build();
    assertThat(attributes.getAll().size()).isEqualTo(3);
    assertThat(attributes.getAll().containsKey("MyLongAttributeKey1")).isTrue();
    assertThat(attributes.getAttributeValue("MyLongAttributeKey1")).isEqualTo(AttributeValue.longAttributeValue(1L));
    assertThat(attributes.getAll().containsKey("MyLongAttributeKey12")).isTrue();
    assertThat(attributes.getAttributeValue("MyLongAttributeKey12")).isEqualTo(AttributeValue.longAttributeValue(12L));
    assertThat(attributes.getAll().containsKey("MyLongAttributeKey123")).isTrue();
    assertThat(attributes.getAttributeValue("MyLongAttributeKey123"))
        .isEqualTo(AttributeValue.longAttributeValue(123L));
  }

  @Test
  public void attributes_ToString() {
    Attributes attributes =
        Attributes.builder().putStringAttribute("MyStringAttributeKey", "MyStringAttributeValue").build();
    assertThat(attributes.toString()).contains("MyStringAttributeKey");
    assertThat(attributes.toString())
        .contains(AttributeValue.stringAttributeValue("MyStringAttributeValue").toString());
    attributes = Attributes.builder().putBooleanAttribute("MyBooleanAttributeKey", true).build();
    assertThat(attributes.toString()).contains("MyBooleanAttributeKey");
    assertThat(attributes.toString()).contains(AttributeValue.booleanAttributeValue(true).toString());
    attributes = Attributes.builder().putLongAttribute("MyLongAttributeKey", 123L).build();
    assertThat(attributes.toString()).contains("MyLongAttributeKey");
    assertThat(attributes.toString()).contains(AttributeValue.longAttributeValue(123L).toString());
  }
}
