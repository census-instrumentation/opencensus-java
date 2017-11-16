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
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.opencensus.implcore.internal.VarInt;
import io.opencensus.implcore.tags.TagsComponentImplBase;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import io.opencensus.tags.Tagger;
import io.opencensus.tags.TagsComponent;
import io.opencensus.tags.propagation.TagContextBinarySerializer;
import io.opencensus.tags.propagation.TagContextDeserializationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for deserializing tags with {@link SerializationUtils} and {@link
 * TagContextBinarySerializerImpl}.
 */
@RunWith(JUnit4.class)
public class TagContextDeserializationTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  private final TagsComponent tagsComponent = new TagsComponentImplBase();
  private final TagContextBinarySerializer serializer =
      tagsComponent.getTagPropagationComponent().getBinarySerializer();
  private final Tagger tagger = tagsComponent.getTagger();

  @Test
  public void testConstants() {
    // Refer to the JavaDoc on SerializationUtils for the definitions on these constants.
    assertThat(SerializationUtils.VERSION_ID).isEqualTo(0);
    assertThat(SerializationUtils.TAG_FIELD_ID).isEqualTo(0);
    assertThat(SerializationUtils.TAGCONTEXT_SERIALIZED_SIZE_LIMIT).isEqualTo(8192);
  }

  @Test
  public void testDeserializeNoTags() throws TagContextDeserializationException {
    TagContext expected = tagger.empty();
    TagContext actual =
        serializer.fromByteArray(
            new byte[] {SerializationUtils.VERSION_ID}); // One byte that represents Version ID.
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testDeserializeEmptyByteArrayThrowException()
      throws TagContextDeserializationException {
    thrown.expect(TagContextDeserializationException.class);
    thrown.expectMessage("Input byte[] can not be empty.");
    serializer.fromByteArray(new byte[0]);
  }

  @Test
  public void testDeserializeTooLargeByteArrayThrowException()
      throws TagContextDeserializationException {
    thrown.expect(TagContextDeserializationException.class);
    thrown.expectMessage("Size of input byte[] exceeds the maximum serialized size ");
    serializer.fromByteArray(new byte[SerializationUtils.TAGCONTEXT_SERIALIZED_SIZE_LIMIT + 1]);
  }

  @Test
  public void testDeserializeInvalidTagKey() throws TagContextDeserializationException {
    ByteArrayDataOutput output = ByteStreams.newDataOutput();
    output.write(SerializationUtils.VERSION_ID);

    // Encode an invalid tag key and a valid tag value:
    encodeTagToOutput("\2key", "value", output);
    final byte[] bytes = output.toByteArray();

    thrown.expect(TagContextDeserializationException.class);
    thrown.expectMessage("Invalid tag key: \2key");
    serializer.fromByteArray(bytes);
  }

  @Test
  public void testDeserializeInvalidTagValue() throws TagContextDeserializationException {
    ByteArrayDataOutput output = ByteStreams.newDataOutput();
    output.write(SerializationUtils.VERSION_ID);

    // Encode a valid tag key and an invalid tag value:
    encodeTagToOutput("my key", "val\3", output);
    final byte[] bytes = output.toByteArray();

    thrown.expect(TagContextDeserializationException.class);
    thrown.expectMessage("Invalid tag value for key TagKey{name=my key}: val\3");
    serializer.fromByteArray(bytes);
  }

  @Test
  public void testDeserializeOneTag() throws TagContextDeserializationException {
    ByteArrayDataOutput output = ByteStreams.newDataOutput();
    output.write(SerializationUtils.VERSION_ID);
    encodeTagToOutput("Key", "Value", output);
    TagContext expected =
        tagger.emptyBuilder().put(TagKey.create("Key"), TagValue.create("Value")).build();
    assertThat(serializer.fromByteArray(output.toByteArray())).isEqualTo(expected);
  }

  @Test
  public void testDeserializeMultipleTags() throws TagContextDeserializationException {
    ByteArrayDataOutput output = ByteStreams.newDataOutput();
    output.write(SerializationUtils.VERSION_ID);
    encodeTagToOutput("Key1", "Value1", output);
    encodeTagToOutput("Key2", "Value2", output);
    TagContext expected =
        tagger
            .emptyBuilder()
            .put(TagKey.create("Key1"), TagValue.create("Value1"))
            .put(TagKey.create("Key2"), TagValue.create("Value2"))
            .build();
    assertThat(serializer.fromByteArray(output.toByteArray())).isEqualTo(expected);
  }

  @Test
  public void stopParsingAtUnknownField() throws TagContextDeserializationException {
    ByteArrayDataOutput output = ByteStreams.newDataOutput();
    output.write(SerializationUtils.VERSION_ID);
    encodeTagToOutput("Key1", "Value1", output);
    encodeTagToOutput("Key2", "Value2", output);

    // Write unknown field ID 1.
    output.write(1);
    output.write(new byte[] {1, 2, 3, 4});

    encodeTagToOutput("Key3", "Value3", output);

    // key 3 should not be included
    TagContext expected =
        tagger
            .emptyBuilder()
            .put(TagKey.create("Key1"), TagValue.create("Value1"))
            .put(TagKey.create("Key2"), TagValue.create("Value2"))
            .build();
    assertThat(serializer.fromByteArray(output.toByteArray())).isEqualTo(expected);
  }

  @Test
  public void stopParsingAtUnknownTagAtStart() throws TagContextDeserializationException {
    ByteArrayDataOutput output = ByteStreams.newDataOutput();
    output.write(SerializationUtils.VERSION_ID);

    // Write unknown field ID 1.
    output.write(1);
    output.write(new byte[] {1, 2, 3, 4});

    encodeTagToOutput("Key", "Value", output);
    assertThat(serializer.fromByteArray(output.toByteArray())).isEqualTo(tagger.empty());
  }

  @Test
  public void testDeserializeWrongFormat() throws TagContextDeserializationException {
    // encoded tags should follow the format <version_id>(<tag_field_id><tag_encoding>)*
    thrown.expect(TagContextDeserializationException.class);
    serializer.fromByteArray(new byte[3]);
  }

  @Test
  public void testDeserializeWrongVersionId() throws TagContextDeserializationException {
    thrown.expect(TagContextDeserializationException.class);
    thrown.expectMessage("Wrong Version ID: 1. Currently supported version is: 0");
    serializer.fromByteArray(new byte[] {(byte) (SerializationUtils.VERSION_ID + 1)});
  }

  //     <tag_encoding> ==
  //       <tag_key_len><tag_key><tag_val_len><tag_val>
  //         <tag_key_len> == varint encoded integer
  //         <tag_key> == tag_key_len bytes comprising tag key name
  //         <tag_val_len> == varint encoded integer
  //         <tag_val> == tag_val_len bytes comprising UTF-8 string
  private static void encodeTagToOutput(String key, String value, ByteArrayDataOutput output) {
    output.write(SerializationUtils.TAG_FIELD_ID);
    encodeString(key, output);
    encodeString(value, output);
  }

  private static void encodeString(String input, ByteArrayDataOutput output) {
    int length = input.length();
    byte[] bytes = new byte[VarInt.varIntSize(length)];
    VarInt.putVarInt(length, bytes, 0);
    output.write(bytes);
    output.write(input.getBytes(Charsets.UTF_8));
  }
}
