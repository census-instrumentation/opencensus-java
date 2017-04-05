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

package com.google.instrumentation.stats;

import com.google.common.collect.Multimap;
import com.google.instrumentation.common.Timestamp;
import com.google.instrumentation.stats.View.DistributionView;
import com.google.instrumentation.stats.View.IntervalView;
import com.google.instrumentation.stats.ViewDescriptor.DistributionViewDescriptor;
import com.google.instrumentation.stats.ViewDescriptor.IntervalViewDescriptor;
import java.util.Collection;

/**
 * Native Implementation of {@link StatsManager}.
 */
public final class StatsManagerImpl extends StatsManager {
  private final Multimap<MeasurementDescriptor, View> measurementDescriptorToViewMap =
      MeasurementDescriptorToViewMap.getMap();

  @Override
  public void registerView(ViewDescriptor viewDescriptor) {
    // We are using a preset measurement {@link RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY}
    // and view {@link RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW} for this prototype.
    // The prototype does not allow setting measurement descriptor entries dynamically for now.
    // TODO(songya): remove the logic for checking preset descriptor.
    if (!viewDescriptor.equals(RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW)) {
      throw new UnsupportedOperationException(
          "The prototype will only support Distribution View RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW.");
    }

    MeasurementDescriptor measurementDescriptor = viewDescriptor.getMeasurementDescriptor();
    Collection<View> views = measurementDescriptorToViewMap.get(measurementDescriptor);

    if (views != null) {
      for (View view : views) {
        if (view.getViewDescriptor().equals(viewDescriptor)) {
          throw new IllegalArgumentException("View has already been registered.");
        }
      }
    }

    // TODO(songya): Extend View class and construct View with internal Distributions.
    View newView;
    if (viewDescriptor instanceof DistributionViewDescriptor) {
      newView = DistributionView.create(
          (DistributionViewDescriptor) viewDescriptor,
          null,  // TODO(songya): Create Distribution Aggregations from internal Distributions.
          Timestamp.fromMillis(System.currentTimeMillis()),
          Timestamp.fromMillis(System.currentTimeMillis()));
    } else if (viewDescriptor instanceof IntervalViewDescriptor) {
      newView = IntervalView.create(
          (IntervalViewDescriptor) viewDescriptor,
          null); // TODO(songya): Create Interval Aggregations from internal Distributions.
    } else {
      throw new IllegalArgumentException(
          "Unknown type of ViewDescriptor " + viewDescriptor.getClass());
    }

    measurementDescriptorToViewMap.put(measurementDescriptor, newView);
  }

  @Override
  public View getView(ViewDescriptor viewDescriptor) {
    View view = null;
    Collection<View> views = measurementDescriptorToViewMap.get(
        viewDescriptor.getMeasurementDescriptor());

    if (views != null) {
      for (View v : views) {
        if (v.getViewDescriptor().equals(viewDescriptor)) {
          view = v;
          break;
        }
      }
    }

    if (view == null) {
      throw new IllegalArgumentException(
          "View for view descriptor " + viewDescriptor.getName() + " not found.");
    } else {
      return view;
    }
  }
}
