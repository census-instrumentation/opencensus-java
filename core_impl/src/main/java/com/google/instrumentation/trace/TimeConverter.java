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

import com.google.common.annotations.VisibleForTesting;
import com.google.instrumentation.common.Timestamp;
import com.google.instrumentation.common.TimestampFactory;
import javax.annotation.concurrent.Immutable;

/**
 * This class provides a mechanism for converting {@link Time} values to {@link Timestamp}. An
 * instance of this class in initialized with a pair of timestamp/nanoTime measurements that wre
 * taken at approximately the same time.
 */
@Immutable
final class TimeConverter {
  private final Timestamp timestamp;
  private final long nanoTime;

  static TimeConverter now() {
    return new TimeConverter(TimestampFactory.now(), System.nanoTime());
  }

  /**
   * Converts a {@link Time} value to {@link Timestamp}. If the given {@code Time} has a {@code
   * Timestamp} it returns it otherwise converts the nanoTime to a {@code Timestamp} using this
   * converter.
   *
   * @param time value to convert.
   * @return the {@code Timestamp} representation of the {@code time}.
   */
  Timestamp convert(Time time) {
    if (time.hasTimestamp()) {
      return time.getTimestamp();
    }
    return timestamp.addNanos(nanoTime - time.getNanoTime());
  }

  @VisibleForTesting
  TimeConverter(Timestamp timestamp, long nanoTime) {
    this.timestamp = timestamp;
    this.nanoTime = nanoTime;
  }
}
