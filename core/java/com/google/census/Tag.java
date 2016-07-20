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

import java.util.Map.Entry;

/**
 * Immutable representation of a Tag.
 */
public class Tag implements Entry<String, String> {

  public Tag(TagKey key, TagValue value) {
    this.key = key.toString();
    this.value = value.toString();
  }

  @Override
  public String getKey() {
    return key;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public String setValue(String value) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Tag)) {
      return false;
    }
    Tag that = (Tag) obj;
    return key.equals(that.key) && value.equals(that.value);
  }

  @Override
  public int hashCode() {
    return key.hashCode() * 31 + value.hashCode();
  }

  // Assumes key and value have already been sanitized.
  Tag(String key, String value) {
    this.key = key;
    this.value = value;
  }

  static final int MAX_LENGTH = 255;
  static final char UNPRINTABLE_CHAR_SUBSTITUTE = '_';

  static String sanitize(String s) {
    if (s.length() > MAX_LENGTH) {
      s = s.substring(0, MAX_LENGTH);
    }
    if (isPrintableString(s)) {
      return s;
    }
    StringBuilder builder = new StringBuilder(s.length());
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      builder.append(isPrintableChar(c) ? c : UNPRINTABLE_CHAR_SUBSTITUTE);
    }
    return builder.toString();
  }

  private static boolean isPrintableString(String s) {
    for (int i = 0; i < s.length(); i++) {
      if (!isPrintableChar(s.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  private static boolean isPrintableChar(char c) {
    return c >= ' ' && c <= '~';
  }

  private final String key;
  private final String value;
}
