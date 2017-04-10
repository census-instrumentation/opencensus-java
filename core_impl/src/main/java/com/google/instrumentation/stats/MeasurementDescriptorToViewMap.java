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

/**
 * A class that stores a singleton map from {@link MeasurementDescriptor.Name} to {@link View}.
 */
final class MeasurementDescriptorToViewMap {

  /*
   * A synchronized singleton map that stores the one-to-many mapping from MeasurementDescriptors
   * to Views.
   */
  private final Multimap<MeasurementDescriptor.Name, View> mutableMap =
      Multimaps.synchronizedMultimap(HashMultimap.<MeasurementDescriptor.Name, View>create());

  /**
   * Returns a {@link View} corresponding to the given {@link ViewDescriptor}.
   */
  final View getView(ViewDescriptor viewDescriptor) {
    Collection<View> views = mutableMap.get(
        viewDescriptor.getMeasurementDescriptor().getMeasurementDescriptorName());
    synchronized (mutableMap) {
      for (View view : views) {
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
  void putView(MeasurementDescriptor.Name name, View view) {
    mutableMap.put(name, view);
  }
}
