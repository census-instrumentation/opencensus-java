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
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import javax.annotation.concurrent.Immutable;

/**
 * A class that represents global trace options. These options are propagated to all child {@link
 * Span spans}. These determine features such as whether a {@code Span} should be traced. It is
 * implemented as a bitmask.
 */
@Immutable
public final class TraceOptions {
  // Default options. Nothing set.
  private static final int DEFAULT_OPTIONS = 0;
  // Mask to extract a byte value.
  private static final int BYTE_MASK = 0xFF;
  // Bit to represent whether trace is sampled or not.
  private static final int IS_SAMPLED = 0x1;

  /**
   * The size in bytes of the {@code TraceOptions}.
   */
  public static final int SIZE = 4;

  /**
   * The default {@code TraceOptions}.
   */
  public static final TraceOptions DEFAULT = new TraceOptions(DEFAULT_OPTIONS);

  // The set of enabled features is determined by all the enabled bits.
  private final int options;

  // Creates a new {@code TraceOptions} with the given options.
  private TraceOptions(int options) {
    this.options = options;
  }

  /**
   * Returns a {@code TraceOptions} built from a byte representation.
   *
   * @param bytes the representation of the {@code TraceOptions}.
   * @return a {@code TraceOptions} whose representation is given by the {@code bytes} parameter.
   * @throws NullPointerException if {@code bytes} is null.
   * @throws IllegalArgumentException if {@code bytes.length} is not 4.
   */
  public static TraceOptions fromBytes(byte[] bytes) {
    checkNotNull(bytes);
    checkArgument(bytes.length == SIZE);
    return new TraceOptions(intFromBytes(bytes, 0));
  }

  /**
   * Returns the 4-byte array representation of the {@code TraceOptions}.
   *
   * @return the 4-byte array representation of the {@code TraceOptions}.
   */
  public byte[] getBytes() {
    byte[] bytes = new byte[SIZE];
    intToBytes(options, bytes, 0);
    return bytes;
  }

  /**
   * Returns a new {@link Builder} with default options.
   *
   * @return a new {@code Builder} with default options.
   */
  public static Builder builder() {
    return new Builder(DEFAULT_OPTIONS);
  }

  /**
   * Returns a new {@link Builder} with all given options set.
   *
   * @param traceOptions the given options set.
   * @return a new {@code Builder} with all given options set.
   */
  public static Builder builder(TraceOptions traceOptions) {
    return new Builder(traceOptions.options);
  }

  /**
   * Returns a boolean indicating whether this {@code Span} is part of a sampled trace and data
   * should be exported to a persistent store.
   *
   * @return a boolean indicating whether the trace is sampled.
   */
  public boolean isSampled() {
    return hasOption(IS_SAMPLED);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }

    if (!(obj instanceof TraceOptions)) {
      return false;
    }

    TraceOptions that = (TraceOptions) obj;
    return options == that.options;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(options);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("sampled", isSampled()).toString();
  }

  /**
   * Builder class for {@link TraceOptions}.
   */
  public static final class Builder {
    private int options;

    private Builder(int options) {
      this.options = options;
    }

    /**
     * Marks this trace as sampled.
     *
     * @return this.
     */
    Builder setIsSampled() {
      options |= IS_SAMPLED;
      return this;
    }

    /**
     * Builds and returns a {@code TraceOptions} with the desired options.
     *
     * @return a {@code TraceOptions} with the desired options.
     */
    TraceOptions build() {
      return new TraceOptions(options);
    }
  }

  // Returns the current set of options bitmask.
  @VisibleForTesting
  int getOptions() {
    return options;
  }

  private boolean hasOption(int mask) {
    return (this.options & mask) != 0;
  }

  // Returns the int value whose big-endian representation is stored in the first 4 bytes of src.
  private static int intFromBytes(byte[] src, int srcPos) {
    return src[srcPos] << (3 * Byte.SIZE)
        | (src[srcPos + 1] & BYTE_MASK) << (2 * Byte.SIZE)
        | (src[srcPos + 2] & BYTE_MASK) << Byte.SIZE
        | (src[srcPos + 3] & BYTE_MASK);
  }

  // Appends the big-endian representation of value as a 4-element byte array to the destination.
  private static void intToBytes(int value, byte[] dest, int destPos) {
    dest[destPos] = (byte) (value >> (3 * Byte.SIZE));
    dest[destPos + 1] = (byte) (value >> (2 * Byte.SIZE));
    dest[destPos + 2] = (byte) (value >> Byte.SIZE);
    dest[destPos + 3] = (byte) value;
  }
}
