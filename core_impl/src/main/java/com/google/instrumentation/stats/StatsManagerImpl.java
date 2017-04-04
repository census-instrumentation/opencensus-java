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

import java.util.Set;

/**
 * Native Implementation of {@link StatsManager}.
 */
public class StatsManagerImpl extends StatsManager {

  @Override
  public void registerView(ViewDescriptor viewDescriptor) {
    Set<View> viewSet = MeasurementDescriptorToViewMap.getMap()
        .get(viewDescriptor.getMeasurementDescriptor());
    if (viewSet == null) {
      throw new AssertionError(
          "The prototype will only support measurement descriptor RPC_CLIENT_ROUNDTRIP_LATENCY.");
    } else {
      // TODO(songya): Create a new MutableView and add it into the view set.
    }
  }

  @Override
  public View getView(ViewDescriptor viewDescriptor) {
    throw new UnsupportedOperationException("Not implemented yet.");
  }
}
