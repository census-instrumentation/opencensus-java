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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.concurrent.GuardedBy;

/** Implementation of the {@link TraceExporter}. */
final class TraceExporterImpl extends TraceExporter {
  private final WorkerThread workerThread;

  /**
   * Constructs a {@code TraceExporterImpl} that exports the {@link SpanData} asynchronously.
   *
   * <p>Starts a separate thread that wakes up every {@code scheduleDelay} and exports any available
   * spans data. If the number of buffered span data is greater than {@code bufferSize} then the
   * thread wakes up sooner.
   *
   * @param bufferSize the size of the buffered span data.
   * @param scheduleDelay the maximum delay in milliseconds.
   */
  TraceExporterImpl(int bufferSize, long scheduleDelay) {
    workerThread = new WorkerThread(bufferSize, scheduleDelay);
    workerThread.setDaemon(true);
    workerThread.start();
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

  private static final class WorkerThread extends Thread {
    // Protects all variables.
    private final Object monitor = new Object();

    @GuardedBy("monitor")
    private final List<SpanImpl> spans;

    @GuardedBy("monitor")
    private final Map<String, ServiceHandler> serviceHandlers =
        new HashMap<String, ServiceHandler>();
    // This must be immutable and changed only by swapping the reference, because reference
    // store/load are atomic in Java.
    private List<ServiceHandler> registeredServiceHandlers;
    private final int bufferSize;
    private final long scheduleDelay;

    void addSpan(SpanImpl span) {
      synchronized (monitor) {
        this.spans.add(span);
        if (spans.size() > bufferSize) {
          monitor.notifyAll();
        }
      }
    }

    private void registerServiceHandler(String name, ServiceHandler serviceHandler) {
      synchronized (monitor) {
        serviceHandlers.put(name, serviceHandler);
        registeredServiceHandlers = new ArrayList<ServiceHandler>(serviceHandlers.values());
      }
    }

    private void unregisterServiceHandler(String name) {
      synchronized (monitor) {
        serviceHandlers.remove(name);
      }
    }

    // Exports the list of SpanData to all the ServiceHandlers.
    private void onBatchExport(List<SpanData> spanDataList) {
      for (ServiceHandler serviceHandler : registeredServiceHandlers) {
        serviceHandler.export(spanDataList);
      }
    }

    private WorkerThread(int bufferSize, long scheduleDelay) {
      spans = new LinkedList<SpanImpl>();
      this.bufferSize = bufferSize;
      this.scheduleDelay = scheduleDelay;
      setDaemon(true);
    }

    // Returns an unmodifiable list of all buffered spans data.
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
        List<SpanData> spanDataList;
        synchronized (monitor) {
          if (spans.size() < bufferSize) {
            do {
              try {
                monitor.wait(scheduleDelay);
              } catch (InterruptedException ie) {
                // Preserve the interruption status as per guidance.
                Thread.currentThread().interrupt();
              }
            } while (spans.isEmpty());
          }
          spanDataList = fromSpanImplToSpanData(spans);
          spans.clear();
        }
        // Execute the batch export outside the synchronized to not block all producers.
        if (!spanDataList.isEmpty()) {
          onBatchExport(spanDataList);
        }
      }
    }
  }
}
