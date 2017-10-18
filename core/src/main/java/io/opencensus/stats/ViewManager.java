/*
 * Copyright 2016-17, OpenCensus Authors
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

import io.opencensus.common.Duration;
import java.util.Collection;
import java.util.List;

/**
 * Provides facilities to register {@link View}s for collecting stats and retrieving
 * stats data as a {@link ViewData}.
 */
public abstract class ViewManager {
  /**
   * Pull model for stats. Registers a {@link View} that will collect data to be accessed
   * via {@link #getView(View.Name)}.
   */
  public abstract void registerView(View view);

  /**
   * Push model for stats. Registers a {@link View} that will collect data to be accessed
   * via {@link Handler#export(Collection)} of the passed-in {@link Handler}s. Though it's
   * also possible to manually get stats data via {@link #getView(View.Name)}.
   */
  public abstract void registerView(View view, List<? extends Handler> handlers);

  /**
   * Set the interval for exporting {@code ViewData} to the given {@code Duration}.
   *
   * <p>By default, {@code ViewData} will be pushed to {@code Handler}s every 10 seconds.
   *
   * @param duration the interval for exporting stats.
   */
  public abstract void setExportInterval(Duration duration);

  /**
   * Returns the current stats data, {@link ViewData}, associated with the given
   * {@link View.Name}.
   */
  public abstract ViewData getView(View.Name view);

  /**
   * An abstract class that registers {@code View} and exports recorded {@code ViewData} in their
   * own format.
   */
  public abstract static class Handler {

    /**
     * Register a {@code View}. Each {@code View} must be registered first, so that its stats can be
     * exported appropriately.
     *
     * @param view the {@code View} to be registered.
     */
    public abstract void registerView(View view);

    /**
     * Exports a list of {@code ViewData}.
     *
     * <p>Implementation SHOULD not block the calling thread. It should execute the export on a
     * different thread if possible.
     *
     * @param viewDataList a collection of {@code ViewData} objects to be exported.
     */
    public abstract void export(Collection<ViewData> viewDataList);
  }
}
