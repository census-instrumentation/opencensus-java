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

import io.opencensus.stats.export.StatsExporter.Handler;
import java.util.Collection;
import java.util.List;

/**
 * Provides facilities to register {@link View}s for collecting stats and retrieving
 * stats data as a {@link ViewData}.
 */
public abstract class ViewManager {
  /**
   * Pull model for stats. Registers a {@link View} that will collect data to be accessed
   * via {@link #getView(View)}.
   */
  public abstract void registerView(View view);

  /**
   * Push model for stats. Registers a {@link View} that will collect data to be accessed
   * via {@link Handler#export(Collection)} of the passed-in {@link Handler}s. Though it's
   * also possible to manually get stats data via {@link #getView(View)}.
   */
  public abstract void registerView(View view, List<Handler> handlers);

  /**
   * Returns the current stats data, {@link ViewData}, associated with the given
   * {@link View}.
   */
  public abstract ViewData getView(View view);
}
