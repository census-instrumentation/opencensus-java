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

import com.google.common.testing.EqualsTester;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link MetricName}
 */
@RunWith(JUnit4.class)
public final class MetricNameTest {
  @Test
  public void testNameMaxLength() {
    char[] name = new char[MetricName.MAX_LENGTH];
    char[] truncName = new char[MetricName.MAX_LENGTH + 10];
    Arrays.fill(name, 'n');
    Arrays.fill(truncName, 'n');
    assertThat(new MetricName(new String(name)).toString())
        .isEqualTo(new MetricName(new String(truncName)).toString());
  }

  @Test
  public void testNameBadChar() {
    assertThat(new MetricName("\2ab\3cd").toString())
        .isEqualTo(StringUtil.UNPRINTABLE_CHAR_SUBSTITUTE + "ab"
                 + StringUtil.UNPRINTABLE_CHAR_SUBSTITUTE + "cd");
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new MetricName("foo"), new MetricName("foo"))
        .addEqualityGroup(new MetricName("bar"))
        .testEquals();
  }

  @Test
  public void testToString() {
    assertThat(new MetricName("foo").toString()).isEqualTo("foo");
    assertThat(new MetricName("bar").toString()).isEqualTo("bar");
  }
}
