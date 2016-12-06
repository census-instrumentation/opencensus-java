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
import com.google.protobuf.InvalidProtocolBufferException;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.annotation.Nullable;

/**
 * Native implementation {@link StatsContext} serialization.
 */
final class StatsSerializer {
  private static final char TAG_PREFIX = '\2';
  private static final char TAG_DELIM = '\3';


  // Serializes a StatsContext by transforming it into a StatsContextProto. The
  // encoded tags are of the form: (<tag prefix> + 'key' + <tag delim> + 'value')*
  static ByteBuffer serialize(StatsContextImpl context) {
    StringBuilder builder = new StringBuilder();
    for (Entry<String, String> tag : context.tags.entrySet()) {
      builder
          .append(TAG_PREFIX)
          .append(tag.getKey())
          .append(TAG_DELIM)
          .append(tag.getValue());
    }
    return ByteBuffer.wrap(StatsContextProto.StatsContext
        .newBuilder().setTags(builder.toString()).build().toByteArray());
  }

  // Deserializes based on an serialized StatsContextProto. The encoded tags are of the form:
  // (<tag prefix> + 'key' + <tag delim> + 'value')*
  @Nullable
  static StatsContextImpl deserialize(ByteBuffer buffer) {
    try {
      StatsContextProto.StatsContext context =
          StatsContextProto.StatsContext.parser().parseFrom(buffer.array());
      return new StatsContextImpl(tagsFromString(context.getTags()));
    } catch (IllegalArgumentException e) {
      return null;
    } catch (InvalidProtocolBufferException e) {
      return null;
    }
  }

  private static HashMap<String, String> tagsFromString(String encoded) {
    HashMap<String, String> tags = new HashMap<String, String>();
    int length = encoded.length();
    int index = 0;
    while (index != length) {
      int valIndex = endOfKey(encoded, index);
      String key = encoded.substring(index + 1, valIndex);
      index = endOfValue(encoded, valIndex);
      String val = encoded.substring(valIndex + 1, index);
      tags.put(key, val);
    }
    return tags;
  }

  private static int endOfKey(String encoded, int index) {
    int valIndex = encoded.indexOf(TAG_DELIM, index);
    if (valIndex == -1) {
      throw new IllegalArgumentException("Missing tag delimiter.");
    }
    int keyIndex = encoded.lastIndexOf(TAG_PREFIX, valIndex);
    if (keyIndex != index) {
      throw new IllegalArgumentException("Missing tag prefix.");
    }
    return valIndex;
  }

  private static int endOfValue(String encoded, int index) {
    int keyIndex = encoded.indexOf(TAG_PREFIX, index);
    if (keyIndex == -1) {
      keyIndex = encoded.length();
    }
    int valIndex = encoded.lastIndexOf(TAG_DELIM, keyIndex);
    if (valIndex != index) {
      throw new IllegalArgumentException("Missing tag delimiter.");
    }
    return keyIndex;
  }
}
