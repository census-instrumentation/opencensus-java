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

/**
 * An interface for a factory that generates timestamps.
 *
 * @see Timestamp
 */
public interface TimestampFactory {
  /**
   * Returns a new timestamp that represents the instant this method was called.
   *
   * @return the new timestamp.
   */
  Timestamp now();
}
