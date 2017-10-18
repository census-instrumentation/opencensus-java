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

package io.opencensus.contrib.agent.instrumentation;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.grpc.Context;
import io.opencensus.contrib.agent.bootstrap.TraceStrategy;
import java.lang.ref.WeakReference;

/**
 * Implementation of {@link TraceStrategy} for tracing-related methods.
 */
final class TraceStrategyImpl implements TraceStrategy {

  /**
   * Thread-safe mapping of {@link Thread}s to {@link Context}s, used for tunneling the caller's
   * {@link Context} of {@link Thread#start()} to {@link Thread#run()}.
   *
   * <p>A thread is inserted into this map when {@link Thread#start()} is called, and removed when
   * {@link Thread#run()} is called.
   *
   * <p>NB: {@link Thread#run()} is not guaranteed to be called after {@link Thread#start()}, for
   * example when attempting to start a thread a second time. Therefore, threads are wrapped in
   * {@link WeakReference}s so that this map does not prevent the garbage collection of otherwise
   * unreferenced threads. Unreferenced threads will be automatically removed from the map by the
   * routine cleanup of the underlying {@link Cache} implementation.
   *
   * <p>NB: A side-effect of {@link CacheBuilder#weakKeys()} is the use of identity ({@code ==})
   * comparison to determine equality of threads. Identity comparison is required here because
   * subclasses of {@link Thread} might override {@link Object#hashCode()} and {@link
   * Object#equals(java.lang.Object)} with potentially broken implementations.
   *
   * <p>NB: Using thread IDs as keys was considered: It's unclear how to safely detect and cleanup
   * otherwise unreferenced threads IDs from the map.
   */
  private final Cache<Thread, Context> savedContexts
          = CacheBuilder.newBuilder().weakKeys().build();

  @Override
  public Runnable wrapInCurrentContext(Runnable runnable) {
    return Context.current().wrap(runnable);
  }

  @Override
  public void saveContextForThread(Thread thread) {
    savedContexts.put(thread, Context.current());
  }

  @Override
  public void attachContextForThread(Thread thread) {
    if (Thread.currentThread() == thread) {
      Context context = savedContexts.getIfPresent(thread);
      if (context != null) {
        savedContexts.invalidate(thread);
        context.attach();
      }
    }
  }
}
