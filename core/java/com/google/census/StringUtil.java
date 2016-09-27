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

/**
 * Utility methods for working with tag keys, tag values, and metric names.
 */
final class StringUtil {

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

  //Visible for testing
  StringUtil() {
    throw new AssertionError();
  }
}
