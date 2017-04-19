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
import com.google.common.collect.Multimaps;
import java.util.Collection;
import java.util.Map;

/**
 * A class that stores a singleton map from {@link MeasurementDescriptor.Name}s to
 * {@link MutableView}s.
 */
final class MeasurementDescriptorToViewMap {

  /*
   * A synchronized singleton map that stores the one-to-many mapping from MeasurementDescriptors
   * to MutableViews.
   */
  private final Multimap<MeasurementDescriptor.Name, MutableView> mutableMap =
      Multimaps.synchronizedMultimap(
          HashMultimap.<MeasurementDescriptor.Name, MutableView>create());

  /**
   * Returns a {@link MutableView} corresponding to the given {@link ViewDescriptor}.
   */
  final MutableView getMutableView(ViewDescriptor viewDescriptor) {
    Collection<MutableView> views = mutableMap.get(
        viewDescriptor.getMeasurementDescriptor().getMeasurementDescriptorName());
    synchronized (mutableMap) {
      for (MutableView view : views) {
        if (view.getViewDescriptor().equals(viewDescriptor)) {
          return view;
        }
      }
    }
    return null;
  }

  /**
   * Map a new {@link View} to a {@link MeasurementDescriptor.Name}.
   */
  void putMutableView(MeasurementDescriptor.Name name, MutableView view) {
    mutableMap.put(name, view);
  }

  // Records stats with a set of tags.
  void record(Map<String, String> tags, MeasurementMap stats) {
    for (MeasurementValue mv : stats) {
      if (mv.getMeasurement()
          .getMeasurementDescriptorName()
          .equals(SupportedViews.SUPPORTED_MEASUREMENT_DESCRIPTOR.getMeasurementDescriptorName())) {
        recordSupportedMeasurement(tags, mv.getValue());
      }
    }
  }

  private void recordSupportedMeasurement(Map<String, String> tags, double value) {
    MutableView view = getMutableView(SupportedViews.SUPPORTED_VIEW);
    if (view == null) {
      throw new IllegalArgumentException("View not registered yet.");
    }
    synchronized (mutableMap) {
      view.record(tags, value);
    }
  }
}
