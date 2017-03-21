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
import com.google.instrumentation.common.Timestamp;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Class that encapsulates one of values {@link Timestamp} or nanoTime. Used for internal
 * representation of the time.
 */
@Immutable
final class Time {
  private final Timestamp timestamp;
  private final long nanoTime;

  /**
   * Returns the {@code Time} with the given {@code timestamp}.
   *
   * @param timestamp the timestamp associated with this {@code Time}.
   * @return the {@code Time} with the given {@code timestamp}.
   */
  static Time withTimestamp(Timestamp timestamp) {
    return new Time(timestamp, 0);
  }

  /**
   * Returns the {@code Time} with the given {@code nanoTime}.
   *
   * @param nanoTime the nano time associated with this {@code Time}.
   * @return the {@code Time} with the given {@code nanoTime}.
   */
  static Time withNanoTime(long nanoTime) {
    return new Time(null, nanoTime);
  }

  /**
   * Returns {@code true} if this {@code Time} has a timestamp encapsulated, otherwise {@code
   * false}.
   *
   * @return {@code true} if this {@code Time} has a timestamp encapsulated, otherwise {@code
   *     false}.
   */
  boolean hasTimestamp() {
    return timestamp != null;
  }

  /**
   * Returns the timestamp associated with this {@code Time}.
   *
   * @return the timestamp associated with this {@code Time}.
   */
  Timestamp getTimestamp() {
    return timestamp;
  }

  /**
   * Returns the nano time associated with this {@code Time}.
   *
   * @return the nano time associated with this {@code Time}.
   */
  long getNanoTime() {
    return nanoTime;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("timestamp", timestamp)
        .add("nanoTime", nanoTime)
        .toString();
  }

  private Time(@Nullable Timestamp timestamp, long nanoTime) {
    this.timestamp = timestamp;
    this.nanoTime = nanoTime;
  }
}
