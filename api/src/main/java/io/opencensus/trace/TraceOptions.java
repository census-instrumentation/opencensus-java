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

import io.opencensus.internal.DefaultVisibilityForTesting;
import io.opencensus.internal.Utils;
import java.util.Arrays;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A class that represents global trace options. These options are propagated to all child {@link
 * io.opencensus.trace.Span spans}. These determine features such as whether a {@code Span} should
 * be traced. It is implemented as a bitmask.
 *
 * @since 0.5
 */
@Immutable
public final class TraceOptions {
  // Default options. Nothing set.
  private static final byte DEFAULT_OPTIONS = 0;
  // Bit to represent whether trace is sampled or not.
  private static final byte IS_SAMPLED = 0x1;

  /**
   * The size in bytes of the {@code TraceOptions}.
   *
   * @since 0.5
   */
  public static final int SIZE = 1;

  /**
   * The default {@code TraceOptions}.
   *
   * @since 0.5
   */
  public static final TraceOptions DEFAULT = new TraceOptions(DEFAULT_OPTIONS);

  // The set of enabled features is determined by all the enabled bits.
  private final byte options;

  // Creates a new {@code TraceOptions} with the given options.
  private TraceOptions(byte options) {
    this.options = options;
  }

  /**
   * Returns a {@code TraceOptions} built from a byte representation.
   *
   * <p>Equivalent with:
   *
   * <pre>{@code
   * TraceOptions.fromBytes(buffer, 0);
   * }</pre>
   *
   * @param buffer the representation of the {@code TraceOptions}.
   * @return a {@code TraceOptions} whose representation is given by the {@code buffer} parameter.
   * @throws NullPointerException if {@code buffer} is null.
   * @throws IllegalArgumentException if {@code buffer.length} is not {@link TraceOptions#SIZE}.
   * @since 0.5
   */
  public static TraceOptions fromBytes(byte[] buffer) {
    Utils.checkNotNull(buffer, "buffer");
    Utils.checkArgument(
        buffer.length == SIZE, "Invalid size: expected %s, got %s", SIZE, buffer.length);
    return new TraceOptions(buffer[0]);
  }

  /**
   * Returns a {@code TraceOptions} whose representation is copied from the {@code src} beginning at
   * the {@code srcOffset} offset.
   *
   * @param src the buffer where the representation of the {@code TraceOptions} is copied.
   * @param srcOffset the offset in the buffer where the representation of the {@code TraceOptions}
   *     begins.
   * @return a {@code TraceOptions} whose representation is copied from the buffer.
   * @throws NullPointerException if {@code src} is null.
   * @throws IndexOutOfBoundsException if {@code srcOffset+TraceOptions.SIZE} is greater than {@code
   *     src.length}.
   * @since 0.5
   */
  public static TraceOptions fromBytes(byte[] src, int srcOffset) {
    Utils.checkIndex(srcOffset, src.length);
    return new TraceOptions(src[srcOffset]);
  }

  /**
   * Returns the 1-byte array representation of the {@code TraceOptions}.
   *
   * @return the 1-byte array representation of the {@code TraceOptions}.
   * @since 0.5
   */
  public byte[] getBytes() {
    byte[] bytes = new byte[SIZE];
    bytes[0] = options;
    return bytes;
  }

  /**
   * Copies the byte representations of the {@code TraceOptions} into the {@code dest} beginning at
   * the {@code destOffset} offset.
   *
   * <p>Equivalent with (but faster because it avoids any new allocations):
   *
   * <pre>{@code
   * System.arraycopy(getBytes(), 0, dest, destOffset, TraceOptions.SIZE);
   * }</pre>
   *
   * @param dest the destination buffer.
   * @param destOffset the starting offset in the destination buffer.
   * @throws NullPointerException if {@code dest} is null.
   * @throws IndexOutOfBoundsException if {@code destOffset+TraceOptions.SIZE} is greater than
   *     {@code dest.length}.
   * @since 0.5
   */
  public void copyBytesTo(byte[] dest, int destOffset) {
    Utils.checkIndex(destOffset, dest.length);
    dest[destOffset] = options;
  }

  /**
   * Returns a new {@link Builder} with default options.
   *
   * @return a new {@code Builder} with default options.
   * @since 0.5
   */
  public static Builder builder() {
    return new Builder(DEFAULT_OPTIONS);
  }

  /**
   * Returns a new {@link Builder} with all given options set.
   *
   * @param traceOptions the given options set.
   * @return a new {@code Builder} with all given options set.
   * @since 0.5
   */
  public static Builder builder(TraceOptions traceOptions) {
    return new Builder(traceOptions.options);
  }

  /**
   * Returns a boolean indicating whether this {@code Span} is part of a sampled trace and data
   * should be exported to a persistent store.
   *
   * @return a boolean indicating whether the trace is sampled.
   * @since 0.5
   */
  public boolean isSampled() {
    return hasOption(IS_SAMPLED);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
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
    return Arrays.hashCode(new byte[] {options});
  }

  @Override
  public String toString() {
    return "TraceOptions{sampled=" + isSampled() + "}";
  }

  /**
   * Builder class for {@link TraceOptions}.
   *
   * @since 0.5
   */
  public static final class Builder {
    private byte options;

    private Builder(byte options) {
      this.options = options;
    }

    /**
     * @deprecated Use {@code Builder.setIsSampled(true)}.
     * @return this.
     * @since 0.5
     */
    @Deprecated
    public Builder setIsSampled() {
      return setIsSampled(true);
    }

    /**
     * Sets the sampling bit in the options.
     *
     * @param isSampled the sampling bit.
     * @return this.
     * @since 0.7
     */
    public Builder setIsSampled(boolean isSampled) {
      if (isSampled) {
        options = (byte) (options | IS_SAMPLED);
      } else {
        options = (byte) (options & ~IS_SAMPLED);
        ;
      }
      return this;
    }

    /**
     * Builds and returns a {@code TraceOptions} with the desired options.
     *
     * @return a {@code TraceOptions} with the desired options.
     * @since 0.5
     */
    public TraceOptions build() {
      return new TraceOptions(options);
    }
  }

  // Returns the current set of options bitmask.
  @DefaultVisibilityForTesting
  byte getOptions() {
    return options;
  }

  private boolean hasOption(int mask) {
    return (this.options & mask) != 0;
  }
}
