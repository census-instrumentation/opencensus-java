/*
 * Copyright 2016-17, OpenCensus Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.common;

import static io.opencensus.common.TimeUtils.MAX_NANOS;
import static io.opencensus.common.TimeUtils.MAX_SECONDS;
import static io.opencensus.common.TimeUtils.MILLIS_PER_SECOND;
import static io.opencensus.common.TimeUtils.NANOS_PER_MILLI;

import com.google.auto.value.AutoValue;
import io.opencensus.internal.Utils;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.Immutable;

/**
 * Represents a signed, fixed-length span of time represented as a count of seconds and fractions of
 * seconds at nanosecond resolution. It is independent of any calendar and concepts like "day" or
 * "month". Range is approximately +-10,000 years.
 *
 * @since 0.5
 */
@Immutable
@AutoValue
public abstract class Duration implements Comparable<Duration> {
  private static final Duration ZERO = create(0, 0);

  /**
   * Creates a new time duration from given seconds and nanoseconds.
   *
   * @param seconds Signed seconds of the span of time. Must be from -315,576,000,000 to
   *     +315,576,000,000 inclusive.
   * @param nanos Signed fractions of a second at nanosecond resolution of the span of time.
   *     Durations less than one second are represented with a 0 `seconds` field and a positive or
   *     negative `nanos` field. For durations of one second or more, a non-zero value for the
   *     `nanos` field must be of the same sign as the `seconds` field. Must be from -999,999,999 to
   *     +999,999,999 inclusive.
   * @return new {@code Duration} with specified fields. For invalid inputs, a {@code Duration} of
   *     zero is returned.
   * @since 0.5
   */
  public static Duration create(long seconds, int nanos) {
    if (seconds < -MAX_SECONDS || seconds > MAX_SECONDS) {
      return ZERO;
    }
    if (nanos < -MAX_NANOS || nanos > MAX_NANOS) {
      return ZERO;
    }
    if ((seconds < 0 && nanos > 0) || (seconds > 0 && nanos < 0)) {
      return ZERO;
    }
    return new AutoValue_Duration(seconds, nanos);
  }

  /**
   * Creates a new {@code Duration} from given milliseconds.
   *
   * @param millis the duration in milliseconds.
   * @return a new {@code Duration} from given milliseconds. For invalid inputs, a {@code Duration}
   *     of zero is returned.
   * @since 0.5
   */
  public static Duration fromMillis(long millis) {
    long seconds = millis / MILLIS_PER_SECOND;
    int nanos = (int) (millis % MILLIS_PER_SECOND * NANOS_PER_MILLI);
    return Duration.create(seconds, nanos);
  }

  /**
   * Converts a {@link Duration} to milliseconds.
   *
   * <p>Returns {@code Long.MIN_VALUE} if conversion would negatively overflow, or {@code
   * Long.MAX_VALUE} if it would positively overflow.
   *
   * @param duration a {@code Duration}.
   * @return the milliseconds representation of this {@code Duration}.
   * @since 0.13
   */
  public static long toMillis(Duration duration) {
    return TimeUnit.SECONDS.toMillis(duration.getSeconds())
        + TimeUnit.NANOSECONDS.toMillis(duration.getNanos());
  }

  /**
   * Returns the number of seconds in the {@code Duration}.
   *
   * @return the number of seconds in the {@code Duration}.
   * @since 0.5
   */
  public abstract long getSeconds();

  /**
   * Returns the number of nanoseconds in the {@code Duration}.
   *
   * @return the number of nanoseconds in the {@code Duration}.
   * @since 0.5
   */
  public abstract int getNanos();

  /**
   * Compares this {@code Duration} to the specified {@code Duration}.
   *
   * @param otherDuration the other {@code Duration} to compare to, not {@code null}.
   * @return the comparator value: zero if equal, negative if this duration is smaller than
   *     otherDuration, positive if larger.
   * @throws NullPointerException if otherDuration is {@code null}.
   */
  @Override
  public int compareTo(Duration otherDuration) {
    int cmp = Utils.compareLongs(getSeconds(), otherDuration.getSeconds());
    if (cmp != 0) {
      return cmp;
    }
    return Utils.compareLongs(getNanos(), otherDuration.getNanos());
  }

  Duration() {}
}
