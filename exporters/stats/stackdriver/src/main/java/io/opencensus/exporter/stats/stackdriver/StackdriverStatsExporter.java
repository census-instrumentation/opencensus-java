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

import static com.google.api.client.util.Preconditions.checkArgument;
import static com.google.api.client.util.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.api.MetricDescriptor;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.ServiceOptions;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.cloud.monitoring.v3.MetricServiceSettings;
import com.google.common.annotations.VisibleForTesting;
import com.google.monitoring.v3.CreateMetricDescriptorRequest;
import io.opencensus.common.Duration;
import io.opencensus.stats.Stats;
import io.opencensus.stats.View;
import io.opencensus.stats.ViewManager;
import java.io.IOException;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

/**
 * Exporter to Stackdriver Monitoring Client API v3.
 *
 * <p>Example of usage on Google Cloud VMs:
 *
 * <pre><code>
 *   public static void main(String[] args) {
 *     StackdriverStatsExporter.createWithProjectId(
 *         "MyStackdriverProjectId", Duration.fromMillis(100000));
 *     StackdriverStatsExporter.registerView(myView);
 *     ... // Do work.
 *   }
 * </code></pre>
 */
public final class StackdriverStatsExporter {

  private static final Object monitor = new Object();

  private final String projectId;
  private final MetricServiceClient metricServiceClient;
  private final WorkerThread workerThread;

  @GuardedBy("monitor")
  private static StackdriverStatsExporter exporter = null;

  private static final Duration ZERO = Duration.create(0, 0);

  @VisibleForTesting
  StackdriverStatsExporter(
      String projectId,
      MetricServiceClient metricServiceClient,
      Duration exportInterval,
      ViewManager viewManager) {
    checkArgument(exportInterval.compareTo(ZERO) > 0, "Duration must be positive");
    this.projectId = projectId;
    this.metricServiceClient = metricServiceClient;
    this.workerThread =
        new WorkerThread(projectId, metricServiceClient, exportInterval, viewManager);
  }

  /**
   * Creates a StackdriverStatsExporter for an explicit project ID and using explicit credentials.
   *
   * <p>Only one Stackdriver exporter can be created.
   *
   * @param credentials a credentials used to authenticate API calls.
   * @param projectId the cloud project id.
   * @param exportInterval the interval between pushing stats to StackDriver.
   * @throws IllegalStateException if a Stackdriver exporter already exists.
   */
  public static void createWithCredentialsAndProjectId(
      Credentials credentials, String projectId, Duration exportInterval) throws IOException {
    checkNotNull(credentials, "credentials");
    checkNotNull(projectId, "projectId");
    checkNotNull(exportInterval, "exportInterval");
    createInternal(credentials, projectId, exportInterval);
  }

  /**
   * Creates a Stackdriver Stats exporter for an explicit project ID.
   *
   * <p>Only one Stackdriver exporter can be created.
   *
   * <p>This uses the default application credentials. See {@link
   * GoogleCredentials#getApplicationDefault}.
   *
   * <p>This is equivalent with:
   *
   * <pre>{@code
   * StackdriverStatsExporter.createWithCredentialsAndProjectId(
   *     GoogleCredentials.getApplicationDefault(), projectId);
   * }</pre>
   *
   * @param projectId the cloud project id.
   * @param exportInterval the interval between pushing stats to StackDriver.
   * @throws IllegalStateException if a Stackdriver exporter is already created.
   */
  public static void createWithProjectId(String projectId, Duration exportInterval)
      throws IOException {
    checkNotNull(projectId, "projectId");
    checkNotNull(exportInterval, "exportInterval");
    createInternal(null, projectId, exportInterval);
  }

  /**
   * Creates a Stackdriver Stats exporter.
   *
   * <p>Only one Stackdriver exporter can be created.
   *
   * <p>This uses the default application credentials. See {@link
   * GoogleCredentials#getApplicationDefault}.
   *
   * <p>This uses the default project ID configured see {@link ServiceOptions#getDefaultProjectId}.
   *
   * <p>This is equivalent with:
   *
   * <pre>{@code
   * StackdriverStatsExporter.createWithProjectId(ServiceOptions.getDefaultProjectId());
   * }</pre>
   *
   * @param exportInterval the interval between pushing stats to StackDriver.
   * @throws IllegalStateException if a Stackdriver exporter is already created.
   */
  public static void create(Duration exportInterval) throws IOException {
    checkNotNull(exportInterval, "exportInterval");
    createInternal(null, ServiceOptions.getDefaultProjectId(), exportInterval);
  }

  // Use createInternal() (instead of constructor) to enforce singleton.
  private static void createInternal(
      @Nullable Credentials credentials, String projectId, Duration exportInterval)
      throws IOException {
    synchronized (monitor) {
      checkState(exporter == null, "Stackdriver stats exporter is already created.");
      MetricServiceClient metricServiceClient;
      // Initialize MetricServiceClient inside lock to avoid creating multiple clients.
      if (credentials == null) {
        metricServiceClient = MetricServiceClient.create();
      } else {
        metricServiceClient =
            MetricServiceClient.create(
                MetricServiceSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build());
      }
      exporter =
          new StackdriverStatsExporter(
              projectId, metricServiceClient, exportInterval, Stats.getViewManager());
      exporter.workerThread.start();
    }
  }

  // Method for setting exporter to a fake exporter or null (reset) for unit tests.
  @VisibleForTesting
  static void setExporter(StackdriverStatsExporter exporter) {
    synchronized (monitor) {
      StackdriverStatsExporter.exporter = exporter;
      if (exporter != null) {
        exporter.workerThread.start();
      }
    }
  }

  /**
   * Register a {@link View} against this exporter, and upload it as a {@link MetricDescriptor} to
   * StackDriver.
   *
   * @param view the {@code View} to be registered.
   * @throws IllegalStateException if a Stackdriver stats exporter has not been created yet.
   */
  // TODO(songya): remove this API and have exporter polls stats using getAllExportedView(). Views
  // should not be registered against exporter, since in the future we'll probably switch to a push
  // model.
  public static void registerView(View view) {
    synchronized (monitor) {
      checkState(exporter != null, "Stackdriver stats exporter has not been created.");
      // TODO(songya): don't need to create MetricDescriptor for RpcViewConstants once we defined
      // canonical metrics. Registration is required only for custom view definitions. Canonical
      // views should be pre-registered.
      MetricDescriptor metricDescriptor =
          StackdriverExportUtils.createMetricDescriptor(view, exporter.projectId);
      if (metricDescriptor != null) {
        exporter.metricServiceClient.createMetricDescriptor(
            CreateMetricDescriptorRequest.newBuilder()
                .setMetricDescriptor(metricDescriptor)
                .build());
        exporter.workerThread.registerView(view);
      }
    }
  }
}
