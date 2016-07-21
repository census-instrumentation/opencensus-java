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

package com.google.census;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map.Entry;

import javax.annotation.Nullable;

/** Native implementation {@link CensusContext} serialization. */
final class CensusSerializer {
  private static final char TAG_PREFIX = '\2';
  private static final char TAG_DELIM = '\3';

  // The serialized tags are of the form: (<tag prefix> + 'key' + <tag delim> + 'value')*
  static ByteBuffer serialize(CensusContextImpl context) {
    StringBuilder builder = new StringBuilder();
    for (Entry<String, String> tag : context.tags.entrySet()) {
      builder
          .append(TAG_PREFIX)
          .append(tag.getKey())
          .append(TAG_DELIM)
          .append(tag.getValue());
    }
    return ByteBuffer.wrap(builder.toString().getBytes(UTF_8));
  }

  // The serialized tags are of the form: (<tag prefix> + 'key' + <tag delim> + 'value')*
  @Nullable
  static CensusContextImpl deserialize(ByteBuffer buffer) {
    String input = new String(buffer.array(), UTF_8);
    HashMap<String, String> tags = new HashMap<>();
    if (!input.matches("(\2[^\2\3]*\3[^\2\3]*)*")) {
      return null;
    }
    if (!input.isEmpty()) {
      int keyIndex = 0;
      do {
        int valIndex = input.indexOf(TAG_DELIM, keyIndex + 1);
        String key = input.substring(keyIndex + 1, valIndex);
        keyIndex = input.indexOf(TAG_PREFIX, valIndex + 1);
        String val = input.substring(valIndex + 1, keyIndex == -1 ? input.length() : keyIndex);
        tags.put(key, val);
      } while (keyIndex != -1);
    }
    return new CensusContextImpl(tags);
  }
}
