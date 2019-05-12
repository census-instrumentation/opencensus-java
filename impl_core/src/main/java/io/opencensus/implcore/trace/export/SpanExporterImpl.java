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

package io.opencensus.implcore.trace.export;

import com.google.common.annotations.VisibleForTesting;
import io.opencensus.common.Duration;
import io.opencensus.common.ToLongFunction;
import io.opencensus.implcore.internal.DaemonThreadFactory;
import io.opencensus.implcore.trace.RecordEventsSpanImpl;
import io.opencensus.metrics.DerivedLongCumulative;
import io.opencensus.metrics.DerivedLongGauge;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.MetricOptions;
import io.opencensus.metrics.Metrics;
import io.opencensus.trace.export.ExportComponent;
import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.export.SpanExporter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.GuardedBy;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

/** Implementation of the {@link SpanExporter}. */
public final class SpanExporterImpl extends SpanExporter {
  private static final Logger logger = Logger.getLogger(ExportComponent.class.getName());
  private static final DerivedLongCumulative droppedSpans =
      Metrics.getMetricRegistry()
          .addDerivedLongCumulative(
              "oc_worker_spans_dropped",
              MetricOptions.builder()
                  .setDescription("Number of spans dropped by the exporter thread.")
                  .setUnit("1")
                  .build());
  private static final DerivedLongCumulative pushedSpans =
      Metrics.getMetricRegistry()
          .addDerivedLongCumulative(
              "oc_worker_spans_pushed",
              MetricOptions.builder()
                  .setDescription("Number of spans pushed by the exporter thread to the exporter.")
                  .setUnit("1")
                  .build());
  private static final DerivedLongGauge referencedSpans =
      Metrics.getMetricRegistry()
          .addDerivedLongGauge(
              "oc_worker_spans_referenced",
              MetricOptions.builder()
                  .setDescription("Current number of spans referenced by the exporter thread.")
                  .setUnit("1")
                  .build());

  private final Worker worker;
  private final Thread workerThread;

  /**
   * Constructs a {@code SpanExporterImpl} that exports the {@link SpanData} asynchronously.
   *
   * <p>Starts a separate thread that wakes up every {@code scheduleDelay} and exports any available
   * spans data. If the number of buffered SpanData objects is greater than {@code bufferSize} then
   * the thread wakes up sooner.
   *
   * @param bufferSize the size of the buffered span data.
   * @param scheduleDelay the maximum delay.
   */
  static SpanExporterImpl create(int bufferSize, Duration scheduleDelay) {
    // TODO(bdrutu): Consider to add a shutdown hook to not avoid dropping data.
    final Worker worker = new Worker(bufferSize, scheduleDelay);
    return new SpanExporterImpl(worker);
  }

  /**
   * Adds a Span to the exporting service.
   *
   * @param span the {@code Span} to be added.
   */
  public void addSpan(RecordEventsSpanImpl span) {
    worker.addSpan(span);
  }

  @Override
  public void registerHandler(String name, Handler handler) {
    worker.registerHandler(name, handler);
  }

  @Override
  public void unregisterHandler(String name) {
    worker.unregisterHandler(name);
  }

  void flush() {
    worker.flush();
  }

  void shutdown() {
    flush();
    workerThread.interrupt();
  }

  private SpanExporterImpl(Worker worker) {
    this.workerThread =
        new DaemonThreadFactory("ExportComponent.ServiceExporterThread").newThread(worker);
    this.workerThread.start();
    this.worker = worker;
    droppedSpans.createTimeSeries(
        Collections.<LabelValue>emptyList(), this.worker, new ReportDroppedSpans());
    referencedSpans.createTimeSeries(
        Collections.<LabelValue>emptyList(), this.worker, new ReportReferencedSpans());
    pushedSpans.createTimeSeries(
        Collections.<LabelValue>emptyList(), this.worker, new ReportPushedSpans());
  }

  private static class ReportDroppedSpans implements ToLongFunction</*@Nullable*/ Worker> {
    @Override
    public long applyAsLong(/*@Nullable*/ Worker worker) {
      if (worker == null) {
        return 0;
      }
      return worker.getDroppedSpans();
    }
  }

  private static class ReportReferencedSpans implements ToLongFunction</*@Nullable*/ Worker> {
    @Override
    public long applyAsLong(/*@Nullable*/ Worker worker) {
      if (worker == null) {
        return 0;
      }
      return worker.getReferencedSpans();
    }
  }

  private static class ReportPushedSpans implements ToLongFunction</*@Nullable*/ Worker> {
    @Override
    public long applyAsLong(/*@Nullable*/ Worker worker) {
      if (worker == null) {
        return 0;
      }
      return worker.getPushedSpans();
    }
  }

  @VisibleForTesting
  Thread getServiceExporterThread() {
    return workerThread;
  }

  @VisibleForTesting
  long getDroppedSpans() {
    return worker.getDroppedSpans();
  }

  @VisibleForTesting
  long getReferencedSpans() {
    return worker.getReferencedSpans();
  }

  @VisibleForTesting
  long getPushedSpans() {
    return worker.getPushedSpans();
  }

  // Worker in a thread that batches multiple span data and calls the registered services to export
  // that data.
  //
  // The map of registered handlers is implemented using ConcurrentHashMap ensuring full
  // concurrency of retrievals and adjustable expected concurrency for updates. Retrievals
  // reflect the results of the most recently completed update operations held upon their onset.
  //
  // The list of batched data is protected by an explicit monitor object which ensures full
  // concurrency.
  private static final class Worker implements Runnable {
    private final Object monitor = new Object();

    @GuardedBy("monitor")
    private final List<RecordEventsSpanImpl> spans;

    @GuardedBy("monitor")
    private long referencedSpans = 0;

    @GuardedBy("monitor")
    private long droppedSpans = 0;

    @GuardedBy("monitor")
    private long pushedSpans = 0;

    private final Map<String, Handler> serviceHandlers = new ConcurrentHashMap<>();
    private final int bufferSize;
    private final long maxReferencedSpans;
    private final long scheduleDelayMillis;

    // See SpanExporterImpl#addSpan.
    private void addSpan(RecordEventsSpanImpl span) {
      synchronized (monitor) {
        if (referencedSpans + 1L > maxReferencedSpans) {
          droppedSpans++;
          return;
        }
        this.spans.add(span);
        referencedSpans++;
        if (spans.size() >= bufferSize) {
          monitor.notifyAll();
        }
      }
    }

    // See SpanExporter#registerHandler.
    private void registerHandler(String name, Handler serviceHandler) {
      serviceHandlers.put(name, serviceHandler);
    }

    // See SpanExporter#unregisterHandler.
    private void unregisterHandler(String name) {
      serviceHandlers.remove(name);
    }

    // Exports the list of SpanData to all the ServiceHandlers.
    private void onBatchExport(List<SpanData> spanDataList) {
      // From the java documentation of the ConcurrentHashMap#entrySet():
      // The view's iterator is a "weakly consistent" iterator that will never throw
      // ConcurrentModificationException, and guarantees to traverse elements as they existed
      // upon construction of the iterator, and may (but is not guaranteed to) reflect any
      // modifications subsequent to construction.
      for (Map.Entry<String, Handler> it : serviceHandlers.entrySet()) {
        // In case of any exception thrown by the service handlers continue to run.
        try {
          it.getValue().export(spanDataList);
        } catch (Throwable e) {
          logger.log(Level.WARNING, "Exception thrown by the service export " + it.getKey(), e);
        }
      }
    }

    private Worker(int bufferSize, Duration scheduleDelay) {
      spans = new ArrayList<>(bufferSize);
      this.bufferSize = bufferSize;
      this.maxReferencedSpans = 10L * bufferSize;
      this.scheduleDelayMillis = scheduleDelay.toMillis();
    }

    @Override
    public void run() {
      while (true) {
        // Copy all the batched spans in a separate list to release the monitor lock asap to
        // avoid blocking the producer thread.
        ArrayList<RecordEventsSpanImpl> spansCopy;
        synchronized (monitor) {
          if (spans.size() < bufferSize) {
            do {
              // In the case of a spurious wakeup we export only if we have at least one span in
              // the batch. It is acceptable because batching is a best effort mechanism here.
              try {
                monitor.wait(scheduleDelayMillis);
              } catch (InterruptedException ie) {
                // Preserve the interruption status as per guidance and stop doing any work.
                Thread.currentThread().interrupt();
                return;
              }
            } while (spans.isEmpty());
          }
          spansCopy = new ArrayList<>(spans);
          spans.clear();
        }
        // Execute the batch export outside the synchronized to not block all producers.
        exportBatches(spansCopy);
      }
    }

    private void flush() {
      ArrayList<RecordEventsSpanImpl> spansCopy;
      synchronized (monitor) {
        spansCopy = new ArrayList<>(spans);
        spans.clear();
      }
      // Execute the batch export outside the synchronized to not block all producers.
      exportBatches(spansCopy);
    }

    private long getDroppedSpans() {
      synchronized (monitor) {
        return droppedSpans;
      }
    }

    private long getReferencedSpans() {
      synchronized (monitor) {
        return referencedSpans;
      }
    }

    private long getPushedSpans() {
      synchronized (monitor) {
        return pushedSpans;
      }
    }

    @SuppressWarnings("argument.type.incompatible")
    private void exportBatches(ArrayList<RecordEventsSpanImpl> spanList) {
      ArrayList<SpanData> spanDataList = new ArrayList<>(bufferSize);
      for (int i = 0; i < spanList.size(); i++) {
        spanDataList.add(spanList.get(i).toSpanData());
        // Remove the reference to the RecordEventsSpanImpl to allow GC to free the memory.
        spanList.set(i, null);
        if (spanDataList.size() == bufferSize) {
          // One full batch, export it now. Wrap the list with unmodifiableList to ensure exporter
          // does not change the list.
          onBatchExport(Collections.unmodifiableList(spanDataList));
          // Cannot clear because the exporter may still have a reference to this list (e.g. async
          // scheduled work), so just create a new list.
          spanDataList = new ArrayList<>(bufferSize);
          // We removed reference for bufferSize Spans.
          synchronized (monitor) {
            referencedSpans -= bufferSize;
            pushedSpans += bufferSize;
          }
        }
      }
      // Last incomplete batch, send this as well.
      if (!spanDataList.isEmpty()) {
        // Wrap the list with unmodifiableList to ensure exporter does not change the list.
        onBatchExport(Collections.unmodifiableList(spanDataList));
        // We removed reference for spanDataList.size() Spans.
        synchronized (monitor) {
          referencedSpans -= spanDataList.size();
          pushedSpans += spanDataList.size();
        }
        spanDataList.clear();
      }
    }
  }
}
