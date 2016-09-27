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

import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link StringUtil}.
 */
@RunWith(JUnit4.class)
public final class StringUtilTest {
  @Test
  public void testMaxLength() {
    char[] string = new char[StringUtil.MAX_LENGTH];
    char[] truncString = new char[StringUtil.MAX_LENGTH + 10];
    Arrays.fill(string, 'v');
    Arrays.fill(truncString, 'v');
    assertThat(StringUtil.sanitize(new String(truncString))).isEqualTo(new String(string));
  }

  @Test
  public void testBadChar() {
    String string = "\2ab\3cd";
    assertThat(StringUtil.sanitize(string).toString()).isEqualTo("_ab_cd");
  }

  @Test(expected=AssertionError.class)
  public void testConstructor() {
    new StringUtil();
  }
}
