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

package com.google.instrumentation.common;

import com.google.instrumentation.internal.MillisClock;

/**
 * Factory for {@link Timestamp}. See {@link #now} for how to use this class.
 *
 * @deprecated Use {@link Clock} instead.
 */
@Deprecated
public final class TimestampFactory {
  private static final Clock CLOCK = MillisClock.getInstance();

  private TimestampFactory() {}

  /**
   * Obtains the current instant from the system clock.
   *
   * @return the current instant using the system clock, not null.
   */
  public static Timestamp now() {
    return CLOCK.now();
  }
}
