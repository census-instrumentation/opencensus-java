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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.util.Formatter;
import java.util.Locale;
import javax.annotation.concurrent.Immutable;

/**
 * A class that represents a span identifier. A span identifier is a 64-bit, unsigned integer.
 */
@Immutable
public final class SpanId {
  private static final long INVALID_SPAN_ID = 0;
  private final long spanId;

  /**
   * The invalid {@code SpanId}.
   */
  public static final SpanId INVALID = new SpanId(INVALID_SPAN_ID);

  /**
   * Creates a span identifier whose value is taken from the given {@code long}.
   *
   * @param spanId a long used as the span identifier.
   */
  SpanId(long spanId) {
    this.spanId = spanId;
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
    return spanId == that.spanId;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(spanId);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("spanId", new Formatter(Locale.US).format("%016x", spanId))
        .toString();
  }
  /**
   * Returns whether the span identifier is valid. A valid span identifer is non-zero long value.
   *
   * @return whether the span identifier is valid.
   */
  public boolean isValid() {
    return spanId != INVALID_SPAN_ID;
  }

  /**
   * Returns the long that represents the span identifier.
   *
   * @return the span identifier.
   */
  public long getSpanId() {
    return spanId;
  }
}
