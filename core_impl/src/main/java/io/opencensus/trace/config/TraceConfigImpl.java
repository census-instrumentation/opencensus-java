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

package io.opencensus.trace.config;

/**
 * Global configuration of the trace service. This allows users to change configs for the default
 * sampler, maximum events to be kept, etc.
 */
public final class TraceConfigImpl extends TraceConfig {
  // Reads and writes are atomic for reference variables. Use volatile to ensure that these
  // operations are visible on other CPUs as well.
  private volatile TraceParams activeTraceParams = TraceParams.DEFAULT;

  /**
   * Constructs a new {@code TraceConfigImpl}.
   */
  public TraceConfigImpl() {}

  @Override
  public TraceParams getActiveTraceParams() {
    return activeTraceParams;
  }

  @Override
  public void updateActiveTraceParams(TraceParams traceParams) {
    activeTraceParams = traceParams;
  }
}
