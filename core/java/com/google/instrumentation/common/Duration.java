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

package com.google.instrumentation.common;

/**
 * Represents a signed, fixed-length span of time represented as a count of seconds and fractions
 * of seconds at nanosecond resolution. It is independent of any calendar and concepts like "day"
 * or "month". Range is approximately +-10,000 years.
 */
public class Duration {
  /**
   * Creates a new timestamp from given seconds and nanoseconds.
   *
   * @param seconds Signed seconds of the span of time. Must be from -315,576,000,000
   *     to +315,576,000,000 inclusive.
   *
   * @param nanos Signed fractions of a second at nanosecond resolution of the span
   *     of time. Durations less than one second are represented with a 0
   *     `seconds` field and a positive or negative `nanos` field. For durations
   *     of one second or more, a non-zero value for the `nanos` field must be
   *     of the same sign as the `seconds` field. Must be from -999,999,999
   *     to +999,999,999 inclusive.
   */
  public static Duration create(long seconds, int nanos) {
    return new Duration(seconds, nanos);
  }

  /**
   * Creates a new timestamp from given seconds and nanoseconds.
   */
  public static Duration fromMillis(long millis) {
    long seconds = millis / NUM_MILLIS_PER_SECOND;
    int nanos = (int) (millis % NUM_MILLIS_PER_SECOND) * NUM_NANOS_PER_MILLI;
    return new Duration(seconds, seconds < 0 ? -nanos : nanos);
  }

  /**
   * Returns the number of seconds since the Unix Epoch represented by this timestamp.
   *
   * @return the number of seconds since the Unix Epoch.
   */
  public long getSeconds() {
    return seconds;
  }

  /**
   * Returns the number of nanoseconds after the number of seconds since the Unix Epoch represented
   * by this timestamp.
   *
   * @return the number of nanoseconds after the number of seconds since the Unix Epoch.
   */
  public int getNanos() {
    return nanos;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }

    if (!(obj instanceof Duration)) {
      return false;
    }

    Duration that = (Duration) obj;
    return seconds == that.seconds && nanos == that.nanos;
  }

  @Override
  public int hashCode() {
    int result = 17;
    result = 31 * result + (int) (seconds ^ (seconds >>> 32));
    result = 31 * result + nanos;
    return result;
  }

  private static final long NUM_MILLIS_PER_SECOND = 1000L;
  private static final int NUM_NANOS_PER_MILLI = 1000000;
  private final long seconds;
  private final int nanos;

  private Duration(long seconds, int nanos) {
    this.seconds = seconds;
    this.nanos = nanos;
  }
}
