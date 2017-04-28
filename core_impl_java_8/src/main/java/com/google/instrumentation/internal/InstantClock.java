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
import java.time.Instant;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A {@link Clock} that uses {@link Instant#now()}.
 */
@ThreadSafe
public final class InstantClock extends Clock {
  private static final InstantClock INSTANCE = new InstantClock();

  private InstantClock() {}

  /**
   * Returns an {@code InstantClock}.
   *
   * @return an {@code InstantClock}.
   */
  public static InstantClock getInstance() {
    return INSTANCE;
  }

  @Override
  public Timestamp now() {
    Instant instant = Instant.now();
    return Timestamp.create(instant.getEpochSecond(), instant.getNano());
  }

  @Override
  public long nowNanos() {
    // TODO(sebright): Is this correct?
    return System.nanoTime();
  }
}
