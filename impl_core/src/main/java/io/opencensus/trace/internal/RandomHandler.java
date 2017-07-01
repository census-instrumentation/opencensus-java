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

package io.opencensus.trace.internal;

import java.security.SecureRandom;
import java.util.Random;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Abstract class to access the current {@link Random}.
 *
 * <p>Implementation can have a per thread instance or a single global instance.
 */
@ThreadSafe
public abstract class RandomHandler {
  /**
   * Returns the current {@link Random}.
   *
   * @return the current {@code Random}.
   */
  public abstract Random current();

  /** Implementation of the {@link RandomHandler} using {@link SecureRandom}. */
  @ThreadSafe
  public static final class SecureRandomHandler extends RandomHandler {
    private final Random random = new SecureRandom();

    /** Constructs a new {@link SecureRandomHandler}. */
    public SecureRandomHandler() {}

    @Override
    public Random current() {
      return random;
    }
  }
}
