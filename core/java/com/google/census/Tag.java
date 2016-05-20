/*
 * Copyright 2016, Google Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *    * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *
 *    * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
