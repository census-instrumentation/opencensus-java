/*
 * Copyright 2018, OpenCensus Authors
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

package io.opencensus.metrics;

/*>>>
import org.checkerframework.checker.nullness.qual.NonNull;
*/

/** General internal utility methods. */
// TODO(songya): remove this class and use shared Utils instead.
final class Utils {

  private Utils() {}

  /**
   * Throws an {@link IllegalArgumentException} if the argument is false. This method is similar to
   * {@code Preconditions.checkArgument(boolean, Object)} from Guava.
   *
   * @param isValid whether the argument check passed.
   * @param message the message to use for the exception.
   */
  static void checkArgument(boolean isValid, String message) {
    if (!isValid) {
      throw new IllegalArgumentException(message);
    }
  }

  /**
   * Throws a {@link NullPointerException} if the argument is null. This method is similar to {@code
   * Preconditions.checkNotNull(Object, Object)} from Guava.
   *
   * @param arg the argument to check for null.
   * @param message the message to use for the exception.
   * @return the argument, if it passes the null check.
   */
  static <T /*>>> extends @NonNull Object*/> T checkNotNull(T arg, String message) {
    if (arg == null) {
      throw new NullPointerException(message);
    }
    return arg;
  }
}
