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

package io.opencensus.internal;

import com.google.common.math.LongMath;
import io.opencensus.common.Clock;
import io.opencensus.common.Duration;
import io.opencensus.common.Timestamp;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/** A {@link Clock} that allows the time to be set for testing. */
@ThreadSafe
public final class TestClock extends Clock {
  private static final int NUM_NANOS_PER_SECOND = 1000 * 1000 * 1000;

  @GuardedBy("this")
  private Timestamp currentTime = validateNanos(Timestamp.create(1493419949, 223123456));

  private TestClock() {}

  /**
   * Creates a clock initialized to a constant non-zero time. {@code Timestamp.create(0, 0)} is not
   * a good default, because it represents an invalid time.
   *
   * @return a clock initialized to a constant non-zero time.
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
  public synchronized void setTime(Timestamp time) {
    currentTime = validateNanos(time);
  }

  /**
   * Advances the time by a duration.
   *
   * @param duration the increase in time.
   */
  // TODO(sebright): Consider adding an 'addDuration' method to Timestamp.
  public synchronized void advanceTime(Duration duration) {
    currentTime =
        validateNanos(
            Timestamp.create(
                    LongMath.checkedAdd(currentTime.getSeconds(), duration.getSeconds()),
                    currentTime.getNanos())
                .addNanos(duration.getNanos()));
  }

  @Override
  public synchronized Timestamp now() {
    return currentTime;
  }

  @Override
  public synchronized long nowNanos() {
    return getNanos(currentTime);
  }

  private static Timestamp validateNanos(Timestamp time) {
    getNanos(time);
    return time;
  }

  // Converts Timestamp into nanoseconds since time 0 and throws an exception if it overflows.
  private static long getNanos(Timestamp time) {
    return LongMath.checkedAdd(
        LongMath.checkedMultiply(time.getSeconds(), NUM_NANOS_PER_SECOND), time.getNanos());
  }
}
