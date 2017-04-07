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

import com.google.instrumentation.common.Function;
import com.google.instrumentation.common.Timestamp;
import com.google.instrumentation.stats.View.DistributionView;
import com.google.instrumentation.stats.View.IntervalView;
import com.google.instrumentation.stats.ViewDescriptor.DistributionViewDescriptor;
import com.google.instrumentation.stats.ViewDescriptor.IntervalViewDescriptor;

/**
 * Native Implementation of {@link StatsManager}.
 */
public final class StatsManagerImpl extends StatsManager {

  private final MeasurementDescriptorToViewMap measurementDescriptorToViewMap =
      new MeasurementDescriptorToViewMap();
  private final StatsContextFactoryImpl statsContextFactory = new StatsContextFactoryImpl();

  @Override
  public void registerView(ViewDescriptor viewDescriptor) {
    // We are using a preset measurement RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY
    // and view RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW for this prototype.
    // The prototype does not allow setting measurement descriptor entries dynamically for now.
    // TODO(songya): remove the logic for checking the preset descriptor.
    if (!viewDescriptor.equals(RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW)) {
      throw new UnsupportedOperationException(
          "The prototype will only support Distribution View RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW.");
    }

    if (measurementDescriptorToViewMap.getView(viewDescriptor) != null) {
      // Ignore views that are already registered.
      return;
    }

    // TODO(songya): Extend View class and construct View with internal Distributions.
    View newView = viewDescriptor.match(
        new CreateDistributionViewFunction(), new CreateIntervalViewFunction());

    measurementDescriptorToViewMap.putView(
        viewDescriptor.getMeasurementDescriptor().getMeasurementDescriptorName(), newView);
  }

  @Override
  public View getView(ViewDescriptor viewDescriptor) {
    View view = measurementDescriptorToViewMap.getView(viewDescriptor);
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

  private static final class CreateDistributionViewFunction
      implements Function<DistributionViewDescriptor, View> {
    @Override
    public View apply(DistributionViewDescriptor viewDescriptor) {
      // TODO(songya): Create Distribution Aggregations from internal Distributions,
      // update the start and end time.
      return DistributionView.create(
          viewDescriptor,
          null,
          Timestamp.fromMillis(System.currentTimeMillis()),
          Timestamp.fromMillis(System.currentTimeMillis()));
    }
  }

  private static final class CreateIntervalViewFunction
      implements Function<IntervalViewDescriptor, View> {
    @Override
    public View apply(IntervalViewDescriptor viewDescriptor) {
      // TODO(songya): Create Interval Aggregations from internal Distributions.
      return IntervalView.create(viewDescriptor, null);
    }
  }
}
