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

package com.google.instrumentation.stats;

import com.google.instrumentation.common.NonThrowingCloseable;
import io.grpc.Context;

/**
 * Util methods/functionality to interact with the {@link io.grpc.Context}.
 *
 * <p>Users must interact with the current Context via the public APIs in {@link
 * StatsContextFactory} and avoid usages of the {@link #STATS_CONTEXT_KEY} directly.
 */
public final class ContextUtils {

  /**
   * The {@link io.grpc.Context.Key} used to interact with {@link io.grpc.Context}.
   */
  public static final Context.Key<StatsContext> STATS_CONTEXT_KEY = Context.key(
      "instrumentation-stats-key");

  // Static class.
  private ContextUtils() {
  }

  /**
   * Returns The {@link StatsContext} from the current context.
   *
   * @return The {@code StatsContext} from the current context.
   */
  static StatsContext getCurrentStatsContext() {
    return STATS_CONTEXT_KEY.get(Context.current());
  }

  /**
   * Enters the scope of code where the given {@link StatsContext} is in the current context, and
   * returns an object that represents that scope. The scope is exited when the returned object
   * is closed.
   *
   * <p>Supports try-with-resource idiom.
   *
   * @param statsContext The {@code StatsContext} to be set to the current context.
   * @return An object that defines a scope where the given {@code StatsContext} is set to the
   *     current context.
   */
  static NonThrowingCloseable withStatsContext(StatsContext statsContext) {
    return new WithStatsContext(statsContext, STATS_CONTEXT_KEY);
  }

  // Supports try-with-resources idiom.
  private static final class WithStatsContext implements NonThrowingCloseable {

    private final Context origContext;

    /**
     * Constructs a new {@link WithStatsContext}.
     *
     * @param statsContext is the {@code StatsContext} to be added to the current {@code Context}.
     * @param contextKey is the {@code Context.Key} used to set/get {@code StatsContext} from the
     * {@code Context}.
     */
    WithStatsContext(StatsContext statsContext, Context.Key<StatsContext> contextKey) {
      origContext = Context.current().withValue(contextKey, statsContext).attach();
    }

    @Override
    public void close() {
      Context.current().detach(origContext);
    }
  }
}
