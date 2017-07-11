/*
 * Copyright 2016, Google Inc.
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

package io.opencensus.stats;

import static com.google.common.base.Preconditions.checkNotNull;

import io.opencensus.common.NonThrowingCloseable;
import java.io.IOException;
import java.io.InputStream;

/**
 * Factory class for {@link StatsContext}.
 */
public abstract class StatsContextFactory {

  /**
   * Creates a {@link StatsContext} from the given on-the-wire encoded representation.
   *
   * <p>Should be the inverse of {@link StatsContext#serialize(java.io.OutputStream)}. The
   * serialized representation should be based on the {@link StatsContext} binary representation.
   *
   * @param input on-the-wire representation of a {@link StatsContext}
   * @return a {@link StatsContext} deserialized from {@code input}
   */
  public abstract StatsContext deserialize(InputStream input) throws IOException;

  /**
   * Returns the default {@link StatsContext}.
   */
  public abstract StatsContext getDefault();

  /**
   * Get current StatsContext from current gRPC Context.
   *
   * @return the current {@code StatsContext} from {@code io.grpc.Context}, or the default
   * {@code StatsContext} if there's no {@code StatsContext} associated with current
   * {@code io.grpc.Context}.
   */
  public final StatsContext getCurrentStatsContext() {
    StatsContext statsContext = CurrentTagsUtils.getCurrentStatsContext();
    return statsContext != null ? statsContext : getDefault();
  }

  /**
   * Enters the scope of code where the given {@link StatsContext} is in the current Context, and
   * returns an object that represents that scope. The scope is exited when the returned object is
   * closed.
   *
   * <p>Supports try-with-resource idiom.
   *
   * <p>Example of usage:
   *
   * <pre>{@code
   * private final StatsContextFactory statsCtxFactory =
   *     checkNotNull(Stats.getStatsContextFactory(), "statsCtxFactory");
   * void doWork() {
   *   // Construct a new StatsContext with required tags to be set into current context.
   *   StatsContext statsCtx = statsCtxFactory.getCurrentStatsContext().with(tagKey, tagValue);
   *   try (NonThrowingCloseable scopedStatsCtx = statsCtxFactory.withStatsContext(statsCtx)) {
   *     doSomeOtherWork();  // Here "scopedStatsCtx" is the current StatsContext.
   *   }
   * }
   * }</pre>
   *
   * <p>Prior to Java SE 7, you can use a finally block to ensure that a resource is closed
   * regardless of whether the try statement completes normally or abruptly.
   *
   * <p>Example of usage prior to Java SE7:
   *
   * <pre>{@code
   * private final StatsContextFactory statsCtxFactory =
   *     checkNotNull(Stats.getStatsContextFactory(), "statsCtxFactory");
   * void doWork() {
   *   // Construct a new StatsContext with required tags to be set into current context.
   *   StatsContext statsCtx = statsCtxFactory.getCurrentStatsContext().with(tagKey, tagValue);
   *   NonThrowingCloseable scopedStatsCtx = statsCtxFactory.withStatsContext(statsCtx);
   *   try {
   *     doSomeOtherWork();  // Here "scopedStatsCtx" is the current StatsContext.
   *   } finally {
   *     scopedStatsCtx.close();
   *   }
   * }
   * }</pre>
   *
   * @param statsContext The {@link StatsContext} to be set to the current Context.
   * @return an object that defines a scope where the given {@link StatsContext} will be set to the
   *     current Context.
   * @throws NullPointerException if statsContext is null.
   */
  public final NonThrowingCloseable withStatsContext(StatsContext statsContext) {
    return CurrentTagsUtils.withStatsContext(checkNotNull(statsContext, "statsContext"));
  }
}
