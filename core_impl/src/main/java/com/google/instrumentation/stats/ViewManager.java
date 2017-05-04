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
    // We are using a preset measurement RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY
    // and view RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW for this prototype.
    // The prototype does not allow setting measurement descriptor entries dynamically for now.
    // TODO(songya): remove the logic for checking the preset descriptor.
    if (!viewDescriptor.equals(SupportedViews.SUPPORTED_VIEW)) {
      throw new UnsupportedOperationException(
          "The prototype will only support Distribution View "
              + SupportedViews.SUPPORTED_VIEW.getName());
    }
    measurementDescriptorToViewMap.registerView(viewDescriptor, clock);
  }

  View getView(ViewDescriptor viewDescriptor) {
    View view = measurementDescriptorToViewMap.getView(viewDescriptor, clock);
    if (view == null) {
      throw new IllegalArgumentException(
          "View for view descriptor " + viewDescriptor.getName() + " not found.");
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
    private final ViewManager statsCollector;

    StatsEvent(ViewManager statsCollector, StatsContextImpl tags, MeasurementMap stats) {
      this.statsCollector = statsCollector;
      this.tags = tags;
      this.stats = stats;
    }

    @Override
    public void process() {
      statsCollector.measurementDescriptorToViewMap.record(tags, stats);
    }
  }
}
