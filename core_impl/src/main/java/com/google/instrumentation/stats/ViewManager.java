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

import com.google.instrumentation.common.Clock;
import com.google.instrumentation.common.EventQueue;
import com.google.instrumentation.stats.ViewDescriptor.DistributionViewDescriptor;

/** Object that stores all views and stats. */
final class ViewManager {

  private final EventQueue queue;

  // clock used throughout the stats implementation
  private final Clock clock;

  private final MeasurementDescriptorToViewMap measurementDescriptorToViewMap =
      new MeasurementDescriptorToViewMap();

  ViewManager(EventQueue queue, Clock clock) {
    this.queue = queue;
    this.clock = clock;
  }

  void registerView(ViewDescriptor viewDescriptor) {
    // Only DistributionViews are supported currently.
    // TODO(sebright): Remove this once all views are supported.
    if (!(viewDescriptor instanceof DistributionViewDescriptor)) {
      throw new UnsupportedOperationException(
          "The prototype will only support distribution views.");
    }
    measurementDescriptorToViewMap.registerView(viewDescriptor, clock);
  }

  View getView(ViewDescriptor.Name viewName) {
    View view = measurementDescriptorToViewMap.getView(viewName, clock);
    if (view == null) {
      throw new IllegalArgumentException(
          "View for view descriptor " + viewName + " not found.");
    } else {
      return view;
    }
  }

  void record(StatsContextImpl tags, MeasurementMap measurementValues) {
    queue.enqueue(new StatsEvent(this, tags, measurementValues));
  }

  // An EventQueue entry that records the stats from one call to StatsManager.record(...).
  private static final class StatsEvent implements EventQueue.Entry {
    private final StatsContextImpl tags;
    private final MeasurementMap stats;
    private final ViewManager viewManager;

    StatsEvent(ViewManager viewManager, StatsContextImpl tags, MeasurementMap stats) {
      this.viewManager = viewManager;
      this.tags = tags;
      this.stats = stats;
    }

    @Override
    public void process() {
      viewManager.measurementDescriptorToViewMap.record(tags, stats);
    }
  }
}
