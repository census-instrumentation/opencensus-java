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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.instrumentation.common.Clock;
import com.google.instrumentation.common.Function;
import com.google.instrumentation.stats.MutableView.MutableDistributionView;
import com.google.instrumentation.stats.MutableView.MutableIntervalView;
import com.google.instrumentation.stats.ViewDescriptor.DistributionViewDescriptor;
import com.google.instrumentation.stats.ViewDescriptor.IntervalViewDescriptor;
import java.util.Collection;
import javax.annotation.concurrent.GuardedBy;

/**
 * A class that stores a singleton map from {@link MeasurementDescriptor.Name}s to {@link
 * MutableView}s.
 */
final class MeasurementDescriptorToViewMap {

  /*
   * A synchronized singleton map that stores the one-to-many mapping from MeasurementDescriptors
   * to MutableViews.
   */
  @GuardedBy("this")
  private final Multimap<MeasurementDescriptor.Name, MutableView> mutableMap =
      HashMultimap.<MeasurementDescriptor.Name, MutableView>create();

  /** Returns a {@link View} corresponding to the given {@link ViewDescriptor}. */
  synchronized View getView(ViewDescriptor viewDescriptor, Clock clock) {
    MutableView view = getMutableView(viewDescriptor);
    return view == null ? null : view.toView(clock);
  }

  private synchronized MutableView getMutableView(ViewDescriptor viewDescriptor) {
    Collection<MutableView> views =
        mutableMap.get(viewDescriptor.getMeasurementDescriptor().getMeasurementDescriptorName());
    for (MutableView view : views) {
      if (view.getViewDescriptor().equals(viewDescriptor)) {
        return view;
      }
    }
    return null;
  }

  /** Enable stats collection for the given {@link ViewDescriptor}. */
  synchronized void registerView(ViewDescriptor viewDescriptor, Clock clock) {
    if (getMutableView(viewDescriptor) != null) {
      // Ignore views that are already registered.
      return;
    }
    MutableView mutableView =
        viewDescriptor.match(
            new CreateMutableDistributionViewFunction(clock),
            new CreateMutableIntervalViewFunction());
    mutableMap.put(
        viewDescriptor.getMeasurementDescriptor().getMeasurementDescriptorName(), mutableView);
  }

  // Records stats with a set of tags.
  synchronized void record(StatsContextImpl tags, MeasurementMap stats) {
    for (MeasurementValue mv : stats) {
      if (mv.getMeasurement()
          .getMeasurementDescriptorName()
          .equals(SupportedViews.SUPPORTED_MEASUREMENT_DESCRIPTOR.getMeasurementDescriptorName())) {
        recordSupportedMeasurement(tags, mv.getValue());
      }
    }
  }

  private void recordSupportedMeasurement(StatsContextImpl tags, double value) {
    MutableView view = getMutableView(SupportedViews.SUPPORTED_VIEW);
    if (view == null) {
      throw new IllegalArgumentException("View not registered yet.");
    }
    view.record(tags, value);
  }

  private static final class CreateMutableDistributionViewFunction
      implements Function<DistributionViewDescriptor, MutableView> {
    private final Clock clock;

    CreateMutableDistributionViewFunction(Clock clock) {
      this.clock = clock;
    }

    @Override
    public MutableView apply(DistributionViewDescriptor viewDescriptor) {
      return MutableDistributionView.create(viewDescriptor, clock.now());
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
