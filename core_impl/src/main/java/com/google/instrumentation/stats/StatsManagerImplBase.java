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
import com.google.instrumentation.common.Function;
import com.google.instrumentation.stats.MutableView.MutableDistributionView;
import com.google.instrumentation.stats.MutableView.MutableIntervalView;
import com.google.instrumentation.stats.ViewDescriptor.DistributionViewDescriptor;
import com.google.instrumentation.stats.ViewDescriptor.IntervalViewDescriptor;

/**
 * Native Implementation of {@link StatsManager}.
 */
public class StatsManagerImplBase extends StatsManager {
  private final EventQueue queue;

  // clock used throughout the stats implementation
  private final Clock clock;

  StatsManagerImplBase(EventQueue queue, Clock clock) {
    this.queue = queue;
    this.clock = clock;
  }

  private final MeasurementDescriptorToViewMap measurementDescriptorToViewMap =
      new MeasurementDescriptorToViewMap();
  private final StatsContextFactoryImpl statsContextFactory = new StatsContextFactoryImpl();

  @Override
  public void registerView(ViewDescriptor viewDescriptor) {
    // We are using a preset measurement RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY
    // and view RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW for this prototype.
    // The prototype does not allow setting measurement descriptor entries dynamically for now.
    // TODO(songya): remove the logic for checking the preset descriptor.
    if (!viewDescriptor.equals(SupportedViews.SUPPORTED_VIEW)) {
      throw new UnsupportedOperationException(
          "The prototype will only support Distribution View "
              + SupportedViews.SUPPORTED_VIEW.getName());
    }

    if (measurementDescriptorToViewMap.getView(viewDescriptor, clock) != null) {
      // Ignore views that are already registered.
      return;
    }

    MutableView mutableView = viewDescriptor.match(
        new CreateMutableDistributionViewFunction(clock), new CreateMutableIntervalViewFunction());

    measurementDescriptorToViewMap.putView(
            viewDescriptor.getMeasurementDescriptor().getMeasurementDescriptorName(), mutableView);
  }

  @Override
  public View getView(ViewDescriptor viewDescriptor) {
    View view = measurementDescriptorToViewMap.getView(viewDescriptor, clock);
    if (view == null) {
      throw new IllegalArgumentException(
          "View for view descriptor " + viewDescriptor.getName() + " not found.");
    } else {
      return view;
    }
  }

  @Override
  StatsContextFactoryImpl getStatsContextFactory() {
    return statsContextFactory;
  }

  /**
   * Records a set of measurements with a set of tags.
   *
   * @param tags the tags associated with the measurements
   * @param measurementValues the measurements to record
   */
  void record(StatsContextImpl tags, MeasurementMap measurementValues) {
    queue.enqueue(new StatsEvent(this, tags, measurementValues));
  }

  // An EventQueue entry that records the stats from one call to StatsManager.record(...).
  private static final class StatsEvent implements EventQueue.Entry {
    private final StatsContextImpl tags;
    private final MeasurementMap stats;
    private final StatsManagerImplBase statsManager;

    StatsEvent(
        StatsManagerImplBase statsManager, StatsContextImpl tags, MeasurementMap stats) {
      this.statsManager = statsManager;
      this.tags = tags;
      this.stats = stats;
    }

    @Override
    public void process() {
      statsManager
          .measurementDescriptorToViewMap
          .record(tags, stats);
    }
  }

  private static final class CreateMutableDistributionViewFunction
      implements Function<DistributionViewDescriptor, MutableView> {
    private final Clock clock;

    public CreateMutableDistributionViewFunction(Clock clock) {
      this.clock = clock;
    }

    @Override
    public MutableView apply(DistributionViewDescriptor viewDescriptor) {
      return MutableDistributionView.create(
          viewDescriptor, clock.now());
    }
  }

  private static final class CreateMutableIntervalViewFunction
      implements Function<IntervalViewDescriptor, MutableView> {
    @Override
    public MutableView apply(IntervalViewDescriptor viewDescriptor) {
      // TODO(songya): Create Interval Aggregations from internal Distributions.
      return MutableIntervalView.create(viewDescriptor);
    }
  }
}
