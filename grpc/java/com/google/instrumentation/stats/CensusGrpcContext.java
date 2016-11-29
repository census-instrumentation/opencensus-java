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

import com.google.instrumentation.common.Provider;
import io.grpc.Context;

/**
 * Utils for embedding {@link CensusContext} in gRPC {@link Context} to support thread-local
 * implicit propagation of the {@link CensusContext}.
 */
public abstract class CensusGrpcContext {
  private static final CensusGrpcContext INSTANCE = Provider.newInstance(
      "com.google.instrumentation.stats.CensusGrpcContextImpl", null);

  /** Returns the default {@link CensusGrpContext} instance. */
  public static CensusGrpcContext getInstance() {
    return INSTANCE;
  }

  /**
   * Adds this {@link CensusContext} to the specified {@link Context} and returns the resulting
   * {@link Context}.
   */
  public abstract Context withCensusContext(Context context, CensusContext censusContext);

  /**
   * Extracts the CensusContext from the given Context. If there is no associated CensusContext,
   * the default CensusContext is returned.
   */
  public abstract CensusContext get(Context context);
}
