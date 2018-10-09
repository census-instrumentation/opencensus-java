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

package io.opencensus.implcore.internal;

import java.util.List;

/** General internal utility methods. */
public final class Utils {

  private Utils() {}

  /**
   * Throws a {@link NullPointerException} if any of the list elements is null.
   *
   * @param list the argument list to check for null.
   * @param errorMessage the message to use for the exception. Will be converted to a string using
   *     {@link String#valueOf(Object)}.
   */
  public static <T /*>>> extends @NonNull Object*/> void checkListElementNotNull(
      List<T> list, @javax.annotation.Nullable Object errorMessage) {
    for (T element : list) {
      if (element == null) {
        throw new NullPointerException(String.valueOf(errorMessage));
      }
    }
  }
}
