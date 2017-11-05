/*
 * Copyright 2017, OpenCensus Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.contrib.agent.instrumentation;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.errorprone.annotations.MustBeClosed;
import io.opencensus.contrib.agent.bootstrap.TraceStrategy;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.samplers.Samplers;
import java.io.Closeable;

/** Implementation of {@link TraceStrategy} for creating and manipulating trace spans. */
final class TraceStrategyImpl implements TraceStrategy {

  @MustBeClosed
  @Override
  public Closeable startScopedSpan(String spanName) {
    checkNotNull(spanName, "spanName");

    return Tracing.getTracer()
        .spanBuilder(spanName)
        .setSampler(Samplers.alwaysSample())
        .setRecordEvents(true)
        .startScopedSpan();
  }
}
