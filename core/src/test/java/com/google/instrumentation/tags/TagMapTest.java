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
import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link TagMap}. */
// TODO(sebright): Add more tests once the API is finalized.
@RunWith(JUnit4.class)
public class TagMapTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  private static final TagKey<String> KS1 = TagKey.createString("k1");
  private static final TagKey<String> KS2 = TagKey.createString("k2");

  @Test
  public void applyBuilderOperationsInOrder() {
    assertThat(newBuilder().set(KS1, "v1").set(KS1, "v2").build().getTags())
        .containsExactly(KS1, "v2");
  }

  @Test
  public void allowMutlipleKeysWithSameNameButDifferentTypes() {
    TagKey<String> stringKey = TagKey.createString("key");
    TagKey<Long> intKey = TagKey.createInt("key");
    TagKey<Boolean> boolKey = TagKey.createBool("key");
    assertThat(
            newBuilder()
                .set(stringKey, "value")
                .set(intKey, 123)
                .set(boolKey, true)
                .build()
                .getTags())
        .containsExactly(stringKey, "value", intKey, 123L, boolKey, true);
  }

  @Test
  public void testSet() {
    TagMap tags = singletonTagMap(KS1, "v1");
    assertThat(tags.toBuilder().set(KS1, "v2").build().getTags()).containsExactly(KS1, "v2");
    assertThat(tags.toBuilder().set(KS2, "v2").build().getTags())
        .containsExactly(KS1, "v1", KS2, "v2");
  }

  @Test
  public void testClear() {
    TagMap tags = singletonTagMap(KS1, "v1");
    assertThat(tags.toBuilder().clear(KS1).build().getTags()).isEmpty();
    assertThat(tags.toBuilder().clear(KS2).build().getTags()).containsExactly(KS1, "v1");
  }

  @Test
  public void allowStringTagValueWithMaxLength() {
    char[] chars = new char[TagMap.MAX_STRING_LENGTH];
    Arrays.fill(chars, 'v');
    String value = new String(chars);
    TagKey<String> key = TagKey.createString("K");
    newBuilder().set(key, value);
  }

  @Test
  public void disallowStringTagValueOverMaxLength() {
    char[] chars = new char[TagMap.MAX_STRING_LENGTH + 1];
    Arrays.fill(chars, 'v');
    String value = new String(chars);
    TagKey<String> key = TagKey.createString("K");
    thrown.expect(IllegalArgumentException.class);
    newBuilder().set(key, value);
  }

  @Test
  public void disallowStringTagValueWithUnprintableChars() {
    String value = "\2ab\3cd";
    TagKey<String> key = TagKey.createString("K");
    thrown.expect(IllegalArgumentException.class);
    newBuilder().set(key, value);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private final TagKey<Long> badIntKey = (TagKey) TagKey.createString("Key");

  @Test(expected = IllegalArgumentException.class)
  public void disallowSettingWrongTypeOfKey() {
    newBuilder().set(badIntKey, 123);
  }

  private static TagMap.Builder newBuilder() {
    return new TagMap.Builder();
  }

  private static <TagValueT> TagMap singletonTagMap(TagKey<TagValueT> key, TagValueT value) {
    return new TagMap(ImmutableMap.<TagKey<?>, Object>of(key, value));
  }
}
