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

import java.util.Objects;

/**
 * A representation of an instant in time. The instant is the number of nanoseconds after the number
 * of seconds since the Unix Epoch.
 */
public class Timestamp {
  private static final long NUM_MILLIS_PER_SECOND = 1000L;
  private static final int NUM_NANOS_PER_MILLI = 1000000;
  private final long seconds;
  private final int nanos;

  private Timestamp(long seconds, int nanos) {
    this.seconds = seconds;
    this.nanos = nanos;
  }

  /**
   * Creates a new timestamp from given seconds and nanoseconds.
   */
  public static Timestamp create(long seconds, int nanos) {
    return new Timestamp(seconds, nanos);
  }

  /**
   * Creates a new timestamp from given seconds and nanoseconds.
   */
  public static Timestamp fromMillis(long millis) {
    long seconds = millis / NUM_MILLIS_PER_SECOND;
    int nanos = (int) (millis % NUM_MILLIS_PER_SECOND) * NUM_NANOS_PER_MILLI;
    return new Timestamp(seconds, seconds < 0 ? -nanos : nanos);
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

    if (!(obj instanceof Timestamp)) {
      return false;
    }

    Timestamp that = (Timestamp) obj;
    return seconds == that.seconds && nanos == that.nanos;
  }

  @Override
  public int hashCode() {
    return Objects.hash(seconds, nanos);
  }
}
