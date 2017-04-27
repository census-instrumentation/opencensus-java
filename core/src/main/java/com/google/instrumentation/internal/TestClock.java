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

package com.google.instrumentation.internal;

import com.google.instrumentation.common.Clock;
import com.google.instrumentation.common.Timestamp;
import java.math.BigInteger;

/**
 * A {@link Clock} that allows the time to be set for testing.
 */
public final class TestClock extends Clock {
  private static final int NUM_NANOS_PER_SECOND = 1000 * 1000 * 1000;

  private Timestamp currentTime = Timestamp.create(0, 0);

  private TestClock() {}

  /**
   * Creates a clock initialized to time 0.
   *
   * @return a clock initialized to time 0.
   */
  public static TestClock create() {
    return new TestClock();
  }

  /**
   * Creates a clock with the given time.
   *
   * @param time the initial time.
   * @return a new {@code TestClock} with the given time.
   */
  public static TestClock create(Timestamp time) {
    TestClock clock = new TestClock();
    clock.setTime(time);
    return clock;
  }

  /**
   * Sets the time.
   *
   * @param time the new time.
   */
  public void setTime(Timestamp time) {
    currentTime = validateNanos(time);
  }

  @Override
  public Timestamp now() {
    return currentTime;
  }

  @Override
  public long nowNanos() {
    return getNanos(currentTime).longValue();
  }

  private static Timestamp validateNanos(Timestamp time) {
    BigInteger nanos = getNanos(time);
    if (nanos.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0
        || nanos.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0) {
      throw new ArithmeticException("Nanoseconds overflow: " + time);
    }
    return time;
  }

  // Converts Timestamp into nanoseconds since time 0.
  private static BigInteger getNanos(Timestamp time) {
    return BigInteger.valueOf(time.getSeconds())
        .multiply(BigInteger.valueOf(NUM_NANOS_PER_SECOND))
        .add(BigInteger.valueOf(time.getNanos()));
  }
}
