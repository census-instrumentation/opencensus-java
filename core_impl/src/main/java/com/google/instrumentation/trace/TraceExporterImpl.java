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

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.GuardedBy;

/** Implementation of the {@link TraceExporter}. */
final class TraceExporterImpl extends TraceExporter {
  private static final Logger logger = Logger.getLogger(TraceExporter.class.getName());

  private final WorkerThread workerThread;

  /**
   * Constructs a {@code TraceExporterImpl} that exports the {@link SpanData} asynchronously.
   *
   * <p>Starts a separate thread that wakes up every {@code scheduleDelay} and exports any available
   * spans data. If the number of buffered span data is greater than {@code bufferSize} then the
   * thread wakes up sooner.
   *
   * @param bufferSize the size of the buffered span data.
   * @param scheduleDelayMillis the maximum delay in milliseconds.
   */
  static TraceExporterImpl create(int bufferSize, long scheduleDelayMillis) {
    WorkerThread workerThread = new WorkerThread(bufferSize, scheduleDelayMillis);
    workerThread.setDaemon(true);
    workerThread.start();
    return new TraceExporterImpl(workerThread);
    // TODO(bdrutu): Consider to add a shutdown hook to not avoid dropping data.
  }

  @Override
  public void registerServiceHandler(String name, ServiceHandler serviceHandler) {
    workerThread.registerServiceHandler(name, serviceHandler);
  }

  @Override
  public void unregisterServiceHandler(String name) {
    workerThread.unregisterServiceHandler(name);
  }

  /**
   * Adds a {@link SpanImpl} to be exported.
   *
   * @param span the {@code SpanImpl} that will be exported.
   */
  void addSpan(SpanImpl span) {
    workerThread.addSpan(span);
  }

  @VisibleForTesting
  Thread getWorkerThread() {
    return workerThread;
  }

  private TraceExporterImpl(WorkerThread workerThread) {
    this.workerThread = workerThread;
  }

  // Worker thread that batches multiple span data and call the registered services to export
  // these data.
  //
  // The map of registered handlers is implemented using ConcurrentHashMap ensuring full
  // concurrency of retrievals and adjustable expected concurrency for updates, but retrievals
  // reflect the results of the most recently completed update operations holding upon their onset.
  //
  // The list of batched data is protected by an explicit monitor object which ensures full
  // concurrency.
  private static final class WorkerThread extends Thread {
    // Protects all variables.
    private final Object monitor = new Object();

    @GuardedBy("monitor")
    private final List<SpanImpl> spans;

    private final Map<String, ServiceHandler> serviceHandlers =
        new ConcurrentHashMap<String, ServiceHandler>();
    private final int bufferSize;
    private final long scheduleDelayMillis;

    // See TraceExporterImpl#addSpan.
    private void addSpan(SpanImpl span) {
      synchronized (monitor) {
        this.spans.add(span);
        if (spans.size() > bufferSize) {
          monitor.notifyAll();
        }
      }
    }

    // See TraceExporterImpl#registerServiceHandler.
    private void registerServiceHandler(String name, ServiceHandler serviceHandler) {
      serviceHandlers.put(name, serviceHandler);
    }

    // See TraceExporterImpl#unregisterServiceHandler.
    private void unregisterServiceHandler(String name) {
      serviceHandlers.remove(name);
    }

    // Exports the list of SpanData to all the ServiceHandlers.
    private void onBatchExport(List<SpanData> spanDataList) {
      // From the java documentation of the ConcurrentHashMap#values():
      // The view's iterator is a "weakly consistent" iterator that will never throw
      // ConcurrentModificationException, and guarantees to traverse elements as they existed
      // upon construction of the iterator, and may (but is not guaranteed to) reflect any
      // modifications subsequent to construction.
      for (Map.Entry<String, ServiceHandler> it : serviceHandlers.entrySet()) {
        // In case of any exception thrown by the service handlers continue to run.
        try {
          it.getValue().export(spanDataList);
        } catch (Exception e) {
          logger.log(Level.WARNING, "Exception thrown by the service exporter " + it.getKey(), e);
        }
      }
    }

    private WorkerThread(int bufferSize, long scheduleDelayMillis) {
      spans = new LinkedList<SpanImpl>();
      this.bufferSize = bufferSize;
      this.scheduleDelayMillis = scheduleDelayMillis;
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
