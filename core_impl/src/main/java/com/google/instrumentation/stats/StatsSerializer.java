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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.ByteStreams;
import com.google.instrumentation.internal.VarInt;
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
 *
 * <p>Encoding of stats context information (tags) for passing across RPC's:</p>
 *
 * <ul>
 *   <li>Tags are encoded in single byte sequence. The version 0 format is:
 *   <li>{@code <version_id><encoded_tags>}
 *   <li>{@code <version_id> == a single byte, value 0}
 *   <li>{@code <encoded_tags> == (<tag_field_id><tag_encoding>)*}
 *       <ul>
 *       <li>{@code <tag_field_id>} == a single byte, value 0 (string tag), 1 (int tag),
 *           2 (true bool tag), 3 (false bool tag)
 *       <li>{@code <tag_encoding>}:
 *           <ul>
 *           <li>{@code (tag_field_id == 0) == <tag_key_len><tag_key><tag_val_len><tag_val>}
 *               <ul>
 *               <li>{@code <tag_key_len>} == varint encoded integer
 *               <li>{@code <tag_key>} == tag_key_len bytes comprising tag key name
 *               <li>{@code <tag_val_len>} == varint encoded integer
 *               <li>{@code <tag_val>} == tag_val_len bytes comprising UTF-8 string
 *               </ul>
 *           <li>{@code (tag_field_id == 1) == <tag_key_len><tag_key><int_tag_val>}
 *               <ul>
 *               <li>{@code <tag_key_len>} == varint encoded integer
 *               <li>{@code <tag_key>} == tag_key_len bytes comprising tag key name
 *               <li>{@code <int_tag_value>} == 8 bytes, little-endian integer
 *               </ul>
 *           <li>{@code (tag_field_id == 2 || tag_field_id == 3) == <tag_key_len><tag_key>}
 *               <ul>
 *               <li>{@code <tag_key_len>} == varint encoded integer
 *               <li>{@code <tag_key>} == tag_key_len bytes comprising tag key name
 *               </ul>
 *           </ul>
 *       </ul>
 * </ul>
 */
final class StatsSerializer {

  // TODO(songya): Currently we only support encoding on string type.
  @VisibleForTesting static final int VERSION_ID = 0;
  @VisibleForTesting static final int VALUE_TYPE_STRING = 0;
  @VisibleForTesting static final int VALUE_TYPE_INTEGER = 1;
  @VisibleForTesting static final int VALUE_TYPE_TRUE = 2;
  @VisibleForTesting static final int VALUE_TYPE_FALSE = 3;


  // Serializes a StatsContext to the on-the-wire format.
  // Encoded tags are of the form: <version_id><encoded_tags>
  static void serialize(StatsContextImpl context, OutputStream output) throws IOException {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    byteArrayOutputStream.write(VERSION_ID);

    // TODO(songya): add support for value types integer and boolean
    Set<Entry<TagKey, TagValue>> tags = context.tags.entrySet();
    for (Entry<TagKey, TagValue> tag : tags) {
      encodeStringTag(tag.getKey(), tag.getValue(), byteArrayOutputStream);
    }
    byteArrayOutputStream.writeTo(output);
  }

  // Deserializes input to StatsContext based on the binary format standard.
  // The encoded tags are of the form: <version_id><encoded_tags>
  static StatsContextImpl deserialize(StatsManagerImplBase statsManager, InputStream input)
      throws IOException {
    try {
      byte[] bytes = ByteStreams.toByteArray(input);
      HashMap<TagKey, TagValue> tags = new HashMap<TagKey, TagValue>();
      if (bytes.length == 0) {
        // Does not allow empty byte array.
        throw new IOException("Input byte stream can not be empty.");
      }

      ByteBuffer buffer = ByteBuffer.wrap(bytes).asReadOnlyBuffer();
      int versionId = buffer.get();
      if (versionId != VERSION_ID) {
        throw new IOException(
            "Wrong Version ID: " + versionId + ". Currently supported version is: " + VERSION_ID);
      }

      int limit = buffer.limit();
      while (buffer.position() < limit) {
        int type = buffer.get();
        switch (type) {
          case VALUE_TYPE_STRING:
            TagKey key = TagKey.create(decodeString(buffer));
            TagValue val = TagValue.create(decodeString(buffer));
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
      return new StatsContextImpl(statsManager, tags);
    } catch (BufferUnderflowException exn) {
      throw new IOException(exn.toString());  // byte array format error.
    }
  }

  //  TODO(songya): Currently we only support encoding on string type.
  private static final void encodeStringTag(
      TagKey key, TagValue value, ByteArrayOutputStream byteArrayOutputStream)
      throws IOException {
    byteArrayOutputStream.write(VALUE_TYPE_STRING);
    encodeString(key.asString(), byteArrayOutputStream);
    encodeString(value.asString(), byteArrayOutputStream);
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