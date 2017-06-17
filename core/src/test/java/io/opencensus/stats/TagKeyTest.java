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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

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
    assertThat(TagKey.create(new String(key)).toString())
        .isEqualTo(TagKey.create(new String(truncKey)).toString());
  }

  @Test
  public void testKeyBadChar() {
    String key = "\2ab\3cd";
    assertThat(TagKey.create(key).toString())
        .isEqualTo(StringUtil.UNPRINTABLE_CHAR_SUBSTITUTE + "ab"
                 + StringUtil.UNPRINTABLE_CHAR_SUBSTITUTE + "cd");
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(TagKey.create("foo"), TagKey.create("foo"))
        .addEqualityGroup(TagKey.create("bar"))
        .testEquals();
  }

  @Test
  public void testToString() {
    assertThat(TagKey.create("foo").toString()).isEqualTo("foo");
  }
}
