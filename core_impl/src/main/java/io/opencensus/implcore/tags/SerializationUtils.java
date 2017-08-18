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

package io.opencensus.implcore.tags;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.ByteStreams;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.implcore.internal.VarInt;
import io.opencensus.tags.Tag;
import io.opencensus.tags.Tag.TagBoolean;
import io.opencensus.tags.Tag.TagLong;
import io.opencensus.tags.Tag.TagString;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagKey.TagKeyString;
import io.opencensus.tags.TagValueString;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Methods for serializing and deserializing {@link TagContext}s.
 *
 * <p>The format defined in this class is shared across all implementations of OpenCensus. It allows
 * tags to propagate across requests.
 *
 * <p>OpenCensus tag context encoding:
 *
 * <ul>
 *   <li>Tags are encoded in single byte sequence. The version 0 format is:
 *   <li>{@code <version_id><encoded_tags>}
 *   <li>{@code <version_id> == a single byte, value 0}
 *   <li>{@code <encoded_tags> == (<tag_field_id><tag_encoding>)*}
 *       <ul>
 *         <li>{@code <tag_field_id>} == a single byte, value 0 (string tag), 1 (int tag), 2 (true
 *             bool tag), 3 (false bool tag)
 *         <li>{@code <tag_encoding>}:
 *             <ul>
 *               <li>{@code (tag_field_id == 0) == <tag_key_len><tag_key><tag_val_len><tag_val>}
 *                   <ul>
 *                     <li>{@code <tag_key_len>} == varint encoded integer
 *                     <li>{@code <tag_key>} == tag_key_len bytes comprising tag key name
 *                     <li>{@code <tag_val_len>} == varint encoded integer
 *                     <li>{@code <tag_val>} == tag_val_len bytes comprising UTF-8 string
 *                   </ul>
 *               <li>{@code (tag_field_id == 1) == <tag_key_len><tag_key><int_tag_val>}
 *                   <ul>
 *                     <li>{@code <tag_key_len>} == varint encoded integer
 *                     <li>{@code <tag_key>} == tag_key_len bytes comprising tag key name
 *                     <li>{@code <int_tag_value>} == 8 bytes, little-endian integer
 *                   </ul>
 *               <li>{@code (tag_field_id == 2 || tag_field_id == 3) == <tag_key_len><tag_key>}
 *                   <ul>
 *                     <li>{@code <tag_key_len>} == varint encoded integer
 *                     <li>{@code <tag_key>} == tag_key_len bytes comprising tag key name
 *                   </ul>
 *             </ul>
 *       </ul>
 * </ul>
 */
final class SerializationUtils {
  private SerializationUtils() {}

  // TODO(songya): Currently we only support encoding on string type.
  @VisibleForTesting static final int VERSION_ID = 0;
  @VisibleForTesting static final int VALUE_TYPE_STRING = 0;
  @VisibleForTesting static final int VALUE_TYPE_INTEGER = 1;
  @VisibleForTesting static final int VALUE_TYPE_TRUE = 2;
  @VisibleForTesting static final int VALUE_TYPE_FALSE = 3;

  // Serializes a TagContext to the on-the-wire format.
  // Encoded tags are of the form: <version_id><encoded_tags>
  static void serializeBinary(TagContext tags, OutputStream output) throws IOException {
    final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    byteArrayOutputStream.write(VERSION_ID);

    // TODO(songya): add support for value types integer and boolean
    for (Iterator<Tag> i = tags.unsafeGetIterator(); i.hasNext(); ) {
      Tag tag = i.next();

      // TODO(sebright): Is there a better way to handle checked exceptions in function objects?
      IOException ex =
          tag.match(
              new Function<TagString, IOException>() {
                @Override
                public IOException apply(TagString arg) {
                  try {
                    encodeStringTag(arg, byteArrayOutputStream);
                    return null;
                  } catch (IOException e) {
                    return e;
                  }
                }
              },
              new Function<TagLong, IOException>() {
                @Override
                public IOException apply(TagLong arg) {
                  throw new IllegalArgumentException("long tags are not supported.");
                }
              },
              new Function<TagBoolean, IOException>() {
                @Override
                public IOException apply(TagBoolean arg) {
                  throw new IllegalArgumentException("boolean tags are not supported.");
                }
              },
              Functions.<IOException>throwAssertionError());
      if (ex != null) {
        throw ex;
      }
    }
    byteArrayOutputStream.writeTo(output);
  }

  // Deserializes input to TagContext based on the binary format standard.
  // The encoded tags are of the form: <version_id><encoded_tags>
  static TagContextImpl deserializeBinary(InputStream input) throws IOException {
    try {
      byte[] bytes = ByteStreams.toByteArray(input);
      HashMap<TagKey, Object> tags = new HashMap<TagKey, Object>();
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
            TagKeyString key = TagKeyString.create(decodeString(buffer));
            TagValueString val = TagValueString.create(decodeString(buffer));
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
      return new TagContextImpl(tags);
    } catch (BufferUnderflowException exn) {
      throw new IOException(exn.toString()); // byte array format error.
    }
  }

  //  TODO(songya): Currently we only support encoding on string type.
  private static final void encodeStringTag(
      TagString tag, ByteArrayOutputStream byteArrayOutputStream) throws IOException {
    byteArrayOutputStream.write(VALUE_TYPE_STRING);
    encodeString(tag.getKey().getName(), byteArrayOutputStream);
    encodeString(tag.getValue().asString(), byteArrayOutputStream);
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
