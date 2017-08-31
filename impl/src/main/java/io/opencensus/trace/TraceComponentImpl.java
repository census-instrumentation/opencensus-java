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

package io.opencensus.trace;

import io.opencensus.common.Clock;
import io.opencensus.impl.internal.DisruptorEventQueue;
import io.opencensus.impl.trace.internal.ThreadLocalRandomHandler;
import io.opencensus.implcore.common.MillisClock;
import io.opencensus.implcore.trace.TraceComponentImplBase;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.export.ExportComponent;
import io.opencensus.trace.propagation.PropagationComponent;

/** Java 7 and 8 implementation of the {@link TraceComponent}. */
// TraceComponentImpl was moved to io.opencensus.impl.trace. This class exists for backwards
// compatibility, so that it can be loaded by opencensus-api 0.5.
@Deprecated
public final class TraceComponentImpl extends TraceComponent {
  private final TraceComponentImplBase traceComponentImplBase;

  /** Public constructor to be used with reflection loading. */
  public TraceComponentImpl() {
    traceComponentImplBase =
        new TraceComponentImplBase(
            MillisClock.getInstance(),
            new ThreadLocalRandomHandler(),
            DisruptorEventQueue.getInstance());
  }

  @Override
  public Tracer getTracer() {
    return traceComponentImplBase.getTracer();
  }

  @Override
  public PropagationComponent getPropagationComponent() {
    return traceComponentImplBase.getPropagationComponent();
  }

  @Override
  public Clock getClock() {
    return traceComponentImplBase.getClock();
  }

  @Override
  public ExportComponent getExportComponent() {
    return traceComponentImplBase.getExportComponent();
  }

  @Override
  public TraceConfig getTraceConfig() {
    return traceComponentImplBase.getTraceConfig();
  }
}
