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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.util.Formatter;
import java.util.Locale;
import javax.annotation.concurrent.Immutable;

/**
 * A class that represents a trace identifier. A trace identifier is a 128-bit, unsigned integer.
 * The value 0 is considered invalid.
 */
@Immutable
public final class TraceId {
  private static final TraceId INVALID_ID = new TraceId(0, 0);
  private final long traceIdHi;
  private final long traceIdLo;

  /**
   * Creates a new {@code TraceId} whose value is taken from the given params.
   *
   * @param traceIdHi the higher bits.
   * @param traceIdLo the lower bits.
   */
  TraceId(long traceIdHi, long traceIdLo) {
    this.traceIdHi = traceIdHi;
    this.traceIdLo = traceIdLo;
  }

  /**
   * Returns the high 64 bits of the {@code TraceId}.
   *
   * @return the high 64 bits of the {@code TraceId}.
   */
  public long getTraceIdHi() {
    return traceIdHi;
  }

  /**
   * Returns the low 64 bits of the {@code TraceId}.
   *
   * @return the low 64 bits of the {@code TraceId}.
   */
  public long getTraceIdLo() {
    return traceIdLo;
  }

  /**
   * Returns true if the {@code TraceId} is valid.
   *
   * @return true if the {@code TraceId} is valid.
   */
  public boolean isValid() {
    return !this.equals(INVALID_ID);
  }

  /**
   * Returns the invalid {@code TraceId}.
   *
   * @return the invalid {@code TraceId}.
   */
  public static TraceId getInvalid() {
    return INVALID_ID;
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
    return traceIdLo == that.traceIdLo && traceIdHi == that.traceIdHi;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(traceIdHi, traceIdLo);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("traceId", new Formatter(Locale.US).format("%016x%016x", traceIdHi, traceIdLo))
        .toString();
  }
}
