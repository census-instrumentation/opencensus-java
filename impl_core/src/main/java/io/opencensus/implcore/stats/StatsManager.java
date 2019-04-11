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

import static com.google.common.base.Preconditions.checkNotNull;

import io.opencensus.common.Clock;
import io.opencensus.implcore.internal.CurrentState;
import io.opencensus.implcore.internal.CurrentState.State;
import io.opencensus.implcore.internal.EventQueue;
import io.opencensus.metrics.export.Metric;
import io.opencensus.stats.View;
import io.opencensus.stats.ViewData;
import io.opencensus.tags.TagContext;
import java.util.Collection;
import java.util.Set;
import javax.annotation.Nullable;

/** Object that stores all views and stats. */
final class StatsManager {

  private final EventQueue queue;

  // clock used throughout the stats implementation
  private final Clock clock;

  private final CurrentState state;
  private final MeasureToViewMap measureToViewMap = new MeasureToViewMap();

  StatsManager(EventQueue queue, Clock clock, CurrentState state) {
    checkNotNull(queue, "EventQueue");
    checkNotNull(clock, "Clock");
    checkNotNull(state, "state");
    this.queue = queue;
    this.clock = clock;
    this.state = state;
  }

  void registerView(View view) {
    measureToViewMap.registerView(view, clock);
  }

  @Nullable
  ViewData getView(View.Name viewName) {
    return measureToViewMap.getView(viewName, clock, state.getInternal());
  }

  Set<View> getExportedViews() {
    return measureToViewMap.getExportedViews();
  }

  void record(TagContext tags, MeasureMapInternal measurementValues) {
    // TODO(songya): consider exposing No-op MeasureMap and use it when stats state is DISABLED, so
    // that we don't need to create actual MeasureMapImpl.
    if (state.getInternal() == State.ENABLED) {
      queue.enqueue(new StatsEvent(this, tags, measurementValues));
    }
  }

  Collection<Metric> getMetrics() {
    return measureToViewMap.getMetrics(clock, state.getInternal());
  }

  void clearStats() {
    measureToViewMap.clearStats();
  }

  void resumeStatsCollection() {
    measureToViewMap.resumeStatsCollection(clock.now());
  }

  // An EventQueue entry that records the stats from one call to StatsManager.record(...).
  private static final class StatsEvent implements EventQueue.Entry {
    private final TagContext tags;
    private final MeasureMapInternal stats;
    private final StatsManager statsManager;

    StatsEvent(StatsManager statsManager, TagContext tags, MeasureMapInternal stats) {
      this.statsManager = statsManager;
      this.tags = tags;
      this.stats = stats;
    }

    @Override
    public void process() {
      // Add Timestamp to value after it went through the DisruptorQueue.
      statsManager.measureToViewMap.record(tags, stats, statsManager.clock.now());
    }

    @Override
    public void rejected() {}
  }
}
