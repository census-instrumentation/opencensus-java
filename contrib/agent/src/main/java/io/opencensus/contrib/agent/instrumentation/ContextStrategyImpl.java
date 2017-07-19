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

package io.opencensus.contrib.agent.instrumentation;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.grpc.Context;
import io.opencensus.contrib.agent.bootstrap.ContextStrategy;

/**
 * Implementation of {@link ContextStrategy} for accessing and manipulating the
 * {@link io.grpc.Context}.
 */
final class ContextStrategyImpl implements ContextStrategy {

  private static final Cache<Thread, Context> SAVED_CONTEXTS
          = CacheBuilder.newBuilder().weakKeys().build();

  @Override
  public Runnable wrapInCurrentContext(Runnable runnable) {
    return Context.current().wrap(runnable);
  }

  @Override
  public void saveContextForThread(Thread thread) {
    SAVED_CONTEXTS.put(thread, Context.current());
  }

  @Override
  public void attachContextForThread(Thread thread) {
    if (Thread.currentThread() == thread) {
      Context context = SAVED_CONTEXTS.getIfPresent(thread);
      if (context != null) {
        SAVED_CONTEXTS.invalidate(thread);
        context.attach();
      }
    }
  }
}
