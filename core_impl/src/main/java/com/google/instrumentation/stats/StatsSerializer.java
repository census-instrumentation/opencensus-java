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

import com.google.common.io.ByteStreams;
import com.google.io.base.VarInt;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Native implementation {@link StatsContext} serialization.
 */
final class StatsSerializer {

  // This describes the encoding of stats context information (tags)
  // for passing across RPC's.
  // Tags are encoded in single byte sequence. The format is:
  // <version_id><encoded_tags>
  //   <version_id> == a single byte, value 0
  //   <encoded_tags> == (<tag_field_id><tag_encoding>)*
  //     <tag_field_id> == a single byte, value 0 (string tag), 1 (int tag),
  //                       2 (true bool tag), 3 (false bool flag)
  //     <tag_encoding> (tag_field_id == 0) ==
  //       <tag_key_len><tag_key><tag_val_len><tag_val>
  //         <tag_key_len> == varint encoded integer
  //         <tag_key> == tag_key_len bytes comprising tag key name
  //         <tag_val_len> == varint encoded integer
  //         <tag_val> == tag_val_len bytes comprising UTF-8 string
  //     <tag_encoding> (tag_field_id == 1) ==
  //       <tag_key_len><tag_key><int_tag_val>
  //         <tag_key_len> == varint encoded integer
  //         <tag_key> == tag_key_len bytes comprising tag key name
  //         <int_tag_value> == 8 bytes, little-endian integer
  //     <tag_encoding> (tag_field_id == 2 || tag_field_id == 3) ==
  //       <tag_key_len><tag_key>
  //         <tag_key_len> == varint encoded integer
  //         <tag_key> == tag_key_len bytes comprising tag key name
  //    TODO(songya): Currently we only support encoding on string type.
  private static final int VERSION_ID = 0;
  private static final int VALUE_TYPE_STRING = 0;
  private static final int VALUE_TYPE_INTEGER = 1;
  private static final int VALUE_TYPE_TRUE = 2;
  private static final int VALUE_TYPE_FALSE = 3;


  // Serializes a StatsContext to the on-the-wire format.
  // Encoded tags are of the form: <version_id><encoded_tags>
  static void serialize(StatsContextImpl context, OutputStream output) throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    VarInt.putVarInt(VERSION_ID, byteArrayOutputStream);

    // TODO(songya): add support for value types integer and boolean
    Set<Entry<String, String>> tags = context.tags.entrySet();
    for (Entry<String, String> tag : tags) {
      encodeStringTagWithType(
          VALUE_TYPE_STRING, tag.getKey(), tag.getValue(), byteArrayOutputStream);
    }
    byteArrayOutputStream.writeTo(output);
  }

  // Deserializes input to StatsContext based on the binary format standard.
  // The encoded tags are of the form: <version_id><encoded_tags>
  static StatsContextImpl deserialize(InputStream input) throws IOException {
    try {
      byte[] bytes = ByteStreams.toByteArray(input);
      HashMap<String, String> tags = new HashMap<String, String>();
      if (bytes.length == 0) {
        // Return a default StatsContext on empty input.
        return new StatsContextImpl(tags);
      }

      ByteBuffer buffer = ByteBuffer.wrap(bytes).asReadOnlyBuffer();
      if (VarInt.getVarInt(buffer) != VERSION_ID) {
        throw new IOException("Wrong Version ID.");
      }

      int limit = buffer.limit();
      while (buffer.position() < limit) {
        int type = VarInt.getVarInt(buffer);
        switch (type) {
          case VALUE_TYPE_STRING:
            String key = decodeString(buffer);
            String val = decodeString(buffer);
            tags.put(key, val);
            break;
          case VALUE_TYPE_INTEGER:
          case VALUE_TYPE_TRUE:
          case VALUE_TYPE_FALSE:
          default:
            // TODO(songya): add support for value types integer and boolean
            throw new IOException("Unsupported tag value type.");
        }
      }
      return new StatsContextImpl(tags);
    } catch (BufferUnderflowException exn) {
      throw new IOException(exn);
    }
  }

  //  TODO(songya): Currently we only support encoding on string type.
  private static final void encodeStringTagWithType(
      int valueType, String key, String value, ByteArrayOutputStream byteArrayOutputStream)
      throws IOException {
    VarInt.putVarInt(valueType, byteArrayOutputStream);
    encodeString(key, byteArrayOutputStream);
    encodeString(value, byteArrayOutputStream);
  }

  private static final void encodeString(String input, ByteArrayOutputStream byteArrayOutputStream)
      throws IOException {
    VarInt.putVarInt(input.length(), byteArrayOutputStream);
    byteArrayOutputStream.write(input.getBytes("UTF-8"));
  }

  private static final String decodeString(ByteBuffer buffer) {
    int length = VarInt.getVarInt(buffer);
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < length; i++) {
      builder.append((char) buffer.get());
    }
    return builder.toString();
  }
}