/*
 * Copyright 2017, OpenCensus Authors
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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.common.io.BaseEncoding;
import java.util.Arrays;
import java.util.Random;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A class that represents a span identifier. A valid span identifier is an 8-byte array with at
 * least one non-zero byte.
 */
@Immutable
public final class SpanId implements Comparable<SpanId> {
  /** The size in bytes of the {@code SpanId}. */
  public static final int SIZE = 8;

  /** The invalid {@code SpanId}. All bytes are 0. */
  public static final SpanId INVALID = new SpanId(new byte[SIZE]);

  // The internal representation of the SpanId.
  private final byte[] bytes;

  private SpanId(byte[] bytes) {
    this.bytes = bytes;
  }

  /**
   * Returns a {@code SpanId} built from a byte representation.
   *
   * <p>Equivalent with:
   *
   * <pre>{@code
   * SpanId.fromBytes(buffer, 0);
   * }</pre>
   *
   * @param buffer the representation of the {@code SpanId}.
   * @return a {@code SpanId} whose representation is given by the {@code buffer} parameter.
   * @throws NullPointerException if {@code buffer} is null.
   * @throws IllegalArgumentException if {@code buffer.length} is not {@link SpanId#SIZE}.
   */
  public static SpanId fromBytes(byte[] buffer) {
    checkNotNull(buffer, "buffer");
    checkArgument(buffer.length == SIZE, "Invalid size: expected %s, got %s", SIZE, buffer.length);
    byte[] bytesCopied = Arrays.copyOf(buffer, SIZE);
    return new SpanId(bytesCopied);
  }

  /**
   * Returns a {@code SpanId} whose representation is copied from the {@code src} beginning at the
   * {@code srcOffset} offset.
   *
   * @param src the buffer where the representation of the {@code SpanId} is copied.
   * @param srcOffset the offset in the buffer where the representation of the {@code SpanId}
   *     begins.
   * @return a {@code SpanId} whose representation is copied from the buffer.
   * @throws NullPointerException if {@code src} is null.
   * @throws IndexOutOfBoundsException if {@code srcOffset+SpanId.SIZE} is greater than {@code
   *     src.length}.
   */
  public static SpanId fromBytes(byte[] src, int srcOffset) {
    byte[] bytes = new byte[SIZE];
    System.arraycopy(src, srcOffset, bytes, 0, SIZE);
    return new SpanId(bytes);
  }

  /**
   * Returns a {@code SpanId} built from a lowercase base16 representation.
   *
   * @param src the lowercase base16 representation.
   * @return a {@code SpanId} built from a lowercase base16 representation.
   * @throws NullPointerException if {@code src} is null.
   * @throws IllegalArgumentException if {@code src.length} is not {@code 2 * SpanId.SIZE} OR if the
   *     {@code str} has invalid characters.
   */
  public static SpanId fromLowerBase16(CharSequence src) {
    checkArgument(
        src.length() == 2 * SIZE, "Invalid size: expected %s, got %s", 2 * SIZE, src.length());
    byte[] bytes = BaseEncoding.base16().lowerCase().decode(src);
    return new SpanId(bytes);
  }

  /**
   * Generates a new random {@code SpanId}.
   *
   * @param random The random number generator.
   * @return a valid new {@code SpanId}.
   */
  public static SpanId generateRandomId(Random random) {
    byte[] bytes = new byte[SIZE];
    do {
      random.nextBytes(bytes);
    } while (Arrays.equals(bytes, INVALID.bytes));
    return new SpanId(bytes);
  }

  /**
   * Returns the byte representation of the {@code SpanId}.
   *
   * @return the byte representation of the {@code SpanId}.
   */
  public byte[] getBytes() {
    return Arrays.copyOf(bytes, SIZE);
  }

  /**
   * Copies the byte array representations of the {@code SpanId} into the {@code dest} beginning at
   * the {@code destOffset} offset.
   *
   * <p>Equivalent with (but faster because it avoids any new allocations):
   *
   * <pre>{@code
   * System.arraycopy(getBytes(), 0, dest, destOffset, SpanId.SIZE);
   * }</pre>
   *
   * @param dest the destination buffer.
   * @param destOffset the starting offset in the destination buffer.
   * @throws NullPointerException if {@code dest} is null.
   * @throws IndexOutOfBoundsException if {@code destOffset+SpanId.SIZE} is greater than {@code
   *     dest.length}.
   */
  public void copyBytesTo(byte[] dest, int destOffset) {
    System.arraycopy(bytes, 0, dest, destOffset, SIZE);
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

  /**
   * Returns the lowercase base16 encoding of this {@code SpanId}.
   *
   * @return the lowercase base16 encoding of this {@code SpanId}.
   */
  public String toLowerBase16() {
    return BaseEncoding.base16().lowerCase().encode(bytes);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
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
        .add("spanId", BaseEncoding.base16().lowerCase().encode(bytes))
        .toString();
  }

  @Override
  public int compareTo(SpanId that) {
    for (int i = 0; i < SIZE; i++) {
      if (bytes[i] != that.bytes[i]) {
        return bytes[i] < that.bytes[i] ? -1 : 1;
      }
    }
    return 0;
  }
}
