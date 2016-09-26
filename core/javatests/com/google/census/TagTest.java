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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link Tag}
 */
@RunWith(JUnit4.class)
public final class TagTest {

  @Test
  public void testGetKey() {
    assertThat(new Tag(KEY, VAL).getKey()).isEqualTo("key");
    assertThat(new Tag("key", "val").getKey()).isEqualTo("key");
  }

  @Test
  public void testGetValue() {
    assertThat(new Tag(KEY, VAL).getValue()).isEqualTo("val");
    assertThat(new Tag("key", "val").getValue()).isEqualTo("val");
  }

  @Test
  public void testSetValue() {
    boolean fail = true;
    try {
      new Tag("key", "val").setValue("val1");
      fail = false;
    } catch (UnsupportedOperationException expected) {
    }
    assertThat(fail).isTrue();
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new Tag("key", "val"), new Tag(KEY, VAL))
        .addEqualityGroup(new Tag("key1", "val"))
        .addEqualityGroup(new Tag("key", "val1"))
        .addEqualityGroup(new Tag("key1", "val1"))
        .addEqualityGroup("foo")
        .testEquals();
  }

  private static final TagKey KEY = new TagKey("key");
  private static final TagValue VAL = new TagValue("val");
}
