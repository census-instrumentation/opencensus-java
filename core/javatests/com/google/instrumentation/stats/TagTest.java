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

package com.google.instrumentation.stats;

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
  public void testTagTest() {
    Tag tag = Tag.create(K1, V1);
    assertThat(tag.getKey()).isEqualTo(K1);
    assertThat(tag.getValue()).isEqualTo(V1);
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(Tag.create(K1, V1), Tag.create(K1, V1))
        .addEqualityGroup(Tag.create(K2, V1))
        .addEqualityGroup(Tag.create(K1, V2))
        .testEquals();
  }

  @Test
  public void testHashCode() {
    new EqualsTester()
        .addEqualityGroup(Tag.create(K1, V1).hashCode(), Tag.create(K1, V1).hashCode())
        .addEqualityGroup(Tag.create(K2, V1).hashCode())
        .addEqualityGroup(Tag.create(K1, V2).hashCode())
        .testEquals();
  }

  @Test
  public void testToString() {
    assertThat(Tag.create(K1, V1).toString()).isEqualTo("Tag<k1,v1>");
    assertThat(Tag.create(K2, V1).toString()).isEqualTo("Tag<k2,v1>");
    assertThat(Tag.create(K1, V2).toString()).isEqualTo("Tag<k1,v2>");
  }

  private static final TagKey K1 = TagKey.create("k1");
  private static final TagKey K2 = TagKey.create("k2");
  private static final TagValue V1 = TagValue.create("v1");
  private static final TagValue V2 = TagValue.create("v2");
}
