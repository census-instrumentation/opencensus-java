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

package io.opencensus.internal;

import java.util.List;
import javax.annotation.Nullable;

/*>>>
import org.checkerframework.checker.nullness.qual.NonNull;
*/

/** General internal utility methods. */
public final class Utils {

  private Utils() {}

  /**
   * Throws an {@link IllegalArgumentException} if the argument is false. This method is similar to
   * {@code Preconditions.checkArgument(boolean, Object)} from Guava.
   *
   * @param isValid whether the argument check passed.
   * @param message the message to use for the exception.
   */
  public static void checkArgument(boolean isValid, String message) {
    if (!isValid) {
      throw new IllegalArgumentException(message);
    }
  }

  /**
   * Throws an {@link IllegalStateException} if the argument is false. This method is similar to
   * {@code Preconditions.checkState(boolean, Object)} from Guava.
   *
   * @param isValid whether the state check passed.
   * @param message the message to use for the exception.
   */
  public static void checkState(boolean isValid, String message) {
    if (!isValid) {
      throw new IllegalStateException(message);
    }
  }

  /**
   * Validates an index in an array or other container. This method throws an {@link
   * IllegalArgumentException} if the size is negative and throws an {@link
   * IndexOutOfBoundsException} if the index is negative or greater than or equal to the size. This
   * method is similar to {@code Preconditions.checkElementIndex(int, int)} from Guava.
   *
   * @param index the index to validate.
   * @param size the size of the array or container.
   */
  public static void checkIndex(int index, int size) {
    if (size < 0) {
      throw new IllegalArgumentException("Negative size: " + size);
    }
    if (index < 0 || index >= size) {
      throw new IndexOutOfBoundsException("Index out of bounds: size=" + size + ", index=" + index);
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
  public static <T /*>>> extends @NonNull Object*/> T checkNotNull(T arg, String message) {
    if (arg == null) {
      throw new NullPointerException(message);
    }
    return arg;
  }

  /**
   * Throws a {@link NullPointerException} if any of the list elements is null.
   *
   * @param list the argument list to check for null.
   * @param message the message to use for the exception.
   */
  public static <T /*>>> extends @NonNull Object*/> void checkListElementNotNull(
      List<T> list, String message) {
    for (T element : list) {
      if (element == null) {
        throw new NullPointerException(message);
      }
    }
  }

  /**
   * Compares two Objects for equality. This functionality is provided by {@code
   * Objects.equal(Object, Object)} in Java 7.
   */
  public static boolean equalsObjects(@Nullable Object x, @Nullable Object y) {
    return x == null ? y == null : x.equals(y);
  }
}
