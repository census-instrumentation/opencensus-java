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

package com.google.instrumentation.trace;

import com.google.instrumentation.common.NonThrowingCloseable;

/** Class to assist in {@link Span}/Context interactions. */
abstract class ContextSpanHandler {
  /**
   * Returns The {@link Span} from the current context.
   *
   * @return The {@code Span} from the current context.
   */
  abstract Span getCurrentSpan();

  /**
   * Enters the scope of code where the given {@link Span} is in the current context, and returns an
   * object that represents that scope. The scope is exited when the returned object is closed.
   *
   * <p>Supports try-with-resource idiom.
   *
   * @param span The {@code Span} to be set to the current context.
   * @return An object that defines a scope where the given {@code Span} will be set to the current
   *     context.
   */
  abstract NonThrowingCloseable withSpan(Span span);
}
