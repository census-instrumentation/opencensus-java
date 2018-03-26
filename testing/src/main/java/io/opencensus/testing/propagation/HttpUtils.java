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

/**
 * Utilities for HTTP testing.
 *
 * @since 0.13
 */
public final class HttpUtils {

  private HttpUtils() {}

  /**
   * Returns {@code false} if a header name contains invalid characters, otherwise {@code true}.
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
   * @return {@code false} if a header name contains invalid characters, otherwise {@code true}.
   */
  public static boolean validateHeaderName(String name) {
    for (int i = 0; i < name.length(); i++) {
      char c = name.charAt(i);
      switch (c) {
        case '!':
        case '#':
        case '$':
        case '%':
        case '&':
        case '\'':
        case '*':
        case '+':
        case '-':
        case '.':
        case '^':
        case '_':
        case '`':
        case '|':
        case '~':
          continue;
        default:
          if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
            continue;
          }
          return false;
      }
    }
    return true;
  }
}
