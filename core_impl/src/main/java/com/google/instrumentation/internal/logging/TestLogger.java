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

package com.google.instrumentation.internal.logging;

import com.google.auto.value.AutoValue;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A {@code Logger} that stores all of its log messages in memory and can be used for testing
 * logging.
 */
@ThreadSafe
public final class TestLogger implements Logger {

  @GuardedBy("this")
  private final List<LogRecord> messages = new ArrayList<LogRecord>();

  @Override
  public synchronized void log(Level level, String message) {
    messages.add(LogRecord.create(level, message));
  }

  /**
   * Returns a snapshot of all messages logged so far.
   *
   * @return a list of all log messages.
   */
  public synchronized List<LogRecord> getMessages() {
    return new ArrayList<LogRecord>(messages);
  }

  /**
   * A stored log record.
   */
  @Immutable
  @AutoValue
  public abstract static class LogRecord {

    LogRecord() {}

    /**
     * Creates a {@code LogRecord}.
     *
     * @param level the logging level.
     * @param message the log message.
     * @return a {@code LogRecord}.
     */
    public static LogRecord create(Level level, String message) {
      return new AutoValue_TestLogger_LogRecord(level, message);
    }

    /**
     * Returns the logging level of the message.
     *
     * @return the logging level of the message.
     */
    public abstract Level getLevel();

    /**
     * Returns the logged message.
     *
     * @return the logged message.
     */
    public abstract String getMessage();
  }
}
