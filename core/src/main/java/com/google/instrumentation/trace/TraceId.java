/*
 * Copyright 2016, Google Inc.
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
 * A class that represents a trace identifier. A valid trace identifier is a 16-byte array with
 * at least one non-zero byte.
 */
@Immutable
public final class TraceId implements Comparable<TraceId> {
  // The size in bytes of the trace id.
  private static final int TRACE_ID_SIZE = 16;
  private final byte[] bytes;

  /**
   * The invalid {@code TraceId}. All bytes are '\0'.
   */
  public static final TraceId INVALID = new TraceId(new byte[TRACE_ID_SIZE]);

  private TraceId(byte[] bytes) {
    this.bytes = bytes;
  }

  /**
   * Returns a {@code TraceId} whose representation is given param.
   *
   * @param bytes the representation of the {@code TraceId}.
   * @return a {@code TraceId} whose representation is given param.
   * @throws NullPointerException if bytes is null.
   * @throws IllegalArgumentException if bytes length is not 16.
   */
  public static TraceId fromBytes(byte[] bytes) {
    checkArgument(bytes.length == TRACE_ID_SIZE, "bytes");
    byte[] bytesCopy = Arrays.copyOf(bytes, TRACE_ID_SIZE);
    return Arrays.equals(bytesCopy, INVALID.bytes) ? INVALID : new TraceId(bytes);
  }

  /**
   * Generates a new random {@code TraceId}.
   *
   * @param random the random number generator.
   * @return a new valid {@code TraceId}.
   */
  public static TraceId generateRandomId(Random random) {
    byte[] bytes = new byte[TRACE_ID_SIZE];
    do {
      random.nextBytes(bytes);
    } while (Arrays.equals(bytes, INVALID.bytes));
    return new TraceId(bytes);
  }

  /**
   * Returns the 16-bytes array representation of the {@code TraceId}.
   *
   * @return the 16-bytes array representation of the {@code TraceId}.
   */
  public byte[] getBytes() {
    return Arrays.copyOf(bytes, TRACE_ID_SIZE);
  }

  /**
   * Returns whether the {@code TraceId} is valid. A valid trace identifier is a 16-byte array with
   * at least one non-zero byte.
   *
   * @return {@code true} if the {@code TraceId} is valid.
   */
  public boolean isValid() {
    return !Arrays.equals(bytes, INVALID.bytes);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }

    if (!(obj instanceof TraceId)) {
      return false;
    }

    TraceId that = (TraceId) obj;
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
            "traceId",
            BaseEncoding.base16().lowerCase().encode(bytes))
        .toString();
  }

  @Override
  public int compareTo(TraceId that) {
    for (int i = 0; i < TRACE_ID_SIZE; i++) {
      if (bytes[i] != that.bytes[i]) {
        return bytes[i] < that.bytes[i] ? -1 : 1;
      }
    }
    return 0;
  }
}
