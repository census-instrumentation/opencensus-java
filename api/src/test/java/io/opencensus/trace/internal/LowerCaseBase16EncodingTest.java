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

package io.opencensus.trace.internal;

import static com.google.common.truth.Truth.assertThat;

import java.nio.charset.Charset;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link LowerCaseBase16Encoding}. */
@RunWith(JUnit4.class)
public class LowerCaseBase16EncodingTest {
  private static final Charset CHARSET = Charset.forName("UTF-8");

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void valid_EncodeDecode() {
    testEncoding("", "");
    testEncoding("f", "66");
    testEncoding("fo", "666f");
    testEncoding("foo", "666f6f");
    testEncoding("foob", "666f6f62");
    testEncoding("fooba", "666f6f6261");
    testEncoding("foobar", "666f6f626172");
  }

  @Test
  public void invalidDecodings_UnrecongnizedCharacters() {
    // These contain bytes not in the decoding.
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Invalid character g");
    LowerCaseBase16Encoding.getInstance().decodeToBytes("efhg");
  }

  @Test
  public void invalidDecodings_InvalidInputLength() {
    // Valid base16 strings always have an even length.
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Invalid input length 3");
    LowerCaseBase16Encoding.getInstance().decodeToBytes("abc");
  }

  @Test
  public void invalidDecodings_InvalidInputLengthAndCharacter() {
    // These have a combination of invalid length and unrecognized characters.
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Invalid input length 1");
    LowerCaseBase16Encoding.getInstance().decodeToBytes("?");
  }

  private static void testEncoding(String decoded, String encoded) {
    testEncodes(decoded, encoded);
    testDecodes(encoded, decoded);
  }

  private static void testEncodes(String decoded, String encoded) {
    assertThat(LowerCaseBase16Encoding.getInstance().encodeToString(decoded.getBytes(CHARSET)))
        .isEqualTo(encoded);
  }

  private static void testDecodes(String encoded, String decoded) {
    assertThat(LowerCaseBase16Encoding.getInstance().decodeToBytes(encoded))
        .isEqualTo(decoded.getBytes(CHARSET));
  }
}
