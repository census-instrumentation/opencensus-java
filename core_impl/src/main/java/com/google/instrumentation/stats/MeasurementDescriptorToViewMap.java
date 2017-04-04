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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A singleton class that stores a mapping from MeasurementDescriptors to a set of Views.
 */
public final class MeasurementDescriptorToViewMap {

  // We are using a preset measurement {@link RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY}
  // for this prototype.
  // The prototype does not support setting measurements or views dynamically.
  // TODO(songya): remove the hard-coded preset measurement later.
  // TODO(songya): replace View with a new MutableView class.
  private static final Map<MeasurementDescriptor, Set<View>> mutableMap =
      new ConcurrentHashMap<MeasurementDescriptor, Set<View>>();

  static {
    mutableMap.put(RpcConstants.RPC_CLIENT_ROUNDTRIP_LATENCY,
        Collections.synchronizedSet(new HashSet<View>()));
  }

  // TODO(songya): support setting measurements and views dynamically.
  private static final Map<MeasurementDescriptor, Set<View>> map =
      Collections.unmodifiableMap(mutableMap);

  /**
   * Returns the singleton {@link MeasurementDescriptorToViewMap}.
   */
  public static final Map<MeasurementDescriptor, Set<View>> getMap() {
    return map;
  }

  // Visible for testing
  MeasurementDescriptorToViewMap() {
    throw new AssertionError();
  }
}
