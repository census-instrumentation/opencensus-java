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

package io.opencensus.exporter.stats.stackdriver;

import com.google.api.MetricDescriptor;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.monitoring.v3.CreateMetricDescriptorRequest;
import com.google.monitoring.v3.CreateTimeSeriesRequest;
import com.google.monitoring.v3.ProjectName;
import com.google.monitoring.v3.TimeSeries;
import io.opencensus.common.Duration;
import io.opencensus.stats.View;
import io.opencensus.stats.ViewData;
import io.opencensus.stats.ViewManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * Worker {@code Thread} that polls ViewData from Stats library and batch export to StackDriver.
 *
 * <p>{@code StackdriverExporterWorkerThread} is a daemon {@code Thread}.
 *
 * <p>The state of this class should only be accessed from the {@link
 * StackdriverExporterWorkerThread} thread.
 */
@NotThreadSafe
final class StackdriverExporterWorkerThread extends Thread {

  private static final Logger logger =
      Logger.getLogger(StackdriverExporterWorkerThread.class.getName());

  @VisibleForTesting static final int MAX_BATCH_EXPORT_SIZE = 3;

  private final long scheduleDelayMillis;
  private final String projectId;
  private final ProjectName projectName;
  private final MetricServiceClient metricServiceClient;
  private final ViewManager viewManager;
  private final Map<View.Name, View> registeredViews = new HashMap<View.Name, View>();

  StackdriverExporterWorkerThread(
      String projectId,
      MetricServiceClient metricServiceClient,
      Duration exportInterval,
      ViewManager viewManager) {
    this.scheduleDelayMillis = toMillis(exportInterval);
    this.projectId = projectId;
    projectName = ProjectName.newBuilder().setProject(projectId).build();
    this.metricServiceClient = metricServiceClient;
    this.viewManager = viewManager;
    setDaemon(true);
    setName("ExportWorkerThread");
  }

  // Registers the given view to Stackdriver Monitoring if the view is not registered yet, otherwise
  // does nothing. Throws IllegalArgumentException if the given view is a different view that has a
  // same name with a registered view.
  @VisibleForTesting
  void registerView(View view) {
    View existing = registeredViews.get(view.getName());
    if (existing != null) {
      if (existing.equals(view)) {
        // Ignore views that are already registered.
        return;
      } else {
        // If we upload a view that has the same name with a registered view but with different
        // attributes, Stackdriver client will throw an exception.
        logger.log(
            Level.WARNING,
            "A different view with the same name is already registered: " + existing);
        return;
      }
    }
    registeredViews.put(view.getName(), view);

    // TODO(songya): don't need to create MetricDescriptor for RpcViewConstants once we defined
    // canonical metrics. Registration is required only for custom view definitions. Canonical
    // views should be pre-registered.
    MetricDescriptor metricDescriptor =
        StackdriverExportUtils.createMetricDescriptor(view, projectId);
    if (metricDescriptor != null) {
      metricServiceClient.createMetricDescriptor(
          CreateMetricDescriptorRequest.newBuilder()
              .setNameWithProjectName(projectName)
              .setMetricDescriptor(metricDescriptor)
              .build());
    }
  }

  // Polls ViewData from Stats library for all exported views, and upload them as TimeSeries to
  // StackDriver.
  @VisibleForTesting
  void export() {
    List<ViewData> viewDataList = Lists.newArrayList();
    for (View view : viewManager.getAllExportedViews()) {
      registerView(view);
      viewDataList.add(viewManager.getView(view.getName()));
    }
    List<TimeSeries> timeSeriesList = Lists.newArrayList();
    for (ViewData viewData : viewDataList) {
      timeSeriesList.addAll(StackdriverExportUtils.createTimeSeriesList(viewData, projectId));
    }

    for (List<TimeSeries> batchedTimeSeries :
        Lists.partition(timeSeriesList, MAX_BATCH_EXPORT_SIZE)) {
      // Batch export 3 TimeSeries at one call, to avoid exceeding RPC header size limit.
      CreateTimeSeriesRequest.Builder builder =
          CreateTimeSeriesRequest.newBuilder().setNameWithProjectName(projectName);
      for (TimeSeries timeSeries : batchedTimeSeries) {
        builder.addTimeSeries(timeSeries);
      }
      try {
        metricServiceClient.createTimeSeries(builder.build());
      } catch (Throwable e) {
        logger.log(Level.WARNING, "Exception thrown when exporting TimeSeries.", e);
      }
    }
  }

  @Override
  public void run() {
    while (true) {
      try {
        export();
        Thread.sleep(scheduleDelayMillis);
      } catch (InterruptedException ie) {
        // Preserve the interruption status as per guidance and stop doing any work.
        Thread.currentThread().interrupt();
        return;
      } catch (Throwable e) {
        logger.log(Level.WARNING, "Exception thrown by the Stackdriver stats exporter.", e);
      }
    }
  }

  private static final long MILLIS_PER_SECOND = 1000L;
  private static final long NANOS_PER_MILLI = 1000 * 1000;

  private static long toMillis(Duration duration) {
    return duration.getSeconds() * MILLIS_PER_SECOND + duration.getNanos() / NANOS_PER_MILLI;
  }
}
