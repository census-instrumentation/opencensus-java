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

package com.google.instrumentation.tags;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import com.google.instrumentation.internal.StringUtil;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link TagChange}. */
@RunWith(JUnit4.class)
public final class TagChangeTest {

  @Test
  public void testGetters() {
    TagChange tc = TagChange.create(TagKey.createString("K"), TagOp.UPDATE, "V");
    assertThat(tc.getKey()).isEqualTo(TagKey.createString("K"));
    assertThat(tc.getOp()).isEqualTo(TagOp.UPDATE);
    assertThat(tc.getValue()).isEqualTo("V");
  }

  @Test
  public void testValueMaxLength() {
    char[] value = new char[TagChange.MAX_STRING_LENGTH];
    char[] truncValue = new char[TagChange.MAX_STRING_LENGTH + 10];
    Arrays.fill(value, 'v');
    Arrays.fill(truncValue, 'v');
    TagKey<String> key = TagKey.createString("K");
    assertThat(TagChange.create(key, TagOp.INSERT, new String(value)).toString())
        .isEqualTo(TagChange.create(key, TagOp.INSERT, new String(truncValue)).toString());
  }

  @Test
  public void testValueBadChar() {
    String value = "\2ab\3cd";
    assertThat(TagChange.create(TagKey.createString("K"), TagOp.NOOP, value).getValue())
        .isEqualTo(StringUtil.UNPRINTABLE_CHAR_SUBSTITUTE + "ab"
                 + StringUtil.UNPRINTABLE_CHAR_SUBSTITUTE + "cd");
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            TagChange.create(TagKey.createInt("k"), TagOp.CLEAR, 1L),
            TagChange.create(TagKey.createInt("k"), TagOp.CLEAR, 1L))
        .addEqualityGroup(TagChange.create(TagKey.createInt("key"), TagOp.CLEAR, 1L))
        .addEqualityGroup(TagChange.create(TagKey.createInt("k"), TagOp.UPDATE, 1L))
        .addEqualityGroup(TagChange.create(TagKey.createInt("k"), TagOp.CLEAR, 2L))
        .testEquals();
  }
}
