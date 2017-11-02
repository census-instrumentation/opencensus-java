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

/** Strategy interface for accessing and manipulating the context. */
public interface ContextStrategy {

  /**
   * Wraps a {@link Runnable} so that it executes with the context that is associated with the
   * current scope.
   *
   * @param runnable a {@link Runnable} object
   * @return the wrapped {@link Runnable} object
   */
  Runnable wrapInCurrentContext(Runnable runnable);

  /**
   * Saves the context that is associated with the current scope.
   *
   * <p>The context will be attached when entering the specified thread's {@link Thread#run()}
   * method.
   *
   * @param thread a {@link Thread} object
   */
  void saveContextForThread(Thread thread);

  /**
   * Attaches the context that was previously saved for the specified thread.
   *
   * @param thread a {@link Thread} object
   */
  void attachContextForThread(Thread thread);
}
