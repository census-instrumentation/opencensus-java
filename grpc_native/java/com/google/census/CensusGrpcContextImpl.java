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

package com.google.census;

import io.grpc.Context;

/**
 * Utils for embedding {@link CensusContext} in gRPC {@link Context} to support thread-local
 * implicit propagation of the {@link CensusContext}.
 */
public final class CensusGrpcContextImpl extends CensusGrpcContext {
  private static final Context.Key<CensusContext> CENSUS_CONTEXT_KEY =
      Context.keyWithDefault("CensusContextKey", Census.getCensusContextFactory().getDefault());

  @Override
  public Context withCensusContext(Context context, CensusContext censusContext) {
    return context.withValue(CENSUS_CONTEXT_KEY, censusContext);
  }

  @Override
  public CensusContext get(Context context) {
    return CENSUS_CONTEXT_KEY.get(context);
  }
}
