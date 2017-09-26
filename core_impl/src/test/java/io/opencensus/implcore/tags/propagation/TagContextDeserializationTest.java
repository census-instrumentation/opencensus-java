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

import io.opencensus.implcore.internal.VarInt;
import io.opencensus.implcore.tags.TaggerImpl;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagKey.TagKeyString;
import io.opencensus.tags.TagValue.TagValueString;
import io.opencensus.tags.Tagger;
import io.opencensus.tags.propagation.TagContextBinarySerializer;
import io.opencensus.tags.propagation.TagContextParseException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

  private static final String KEY = "Key";
  private static final String VALUE_STRING = "String";
  private static final int VALUE_INT = 10;

  @Rule public final ExpectedException thrown = ExpectedException.none();

  private final TagContextBinarySerializer serializer = new TagContextBinarySerializerImpl();
  private final Tagger tagger = new TaggerImpl();

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
  public void testDeserializeNoTags() throws Exception {
    TagContext expected = tagger.empty();
    TagContext actual =
        testDeserialize(
            new byte[] {SerializationUtils.VERSION_ID}); // One byte that represents Version ID.
    assertThat(actual).isEqualTo(expected);
  }

  @Test(expected = TagContextParseException.class)
  public void testDeserializeEmptyByteArrayThrowException() throws Exception {
    testDeserialize(new byte[0]);
  }

  @Test
  public void testDeserializeInvalidTagKey() throws Exception {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    os.write(SerializationUtils.VERSION_ID);
    os.write(SerializationUtils.VALUE_TYPE_STRING);

    // Encode an invalid tag key and a valid tag value:
    encodeString("\2key", os);
    encodeString("value", os);
    final byte[] bytes = os.toByteArray();

    thrown.expect(TagContextParseException.class);
    thrown.expectMessage("Invalid tag key: \2key");
    serializer.fromByteArray(bytes);
  }

  @Test
  public void testDeserializeInvalidTagValue() throws Exception {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    os.write(SerializationUtils.VERSION_ID);
    os.write(SerializationUtils.VALUE_TYPE_STRING);

    // Encode a valid tag key and an invalid tag value:
    encodeString("key", os);
    encodeString("val\3", os);
    final byte[] bytes = os.toByteArray();

    thrown.expect(TagContextParseException.class);
    thrown.expectMessage("Invalid tag value: val\3");
    serializer.fromByteArray(bytes);
  }

  @Test
  public void testDeserializeValueTypeString() throws Exception {
    TagContext actual =
        testDeserialize(constructSingleTypeTagInputStream(SerializationUtils.VALUE_TYPE_STRING));
    TagContext expected =
        tagger
            .emptyBuilder()
            .put(
                TagKeyString.create(KEY + SerializationUtils.VALUE_TYPE_STRING),
                TagValueString.create(VALUE_STRING))
            .build();
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testDeserializeMultipleString() throws Exception {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    byteArrayOutputStream.write(SerializationUtils.VERSION_ID);
    encodeSingleTypeTagToOutputStream(SerializationUtils.VALUE_TYPE_STRING, byteArrayOutputStream);
    byteArrayOutputStream.write(SerializationUtils.VALUE_TYPE_STRING);
    encodeString("Key2", byteArrayOutputStream);
    encodeString("String2", byteArrayOutputStream);
    TagContext actual =
        testDeserialize(byteArrayOutputStream.toByteArray());
    TagContext expected =
        tagger
            .emptyBuilder()
            .put(
                TagKeyString.create(KEY + SerializationUtils.VALUE_TYPE_STRING),
                TagValueString.create(VALUE_STRING))
            .put(TagKeyString.create("Key2"), TagValueString.create("String2"))
            .build();
    assertThat(actual).isEqualTo(expected);
  }

  @Test(expected = TagContextParseException.class)
  public void testDeserializeValueTypeInteger() throws Exception {
    // TODO(songya): test should pass after we add support for type integer
    testDeserialize(constructSingleTypeTagInputStream(SerializationUtils.VALUE_TYPE_INTEGER));
  }

  @Test(expected = TagContextParseException.class)
  public void testDeserializeValueTypeTrue() throws Exception {
    // TODO(songya): test should pass after we add support for type boolean
    testDeserialize(constructSingleTypeTagInputStream(SerializationUtils.VALUE_TYPE_TRUE));
  }

  @Test(expected = TagContextParseException.class)
  public void testDeserializeValueTypeFalse() throws Exception {
    // TODO(songya): test should pass after we add support for type boolean
    testDeserialize(constructSingleTypeTagInputStream(SerializationUtils.VALUE_TYPE_FALSE));
  }

  @Test(expected = TagContextParseException.class)
  public void testDeserializeMultipleValueType() throws Exception {
    // TODO(songya): test should pass after we add support for type integer and boolean
    testDeserialize(constructMultiTypeTagInputStream());
  }

  @Test(expected = TagContextParseException.class)
  public void testDeserializeWrongFormat() throws Exception {
    // encoded tags should follow the format <version_id>(<tag_field_id><tag_encoding>)*
    testDeserialize(new byte[3]);
  }

  @Test(expected = TagContextParseException.class)
  public void testDeserializeWrongVersionId() throws Exception {
    testDeserialize(new byte[] {(byte) (SerializationUtils.VERSION_ID + 1)});
  }

  private TagContext testDeserialize(byte[] bytes) throws TagContextParseException {
    return serializer.fromByteArray(bytes);
  }

  /*
   * Construct an InputStream with the given type of tag.
   * The input format is:
   *   <version_id><encoded_tags>, and <encoded_tags> == (<tag_field_id><tag_encoding>)*
   * TODO(songya): after supporting serialize integer and boolean,
   * remove this method and use StatsContext.serialize() instead.
   * Currently StatsContext.serialize() can only serialize strings.
   */
  private static byte[] constructSingleTypeTagInputStream(int valueType) throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    byteArrayOutputStream.write(SerializationUtils.VERSION_ID);
    encodeSingleTypeTagToOutputStream(valueType, byteArrayOutputStream);
    return byteArrayOutputStream.toByteArray();
  }

  // Construct an InputStream with all 4 types of tags.
  private static byte[] constructMultiTypeTagInputStream() throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    byteArrayOutputStream.write(SerializationUtils.VERSION_ID);
    encodeSingleTypeTagToOutputStream(SerializationUtils.VALUE_TYPE_STRING, byteArrayOutputStream);
    encodeSingleTypeTagToOutputStream(SerializationUtils.VALUE_TYPE_INTEGER, byteArrayOutputStream);
    encodeSingleTypeTagToOutputStream(SerializationUtils.VALUE_TYPE_TRUE, byteArrayOutputStream);
    encodeSingleTypeTagToOutputStream(SerializationUtils.VALUE_TYPE_FALSE, byteArrayOutputStream);
    return byteArrayOutputStream.toByteArray();
  }

  private static void encodeSingleTypeTagToOutputStream(
      int valueType, ByteArrayOutputStream byteArrayOutputStream) throws IOException {
    byteArrayOutputStream.write(valueType);

    // encode <tag_key_len><tag_key>, tag key is a string "KEY" appended by the field id here.
    encodeString(KEY + valueType, byteArrayOutputStream);
    switch (valueType) {
      case SerializationUtils.VALUE_TYPE_STRING:
        // String encoded format: <tag_key_len><tag_key><tag_val_len><tag_val>.
        encodeString(VALUE_STRING, byteArrayOutputStream);
        break;
      case SerializationUtils.VALUE_TYPE_INTEGER:
        // Integer encoded format: <tag_key_len><tag_key><int_tag_val>.
        encodeInteger(VALUE_INT, byteArrayOutputStream);
        break;
      case SerializationUtils.VALUE_TYPE_TRUE:
      case SerializationUtils.VALUE_TYPE_FALSE:
        // Boolean encoded format: <tag_key_len><tag_key>. No tag_value is needed
        break;
      default:
        return;
    }
  }

  //     <tag_encoding> (tag_field_id == 0) ==
  //       <tag_key_len><tag_key><tag_val_len><tag_val>
  //         <tag_key_len> == varint encoded integer
  //         <tag_key> == tag_key_len bytes comprising tag key name
  //         <tag_val_len> == varint encoded integer
  //         <tag_val> == tag_val_len bytes comprising UTF-8 string
  private static void encodeString(String input, ByteArrayOutputStream byteArrayOutputStream)
      throws IOException {
    VarInt.putVarInt(input.length(), byteArrayOutputStream);
    byteArrayOutputStream.write(input.getBytes("UTF-8"));
  }

  //     <tag_encoding> (tag_field_id == 1) ==
  //       <tag_key_len><tag_key><int_tag_val>
  //         <tag_key_len> == varint encoded integer
  //         <tag_key> == tag_key_len bytes comprising tag key name
  //         <int_tag_value> == 8 bytes, little-endian integer
  private static void encodeInteger(int input, ByteArrayOutputStream byteArrayOutputStream) {
    byteArrayOutputStream.write((byte) input);
  }
}
