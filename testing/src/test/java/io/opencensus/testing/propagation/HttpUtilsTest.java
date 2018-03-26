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

import static com.google.common.truth.Truth.assertWithMessage;

import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link HttpUtils}.
 *
 * <p>Test cases comes from RFC-2616. The character set defined in this RFC is actually the same
 * with the one in RFC-7230.
 *
 * <pre>
 * token      = 1*&lt;any CHAR except CTLs or separators&gt;
 * CTL        = &lt;any US-ASCII control character(octets 0 - 31) and DEL (127)&gt;
 * separators = "(" | ")" | "&lt;" | "&gt;" | "{@literal @}" | "," | ";" | ":" | "\" | &lt;"&gt;
 *            | "/" | "[" | "]" | "?" | "=" | "{" | "}" | SP | HT
 * SP         = &lt;US-ASCII SP, space (32)&gt;
 * HT         = &lt;US-ASCII HT, horizontal-tab (9)&gt;
 * </pre>
 */
@RunWith(JUnit4.class)
public final class HttpUtilsTest {

  private final Set<Character> disallowed = new HashSet<Character>();

  @Before
  public void setUp() {
    // non-ascii and CTLs
    for (int i = -128; i <= 31; i++) {
      disallowed.add((char) i);
    }
    disallowed.add((char) 127);
    // separators
    disallowed.add('(');
    disallowed.add(')');
    disallowed.add('<');
    disallowed.add('>');
    disallowed.add('@');
    disallowed.add(',');
    disallowed.add(';');
    disallowed.add(':');
    disallowed.add('\\');
    disallowed.add('"');
    disallowed.add('/');
    disallowed.add('[');
    disallowed.add(']');
    disallowed.add('?');
    disallowed.add('=');
    disallowed.add('{');
    disallowed.add('}');
    disallowed.add(' ');
    disallowed.add('\t');
  }

  @Test
  public void allowedAndDisallowed() {
    for (int i = -128; i <= 127; i++) {
      char ch = (char) i;
      String header = "" + ch;
      if (disallowed.contains(ch)) {
        assertWithMessage(String.format("ASCII %d (%s)", i, header))
            .that(HttpUtils.validateHeaderName(header))
            .isEqualTo(false);
      } else {
        assertWithMessage(String.format("ASCII %d (%s)", i, header))
            .that(HttpUtils.validateHeaderName(header))
            .isEqualTo(true);
      }
    }
  }
}
