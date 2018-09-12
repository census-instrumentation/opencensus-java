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

package io.opencensus.trace;

import io.opencensus.internal.Utils;
import java.util.Arrays;

/** Internal copy of the Guava implementation of the {@code BaseEncoding.base16().lowerCase()}. */
final class LowerCaseBase16Encoding {
  private static final String ALPHABET = "0123456789abcdef";
  private static final int ASCII_CHARACTERS = 128;
  private static final char[] ENCODING = buildEncodingArray();
  private static final byte[] DECODING = buildDecodingArray();

  private static char[] buildEncodingArray() {
    char[] encoding = new char[512];
    for (int i = 0; i < 256; ++i) {
      encoding[i] = ALPHABET.charAt(i >>> 4);
      encoding[i | 0x100] = ALPHABET.charAt(i & 0xF);
    }
    return encoding;
  }

  private static byte[] buildDecodingArray() {
    byte[] decoding = new byte[ASCII_CHARACTERS];
    Arrays.fill(decoding, (byte) -1);
    for (int i = 0; i < ALPHABET.length(); i++) {
      char c = ALPHABET.charAt(i);
      decoding[c] = (byte) i;
    }
    return decoding;
  }

  /**
   * Encodes the specified byte array, and returns the encoded {@code String}.
   *
   * @param bytes byte array to be encoded.
   * @return the encoded {@code String}.
   */
  static String encodeToString(byte[] bytes) {
    StringBuilder stringBuilder = new StringBuilder(bytes.length * 2);
    for (byte byteVal : bytes) {
      int b = byteVal & 0xFF;
      stringBuilder.append(ENCODING[b]);
      stringBuilder.append(ENCODING[b | 0x100]);
    }
    return stringBuilder.toString();
  }

  /**
   * Decodes the specified character sequence, and returns the resulting {@code byte[]}.
   *
   * @param chars the character sequence to be decoded.
   * @return the resulting {@code byte[]}
   * @throws IllegalArgumentException if the input is not a valid encoded string according to this
   *     encoding.
   */
  static byte[] decodeToBytes(CharSequence chars) {
    Utils.checkArgument(chars.length() % 2 == 0, "Invalid input length " + chars.length());
    int bytesWritten = 0;
    byte[] bytes = new byte[chars.length() / 2];
    for (int i = 0; i < chars.length(); i += 2) {
      bytes[bytesWritten++] = decodeByte(chars.charAt(i), chars.charAt(i + 1));
    }
    return bytes;
  }

  private static byte decodeByte(char hi, char lo) {
    Utils.checkArgument(lo < ASCII_CHARACTERS && DECODING[lo] != -1, "Invalid character " + lo);
    Utils.checkArgument(hi < ASCII_CHARACTERS && DECODING[hi] != -1, "Invalid character " + hi);
    int decoded = DECODING[hi] << 4 | DECODING[lo];
    return (byte) decoded;
  }

  // Private constructor to disallow instances.
  private LowerCaseBase16Encoding() {}
}
