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

import io.opencensus.common.Internal;
import io.opencensus.internal.Utils;
import java.util.Arrays;

/** Internal copy of the Guava implementation of the {@code BaseEncoding.base16().lowerCase()}. */
@Internal
public final class LowerCaseBase16Encoding {
  private static final LowerCaseBase16Encoding INSTANCE = new LowerCaseBase16Encoding();
  private static final String ALPHABET = "0123456789abcdef";
  private static final int ASCII_CHARACTERS = 128;
  private final char[] encoding = new char[512];
  private final byte[] decoding = new byte[ASCII_CHARACTERS];

  private LowerCaseBase16Encoding() {
    for (int i = 0; i < 256; ++i) {
      encoding[i] = ALPHABET.charAt(i >>> 4);
      encoding[i | 0x100] = ALPHABET.charAt(i & 0xF);
    }

    Arrays.fill(decoding, (byte) -1);
    for (int i = 0; i < ALPHABET.length(); i++) {
      char c = ALPHABET.charAt(i);
      decoding[c] = (byte) i;
    }
  }

  /**
   * Encodes the specified byte array, and returns the encoded {@code String}.
   *
   * @param bytes byte array to be encoded.
   * @return the encoded {@code String}.
   */
  public String encodeToString(byte[] bytes) {
    StringBuilder stringBuilder = new StringBuilder(bytes.length * 2);
    for (byte byteVal : bytes) {
      int b = byteVal & 0xFF;
      stringBuilder.append(encoding[b]);
      stringBuilder.append(encoding[b | 0x100]);
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
  public byte[] decodeToBytes(CharSequence chars) {
    Utils.checkArgument(chars.length() % 2 == 0, "Invalid input length " + chars.length());
    int bytesWritten = 0;
    byte[] bytes = new byte[chars.length() / 2];
    for (int i = 0; i < chars.length(); i += 2) {
      bytes[bytesWritten++] = decodeByte(chars.charAt(i), chars.charAt(i + 1));
    }
    return bytes;
  }

  private byte decodeByte(char hi, char lo) {
    Utils.checkArgument(lo < ASCII_CHARACTERS && decoding[lo] != -1, "Invalid character " + lo);
    Utils.checkArgument(hi < ASCII_CHARACTERS && decoding[hi] != -1, "Invalid character " + hi);
    int decoded = decoding[hi] << 4 | decoding[lo];
    return (byte) decoded;
  }

  public static LowerCaseBase16Encoding getInstance() {
    return INSTANCE;
  }
}
