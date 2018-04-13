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
import io.opencensus.common.TimeUtils;
import io.opencensus.implcore.internal.DaemonThreadFactory;
import io.opencensus.implcore.trace.SpanImpl;
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

/** Implementation of the {@link SpanExporter}. */
public final class SpanExporterImpl extends SpanExporter {
  private static final Logger logger = Logger.getLogger(ExportComponent.class.getName());

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
    Worker worker = new Worker(bufferSize, scheduleDelay);
    return new SpanExporterImpl(worker);
  }

  /**
   * Adds a Span to the exporting service.
   *
   * @param span the {@code Span} to be added.
   */
  public void addSpan(SpanImpl span) {
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

  private SpanExporterImpl(Worker worker) {
    this.workerThread =
        new DaemonThreadFactory("ExportComponent.ServiceExporterThread").newThread(worker);
    this.workerThread.start();
    this.worker = worker;
  }

  @VisibleForTesting
  Thread getServiceExporterThread() {
    return workerThread;
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
    private final List<SpanImpl> spans;

    private final Map<String, Handler> serviceHandlers = new ConcurrentHashMap<String, Handler>();
    private final int bufferSize;
    private final long scheduleDelayMillis;

    // See SpanExporterImpl#addSpan.
    private void addSpan(SpanImpl span) {
      synchronized (monitor) {
        this.spans.add(span);
        if (spans.size() > bufferSize) {
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
      spans = new ArrayList<SpanImpl>(bufferSize);
      this.bufferSize = bufferSize;
      this.scheduleDelayMillis = TimeUtils.toMillis(scheduleDelay);
    }

    // Returns an unmodifiable list of all buffered spans data to ensure that any registered
    // service handler cannot modify the list.
    private static List<SpanData> fromSpanImplToSpanData(List<SpanImpl> spans) {
      List<SpanData> spanDatas = new ArrayList<SpanData>(spans.size());
      for (SpanImpl span : spans) {
        spanDatas.add(span.toSpanData());
      }
      return Collections.unmodifiableList(spanDatas);
    }

    @Override
    public void run() {
      while (true) {
        // Copy all the batched spans in a separate list to release the monitor lock asap to
        // avoid blocking the producer thread.
        List<SpanImpl> spansCopy;
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
          spansCopy = new ArrayList<SpanImpl>(spans);
          spans.clear();
        }
        // Execute the batch export outside the synchronized to not block all producers.
        final List<SpanData> spanDataList = fromSpanImplToSpanData(spansCopy);
        if (!spanDataList.isEmpty()) {
          onBatchExport(spanDataList);
        }
      }
    }
  }
}
