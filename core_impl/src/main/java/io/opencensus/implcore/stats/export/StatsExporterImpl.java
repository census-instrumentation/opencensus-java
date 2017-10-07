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

package io.opencensus.implcore.stats.export;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import io.opencensus.stats.View;
import io.opencensus.stats.View.Name;
import io.opencensus.stats.ViewData;
import io.opencensus.stats.export.ExportComponent;
import io.opencensus.stats.export.StatsExporter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.GuardedBy;

/** Implementation for {@link StatsExporter}. */
public final class StatsExporterImpl extends StatsExporter {

  private static final Logger logger = Logger.getLogger(ExportComponent.class.getName());

  private final WorkerThread workerThread;

  /**
   * Constructs a {@code StatsExporterImpl} that exports {@link ViewData} asynchronously.
   *
   * <p>Starts a separate thread that wakes up every {@code scheduleDelay} and exports any available
   * view data. If the number of buffered ViewData objects is greater than {@code bufferSize} then
   * the thread wakes up sooner.
   *
   * @param bufferSize the size of the buffered view data.
   * @param scheduleDelayMillis the maximum delay in milliseconds.
   */
  static StatsExporterImpl create(int bufferSize, long scheduleDelayMillis) {
    WorkerThread workerThread = new WorkerThread(bufferSize, scheduleDelayMillis);
    workerThread.start();
    return new StatsExporterImpl(workerThread);
  }

  /**
   * Registers a {@link View} on a {@link io.opencensus.stats.export.StatsExporter.Handler}.
   *
   * @param handler the {@code Handler}.
   * @param viewName name of {@code View}.
   */
  public void registerViewForHandler(Handler handler, View.Name viewName) {
    workerThread.registerViewForHandler(handler, viewName);
  }

  /**
   * Adds a ViewData to the exporting service.
   *
   * @param viewData the {@code ViewData} to be added.
   */
  public void addViewData(ViewData viewData) {
    workerThread.addViewData(viewData);
  }

  @Override
  public void registerHandler(String name, Handler handler) {
    workerThread.registerHandler(name, handler);
  }

  @Override
  public void unregisterHandler(String name) {
    workerThread.unregisterHandler(name);
  }

  private StatsExporterImpl(WorkerThread workerThread) {
    this.workerThread = workerThread;
  }

  @VisibleForTesting
  Thread getServiceExporterThread() {
    return workerThread;
  }

  // Worker thread that batches multiple view data and calls the registered services to export
  // that data.
  //
  // The map of registered handlers is implemented using ConcurrentHashMap ensuring full
  // concurrency of retrievals and adjustable expected concurrency for updates. Retrievals
  // reflect the results of the most recently completed update operations held upon their onset.
  //
  // The list of batched data is protected by an explicit monitor object which ensures full
  // concurrency.
  private static final class WorkerThread extends Thread {
    private final Object monitor = new Object();

    @GuardedBy("monitor")
    private final List<ViewData> viewDataList;

    private final Map<String, Handler> serviceHandlers = new ConcurrentHashMap<String, Handler>();
    private final Map<Handler, Set<View.Name>> registeredViewsPerHandler =
        new ConcurrentHashMap<Handler, Set<View.Name>>();
    private final int bufferSize;
    private final long scheduleDelayMillis;

    private void registerViewForHandler(Handler handler, View.Name viewName) {
      if (registeredViewsPerHandler.containsKey(handler)) {
        registeredViewsPerHandler.get(handler).add(viewName);
      }
    }

    // See StatsExporterImpl#addViewData.
    private void addViewData(ViewData viewData) {
      synchronized (monitor) {
        this.viewDataList.add(viewData);
        if (viewDataList.size() > bufferSize) {
          monitor.notifyAll();
        }
      }
    }

    // See ViewDataExporter#registerHandler.
    private void registerHandler(String name, Handler serviceHandler) {
      serviceHandlers.put(name, serviceHandler);
      registeredViewsPerHandler.put(serviceHandler, new HashSet<Name>());
    }

    // See ViewDataExporter#unregisterHandler.
    private void unregisterHandler(String name) {
      serviceHandlers.remove(name);
    }

    // Exports the list of ViewData to all the ServiceHandlers.
    private void onBatchExport(List<ViewData> viewDataList) {
      // From the java documentation of the ConcurrentHashMap#entrySet():
      // The view's iterator is a "weakly consistent" iterator that will never throw
      // ConcurrentModificationException, and guarantees to traverse elements as they existed
      // upon construction of the iterator, and may (but is not guaranteed to) reflect any
      // modifications subsequent to construction.
      for (Map.Entry<String, Handler> it : serviceHandlers.entrySet()) {
        // In case of any exception thrown by the service handlers continue to run.
        try {
          // Only export ViewData that has been registered against this Handler.
          List<ViewData> registeredViewData = Lists.newArrayList();
          Set<View.Name> registeredViews = registeredViewsPerHandler.get(it.getValue());
          for (ViewData viewData : viewDataList) {
            if (registeredViews.contains(viewData.getView().getName())) {
              registeredViewData.add(viewData);
            }
          }
          it.getValue().export(registeredViewData);
        } catch (Throwable e) {
          logger.log(Level.WARNING, "Exception thrown by the service export " + it.getKey(), e);
        }
      }
    }

    private WorkerThread(int bufferSize, long scheduleDelayMillis) {
      viewDataList = new LinkedList<ViewData>();
      this.bufferSize = bufferSize;
      this.scheduleDelayMillis = scheduleDelayMillis;
      setDaemon(true);
      setName("ExportComponent.ServiceExporterThread");
    }

    @Override
    public void run() {
      while (true) {
        // Copy all the batched viewDataList in a separate list to release the monitor lock asap to
        // avoid blocking the producer thread.
        List<ViewData> viewDataListCopy;
        synchronized (monitor) {
          if (viewDataList.size() < bufferSize) {
            do {
              // In the case of a spurious wakeup we export only if we have at least one viewData in
              // the batch. It is acceptable because batching is a best effort mechanism here.
              try {
                monitor.wait(scheduleDelayMillis);
              } catch (InterruptedException ie) {
                // Preserve the interruption status as per guidance and stop doing any work.
                Thread.currentThread().interrupt();
                return;
              }
            } while (viewDataList.isEmpty());
          }
          viewDataListCopy = new ArrayList<ViewData>(viewDataList);
          viewDataList.clear();
        }
        if (!viewDataListCopy.isEmpty()) {
          onBatchExport(viewDataListCopy);
        }
      }
    }
  }
}
