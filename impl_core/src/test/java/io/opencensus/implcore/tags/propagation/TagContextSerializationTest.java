/*
 * Copyright 2016-17, OpenCensus Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.implcore.tags.propagation;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.base.Charsets;
import com.google.common.collect.Collections2;
import io.opencensus.implcore.internal.VarInt;
import io.opencensus.implcore.tags.TagsComponentImplBase;
import io.opencensus.tags.Tag;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagContextBuilder;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import io.opencensus.tags.Tagger;
import io.opencensus.tags.TagsComponent;
import io.opencensus.tags.propagation.TagContextBinarySerializer;
import io.opencensus.tags.propagation.TagContextSerializationException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for serializing tags with {@link BinarySerializationUtils} and {@link
 * TagContextBinarySerializerImpl}.
 */
@RunWith(JUnit4.class)
public class TagContextSerializationTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  private static final TagKey K1 = TagKey.create("k1");
  private static final TagKey K2 = TagKey.create("k2");
  private static final TagKey K3 = TagKey.create("k3");
  private static final TagKey K4 = TagKey.create("k4");

  private static final TagValue V1 = TagValue.create("v1");
  private static final TagValue V2 = TagValue.create("v2");
  private static final TagValue V3 = TagValue.create("v3");
  private static final TagValue V4 = TagValue.create("v4");

  private static final Tag T1 = Tag.create(K1, V1);
  private static final Tag T2 = Tag.create(K2, V2);
  private static final Tag T3 = Tag.create(K3, V3);
  private static final Tag T4 = Tag.create(K4, V4);

  private final TagsComponent tagsComponent = new TagsComponentImplBase();
  private final TagContextBinarySerializer serializer =
      tagsComponent.getTagPropagationComponent().getBinarySerializer();
  private final Tagger tagger = tagsComponent.getTagger();

  @Test
  public void testSerializeDefault() throws Exception {
    testSerialize();
  }

  @Test
  public void testSerializeWithOneTag() throws Exception {
    testSerialize(T1);
  }

  @Test
  public void testSerializeWithMultipleTags() throws Exception {
    testSerialize(T1, T2, T3, T4);
  }

  @Test
  public void testSerializeTooLargeTagContext() throws TagContextSerializationException {
    TagContextBuilder builder = tagger.emptyBuilder();
    for (int i = 0; i < BinarySerializationUtils.TAGCONTEXT_SERIALIZED_SIZE_LIMIT / 8 - 1; i++) {
      // Each tag will be with format {key : "0123", value : "0123"}, so the length of it is 8.
      String str;
      if (i < 10) {
        str = "000" + i;
      } else if (i < 100) {
        str = "00" + i;
      } else if (i < 1000) {
        str = "0" + i;
      } else {
        str = String.valueOf(i);
      }
      builder.put(TagKey.create(str), TagValue.create(str));
    }
    // The last tag will be of size 9, so the total size of the TagContext (8193) will be one byte
    // more than limit.
    builder.put(TagKey.create("last"), TagValue.create("last1"));

    TagContext tagContext = builder.build();
    thrown.expect(TagContextSerializationException.class);
    thrown.expectMessage("Size of TagContext exceeds the maximum serialized size ");
    serializer.toByteArray(tagContext);
  }

  private void testSerialize(Tag... tags) throws IOException, TagContextSerializationException {
    TagContextBuilder builder = tagger.emptyBuilder();
    for (Tag tag : tags) {
      builder.put(tag.getKey(), tag.getValue());
    }

    byte[] actual = serializer.toByteArray(builder.build());

    Collection<List<Tag>> tagPermutation = Collections2.permutations(Arrays.asList(tags));
    Set<String> possibleOutputs = new HashSet<String>();
    for (List<Tag> list : tagPermutation) {
      ByteArrayOutputStream expected = new ByteArrayOutputStream();
      expected.write(BinarySerializationUtils.VERSION_ID);
      for (Tag tag : list) {
        expected.write(BinarySerializationUtils.TAG_FIELD_ID);
        encodeString(tag.getKey().getName(), expected);
        encodeString(tag.getValue().asString(), expected);
      }
      possibleOutputs.add(new String(expected.toByteArray(), Charsets.UTF_8));
    }

    assertThat(possibleOutputs).contains(new String(actual, Charsets.UTF_8));
  }

  private static void encodeString(String input, ByteArrayOutputStream byteArrayOutputStream)
      throws IOException {
    VarInt.putVarInt(input.length(), byteArrayOutputStream);
    byteArrayOutputStream.write(input.getBytes(Charsets.UTF_8));
  }
}
