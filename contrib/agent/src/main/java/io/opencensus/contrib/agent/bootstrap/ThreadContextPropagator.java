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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Propagates the context of the thread calling Thread#start to the new thread.
 */
public final class ThreadContextPropagator {

  private static final ConcurrentMap<Long, Object> THREAD_CONTEXTS = new ConcurrentHashMap<>();

  /**
   * Invoked before {@link Thread#start}, remembers the current context of the caller thread.
   *
   * @param thread the caller thread
   */
  public static void beforeThreadStart(Thread thread) {
    THREAD_CONTEXTS.put(thread.getId(), ContextProxy.current());
  }

  /**
   * Invoked before {@link Thread#run}, attaches the previously remembered context of the caller
   * thread.
   *
   * @param thread the caller thread
   */
  public static void beforeThreadRun(Thread thread) {
    if (Thread.currentThread() == thread) {
      Object context = THREAD_CONTEXTS.remove(thread.getId());
      if (context != null) {
        ContextProxy.attach(context);
      }
    }
  }
}
