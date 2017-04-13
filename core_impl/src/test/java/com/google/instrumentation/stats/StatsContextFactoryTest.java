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

import com.google.io.base.VarInt;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link StatsContextFactory}.
 */
@RunWith(JUnit4.class)
public class StatsContextFactoryTest {

  private static final int VERSION_ID = 0;
  private static final int VALUE_TYPE_STRING = 0;
  private static final int VALUE_TYPE_INTEGER = 1;
  private static final int VALUE_TYPE_TRUE = 2;
  private static final int VALUE_TYPE_FALSE = 3;

  private static final String KEY = "Key";
  private static final String VALUE_STRING = "String";
  private static final int VALUE_INT = 10;
  private final HashMap<String, String> sampleTags = new HashMap<String, String>();

  public StatsContextFactoryTest() {
    sampleTags.put(KEY + VALUE_TYPE_STRING, VALUE_STRING);
  }

  @Test
  public void testDeserializeNoTags() throws Exception {
    StatsContext expected = Stats.getStatsContextFactory().getDefault();
    StatsContext actual = testDeserialize(
        new ByteArrayInputStream(new byte[]{VERSION_ID}));  // One byte that represents Version ID.
    assertThat(actual).isEqualTo(expected);
  }

  @Test(expected = IOException.class)
  public void testDeserializeEmptyByteArrayThrowException() throws Exception {
    testDeserialize(new ByteArrayInputStream(new byte[0]));
  }

  @Test
  public void testDeserializeValueTypeString() throws Exception {
    StatsContext expected = new StatsContextImpl(sampleTags);
    StatsContext actual = testDeserialize(constructSingleTypeTagInputStream(VALUE_TYPE_STRING));
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testDeserializeMultipleString() throws Exception {
    sampleTags.put("Key2", "String2");
    StatsContext expected = new StatsContextImpl(sampleTags);

    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    byteArrayOutputStream.write(VERSION_ID);
    encodeSingleTypeTagToOutputStream(VALUE_TYPE_STRING, byteArrayOutputStream);
    byteArrayOutputStream.write(VALUE_TYPE_STRING);
    encodeString("Key2", byteArrayOutputStream);
    encodeString("String2", byteArrayOutputStream);
    StatsContext actual = testDeserialize(
        new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));

    assertThat(actual).isEqualTo(expected);
  }

  @Test(expected = IOException.class)
  public void testDeserializeValueTypeInteger() throws Exception {
    // TODO(songya): test should pass after we add support for type integer
    testDeserialize(constructSingleTypeTagInputStream(VALUE_TYPE_INTEGER));
  }

  @Test(expected = IOException.class)
  public void testDeserializeValueTypeTrue() throws Exception {
    // TODO(songya): test should pass after we add support for type boolean
    testDeserialize(constructSingleTypeTagInputStream(VALUE_TYPE_TRUE));
  }

  @Test(expected = IOException.class)
  public void testDeserializeValueTypeFalse() throws Exception {
    // TODO(songya): test should pass after we add support for type boolean
    testDeserialize(constructSingleTypeTagInputStream(VALUE_TYPE_FALSE));
  }

  @Test(expected = IOException.class)
  public void testDeserializeMultipleValueType() throws Exception {
    // TODO(songya): test should pass after we add support for type integer and boolean
    testDeserialize(constructMultiTypeTagInputStream());
  }

  @Test(expected = IOException.class)
  public void testDeserializeWrongFormat() throws Exception {
    // encoded tags should follow the format <version_id>(<tag_field_id><tag_encoding>)*
    testDeserialize(new ByteArrayInputStream(new byte[3]));
  }

  @Test(expected = IOException.class)
  public void testDeserializeWrongVersionId() throws Exception {
    testDeserialize(new ByteArrayInputStream(new byte[]{(byte) (VERSION_ID + 1)}));
  }

  private static StatsContext testDeserialize(InputStream inputStream)
      throws IOException, IOException {
    return Stats.getStatsContextFactory().deserialize(inputStream);
  }

  /*
   * Construct an InputStream with the given type of tag.
   * The input format is:
   *   <version_id><encoded_tags>, and <encoded_tags> == (<tag_field_id><tag_encoding>)*
   * TODO(songya): after supporting serialize integer and boolean,
   * remove this method and use StatsContext.serialize() instead.
   * Currently StatsContext.serialize() can only serialize strings.
   */
  private static InputStream constructSingleTypeTagInputStream(int valueType) throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    byteArrayOutputStream.write(VERSION_ID);
    encodeSingleTypeTagToOutputStream(valueType, byteArrayOutputStream);
    return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
  }

  // Construct an InputStream with all 4 types of tags.
  private static InputStream constructMultiTypeTagInputStream() throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    byteArrayOutputStream.write(VERSION_ID);
    encodeSingleTypeTagToOutputStream(VALUE_TYPE_STRING, byteArrayOutputStream);
    encodeSingleTypeTagToOutputStream(VALUE_TYPE_INTEGER, byteArrayOutputStream);
    encodeSingleTypeTagToOutputStream(VALUE_TYPE_TRUE, byteArrayOutputStream);
    encodeSingleTypeTagToOutputStream(VALUE_TYPE_FALSE, byteArrayOutputStream);
    return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
  }

  private static void encodeSingleTypeTagToOutputStream(
      int valueType, ByteArrayOutputStream byteArrayOutputStream) throws IOException {
    byteArrayOutputStream.write(valueType);

    // encode <tag_key_len><tag_key>, tag key is a string "KEY" appended by the field id here.
    encodeString(KEY + valueType, byteArrayOutputStream);
    switch (valueType) {
      case VALUE_TYPE_STRING:
        // String encoded format: <tag_key_len><tag_key><tag_val_len><tag_val>.
        encodeString(VALUE_STRING, byteArrayOutputStream);
        break;
      case VALUE_TYPE_INTEGER:
        // Integer encoded format: <tag_key_len><tag_key><int_tag_val>.
        encodeInteger(VALUE_INT, byteArrayOutputStream);
        break;
      case VALUE_TYPE_TRUE:
      case VALUE_TYPE_FALSE:
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
