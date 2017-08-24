/*
 * Copyright 2017, OpenCensus Authors
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

package io.opencensus.contrib.agent.bootstrap;

/**
 * {@code TraceTrampoline} provides access to tracing-related methods of classes which are otherwise
 * inaccessible from classes loaded by the bootstrap classloader.
 *
 * <p>{@code TraceTrampoline} avoids tight coupling with the concrete implementation of the
 * tracing-related methods via the {@link TraceStrategy} interface.
 *
 * <p>Both {@link TraceTrampoline} and {@link TraceStrategy} are loaded by the bootstrap
 * classloader so that they can be used from classes loaded by the bootstrap classloader.
 * A concrete implementation of {@link TraceStrategy} will be loaded by the system classloader.
 * This allows for using the same implementation as the instrumented application.
 *
 * <p>{@code TraceTrampoline} is implemented as a static class to allow for easy and fast use from
 * instrumented bytecode. We cannot use dependency injection for the instrumented bytecode.
 */
public final class TraceTrampoline {

  // Not synchronized to avoid any synchronization costs after initialization.
  // The agent is responsible for initializing this once (through #setTraceStrategy) before any
  // other method of this class is called.
  private static TraceStrategy traceStrategy;

  private TraceTrampoline() {
  }

  /**
   * Sets the concrete strategy for tracing-related methods.
   *
   * <p>NB: The agent is responsible for setting the {@link TraceStrategy} once before any other
   * method of this class is called.
   *
   * @param traceStrategy the concrete strategy for for tracing-related methods
   */
  public static void setTraceStrategy(TraceStrategy traceStrategy) {
    if (TraceTrampoline.traceStrategy != null) {
      throw new IllegalStateException("traceStrategy was already set");
    }

    if (traceStrategy == null) {
      throw new NullPointerException("traceStrategy");
    }

    TraceTrampoline.traceStrategy = traceStrategy;
  }

  /**
   * Wraps a {@link Runnable} so that it executes with the context that is associated with the
   * current scope.
   *
   * @param runnable a {@link Runnable} object
   * @return the wrapped {@link Runnable} object
   *
   * @see TraceStrategy#wrapInCurrentContext
   */
  public static Runnable wrapInCurrentContext(Runnable runnable) {
    return traceStrategy.wrapInCurrentContext(runnable);
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
    traceStrategy.saveContextForThread(thread);
  }

  /**
   * Attaches the context that was previously saved for the specified thread.
   *
   * @param thread a {@link Thread} object
   */
  public static void attachContextForThread(Thread thread) {
    traceStrategy.attachContextForThread(thread);
  }
}
