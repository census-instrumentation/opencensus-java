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

package io.opencensus.implcore.trace;

import io.opencensus.implcore.internal.EventQueue;
import io.opencensus.implcore.trace.RecordEventsSpanImpl.StartEndHandler;
import io.opencensus.implcore.trace.export.RunningSpanStoreImpl;
import io.opencensus.implcore.trace.export.SampledSpanStoreImpl;
import io.opencensus.implcore.trace.export.SpanExporterImpl;
import io.opencensus.trace.Span.Options;
import io.opencensus.trace.export.SpanData;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Uses the provided {@link EventQueue} to defer processing/exporting of the {@link SpanData} to
 * avoid impacting the critical path.
 */
@ThreadSafe
public final class StartEndHandlerImpl implements StartEndHandler {
  private final SpanExporterImpl spanExporter;
  @Nullable private final RunningSpanStoreImpl runningSpanStore;
  @Nullable private final SampledSpanStoreImpl sampledSpanStore;
  private final EventQueue eventQueue;
  // true if any of (runningSpanStore OR sampledSpanStore) are different than null, which
  // means the spans with RECORD_EVENTS should be enqueued in the queue.
  private final boolean enqueueEventForNonSampledSpans;

  /**
   * Constructs a new {@code StartEndHandlerImpl}.
   *
   * @param spanExporter the {@code SpanExporter} implementation.
   * @param runningSpanStore the {@code RunningSpanStore} implementation.
   * @param sampledSpanStore the {@code SampledSpanStore} implementation.
   * @param eventQueue the event queue where all the events are enqueued.
   */
  public StartEndHandlerImpl(
      SpanExporterImpl spanExporter,
      @Nullable RunningSpanStoreImpl runningSpanStore,
      @Nullable SampledSpanStoreImpl sampledSpanStore,
      EventQueue eventQueue) {
    this.spanExporter = spanExporter;
    this.runningSpanStore = runningSpanStore;
    this.sampledSpanStore = sampledSpanStore;
    this.enqueueEventForNonSampledSpans = runningSpanStore != null || sampledSpanStore != null;
    this.eventQueue = eventQueue;
  }

  @Override
  public void onStart(RecordEventsSpanImpl span) {
    if (span.getOptions().contains(Options.RECORD_EVENTS) && enqueueEventForNonSampledSpans) {
      eventQueue.enqueue(new SpanStartEvent(span, runningSpanStore));
    }
  }

  @Override
  public void onEnd(RecordEventsSpanImpl span) {
    if ((span.getOptions().contains(Options.RECORD_EVENTS) && enqueueEventForNonSampledSpans)
        || span.getContext().getTraceOptions().isSampled()) {
      eventQueue.enqueue(new SpanEndEvent(span, spanExporter, runningSpanStore, sampledSpanStore));
    }
  }

  // An EventQueue entry that records the start of the span event.
  private static final class SpanStartEvent implements EventQueue.Entry {
    private final RecordEventsSpanImpl span;
    @Nullable private final RunningSpanStoreImpl activeSpansExporter;

    SpanStartEvent(RecordEventsSpanImpl span, @Nullable RunningSpanStoreImpl activeSpansExporter) {
      this.span = span;
      this.activeSpansExporter = activeSpansExporter;
    }

    @Override
    public void process() {
      if (activeSpansExporter != null) {
        activeSpansExporter.onStart(span);
      }
    }
  }

  // An EventQueue entry that records the end of the span event.
  private static final class SpanEndEvent implements EventQueue.Entry {
    private final RecordEventsSpanImpl span;
    @Nullable private final RunningSpanStoreImpl runningSpanStore;
    private final SpanExporterImpl spanExporter;
    @Nullable private final SampledSpanStoreImpl sampledSpanStore;

    SpanEndEvent(
        RecordEventsSpanImpl span,
        SpanExporterImpl spanExporter,
        @Nullable RunningSpanStoreImpl runningSpanStore,
        @Nullable SampledSpanStoreImpl sampledSpanStore) {
      this.span = span;
      this.runningSpanStore = runningSpanStore;
      this.spanExporter = spanExporter;
      this.sampledSpanStore = sampledSpanStore;
    }

    @Override
    public void process() {
      if (span.getContext().getTraceOptions().isSampled()) {
        spanExporter.addSpan(span);
      }
      if (runningSpanStore != null) {
        runningSpanStore.onEnd(span);
      }
      if (sampledSpanStore != null) {
        sampledSpanStore.considerForSampling(span);
      }
    }
  }
}
