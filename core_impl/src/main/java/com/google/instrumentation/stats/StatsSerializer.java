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

import com.google.instrumentation.stats.proto.StatsContextProto;
import com.google.io.base.VarInt;
import com.google.protobuf.ByteString;

import java.io.InputStream;
import java.io.IOException;
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
  //  * tag_type is one byte, and is used to describe the format of value_bytes.
  //    In particular, the low 2 bits of this byte are used as follows:
  //    00 (value 0): string (UTF-8) encoding
  //    01 (value 1): integer (varint int64 encoding).
  //    10 (value 2): boolean format.
  //    11 (value 3): byte sequence. Arbitrary uninterpreted bytes.
  //    TODO(songya): Currently we only support encoding on string type.
  static final int TYPE_STRING = 0;
  static final int TYPE_INTEGER = 1;
  static final int TYPE_BOOLEAN = 2;
  static final int TYPE_BYTE_SEQUENCE = 3;

  // Serializes a StatsContext by transforming it into a StatsContextProto. The
  // encoded tags are of the form:
  //   [tag_type key_len key_bytes value_len value_bytes]*
  static void serialize(StatsContextImpl context, OutputStream output) throws IOException {
    ByteBuffer buffer = ByteBuffer.allocate(1024);
    // TODO(songya): add support for tag types integer, boolean and byte sequence
    Set<Entry<String, String>> tags = context.tags.entrySet();
    for (Entry<String, String> tag : tags) {
      VarInt.putVarInt(TYPE_STRING, buffer);
      encode(tag.getKey(), buffer);
      encode(tag.getValue(), buffer);
    }
    int length = buffer.capacity() - buffer.remaining();
    buffer.rewind();
    ByteString encodedTags = ByteString.copyFrom(buffer, length);
    StatsContextProto.StatsContext.newBuilder().setTags(encodedTags).build().writeTo(output);
  }

  // Deserializes based on an serialized StatsContextProto. The encoded tags are of the form:
  //   [tag_type key_len key_bytes value_len value_bytes]*
  static StatsContextImpl deserialize(InputStream input) throws IOException {
    try {
      StatsContextProto.StatsContext context = StatsContextProto.StatsContext.parseFrom(input);
      ByteBuffer buffer = context.getTags().asReadOnlyByteBuffer();
      HashMap<String, String> tags = new HashMap<String, String>();
      int limit = buffer.limit();
      while (buffer.position() < limit) {
        int type = VarInt.getVarInt(buffer);
        switch (type) {
          case TYPE_STRING:
            String key = decode(buffer);
            String val = decode(buffer);
            tags.put(key, val);
            break;
          case TYPE_INTEGER:
          case TYPE_BOOLEAN:
          case TYPE_BYTE_SEQUENCE:
            // TODO(songya): add support for tag types integer, boolean and byte sequence
            break;
          default:
            break;
        }
      }
      return new StatsContextImpl(tags);
    } catch (BufferUnderflowException exn) {
      throw new IOException(exn);
    }
  }

  private static final void encode(String input, ByteBuffer buffer) {
    VarInt.putVarInt(input.length(), buffer);
    for (int i = 0; i < input.length(); i++) {
      buffer.put((byte) input.charAt(i));
    }
  }

  private static final String decode(ByteBuffer buffer) {
    int length = VarInt.getVarInt(buffer);
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < length; i++) {
      builder.append((char) buffer.get());
    }
    return builder.toString();
  }
}