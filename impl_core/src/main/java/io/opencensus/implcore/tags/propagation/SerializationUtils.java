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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.opencensus.implcore.internal.VarInt;
import io.opencensus.implcore.tags.TagMapImpl;
import io.opencensus.tags.InternalUtils;
import io.opencensus.tags.Tag;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagKey.TagScope;
import io.opencensus.tags.TagValue;
import io.opencensus.tags.propagation.TagContextDeserializationException;
import io.opencensus.tags.propagation.TagContextSerializationException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
 *         <li>{@code <tag_field_id>} == a single byte, value 0
 *         <li>{@code <tag_encoding>}:
 *             <ul>
 *               <li>{@code <tag_key_len><tag_key><tag_val_len><tag_val>}
 *                   <ul>
 *                     <li>{@code <tag_key_len>} == varint encoded integer
 *                     <li>{@code <tag_key>} == tag_key_len bytes comprising tag key name
 *                     <li>{@code <tag_val_len>} == varint encoded integer
 *                     <li>{@code <tag_val>} == tag_val_len bytes comprising UTF-8 string
 *                   </ul>
 *             </ul>
 *       </ul>
 * </ul>
 */
final class SerializationUtils {
  private SerializationUtils() {}

  @VisibleForTesting static final int VERSION_ID = 0;
  @VisibleForTesting static final int TAG_FIELD_ID = 0;
  // This size limit only applies to the bytes representing tag keys and values.
  @VisibleForTesting static final int TAGCONTEXT_SERIALIZED_SIZE_LIMIT = 8192;

  // Serializes a TagContext to the on-the-wire format.
  // Encoded tags are of the form: <version_id><encoded_tags>
  static byte[] serializeBinary(TagContext tags) throws TagContextSerializationException {
    // Use a ByteArrayDataOutput to avoid needing to handle IOExceptions.
    final ByteArrayDataOutput byteArrayDataOutput = ByteStreams.newDataOutput();
    byteArrayDataOutput.write(VERSION_ID);
    int totalChars = 0; // Here chars are equivalent to bytes, since we're using ascii chars.
    for (Iterator<Tag> i = InternalUtils.getTags(tags); i.hasNext(); ) {
      Tag tag = i.next();
      if (TagScope.LOCAL.equals(tag.getKey().getTagScope())) {
        continue;
      }
      totalChars += tag.getKey().getName().length();
      totalChars += tag.getValue().asString().length();
      encodeTag(tag, byteArrayDataOutput);
    }
    if (totalChars > TAGCONTEXT_SERIALIZED_SIZE_LIMIT) {
      throw new TagContextSerializationException(
          "Size of TagContext exceeds the maximum serialized size "
              + TAGCONTEXT_SERIALIZED_SIZE_LIMIT);
    }
    return byteArrayDataOutput.toByteArray();
  }

  // Deserializes input to TagContext based on the binary format standard.
  // The encoded tags are of the form: <version_id><encoded_tags>
  static TagMapImpl deserializeBinary(byte[] bytes) throws TagContextDeserializationException {
    try {
      if (bytes.length == 0) {
        // Does not allow empty byte array.
        throw new TagContextDeserializationException("Input byte[] can not be empty.");
      }

      ByteBuffer buffer = ByteBuffer.wrap(bytes).asReadOnlyBuffer();
      int versionId = buffer.get();
      if (versionId > VERSION_ID || versionId < 0) {
        throw new TagContextDeserializationException(
            "Wrong Version ID: " + versionId + ". Currently supports version up to: " + VERSION_ID);
      }
      return new TagMapImpl(parseTags(buffer));
    } catch (BufferUnderflowException exn) {
      throw new TagContextDeserializationException(exn.toString()); // byte array format error.
    }
  }

  private static Map<TagKey, TagValue> parseTags(ByteBuffer buffer)
      throws TagContextDeserializationException {
    Map<TagKey, TagValue> tags = new HashMap<TagKey, TagValue>();
    int limit = buffer.limit();
    int totalChars = 0; // Here chars are equivalent to bytes, since we're using ascii chars.
    while (buffer.position() < limit) {
      int type = buffer.get();
      if (type == TAG_FIELD_ID) {
        TagKey key = createTagKey(decodeString(buffer));
        TagValue val = createTagValue(key, decodeString(buffer));
        totalChars += key.getName().length();
        totalChars += val.asString().length();
        tags.put(key, val);
      } else {
        // Stop parsing at the first unknown field ID, since there is no way to know its length.
        // TODO(sebright): Consider storing the rest of the byte array in the TagContext.
        break;
      }
    }
    if (totalChars > TAGCONTEXT_SERIALIZED_SIZE_LIMIT) {
      throw new TagContextDeserializationException(
          "Size of TagContext exceeds the maximum serialized size "
              + TAGCONTEXT_SERIALIZED_SIZE_LIMIT);
    }
    return tags;
  }

  // TODO(sebright): Consider exposing a TagKey name validation method to avoid needing to catch an
  // IllegalArgumentException here.
  private static final TagKey createTagKey(String name) throws TagContextDeserializationException {
    try {
      return TagKey.create(name, TagScope.REQUEST);
    } catch (IllegalArgumentException e) {
      throw new TagContextDeserializationException("Invalid tag key: " + name, e);
    }
  }

  // TODO(sebright): Consider exposing a TagValue validation method to avoid needing to catch
  // an IllegalArgumentException here.
  private static final TagValue createTagValue(TagKey key, String value)
      throws TagContextDeserializationException {
    try {
      return TagValue.create(value);
    } catch (IllegalArgumentException e) {
      throw new TagContextDeserializationException(
          "Invalid tag value for key " + key + ": " + value, e);
    }
  }

  private static final void encodeTag(Tag tag, ByteArrayDataOutput byteArrayDataOutput) {
    byteArrayDataOutput.write(TAG_FIELD_ID);
    encodeString(tag.getKey().getName(), byteArrayDataOutput);
    encodeString(tag.getValue().asString(), byteArrayDataOutput);
  }

  private static final void encodeString(String input, ByteArrayDataOutput byteArrayDataOutput) {
    putVarInt(input.length(), byteArrayDataOutput);
    byteArrayDataOutput.write(input.getBytes(Charsets.UTF_8));
  }

  private static final void putVarInt(int input, ByteArrayDataOutput byteArrayDataOutput) {
    byte[] output = new byte[VarInt.varIntSize(input)];
    VarInt.putVarInt(input, output, 0);
    byteArrayDataOutput.write(output);
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
