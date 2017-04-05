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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

/**
 * A class that stores a singleton map from MeasurementDescriptors to Views.
 */
final class MeasurementDescriptorToViewMap {

  /**
   * A synchronized singleton map that storing the one-to-many mapping from MeasurementDescriptors
   * to Views.
   */
  private static final Multimap<MeasurementDescriptor, View> mutableMap =
      Multimaps.synchronizedMultimap(HashMultimap.<MeasurementDescriptor, View>create());

  /**
   * Returns the singleton MeasurementDescriptors to Views map.
   */
  static final Multimap<MeasurementDescriptor, View> getMap() {
    return mutableMap;
  }

  // Visible for testing
  MeasurementDescriptorToViewMap() {
    throw new AssertionError();
  }
}
