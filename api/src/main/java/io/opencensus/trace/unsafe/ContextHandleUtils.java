/*
 * Copyright 2016-17, OpenCensus Authors
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

package io.opencensus.trace.unsafe;

import io.opencensus.trace.ContextHandle;
import io.opencensus.trace.ContextManager;
import io.opencensus.trace.Span;

public class ContextHandleUtils {

  // No instance of this class.
  private ContextHandleUtils() {}

  private static final ContextManager DEFAULT_CONTEXT_MANAGER = new ContextManagerImpl();
  private static ContextManager contextManager = DEFAULT_CONTEXT_MANAGER;

  /**
   * Overrides context manager with a custom implementation.
   *
   * @param cm custom {@code ContextManager} to be used instead of a default one.
   */
  public static void setContextManager(ContextManager cm) {
    contextManager = cm;
  }

  public static ContextHandle currentContext() {
    return contextManager.currentContext();
  }

  /**
   * Creates a new {@code Ctx} with the given value set.
   *
   * @param context the parent {@code Ctx}.
   * @param span the value to be set.
   * @return a new context with the given value set.
   */
  public static ContextHandle withValue(
      ContextHandle context, @javax.annotation.Nullable Span span) {
    return contextManager.withValue(context, span);
  }

  /**
   * Returns the value from the specified {@code Ctx}.
   *
   * @param context the specified {@code Ctx}.
   * @return the value from the specified {@code Ctx}.
   */
  public static Span getValue(ContextHandle context) {
    return contextManager.getValue(context);
  }
}
