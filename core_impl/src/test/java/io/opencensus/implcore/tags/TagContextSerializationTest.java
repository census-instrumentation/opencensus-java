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

package io.opencensus.implcore.tags;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.Collections2;
import io.opencensus.implcore.internal.VarInt;
import io.opencensus.tags.Tag.TagString;
import io.opencensus.tags.TagContextBinarySerializer;
import io.opencensus.tags.TagContextBuilder;
import io.opencensus.tags.TagContexts;
import io.opencensus.tags.TagKey.TagKeyString;
import io.opencensus.tags.TagValueString;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for serializing tags with {@link SerializationUtils} and {@link
 * TagContextBinarySerializerImpl}.
 */
@RunWith(JUnit4.class)
public class TagContextSerializationTest {

  private static final int VERSION_ID = 0;
  private static final int VALUE_TYPE_STRING = 0;

  private static final TagKeyString K1 = TagKeyString.create("k1");
  private static final TagKeyString K2 = TagKeyString.create("k2");
  private static final TagKeyString K3 = TagKeyString.create("k3");
  private static final TagKeyString K4 = TagKeyString.create("k4");

  private static final TagValueString V1 = TagValueString.create("v1");
  private static final TagValueString V2 = TagValueString.create("v2");
  private static final TagValueString V3 = TagValueString.create("v3");
  private static final TagValueString V4 = TagValueString.create("v4");

  private static final TagString T1 = TagString.create(K1, V1);
  private static final TagString T2 = TagString.create(K2, V2);
  private static final TagString T3 = TagString.create(K3, V3);
  private static final TagString T4 = TagString.create(K4, V4);

  private final TagContextBinarySerializer serializer = new TagContextBinarySerializerImpl();
  private final TagContexts tagContexts = new TagContextsImpl();

  @Test
  public void testSerializeDefault() throws Exception {
    testSerialize();
  }

  @Test
  public void testSerializeWithOneStringTag() throws Exception {
    testSerialize(T1);
  }

  @Test
  public void testSerializeWithMultiStringTags() throws Exception {
    testSerialize(T1, T2, T3, T4);
  }

  private void testSerialize(TagString... tags) throws IOException {
    TagContextBuilder builder = tagContexts.emptyBuilder();
    for (TagString tag : tags) {
      builder.set(tag.getKey(), tag.getValue());
    }

    ByteArrayOutputStream actual = new ByteArrayOutputStream();
    serializer.serialize(builder.build(), actual);

    Collection<List<TagString>> tagPermutation = Collections2.permutations(Arrays.asList(tags));
    Set<String> possibleOutputs = new HashSet<String>();
    for (List<TagString> list : tagPermutation) {
      ByteArrayOutputStream expected = new ByteArrayOutputStream();
      expected.write(VERSION_ID);
      for (TagString tag : list) {
        expected.write(VALUE_TYPE_STRING);
        encodeString(tag.getKey().getName(), expected);
        encodeString(tag.getValue().asString(), expected);
      }
      possibleOutputs.add(expected.toString());
    }

    assertThat(possibleOutputs).contains(actual.toString());
  }

  private static void encodeString(String input, ByteArrayOutputStream byteArrayOutputStream)
      throws IOException {
    VarInt.putVarInt(input.length(), byteArrayOutputStream);
    byteArrayOutputStream.write(input.getBytes("UTF-8"));
  }
}
