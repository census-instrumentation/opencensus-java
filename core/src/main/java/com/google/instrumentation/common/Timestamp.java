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

import javax.annotation.concurrent.Immutable;

/**
 * A representation of an instant in time. The instant is the number of nanoseconds after the number
 * of seconds since the Unix Epoch.
 *
 * <p>Use {@code Tracing.getClock().now()} to get the current timestamp since epoch
 * (1970-01-01T00:00:00Z).
 */
@Immutable
public final class Timestamp {
  private static final long MAX_SECONDS = 315576000000L;
  private static final int MAX_NANOS = 999999999;
  private static final long NUM_MILLIS_PER_SECOND = 1000L;
  private static final int NUM_NANOS_PER_MILLI = 1000 * 1000;
  private static final long NUM_NANOS_PER_SECOND = NUM_NANOS_PER_MILLI * NUM_MILLIS_PER_SECOND;
  private final long seconds;
  private final int nanos;

  private Timestamp(long seconds, int nanos) {
    this.seconds = seconds;
    this.nanos = nanos;
  }

  // TODO(bdrutu): Make create and fromMillis package-protected.

  /**
   * Creates a new timestamp from given seconds and nanoseconds.
   *
   * @param seconds Represents seconds of UTC time since Unix epoch 1970-01-01T00:00:00Z. Must
   *     be from from 0001-01-01T00:00:00Z to 9999-12-31T23:59:59Z inclusive.
   * @param nanos Non-negative fractions of a second at nanosecond resolution. Negative
   *     second values with fractions must still have non-negative nanos values that count forward
   *     in time. Must be from 0 to 999,999,999 inclusive.
   * @return new {@link Timestamp} with specified fields. For invalid inputs, a {@link Timestamp}
   *     of zero is returned.
   */
  public static Timestamp create(long seconds, int nanos) {
    if (seconds < -MAX_SECONDS || seconds > MAX_SECONDS) {
      return new Timestamp(0, 0);
    }
    if (nanos < 0 || nanos > MAX_NANOS) {
      return new Timestamp(0, 0);
    }
    return new Timestamp(seconds, nanos);
  }

  /**
   * Creates a new timestamp from the given milliseconds.
   *
   * @return a new timestamp from the given milliseconds.
   */
  public static Timestamp fromMillis(long millis) {
    long seconds = millis / NUM_MILLIS_PER_SECOND;
    int nanos = (int) (millis % NUM_MILLIS_PER_SECOND) * NUM_NANOS_PER_MILLI;
    if (nanos < 0) {
      return new Timestamp(seconds - 1, (int) (nanos + NUM_NANOS_PER_SECOND));
    } else {
      return new Timestamp(seconds, nanos);
    }
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

  /**
   * Returns a {@code Timestamp} calculated as this {@code Timestamp} plus some number of
   * nanoseconds.
   *
   * @param nanos the nanoseconds to be added to the current timestamp.
   * @return a {@code Timestamp} calculated as this {@code Timestamp} plus some number of
   *     nanoseconds.
   */
  public Timestamp addNanos(long nanos) {
    long newSeconds = seconds + nanos / NUM_NANOS_PER_SECOND;
    nanos %= NUM_NANOS_PER_SECOND;
    // Cannot overflow because: abs(nanos) < NUM_NANOS_PER_SECOND AND
    // this.nanos < NUM_NANOS_PER_SECOND.
    long newNanos = nanos + this.nanos;
    newSeconds += (newNanos / NUM_NANOS_PER_SECOND);
    newNanos %= NUM_NANOS_PER_SECOND;
    if (newNanos >= 0) {
      return Timestamp.create(newSeconds, (int) newNanos);
    } else {
      return Timestamp.create(newSeconds - 1, (int) (newNanos + NUM_NANOS_PER_SECOND));
    }
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
    int result = 17;
    result = 31 * result + (int) (seconds ^ (seconds >>> 32));
    result = 31 * result + nanos;
    return result;
  }

  @Override
  public String toString() {
    return "Timestamp<" + seconds + "," + nanos + ">";
  }
}
