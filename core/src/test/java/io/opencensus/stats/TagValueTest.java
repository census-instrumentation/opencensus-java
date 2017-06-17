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
 * Tests for {@link TagValue}
 */
@RunWith(JUnit4.class)
public final class TagValueTest {
  @Test
  public void testValueMaxLength() {
    char[] value = new char[TagValue.MAX_LENGTH];
    char[] truncValue = new char[TagValue.MAX_LENGTH + 10];
    Arrays.fill(value, 'v');
    Arrays.fill(truncValue, 'v');
    assertThat(TagValue.create(new String(value)).toString())
        .isEqualTo(TagValue.create(new String(truncValue)).toString());
  }

  @Test
  public void testValueBadChar() {
    String value = "\2ab\3cd";
    assertThat(TagValue.create(value).toString())
        .isEqualTo(StringUtil.UNPRINTABLE_CHAR_SUBSTITUTE + "ab"
                 + StringUtil.UNPRINTABLE_CHAR_SUBSTITUTE + "cd");
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(TagValue.create("foo"), TagValue.create("foo"))
        .addEqualityGroup(TagValue.create("bar"))
        .testEquals();
  }

  @Test
  public void testToString() {
    assertThat(TagValue.create("foo").toString()).isEqualTo("foo");
  }
}
