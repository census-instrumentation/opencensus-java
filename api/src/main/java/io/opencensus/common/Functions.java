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

package io.opencensus.common;

/** Commonly used {@link Function} instances. */
public final class Functions {
  private Functions() {}

  private static final Function<Object, Void> RETURN_NULL =
      new Function<Object, Void>() {
        @Override
        public Void apply(Object ignored) {
          return null;
        }
      };

  private static final Function<Object, Void> THROW_ILLEGAL_ARGUMENT_EXCEPTION =
      new Function<Object, Void>() {
        @Override
        public Void apply(Object ignored) {
          throw new IllegalArgumentException();
        }
      };

  /**
   * A {@code Function} that always ignores its argument and returns {@code null}.
   *
   * @return a {@code Function} that always ignores its argument and returns {@code null}.
   */
  public static <T> Function<Object, T> returnNull() {
    // It is safe to cast a producer of Void to anything, because Void is always null.
    @SuppressWarnings("unchecked")
    Function<Object, T> function = (Function<Object, T>) RETURN_NULL;
    return function;
  }

  /**
   * A {@code Function} that always ignores its argument and returns a constant value.
   *
   * @return a {@code Function} that always ignores its argument and returns a constant value.
   */
  public static <T> Function<Object, T> returnConstant(final T constant) {
    return new Function<Object, T>() {
      @Override
      public T apply(Object ignored) {
        return constant;
      }
    };
  }

  /**
   * A {@code Function} that always ignores its argument and throws an {@link
   * IllegalArgumentException}.
   *
   * @return a {@code Function} that always ignores its argument and throws an {@link
   *     IllegalArgumentException}.
   */
  public static <T> Function<Object, T> throwIllegalArgumentException() {
    // It is safe to cast a producer of Void to anything, because Void is always null.
    @SuppressWarnings("unchecked")
    Function<Object, T> function = (Function<Object, T>) THROW_ILLEGAL_ARGUMENT_EXCEPTION;
    return function;
  }
}
