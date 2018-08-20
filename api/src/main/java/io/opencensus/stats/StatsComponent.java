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

package io.opencensus.stats;

/**
 * Class that holds the implementations for {@link ViewManager} and {@link StatsRecorder}.
 *
 * <p>All objects returned by methods on {@code StatsComponent} are cacheable.
 *
 * @since 0.8
 */
public abstract class StatsComponent {

  /**
   * Returns the default {@link ViewManager}.
   *
   * @since 0.8
   */
  public abstract ViewManager getViewManager();

  /**
   * Returns the default {@link StatsRecorder}.
   *
   * @since 0.8
   */
  public abstract StatsRecorder getStatsRecorder();

  /**
   * Returns the current {@code StatsCollectionState}.
   *
   * <p>When no implementation is available, {@code getState} always returns {@link
   * StatsCollectionState#DISABLED}.
   *
   * <p>IMPORTANT: The returned value cannot be cached because subsequent calls to {@link
   * #setState(StatsCollectionState)} can change the current value.
   *
   * @return the current {@code StatsCollectionState}.
   * @since 0.8
   */
  public abstract StatsCollectionState getState();

  /**
   * Sets the current {@code StatsCollectionState}.
   *
   * <p>When no implementation is available, {@code setState} does not change the state.
   *
   * <p>If state is set to {@link StatsCollectionState#DISABLED}, all stats that are previously
   * recorded will be cleared.
   *
   * @param state the new {@code StatsCollectionState}.
   * @deprecated This method is deprecated because other libraries could cache the result of {@link
   *     #getState()}, use a stale value, and behave incorrectly. It is only safe to call early in
   *     initialization.
   * @since 0.8
   */
  @Deprecated
  public abstract void setState(StatsCollectionState state);
}
