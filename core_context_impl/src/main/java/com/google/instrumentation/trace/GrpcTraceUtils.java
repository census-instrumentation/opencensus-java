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

package com.google.instrumentation.trace;

import io.grpc.Context;

/**
 * Util methods/functionality for gRPC system.
 */
public final class GrpcTraceUtils {
  // Reserved io.grpc.Context.Key for the Span.
  private static final Context.Key<Span> CONTEXT_SPAN_KEY =
      Context.key("instrumentation-trace-key");

  /**
   * Returns The {@link io.grpc.Context.Key} used to interact with {@link io.grpc.Context}.
   *
   * @return The {@code io.grpc.Context.Key} used to interact with {@code io.grpc.Context}.
   */
  public static Context.Key<Span> getContextSpanKey() {
    return CONTEXT_SPAN_KEY;
  }

  // No instance of this class.
  private GrpcTraceUtils() {}
}
