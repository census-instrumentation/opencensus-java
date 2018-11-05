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

final class BigendianEncoding {
  static final int LONG_BYTES = Long.SIZE / Byte.SIZE;

  /**
   * Returns the {@code long} value whose big-endian representation is stored in the first 8 bytes
   * of {@code bytes} starting from the {@code offset}.
   *
   * @param bytes the byte array representation of the {@code long}.
   * @param offset the starting offset in the byte array.
   * @return the {@code long} value whose big-endian representation is given.
   * @throws IllegalArgumentException if {@code bytes} has fewer than 8 elements.
   */
  static long longFromByteArray(byte[] bytes, int offset) {
    Utils.checkArgument(bytes.length >= offset + LONG_BYTES, "array too small");
    return (bytes[offset] & 0xFFL) << 56
        | (bytes[offset + 1] & 0xFFL) << 48
        | (bytes[offset + 2] & 0xFFL) << 40
        | (bytes[offset + 3] & 0xFFL) << 32
        | (bytes[offset + 4] & 0xFFL) << 24
        | (bytes[offset + 5] & 0xFFL) << 16
        | (bytes[offset + 6] & 0xFFL) << 8
        | (bytes[offset + 7] & 0xFFL);
  }

  /**
   * Stores the big-endian representation of {@code value} in the {@code dest} starting from the
   * {@code destOffset}.
   *
   * @param value the value to be converted.
   * @param dest the destination byte array.
   * @param destOffset the starting offset in the destination byte array.
   */
  static void longToByteArray(long value, byte[] dest, int destOffset) {
    Utils.checkArgument(dest.length >= destOffset + LONG_BYTES, "array too small");
    dest[destOffset + 7] = (byte) (value & 0xFFL);
    dest[destOffset + 6] = (byte) (value >> 8 & 0xFFL);
    dest[destOffset + 5] = (byte) (value >> 16 & 0xFFL);
    dest[destOffset + 4] = (byte) (value >> 24 & 0xFFL);
    dest[destOffset + 3] = (byte) (value >> 32 & 0xFFL);
    dest[destOffset + 2] = (byte) (value >> 40 & 0xFFL);
    dest[destOffset + 1] = (byte) (value >> 48 & 0xFFL);
    dest[destOffset] = (byte) (value >> 56 & 0xFFL);
  }

  private BigendianEncoding() {}
}
