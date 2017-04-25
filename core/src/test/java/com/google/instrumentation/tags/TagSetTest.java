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
    assertThat(
            TagSet.empty()
                .newContext(
                    TagChange.create(KS1, TagOp.SET, "v1"), TagChange.create(KS1, TagOp.SET, "v2"))
                .getTags())
        .containsExactly(KS1, "v2");
  }

  @Test
  public void testInsert() {
    TagSet ctx = singletonTagSet(KS1, "v1");
    assertThat(ctx.newContext(TagChange.create(KS1, TagOp.INSERT, "v2")).getTags())
        .containsExactly(KS1, "v1");
    assertThat(ctx.newContext(TagChange.create(KS2, TagOp.INSERT, "v2")).getTags())
        .containsExactly(KS1, "v1", KS2, "v2");
  }

  @Test
  public void testSet() {
    TagSet ctx = singletonTagSet(KS1, "v1");
    assertThat(ctx.newContext(TagChange.create(KS1, TagOp.SET, "v2")).getTags())
        .containsExactly(KS1, "v2");
    assertThat(ctx.newContext(TagChange.create(KS2, TagOp.SET, "v2")).getTags())
        .containsExactly(KS1, "v1", KS2, "v2");
  }

  @Test
  public void testUpdate() {
    TagSet ctx = singletonTagSet(KS1, "v1");
    assertThat(ctx.newContext(TagChange.create(KS1, TagOp.UPDATE, "v2")).getTags())
        .containsExactly(KS1, "v2");
    assertThat(ctx.newContext(TagChange.create(KS2, TagOp.UPDATE, "v2")).getTags())
        .containsExactly(KS1, "v1");
  }

  @Test
  public void testClear() {
    TagSet ctx = singletonTagSet(KS1, "v1");
    assertThat(ctx.newContext(TagChange.create(KS1, TagOp.CLEAR, "v2")).getTags()).isEmpty();
    assertThat(ctx.newContext(TagChange.create(KS2, TagOp.CLEAR, "v2")).getTags())
        .containsExactly(KS1, "v1");
  }

  @Test
  public void testNoop() {
    TagSet ctx = singletonTagSet(KS1, "v1");
    assertThat(ctx.newContext(TagChange.create(KS1, TagOp.NOOP, "v2")).getTags())
        .containsExactly(KS1, "v1");
    assertThat(ctx.newContext(TagChange.create(KS2, TagOp.NOOP, "v2")).getTags())
        .containsExactly(KS1, "v1");
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(TagSet.empty(), TagSet.empty())
        .addEqualityGroup(
            TagSet.empty().newContext(TagChange.create(KS1, TagOp.INSERT, "v1")),
            TagSet.empty().newContext(TagChange.create(KS1, TagOp.SET, "v1")),
            TagSet.empty().newContext(
                TagChange.create(KS1, TagOp.SET, "v1"), TagChange.create(KS1, TagOp.SET, "v1")))
        .addEqualityGroup(
            TagSet.empty().newContext(
                TagChange.create(KB, TagOp.SET, true), TagChange.create(KI, TagOp.SET, 2L)),
            TagSet.empty().newContext(
                TagChange.create(KI, TagOp.SET, 2L), TagChange.create(KB, TagOp.SET, true)))
        .addEqualityGroup(TagSet.empty().newContext(TagChange.create(KS1, TagOp.SET, "v2")))
        .addEqualityGroup(TagSet.empty().newContext(TagChange.create(KS2, TagOp.SET, "v1")))
        .testEquals();
  }

  private static <TagValueT> TagSet singletonTagSet(
      TagKey<TagValueT> key, TagValueT value) {
    return TagSet.createInternal(ImmutableMap.<TagKey<?>, Object>of(key, value));
  }
}
