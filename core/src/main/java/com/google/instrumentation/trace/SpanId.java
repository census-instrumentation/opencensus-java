/*
 * Copyright 2017, Google Inc.
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

package com.google.instrumentation.trace;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.MoreObjects;
import com.google.common.io.BaseEncoding;

import java.util.Arrays;
import java.util.Random;
import javax.annotation.concurrent.Immutable;

/**
 * A class that represents a span identifier. A valid span identifier is an 8-byte array with
 * at least one non-zero byte.
 */
@Immutable
public final class SpanId implements Comparable<SpanId> {
  // The size in bytes of the span id.
  private static final int SPAN_ID_SIZE = 8;
  private final byte[] bytes;

  private SpanId(byte[] bytes) {
    this.bytes = bytes;
  }

  /**
   * The invalid {@code SpanId}. All bytes are 0.
   */
  public static final SpanId INVALID = new SpanId(new byte[SPAN_ID_SIZE]);

  /**
   * Returns a {@code SpanId} whose representation is given param.
   *
   * @param bytes the representation of the {@code SpanId}.
   * @return a {@code SpanId} whose representation is given param.
   * @throws NullPointerException if bytes is null.
   * @throws IllegalArgumentException if bytes length is not 8.
   */
  public static SpanId fromBytes(byte[] bytes) {
    checkArgument(bytes.length == SPAN_ID_SIZE, "bytes");
    byte[] bytesCopy = Arrays.copyOf(bytes, SPAN_ID_SIZE);
    return Arrays.equals(bytesCopy, INVALID.bytes) ? INVALID : new SpanId(bytes);
  }

  /**
   * Generates a new random {@code SpanId}.
   *
   * @param random The random number generator.
   * @return a valid new {@code SpanId}.
   */
  public static SpanId generateRandomId(Random random) {
    byte[] bytes = new byte[SPAN_ID_SIZE];
    do {
      random.nextBytes(bytes);
    } while (Arrays.equals(bytes, INVALID.bytes));
    return new SpanId(bytes);
  }

  /**
   * Returns the 8-bytes array representation of the {@code SpanId}.
   *
   * @return the 8-bytes array representation of the {@code SpanId}.
   */
  public byte[] getBytes() {
    return Arrays.copyOf(bytes, SPAN_ID_SIZE);
  }


  /**
   * Returns whether the span identifier is valid. A valid span identifier is an 8-byte array with
   * at least one non-zero byte.
   *
   * @return {@code true} if the span identifier is valid.
   */
  public boolean isValid() {
    return !Arrays.equals(bytes, INVALID.bytes);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }

    if (!(obj instanceof SpanId)) {
      return false;
    }

    SpanId that = (SpanId) obj;
    return Arrays.equals(bytes, that.bytes);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(bytes);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add(
            "spanId",
            BaseEncoding.base16().lowerCase().encode(bytes))
        .toString();
  }

  @Override
  public int compareTo(SpanId that) {
    for (int i = 0; i < SPAN_ID_SIZE; i++) {
      if (bytes[i] != that.bytes[i]) {
        return bytes[i] < that.bytes[i] ? -1 : 1;
      }
    }
    return 0;
  }
}
