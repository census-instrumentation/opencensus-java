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


package com.google.census;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;

/**
 * Tests for {@link TagKey}
 */
@RunWith(JUnit4.class)
public final class TagKeyTest {
  @Test
  public void testKeyMaxLength() {
    char[] key = new char[TagKey.MAX_LENGTH];
    char[] truncKey = new char[TagKey.MAX_LENGTH + 10];
    Arrays.fill(key, 'k');
    Arrays.fill(truncKey, 'k');
    assertThat(new TagKey(new String(key)).toString())
        .isEqualTo(new TagKey(new String(truncKey)).toString());
  }

  @Test
  public void testKeyBadChar() {
    String key = "\2ab\3cd";
    assertThat(new TagKey(key).toString())
        .isEqualTo(Tag.UNPRINTABLE_CHAR_SUBSTITUTE + "ab" + Tag.UNPRINTABLE_CHAR_SUBSTITUTE + "cd");
  }

  @Test
  public void testEquals() {
    assertThat(new TagKey("foo").equals(new TagKey("foo"))).isTrue();
    assertThat(new TagKey("foo").equals(new TagKey("bar"))).isFalse();
  }

  @Test
  public void testHashCode() {
    assertThat(new TagKey("foo").hashCode()).isEqualTo(new TagKey("foo").hashCode());
    assertThat(new TagKey("foo").hashCode()).isNotEqualTo(new TagKey("bar").hashCode());
  }

  @Test
  public void testToString() {
    assertThat(new TagKey("foo").toString()).isEqualTo("foo");
  }
}
