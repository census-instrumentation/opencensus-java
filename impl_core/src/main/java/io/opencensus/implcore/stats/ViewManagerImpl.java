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

package io.opencensus.implcore.stats;

import com.google.common.collect.Sets;
import io.opencensus.stats.View;
import io.opencensus.stats.View.AggregationWindow;
import io.opencensus.stats.ViewData;
import io.opencensus.stats.ViewManager;
import java.util.Collections;
import java.util.Set;
import javax.annotation.Nullable;

/** Implementation of {@link ViewManager}. */
public final class ViewManagerImpl extends ViewManager {
  private final StatsManager statsManager;

  ViewManagerImpl(StatsManager statsManager) {
    this.statsManager = statsManager;
  }

  @Override
  public void registerView(View view) {
    statsManager.registerView(view);
  }

  @Override
  @Nullable
  public ViewData getView(View.Name viewName) {
    return statsManager.getView(viewName);
  }

  @Override
  public Set<View> getAllExportedViews() {
    Set<View> exportedViews = Sets.newHashSet();
    for (View view : statsManager.getAllViews()) {
      if (view.getWindow() instanceof AggregationWindow.Cumulative) {
        exportedViews.add(view);
      }
    }
    return Collections.unmodifiableSet(exportedViews);
  }

  void clearStats() {
    statsManager.clearStats();
  }

  void resumeStatsCollection() {
    statsManager.resumeStatsCollection();
  }
}
