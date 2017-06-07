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

package io.opencensus.trace;

import io.opencensus.common.EventQueue;
import io.opencensus.trace.SpanImpl.StartEndHandler;

/**
 * Uses the provided {@link EventQueue} to defer processing/exporting of the {@link SpanData} to
 * avoid impacting the critical path.
 */
final class StartEndHandlerImpl implements StartEndHandler {
  private final EventQueue eventQueue;
  private final SpanExporterImpl sampledSpansServiceExporter;

  StartEndHandlerImpl(
      SpanExporterImpl sampledSpansServiceExporter, EventQueue eventQueue) {
    this.sampledSpansServiceExporter = sampledSpansServiceExporter;
    this.eventQueue = eventQueue;
  }

  @Override
  public void onStart(SpanImpl span) {
    // Do nothing. When ActiveSpans functionality is implemented this will change to record the
    // Span into the ActiveSpans list.
  }

  @Override
  public void onEnd(SpanImpl span) {
    // TODO(bdrutu): Change to RECORD_EVENTS option when active/samples is supported.
    if (span.getContext().getTraceOptions().isSampled()) {
      eventQueue.enqueue(new SpanEndEvent(span, sampledSpansServiceExporter));
    }
  }

  // An EventQueue entry that records the end of the span event.
  private static final class SpanEndEvent implements EventQueue.Entry {
    private final SpanImpl span;
    private final SpanExporterImpl sampledSpansServiceExporter;

    SpanEndEvent(SpanImpl span, SpanExporterImpl sampledSpansServiceExporter) {
      this.span = span;
      this.sampledSpansServiceExporter = sampledSpansServiceExporter;
    }

    @Override
    public void process() {
      sampledSpansServiceExporter.addSpan(span);
    }
  }
}
