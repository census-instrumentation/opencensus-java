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

/** Tests for {@link TagKey} */
@RunWith(JUnit4.class)
public final class TagKeyTest {

  @Test
  public void testGetName() {
    assertThat(TagKey.createString("foo").getName()).isEqualTo("foo");
  }

  @Test
  public void testKeyMaxLength() {
    char[] key = new char[TagKey.MAX_LENGTH];
    char[] truncKey = new char[TagKey.MAX_LENGTH + 10];
    Arrays.fill(key, 'k');
    Arrays.fill(truncKey, 'k');
    assertThat(TagKey.createString(new String(truncKey)).getName()).isEqualTo(new String(key));
  }

  @Test
  public void testKeyBadChar() {
    String key = "\2ab\3cd";
    assertThat(TagKey.createString(key).getName())
        .isEqualTo(
            StringUtil.UNPRINTABLE_CHAR_SUBSTITUTE
                + "ab"
                + StringUtil.UNPRINTABLE_CHAR_SUBSTITUTE
                + "cd");
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(TagKey.createString("foo"), TagKey.createString("foo"))
        .addEqualityGroup(TagKey.createInt("foo"))
        .addEqualityGroup(TagKey.createBoolean("foo"))
        .addEqualityGroup(TagKey.createString("bar"))
        .testEquals();
  }
}
