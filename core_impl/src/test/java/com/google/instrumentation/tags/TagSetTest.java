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
import com.google.instrumentation.internal.StringUtil;
import com.google.instrumentation.internal.logging.TestLogger;
import com.google.instrumentation.internal.logging.TestLogger.LogRecord;
import java.util.Arrays;
import java.util.logging.Level;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link TagSet}. */
// TODO(sebright): Add more tests once the API is finalized.
@RunWith(JUnit4.class)
public class TagSetTest {

  private static final TagKey<String> KS1 = TagKey.createString("k1");
  private static final TagKey<String> KS2 = TagKey.createString("k2");
  private static final TagKey<Long> KI = TagKey.createInt("k2");
  private static final TagKey<Boolean> KB = TagKey.createBoolean("k3");

  private final TestLogger tagSetLogger = new TestLogger();

  @Test
  public void applyTagChangesInOrder() {
    assertThat(newBuilder().set(KS1, "v1").set(KS1, "v2").build().getTags())
        .containsExactly(KS1, "v2");
  }

  @Test
  public void testInsert() {
    TagSetImpl tags = singletonTagSet(KS1, "v1");
    assertThat(tags.toBuilder().insert(KS2, "v2").build().getTags())
        .containsExactly(KS1, "v1", KS2, "v2");
    assertThat(tagSetLogger.getMessages()).isEmpty();
  }

  @Test
  public void testInsertExistingTag() {
    TagSetImpl tags = singletonTagSet(KS1, "v1");
    assertThat(tagSetLogger.getMessages()).isEmpty();
    assertThat(tags.toBuilder().insert(KS1, "v2").build().getTags()).containsExactly(KS1, "v1");
    assertThat(tagSetLogger.getMessages())
        .containsExactly(LogRecord.create(Level.WARNING, "Tag key already exists: " + KS1));
  }

  @Test
  public void testSet() {
    TagSetImpl tags = singletonTagSet(KS1, "v1");
    assertThat(tags.toBuilder().set(KS1, "v2").build().getTags()).containsExactly(KS1, "v2");
    assertThat(tags.toBuilder().set(KS2, "v2").build().getTags())
        .containsExactly(KS1, "v1", KS2, "v2");
    assertThat(tagSetLogger.getMessages()).isEmpty();
  }

  @Test
  public void testUpdate() {
    TagSetImpl tags = singletonTagSet(KS1, "v1");
    assertThat(tags.toBuilder().update(KS1, "v2").build().getTags()).containsExactly(KS1, "v2");
    assertThat(tags.toBuilder().update(KS2, "v2").build().getTags()).containsExactly(KS1, "v1");
    assertThat(tagSetLogger.getMessages()).isEmpty();
  }

  @Test
  public void testClear() {
    TagSetImpl tags = singletonTagSet(KS1, "v1");
    assertThat(tags.toBuilder().clear(KS1).build().getTags()).isEmpty();
    assertThat(tags.toBuilder().clear(KS2).build().getTags()).containsExactly(KS1, "v1");
    assertThat(tagSetLogger.getMessages()).isEmpty();
  }

  @Test
  public void testGenericGetTag() {
    TagSetImpl tags = newBuilder().set(KS1, "my string").set(KB, true).set(KI, 100).build();
    assertThat(tags.getTagValue(KS1)).isEqualTo("my string");
    assertThat(tags.getTagValue(KB)).isEqualTo(true);
    assertThat(tags.getTagValue(KI)).isEqualTo(100);
    assertThat(tags.getTagValue(TagKey.createString("unknown"))).isNull();
  }

  @Test
  public void testSpecializedGetTag() {
    TagSetImpl tags = newBuilder().set(KS1, "my string").set(KB, true).set(KI, 100).build();
    assertThat(tags.getStringTagValue(KS1)).isEqualTo("my string");
    assertThat(tags.getTagValue(TagKey.createString("unknown"))).isNull();
    assertThat(tags.getBooleanTagValue(KB, false)).isEqualTo(true);
    assertThat(tags.getBooleanTagValue(TagKey.createBoolean("unknown"), false)).isFalse();
    assertThat(tags.getIntTagValue(KI, 0)).isEqualTo(100);
    assertThat(tags.getIntTagValue(TagKey.createInt("unknown"), -1)).isEqualTo(-1);
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
            newBuilder()
                .insert(key1, value)
                .set(key2, value)
                .set(key3, "") // allow next line to update existing value
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
            newBuilder()
                .insert(key1, value)
                .set(key2, value)
                .set(key3, "") // allow next line to update existing value
                .update(key3, value)
                .build()
                .getTags())
        .containsExactly(key1, expected, key2, expected, key3, expected);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  private final TagKey<Long> badIntKey = (TagKey) TagKey.createString("Key");

  @Test(expected = IllegalArgumentException.class)
  public void disallowInsertingWrongTypeOfKey() {
    newBuilder().insert(badIntKey, 123);
  }

  @Test(expected = IllegalArgumentException.class)
  public void disallowSettingWrongTypeOfKey() {
    newBuilder().set(badIntKey, 123);
  }

  @Test(expected = IllegalArgumentException.class)
  public void disallowUpdatingWrongTypeOfKey() {
    newBuilder().update(badIntKey, 123);
  }

  // This is only allowed because we cannot prevent it.
  @Test
  public void allowCallingGetWithWrongTypeOfKey() {
    TagSet tags = newBuilder().set(TagKey.createString("Key"), "string").build();
    Object value = tags.getTagValue(badIntKey);
    assertThat(value).isEqualTo("string");
  }

  private TagSetImpl.Builder newBuilder() {
    return new TagSetImpl.Builder(tagSetLogger);
  }

  private <TagValueT> TagSetImpl singletonTagSet(TagKey<TagValueT> key, TagValueT value) {
    return new TagSetImpl(tagSetLogger, ImmutableMap.<TagKey<?>, Object>of(key, value));
  }
}
