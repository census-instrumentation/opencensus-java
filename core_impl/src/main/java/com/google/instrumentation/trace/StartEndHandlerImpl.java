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

import com.google.instrumentation.common.EventQueue;
import com.google.instrumentation.trace.SpanImpl.StartEndHandler;

/** Implementation of the {@link StartEndHandler}. */
final class StartEndHandlerImpl extends StartEndHandler {
  private final EventQueue eventQueue;
  private final TraceExporterImpl traceExporter;

  StartEndHandlerImpl(EventQueue eventQueue, TraceExporterImpl traceExporter) {
    this.eventQueue = eventQueue;
    this.traceExporter = traceExporter;
  }

  @Override
  void onStart(SpanImpl span) {
    // Do nothing.
  }

  @Override
  void onEnd(SpanImpl span) {
    // TODO(bdrutu): Change to RECORD_EVENTS option when active/samples is supported.
    if (span.getContext().getTraceOptions().isSampled()) {
      eventQueue.enqueue(new SpanEndEvent(span, traceExporter));
    }
  }

  // An EventQueue entry that records the end of the span event.
  private static final class SpanEndEvent implements EventQueue.Entry {
    private final SpanImpl span;
    private final TraceExporterImpl traceExporter;

    SpanEndEvent(SpanImpl span, TraceExporterImpl traceExporter) {
      this.span = span;
      this.traceExporter = traceExporter;
    }

    @Override
    public void process() {
      traceExporter.addSpan(span);
    }
  }
}
