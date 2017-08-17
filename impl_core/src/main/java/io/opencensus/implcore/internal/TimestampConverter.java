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

package io.opencensus.implcore.internal;

import io.opencensus.common.Clock;
import io.opencensus.common.Timestamp;
import javax.annotation.concurrent.Immutable;

/**
 * This class provides a mechanism for converting {@link System#nanoTime() nanoTime} values to
 * {@link Timestamp}.
 */
@Immutable
public final class TimestampConverter {
  private final Timestamp timestamp;
  private final long nanoTime;

  // Returns a WallTimeConverter initialized to now.
  public static TimestampConverter now(Clock clock) {
    return new TimestampConverter(clock.now(), clock.nowNanos());
  }

  /**
   * Converts a {@link System#nanoTime() nanoTime} value to {@link Timestamp}.
   *
   * @param nanoTime value to convert.
   * @return the {@code Timestamp} representation of the {@code time}.
   */
  public Timestamp convertNanoTime(long nanoTime) {
    return timestamp.addNanos(nanoTime - this.nanoTime);
  }

  private TimestampConverter(Timestamp timestamp, long nanoTime) {
    this.timestamp = timestamp;
    this.nanoTime = nanoTime;
  }
}
