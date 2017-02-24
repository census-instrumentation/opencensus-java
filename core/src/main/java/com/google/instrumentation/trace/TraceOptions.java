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

import javax.annotation.concurrent.Immutable;

/**
 * A class that represents global trace options. These options are propagated to all child {@link
 * Span spans}. These determine features such as whether a {@code Span} should be traced. It is
 * implemented as a bitmask.
 */
@Immutable
public final class TraceOptions {
  /**
   * Bit to represent whether trace is sampled or not. If set, means this {@code Span} is part of a
   * sampled trace and data should be exported to a persistent store.
   */
  public static final int IS_SAMPLED = 0x1;

  private static final int DEFAULT_OPTIONS = 0;

  private static final TraceOptions DEFAULT_TRACE_OPTIONS = new TraceOptions(DEFAULT_OPTIONS);

  // The set of enabled features is determined by all the enabled bits.
  private final int options;

  /**
   * Returns the default {@code TraceOptions}: Sampling disabled.
   *
   * @return the default {@code TraceOptions}.
   */
  public static TraceOptions getDefault() {
    return DEFAULT_TRACE_OPTIONS;
  }

  /**
   * Creates a new {@code TraceOptions} with the given options.
   *
   * @param options the new trace options.
   */
  TraceOptions(int options) {
    this.options = options;
  }

  /**
   * Returns a boolean indicating whether the trace is sampled.
   *
   * @return a boolean indicating whether the trace is sampled.
   */
  public boolean isSampled() {
    return hasOption(IS_SAMPLED);
  }

  /**
   * Returns the current set of options bitmask. This should be used for propagation, by RPC systems
   * or similar.
   *
   * @return the current set of options bitmask.
   */
  int getOptions() {
    return options;
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

  private boolean hasOption(int mask) {
    return (this.options & mask) != 0;
  }
}
