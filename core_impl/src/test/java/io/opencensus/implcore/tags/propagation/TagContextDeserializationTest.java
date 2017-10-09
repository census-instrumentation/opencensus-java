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
import io.opencensus.tags.TagKey.TagKeyString;
import io.opencensus.tags.TagValue.TagValueString;
import io.opencensus.tags.Tagger;
import io.opencensus.tags.TagsComponent;
import io.opencensus.tags.propagation.TagContextBinarySerializer;
import io.opencensus.tags.propagation.TagContextParseException;
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
  public void testVersionAndValueTypeConstants() {
    // Refer to the JavaDoc on SerializationUtils for the definitions on these constants.
    assertThat(SerializationUtils.VERSION_ID).isEqualTo(0);
    assertThat(SerializationUtils.VALUE_TYPE_STRING).isEqualTo(0);
    assertThat(SerializationUtils.VALUE_TYPE_INTEGER).isEqualTo(1);
    assertThat(SerializationUtils.VALUE_TYPE_TRUE).isEqualTo(2);
    assertThat(SerializationUtils.VALUE_TYPE_FALSE).isEqualTo(3);
  }

  @Test
  public void testDeserializeNoTags() throws TagContextParseException {
    TagContext expected = tagger.empty();
    TagContext actual =
        serializer.fromByteArray(
            new byte[] {SerializationUtils.VERSION_ID}); // One byte that represents Version ID.
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testDeserializeEmptyByteArrayThrowException() throws TagContextParseException {
    thrown.expect(TagContextParseException.class);
    thrown.expectMessage("Input byte[] can not be empty.");
    serializer.fromByteArray(new byte[0]);
  }

  @Test
  public void testDeserializeInvalidTagKey() throws TagContextParseException {
    ByteArrayDataOutput output = ByteStreams.newDataOutput();
    output.write(SerializationUtils.VERSION_ID);

    // Encode an invalid tag key and a valid tag value:
    encodeStringTagToOutput("\2key", "value", output);
    final byte[] bytes = output.toByteArray();

    thrown.expect(TagContextParseException.class);
    thrown.expectMessage("Invalid tag key: \2key");
    serializer.fromByteArray(bytes);
  }

  @Test
  public void testDeserializeInvalidTagValue() throws TagContextParseException {
    ByteArrayDataOutput output = ByteStreams.newDataOutput();
    output.write(SerializationUtils.VERSION_ID);

    // Encode a valid tag key and an invalid tag value:
    encodeStringTagToOutput("my key", "val\3", output);
    final byte[] bytes = output.toByteArray();

    thrown.expect(TagContextParseException.class);
    thrown.expectMessage("Invalid tag value for key TagKeyString{name=my key}: val\3");
    serializer.fromByteArray(bytes);
  }

  @Test
  public void testDeserializeValueTypeString() throws TagContextParseException {
    ByteArrayDataOutput output = ByteStreams.newDataOutput();
    output.write(SerializationUtils.VERSION_ID);
    encodeStringTagToOutput("Key", "Value", output);
    TagContext expected =
        tagger
            .emptyBuilder()
            .put(TagKeyString.create("Key"), TagValueString.create("Value"))
            .build();
    assertThat(serializer.fromByteArray(output.toByteArray())).isEqualTo(expected);
  }

  @Test
  public void testDeserializeMultipleString() throws TagContextParseException {
    ByteArrayDataOutput output = ByteStreams.newDataOutput();
    output.write(SerializationUtils.VERSION_ID);
    encodeStringTagToOutput("Key1", "String1", output);
    encodeStringTagToOutput("Key2", "String2", output);
    TagContext expected =
        tagger
            .emptyBuilder()
            .put(TagKeyString.create("Key1"), TagValueString.create("String1"))
            .put(TagKeyString.create("Key2"), TagValueString.create("String2"))
            .build();
    assertThat(serializer.fromByteArray(output.toByteArray())).isEqualTo(expected);
  }

  @Test
  public void skipIntegerTag() throws TagContextParseException {
    // TODO(songya): Update this test after we add support for type integer
    ByteArrayDataOutput output = ByteStreams.newDataOutput();
    output.write(SerializationUtils.VERSION_ID);
    encodeStringTagToOutput("Key1", "Value1", output);
    encodeLongTagToOutput("Key2", 1L, output);
    encodeStringTagToOutput("Key3", "Value3", output);
    TagContext expected =
        tagger
            .emptyBuilder()
            .put(TagKeyString.create("Key1"), TagValueString.create("Value1"))
            .put(TagKeyString.create("Key3"), TagValueString.create("Value3"))
            .build();
    assertThat(serializer.fromByteArray(output.toByteArray())).isEqualTo(expected);
  }

  @Test
  public void skipTrueBooleanTag() throws TagContextParseException {
    // TODO(songya): Update this test after we add support for type boolean
    ByteArrayDataOutput output = ByteStreams.newDataOutput();
    output.write(SerializationUtils.VERSION_ID);
    encodeStringTagToOutput("Key1", "Value1", output);
    encodeTrueTagToOutput("Key2", output);
    encodeStringTagToOutput("Key3", "Value3", output);
    TagContext expected =
        tagger
            .emptyBuilder()
            .put(TagKeyString.create("Key1"), TagValueString.create("Value1"))
            .put(TagKeyString.create("Key3"), TagValueString.create("Value3"))
            .build();
    assertThat(serializer.fromByteArray(output.toByteArray())).isEqualTo(expected);
  }

  @Test
  public void skipFalseBooleanTag() throws TagContextParseException {
    // TODO(songya): Update this test after we add support for type boolean
    ByteArrayDataOutput output = ByteStreams.newDataOutput();
    output.write(SerializationUtils.VERSION_ID);
    encodeStringTagToOutput("Key1", "Value1", output);
    encodeFalseTagToOutput("Key2", output);
    encodeStringTagToOutput("Key3", "Value3", output);
    TagContext expected =
        tagger
            .emptyBuilder()
            .put(TagKeyString.create("Key1"), TagValueString.create("Value1"))
            .put(TagKeyString.create("Key3"), TagValueString.create("Value3"))
            .build();
    assertThat(serializer.fromByteArray(output.toByteArray())).isEqualTo(expected);
  }

  @Test
  public void skipMultipleUnsupportedTags() throws TagContextParseException {
    // TODO(songya): Update this test after we add support for type integer and boolean
    ByteArrayDataOutput output = ByteStreams.newDataOutput();
    output.write(SerializationUtils.VERSION_ID);
    encodeLongTagToOutput("Key1", 10L, output);
    encodeStringTagToOutput("Key2", "String1", output);
    encodeTrueTagToOutput("Key3", output);
    encodeStringTagToOutput("Key4", "String2", output);
    encodeFalseTagToOutput("Key5", output);
    TagContext expected =
        tagger
            .emptyBuilder()
            .put(TagKeyString.create("Key2"), TagValueString.create("String1"))
            .put(TagKeyString.create("Key4"), TagValueString.create("String2"))
            .build();
    assertThat(serializer.fromByteArray(output.toByteArray())).isEqualTo(expected);
  }

  @Test
  public void stopParsingAtUnknownTag() throws TagContextParseException {
    ByteArrayDataOutput output = ByteStreams.newDataOutput();
    output.write(SerializationUtils.VERSION_ID);
    encodeStringTagToOutput("Key1", "String1", output);
    encodeStringTagToOutput("Key2", "String2", output);

    // Write unknown tag with type 4
    output.write(4);
    encodeString("Key3", output);
    output.write(new byte[] {1, 2, 3, 4});

    encodeStringTagToOutput("Key4", "String3", output);

    // keys 3 and 4 should not be included
    TagContext expected =
        tagger
            .emptyBuilder()
            .put(TagKeyString.create("Key1"), TagValueString.create("String1"))
            .put(TagKeyString.create("Key2"), TagValueString.create("String2"))
            .build();
    assertThat(serializer.fromByteArray(output.toByteArray())).isEqualTo(expected);
  }

  @Test
  public void stopParsingAtUnknownTagAtStart() throws TagContextParseException {
    ByteArrayDataOutput output = ByteStreams.newDataOutput();
    output.write(SerializationUtils.VERSION_ID);

    // Write unknown tag with type 4
    output.write(4);
    encodeString("Key1", output);

    encodeStringTagToOutput("Key2", "String", output);
    assertThat(serializer.fromByteArray(output.toByteArray())).isEqualTo(tagger.empty());
  }

  @Test
  public void testDeserializeWrongFormat() throws TagContextParseException {
    // encoded tags should follow the format <version_id>(<tag_field_id><tag_encoding>)*
    thrown.expect(TagContextParseException.class);
    serializer.fromByteArray(new byte[3]);
  }

  @Test
  public void testDeserializeWrongVersionId() throws TagContextParseException {
    thrown.expect(TagContextParseException.class);
    thrown.expectMessage("Wrong Version ID: 1. Currently supported version is: 0");
    serializer.fromByteArray(new byte[] {(byte) (SerializationUtils.VERSION_ID + 1)});
  }

  private static void encodeStringTagToOutput(
      String key, String value, ByteArrayDataOutput output) {
    output.write(SerializationUtils.VALUE_TYPE_STRING);

    // String encoded format: <tag_key_len><tag_key><tag_val_len><tag_val>.
    encodeString(key, output);
    encodeString(value, output);
  }

  private static void encodeLongTagToOutput(String key, long value, ByteArrayDataOutput output) {
    output.write(SerializationUtils.VALUE_TYPE_INTEGER);

    // Integer encoded format: <tag_key_len><tag_key><int_tag_val>.
    encodeString(key, output);
    encodeLong(value, output);
  }

  private static void encodeTrueTagToOutput(String key, ByteArrayDataOutput output) {
    output.write(SerializationUtils.VALUE_TYPE_TRUE);

    // Boolean encoded format: <tag_key_len><tag_key>. No tag_value is needed
    encodeString(key, output);
  }

  private static void encodeFalseTagToOutput(String key, ByteArrayDataOutput output) {
    output.write(SerializationUtils.VALUE_TYPE_FALSE);

    // Boolean encoded format: <tag_key_len><tag_key>. No tag_value is needed
    encodeString(key, output);
  }

  //     <tag_encoding> (tag_field_id == 0) ==
  //       <tag_key_len><tag_key><tag_val_len><tag_val>
  //         <tag_key_len> == varint encoded integer
  //         <tag_key> == tag_key_len bytes comprising tag key name
  //         <tag_val_len> == varint encoded integer
  //         <tag_val> == tag_val_len bytes comprising UTF-8 string
  private static void encodeString(String input, ByteArrayDataOutput output) {
    int length = input.length();
    byte[] bytes = new byte[VarInt.varIntSize(length)];
    VarInt.putVarInt(length, bytes, 0);
    output.write(bytes);
    output.write(input.getBytes(Charsets.UTF_8));
  }

  //     <tag_encoding> (tag_field_id == 1) ==
  //       <tag_key_len><tag_key><int_tag_val>
  //         <tag_key_len> == varint encoded integer
  //         <tag_key> == tag_key_len bytes comprising tag key name
  //         <int_tag_value> == 8 bytes, little-endian integer
  private static void encodeLong(long input, ByteArrayDataOutput output) {
    output.writeLong(input);
  }
}
