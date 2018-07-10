/*
 * Copyright 2018, OpenCensus Authors
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

package io.opencensus.testing.propagation;

import com.google.common.collect.ImmutableSet;

/**
 * Utilities for HTTP testing.
 *
 * @since 0.13
 */
public final class HttpUtils {

  private HttpUtils() {}

  private static final ImmutableSet.Builder<Character> ALLOWED_BUILDER =
      ImmutableSet.<Character>builder();

  static {
    ALLOWED_BUILDER.add('!', '#', '$', '%', '&', '\'', '*', '+', '-', '.', '^', '_', '`', '|', '~');
    for (char c = '0'; c <= '9'; c++) {
      ALLOWED_BUILDER.add(c);
    }
    for (char c = 'A'; c <= 'Z'; c++) {
      ALLOWED_BUILDER.add(c);
    }
    for (char c = 'a'; c <= 'z'; c++) {
      ALLOWED_BUILDER.add(c);
    }
  }

  private static final ImmutableSet<Character> ALLOWED_HEADER_CHARACTERS = ALLOWED_BUILDER.build();

  /**
   * A utility method to test if a header name contains invalid characters.
   *
   * <p>Specification for HTTP header name: See RFC-7230, which explicitly defines what characters
   * are accepted. Note that RFC-2616 has different BNF rules on header name, but the derived
   * character set is the same.
   *
   * <pre>
   * field-name = token
   * token      = 1*tchar
   * tchar      = "!" / "#" / "$" / "%" / "&amp;" / "'" / "*" / "+" / "-" / "." / "^" / "_"
   *            / "`" / "|" / "~" / DIGIT / ALPHA
   * </pre>
   *
   * <p>This method is also inspired by
   * https://github.com/netty/netty/blob/fc3b145cbbcad4a257f92f0640eb4ded19b44afe/codec-http/src/main/java/io/netty/handler/codec/http/DefaultHttpHeaders.java#L381:25
   *
   * @param name the name of the header.
   * @throws AssertionError if there are invalid characters in the header name.
   */
  public static void assertHeaderNameIsValid(String name) {
    for (int i = 0; i < name.length(); i++) {
      if (!ALLOWED_HEADER_CHARACTERS.contains(name.charAt(i))) {
        throw new AssertionError("Invalid character at index " + i);
      }
    }
  }
}
