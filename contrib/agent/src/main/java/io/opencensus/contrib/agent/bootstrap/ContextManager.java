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
 * by accessing and manipulating the context through the {@link ContextStrategy} interface.
 *
 * <p>Both {@link ContextManager} and {@link ContextStrategy} are loaded by the bootstrap
 * classloader so that they can be used from classes loaded by the bootstrap classloader.
 * A concrete implementation of {@link ContextStrategy} will be loaded by the system classloader.
 * This allows for using the same context implementation as the instrumented application.
 *
 * <p>{@code ContextManager} is implemented as a static class to allow for easy and fast use from
 * instrumented bytecode. We cannot use dependency injection for the instrumented bytecode.
 */
public final class ContextManager {

  // Not synchronized to avoid any synchronization costs after initialization.
  // The agent is responsible for initializing this once (through #setContextStrategy) before any
  // other method of this class is called.
  private static ContextStrategy contextStrategy;

  private ContextManager() {
  }

  /**
   * Sets the concrete strategy for accessing and manipulating the context.
   *
   * <p>NB: The agent is responsible for setting the context strategy once before any other method
   * of this class is called.
   *
   * @param contextStrategy the concrete strategy for accessing and manipulating the context
   */
  public static void setContextStrategy(ContextStrategy contextStrategy) {
    if (ContextManager.contextStrategy != null) {
      throw new IllegalStateException("contextStrategy was already set");
    }

    if (contextStrategy == null) {
      throw new NullPointerException("contextStrategy");
    }

    ContextManager.contextStrategy = contextStrategy;
  }

  /**
   * Wraps a {@link Runnable} so that it executes with the context that is associated with the
   * current scope.
   *
   * @param runnable a {@link Runnable} object
   * @return the wrapped {@link Runnable} object
   *
   * @see ContextStrategy#wrapInCurrentContext
   */
  public static Runnable wrapInCurrentContext(Runnable runnable) {
    return contextStrategy.wrapInCurrentContext(runnable);
  }

  /**
   * Saves the context that is associated with the current scope.
   *
   * <p>The context will be attached when entering the specified thread's {@link Thread#run()}
   * method.
   *
   * @param thread a {@link Thread} object
   */
  public static void saveContextForThread(Thread thread) {
    contextStrategy.saveContextForThread(thread);
  }

  /**
   * Attaches the context that was previously saved for the specified thread.
   *
   * @param thread a {@link Thread} object
   */
  public static void attachContextForThread(Thread thread) {
    contextStrategy.attachContextForThread(thread);
  }
}
