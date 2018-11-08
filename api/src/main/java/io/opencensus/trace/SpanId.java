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

import io.opencensus.internal.Utils;
import java.util.Random;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A class that represents a span identifier. A valid span identifier is an 8-byte array with at
 * least one non-zero byte.
 *
 * @since 0.5
 */
@Immutable
public final class SpanId implements Comparable<SpanId> {
  /**
   * The size in bytes of the {@code SpanId}.
   *
   * @since 0.5
   */
  public static final int SIZE = 8;

  /**
   * The invalid {@code SpanId}. All bytes are 0.
   *
   * @since 0.5
   */
  public static final SpanId INVALID = new SpanId(0);

  private static final int BASE16_SIZE = 2 * SIZE;
  private static final long INVALID_ID = 0;

  // The internal representation of the SpanId.
  private final long id;

  private SpanId(long id) {
    this.id = id;
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
   * @since 0.5
   */
  public static SpanId fromBytes(byte[] buffer) {
    Utils.checkNotNull(buffer, "buffer");
    Utils.checkArgument(
        buffer.length == SIZE, "Invalid size: expected %s, got %s", SIZE, buffer.length);
    return new SpanId(BigendianEncoding.longFromByteArray(buffer, 0));
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
   * @since 0.5
   */
  public static SpanId fromBytes(byte[] src, int srcOffset) {
    return new SpanId(BigendianEncoding.longFromByteArray(src, srcOffset));
  }

  /**
   * Returns a {@code SpanId} built from a lowercase base16 representation.
   *
   * @param src the lowercase base16 representation.
   * @return a {@code SpanId} built from a lowercase base16 representation.
   * @throws NullPointerException if {@code src} is null.
   * @throws IllegalArgumentException if {@code src.length} is not {@code 2 * SpanId.SIZE} OR if the
   *     {@code str} has invalid characters.
   * @since 0.11
   */
  public static SpanId fromLowerBase16(CharSequence src) {
    Utils.checkArgument(
        src.length() == BASE16_SIZE,
        "Invalid size: expected %s, got %s",
        BASE16_SIZE,
        src.length());
    return new SpanId(BigendianEncoding.longFromBase16String(src, 0));
  }

  /**
   * Generates a new random {@code SpanId}.
   *
   * @param random The random number generator.
   * @return a valid new {@code SpanId}.
   * @since 0.5
   */
  public static SpanId generateRandomId(Random random) {
    long id;
    do {
      id = random.nextLong();
    } while (id == INVALID_ID);
    return new SpanId(id);
  }

  /**
   * Returns the byte representation of the {@code SpanId}.
   *
   * @return the byte representation of the {@code SpanId}.
   * @since 0.5
   */
  public byte[] getBytes() {
    byte[] bytes = new byte[SIZE];
    BigendianEncoding.longToByteArray(id, bytes, 0);
    return bytes;
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
   * @since 0.5
   */
  public void copyBytesTo(byte[] dest, int destOffset) {
    BigendianEncoding.longToByteArray(id, dest, destOffset);
  }

  /**
   * Returns whether the span identifier is valid. A valid span identifier is an 8-byte array with
   * at least one non-zero byte.
   *
   * @return {@code true} if the span identifier is valid.
   * @since 0.5
   */
  public boolean isValid() {
    return id != INVALID_ID;
  }

  /**
   * Returns the lowercase base16 encoding of this {@code SpanId}.
   *
   * @return the lowercase base16 encoding of this {@code SpanId}.
   * @since 0.11
   */
  public String toLowerBase16() {
    StringBuilder stringBuilder = new StringBuilder(BASE16_SIZE);
    BigendianEncoding.longToBase16String(id, stringBuilder);
    return stringBuilder.toString();
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
    return id == that.id;
  }

  @Override
  public int hashCode() {
    // Copied from Long.hashCode in java8.
    return (int) (id ^ (id >>> 32));
  }

  @Override
  public String toString() {
    return "SpanId{spanId=" + toLowerBase16() + "}";
  }

  @Override
  public int compareTo(SpanId that) {
    // Copied from Long.compare in java8.
    return (id < that.id) ? -1 : ((id == that.id) ? 0 : 1);
  }
}
