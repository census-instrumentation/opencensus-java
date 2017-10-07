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
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.implcore.internal.VarInt;
import io.opencensus.implcore.tags.TagContextImpl;
import io.opencensus.tags.InternalUtils;
import io.opencensus.tags.Tag;
import io.opencensus.tags.Tag.TagBoolean;
import io.opencensus.tags.Tag.TagLong;
import io.opencensus.tags.Tag.TagString;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagKey.TagKeyString;
import io.opencensus.tags.TagValue;
import io.opencensus.tags.TagValue.TagValueString;
import io.opencensus.tags.propagation.TagContextParseException;
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

  private static final Function<TagLong, Void> ENCODE_TAG_LONG = new EncodeTagLong();
  private static final Function<TagBoolean, Void> ENCODE_TAG_BOOLEAN = new EncodeTagBoolean();

  // Serializes a TagContext to the on-the-wire format.
  // Encoded tags are of the form: <version_id><encoded_tags>
  static byte[] serializeBinary(TagContext tags) {
    // Use a ByteArrayDataOutput to avoid needing to handle IOExceptions.
    final ByteArrayDataOutput byteArrayDataOutput = ByteStreams.newDataOutput();
    byteArrayDataOutput.write(VERSION_ID);

    // TODO(songya): add support for value types integer and boolean
    for (Iterator<Tag> i = InternalUtils.getTags(tags); i.hasNext(); ) {
      Tag tag = i.next();
      tag.match(
          new EncodeTagString(byteArrayDataOutput),
          ENCODE_TAG_LONG,
          ENCODE_TAG_BOOLEAN,
          Functions.<Void>throwAssertionError());
    }
    return byteArrayDataOutput.toByteArray();
  }

  // Deserializes input to TagContext based on the binary format standard.
  // The encoded tags are of the form: <version_id><encoded_tags>
  static TagContextImpl deserializeBinary(byte[] bytes) throws TagContextParseException {
    try {
      HashMap<TagKey, TagValue> tags = new HashMap<TagKey, TagValue>();
      if (bytes.length == 0) {
        // Does not allow empty byte array.
        throw new TagContextParseException("Input byte[] can not be empty.");
      }

      ByteBuffer buffer = ByteBuffer.wrap(bytes).asReadOnlyBuffer();
      int versionId = buffer.get();
      if (versionId != VERSION_ID) {
        throw new TagContextParseException(
            "Wrong Version ID: " + versionId + ". Currently supported version is: " + VERSION_ID);
      }

      int limit = buffer.limit();
      while (buffer.position() < limit) {
        int type = buffer.get();
        switch (type) {
          case VALUE_TYPE_STRING:
            TagKeyString key = createTagKey(decodeString(buffer));
            TagValueString val = createTagValue(key, decodeString(buffer));
            tags.put(key, val);
            break;
          case VALUE_TYPE_INTEGER:
          case VALUE_TYPE_TRUE:
          case VALUE_TYPE_FALSE:
          default:
            // TODO(songya): add support for value types integer and boolean
            throw new TagContextParseException("Unsupported tag value type: " + type);
        }
      }
      return new TagContextImpl(tags);
    } catch (BufferUnderflowException exn) {
      throw new TagContextParseException(exn.toString()); // byte array format error.
    }
  }

  // TODO(sebright): Consider exposing a TagKey name validation method to avoid needing to catch an
  // IllegalArgumentException here.
  private static final TagKeyString createTagKey(String name) throws TagContextParseException {
    try {
      return TagKeyString.create(name);
    } catch (IllegalArgumentException e) {
      throw new TagContextParseException("Invalid tag key: " + name, e);
    }
  }

  // TODO(sebright): Consider exposing a TagValueString validation method to avoid needing to catch
  // an IllegalArgumentException here.
  private static final TagValueString createTagValue(TagKeyString key, String value)
      throws TagContextParseException {
    try {
      return TagValueString.create(value);
    } catch (IllegalArgumentException e) {
      throw new TagContextParseException("Invalid tag value for key " + key + ": " + value, e);
    }
  }

  //  TODO(songya): Currently we only support encoding on string type.
  private static final void encodeStringTag(
      TagString tag, ByteArrayDataOutput byteArrayDataOutput) {
    byteArrayDataOutput.write(VALUE_TYPE_STRING);
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

  private static final class EncodeTagString implements Function<TagString, Void> {
    private final ByteArrayDataOutput output;

    EncodeTagString(ByteArrayDataOutput output) {
      this.output = output;
    }

    @Override
    public Void apply(TagString tag) {
      encodeStringTag(tag, output);
      return null;
    }
  }

  private static final class EncodeTagLong implements Function<TagLong, Void> {
    @Override
    public Void apply(TagLong tag) {
      throw new IllegalArgumentException("long tags are not supported.");
    }
  }

  private static final class EncodeTagBoolean implements Function<TagBoolean, Void> {
    @Override
    public Void apply(TagBoolean tag) {
      throw new IllegalArgumentException("boolean tags are not supported.");
    }
  }
}
