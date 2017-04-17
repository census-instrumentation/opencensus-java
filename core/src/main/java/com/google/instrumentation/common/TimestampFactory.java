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

import com.google.common.annotations.VisibleForTesting;
import com.google.instrumentation.internal.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.Immutable;

/**
 * Factory for {@link Timestamp}. See {@link #now} for how to use this class.
 */
public final class TimestampFactory {
  private static final Logger logger = Logger.getLogger(TimestampFactory.class.getName());
  private static final Handler HANDLER =
      loadTimestampFactoryHandler(Provider.getCorrectClassLoader(Handler.class));

  private TimestampFactory() {}

  /**
   * Obtains the current instant from the system clock.
   *
   * @return the current instant using the system clock, not null.
   */
  public static Timestamp now() {
    return HANDLER.timeNow();
  }

  /**
   * Interface to get the current {@link Timestamp}.
   */
  interface Handler {
    Timestamp timeNow();
  }

  @Immutable
  static final class DefaultHandler implements Handler {
    @Override
    public Timestamp timeNow() {
      return Timestamp.fromMillis(System.currentTimeMillis());
    }
  }

  @VisibleForTesting
  static Handler loadTimestampFactoryHandler(ClassLoader classLoader) {
    try {
      return Provider.createInstance(
          Class.forName(
              "com.google.instrumentation.common.TimestampFactoryHandlerImpl", true, classLoader),
          Handler.class);
    } catch (ClassNotFoundException e) {
      logger.log(Level.FINE, "Using default implementation for TimestampFactory$Handler.", e);
    }
    return new DefaultHandler();
  }
}
