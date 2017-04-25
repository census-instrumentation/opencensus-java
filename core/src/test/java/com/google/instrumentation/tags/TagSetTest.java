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

import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import com.google.instrumentation.internal.StringUtil;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link TagSet}. */
@RunWith(JUnit4.class)
public class TagSetTest {

  private static final TagKey<String> KS1 = TagKey.createString("k1");
  private static final TagKey<String> KS2 = TagKey.createString("k2");
  private static final TagKey<Long> KI = TagKey.createInt("k2");
  private static final TagKey<Boolean> KB = TagKey.createBoolean("k3");

  @Test
  public void testEmpty() {
    assertThat(TagSet.empty().getTags()).isEmpty();
  }

  @Test
  public void applyTagChangesInOrder() {
    assertThat(TagSet.builder().set(KS1, "v1").set(KS1, "v2").build().getTags())
        .containsExactly(KS1, "v2");
  }

  @Test
  public void testInsert() {
    TagSet ctx = singletonTagSet(KS1, "v1");
    assertThat(ctx.toBuilder().insert(KS1, "v2").build().getTags()).containsExactly(KS1, "v1");
    assertThat(ctx.toBuilder().insert(KS2, "v2").build().getTags())
        .containsExactly(KS1, "v1", KS2, "v2");
  }

  @Test
  public void testSet() {
    TagSet ctx = singletonTagSet(KS1, "v1");
    assertThat(ctx.toBuilder().set(KS1, "v2").build().getTags()).containsExactly(KS1, "v2");
    assertThat(ctx.toBuilder().set(KS2, "v2").build().getTags())
        .containsExactly(KS1, "v1", KS2, "v2");
  }

  @Test
  public void testUpdate() {
    TagSet ctx = singletonTagSet(KS1, "v1");
    assertThat(ctx.toBuilder().update(KS1, "v2").build().getTags()).containsExactly(KS1, "v2");
    assertThat(ctx.toBuilder().update(KS2, "v2").build().getTags()).containsExactly(KS1, "v1");
  }

  @Test
  public void testClear() {
    TagSet ctx = singletonTagSet(KS1, "v1");
    assertThat(ctx.toBuilder().clear(KS1).build().getTags()).isEmpty();
    assertThat(ctx.toBuilder().clear(KS2).build().getTags()).containsExactly(KS1, "v1");
  }

  @Test
  public void testValueMaxLength() {
    char[] chars = new char[TagSet.MAX_STRING_LENGTH];
    char[] truncChars = new char[TagSet.MAX_STRING_LENGTH + 10];
    Arrays.fill(chars, 'v');
    Arrays.fill(truncChars, 'v');
    String value = new String(chars);
    String truncValue = new String(chars);
    TagKey<String> key1 = TagKey.createString("K1");
    TagKey<String> key2 = TagKey.createString("K2");
    TagKey<String> key3 = TagKey.createString("K3");
    assertThat(
            TagSet.builder()
                .insert(key1, value)
                .set(key2, value)
                .set(key3, "")  // allow next line to update existing value
                .update(key3, value)
                .build()
                .getTags())
        .containsExactly(key1, truncValue, key2, truncValue, key3, truncValue);
  }

  @Test
  public void testValueBadChar() {
    String value = "\2ab\3cd";
    TagKey<String> key1 = TagKey.createString("K1");
    TagKey<String> key2 = TagKey.createString("K2");
    TagKey<String> key3 = TagKey.createString("K3");
    String expected =
        StringUtil.UNPRINTABLE_CHAR_SUBSTITUTE
            + "ab"
            + StringUtil.UNPRINTABLE_CHAR_SUBSTITUTE
            + "cd";
    assertThat(
            TagSet.builder()
                .insert(key1, value)
                .set(key2, value)
                .set(key3, "")  // allow next line to update existing value
                .update(key3, value)
                .build()
                .getTags())
        .containsExactly(key1, expected, key2, expected, key3, expected);
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(TagSet.empty(), TagSet.empty())
        .addEqualityGroup(
            TagSet.builder().insert(KS1, "v1").build(),
            TagSet.builder().set(KS1, "v1").build(),
            TagSet.builder().set(KS1, "v1").set(KS1, "v1").build())
        .addEqualityGroup(
            TagSet.builder().set(KB, true).set(KI, 2L).build(),
            TagSet.builder().set(KI, 2L).set(KB, true).build())
        .addEqualityGroup(TagSet.builder().set(KS1, "v2"))
        .addEqualityGroup(TagSet.builder().set(KS2, "v1"))
        .testEquals();
  }

  private static <TagValueT> TagSet singletonTagSet(TagKey<TagValueT> key, TagValueT value) {
    return TagSet.createInternal(ImmutableMap.<TagKey<?>, Object>of(key, value));
  }
}
