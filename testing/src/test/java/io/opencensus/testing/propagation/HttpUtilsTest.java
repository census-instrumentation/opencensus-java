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

import com.google.common.collect.ImmutableList;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

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
@RunWith(Parameterized.class)
public final class HttpUtilsTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  /** Returns the parameters used in the test. */
  @Parameters(name = "character: ASCII {0}, disallowed: {1}")
  public static ImmutableList<Object[]> parameters() {
    boolean[] disallowed = new boolean[256];

    // CTLs
    for (int i = 0; i <= 31; i++) {
      disallowed[i] = true;
    }

    // DEL and extended ASCII
    for (int i = 127; i < 256; i++) {
      disallowed[i] = true;
    }

    // separators
    disallowed['('] = true;
    disallowed[')'] = true;
    disallowed['<'] = true;
    disallowed['>'] = true;
    disallowed['@'] = true;
    disallowed[','] = true;
    disallowed[';'] = true;
    disallowed[':'] = true;
    disallowed['\\'] = true;
    disallowed['"'] = true;
    disallowed['/'] = true;
    disallowed['['] = true;
    disallowed[']'] = true;
    disallowed['?'] = true;
    disallowed['='] = true;
    disallowed['{'] = true;
    disallowed['}'] = true;
    disallowed[' '] = true;
    disallowed['\t'] = true;

    ImmutableList.Builder<Object[]> parametersBuilder = ImmutableList.<Object[]>builder();
    for (int i = 0; i < 256; i++) {
      parametersBuilder.add(new Object[] {(byte) i, disallowed[i]});
    }
    return parametersBuilder.build();
  }

  /** Parameter for ASCII. */
  @Parameter(0)
  public byte ascii;

  /** Parameter indicating whetehr the ASCII is disallowed. */
  @Parameter(1)
  public boolean disallowed;

  @Test
  public void assertHeaderNameIsValid() {
    if (disallowed) {
      thrown.expect(AssertionError.class);
    }
    HttpUtils.assertHeaderNameIsValid("" + (char) ascii);
  }
}
