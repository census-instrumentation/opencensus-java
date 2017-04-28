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

/**
 * Constants specifying the view that is supported in the initial stats implementation.
 */
class SupportedViews {
  static final ViewDescriptor SUPPORTED_VIEW = RpcViewConstants.RPC_CLIENT_ROUNDTRIP_LATENCY_VIEW;
  static final MeasurementDescriptor SUPPORTED_MEASUREMENT_DESCRIPTOR =
      RpcMeasurementConstants.RPC_CLIENT_ROUNDTRIP_LATENCY;

  private SupportedViews() {}
}
