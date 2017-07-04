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

package io.opencensus.contrib.agent.bootstrap;

/**
 * {@code ContextManager} provides methods for accessing and manipulating the context from
 * instrumented bytecode.
 *
 * <p>{@code ContextManager} avoids tight coupling with the concrete implementation of the context
 * (such as {@link io.grpc.Context}) by accessing and manipulating the context through the
 * {@link ContextStrategy} interface.
 *
 * <p>Both {@link ContextManager} and {@link ContextStrategy} are loaded by the bootstrap
 * classloader so that they can be used from classes loaded by the bootstrap classloader.
 * A concrete implementations of {@link ContextStrategy} will be loaded by the system classloader.
 * This allows for using the same context implementation as the instrumented application.
 */
public final class ContextManager {

  private static ContextStrategy contextStrategy;

  /**
   * Sets the concrete strategy for accessing and manipulating the context.
   *
   * <p>Must be called before any other method in this class.
   *
   * @param contextStrategy the concrete strategy for accessing and manipulating the context
   */
  public static void setContextHandler(ContextStrategy contextStrategy) {
    if (contextStrategy == null) {
      throw new NullPointerException("contextStrategy");
    }

    ContextManager.contextStrategy = contextStrategy;
  }

  /**
   * @see ContextStrategy#wrapInCurrentContext(java.lang.Runnable)
   */
  public static Runnable wrapInCurrentContext(Runnable runnable) {
    return contextStrategy.wrapInCurrentContext(runnable);
  }
}
