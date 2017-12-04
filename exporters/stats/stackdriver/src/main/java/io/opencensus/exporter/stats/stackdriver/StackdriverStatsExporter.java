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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.api.MonitoredResource;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.ServiceOptions;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.cloud.monitoring.v3.MetricServiceSettings;
import com.google.common.annotations.VisibleForTesting;
import io.opencensus.common.Duration;
import io.opencensus.stats.Stats;
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
 *     StackdriverStatsExporter.createAndRegisterWithProjectId(
 *         "MyStackdriverProjectId", Duration.fromMillis(100000));
 *     ... // Do work.
 *   }
 * </code></pre>
 */
public final class StackdriverStatsExporter {

  private static final Object monitor = new Object();

  private final StackdriverExporterWorkerThread workerThread;

  @GuardedBy("monitor")
  private static StackdriverStatsExporter exporter = null;

  private static final Duration ZERO = Duration.create(0, 0);
  private static final MonitoredResource DEFAULT_RESOURCE =
      MonitoredResource.newBuilder().setType("global").build();

  @VisibleForTesting
  StackdriverStatsExporter(
      String projectId,
      MetricServiceClient metricServiceClient,
      Duration exportInterval,
      ViewManager viewManager,
      MonitoredResource monitoredResource) {
    checkArgument(exportInterval.compareTo(ZERO) > 0, "Duration must be positive");
    this.workerThread =
        new StackdriverExporterWorkerThread(
            projectId, metricServiceClient, exportInterval, viewManager, monitoredResource);
  }

  /**
   * Creates a StackdriverStatsExporter for an explicit project ID and using explicit credentials,
   * with default Monitored Resource.
   *
   * <p>Only one Stackdriver exporter can be created.
   *
   * @param credentials a credentials used to authenticate API calls.
   * @param projectId the cloud project id.
   * @param exportInterval the interval between pushing stats to StackDriver.
   * @throws IllegalStateException if a Stackdriver exporter already exists.
   */
  public static void createAndRegisterWithCredentialsAndProjectId(
      Credentials credentials, String projectId, Duration exportInterval) throws IOException {
    checkNotNull(credentials, "credentials");
    checkNotNull(projectId, "projectId");
    checkNotNull(exportInterval, "exportInterval");
    createInternal(credentials, projectId, exportInterval, null);
  }

  /**
   * Creates a Stackdriver Stats exporter for an explicit project ID, with default Monitored
   * Resource.
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
  public static void createAndRegisterWithProjectId(String projectId, Duration exportInterval)
      throws IOException {
    checkNotNull(projectId, "projectId");
    checkNotNull(exportInterval, "exportInterval");
    createInternal(null, projectId, exportInterval, null);
  }

  /**
   * Creates a Stackdriver Stats exporter with default Monitored Resource.
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
  public static void createAndRegister(Duration exportInterval) throws IOException {
    checkNotNull(exportInterval, "exportInterval");
    createInternal(null, ServiceOptions.getDefaultProjectId(), exportInterval, null);
  }

  /**
   * Creates a Stackdriver Stats exporter with an explicit project ID and a custom Monitored
   * Resource.
   *
   * <p>Only one Stackdriver exporter can be created.
   *
   * <p>Please refer to cloud.google.com/monitoring/custom-metrics/creating-metrics#which-resource
   * for a list of valid {@code MonitoredResource}s.
   *
   * <p>This uses the default application credentials. See {@link
   * GoogleCredentials#getApplicationDefault}.
   *
   * @param projectId the cloud project id.
   * @param exportInterval the interval between pushing stats to StackDriver.
   * @param monitoredResource the Monitored Resource used by exporter.
   * @throws IllegalStateException if a Stackdriver exporter is already created.
   */
  public static void createAndRegisterWithProjectIdAndMonitoredResource(
      String projectId, Duration exportInterval, MonitoredResource monitoredResource)
      throws IOException {
    checkNotNull(projectId, "projectId");
    checkNotNull(exportInterval, "exportInterval");
    checkNotNull(monitoredResource, "monitoredResource");
    createInternal(null, projectId, exportInterval, monitoredResource);
  }

  /**
   * Creates a Stackdriver Stats exporter with a custom Monitored Resource.
   *
   * <p>Only one Stackdriver exporter can be created.
   *
   * <p>Please refer to cloud.google.com/monitoring/custom-metrics/creating-metrics#which-resource
   * for a list of valid {@code MonitoredResource}s.
   *
   * <p>This uses the default application credentials. See {@link
   * GoogleCredentials#getApplicationDefault}.
   *
   * <p>This uses the default project ID configured see {@link ServiceOptions#getDefaultProjectId}.
   *
   * @param exportInterval the interval between pushing stats to StackDriver.
   * @param monitoredResource the Monitored Resource used by exporter.
   * @throws IllegalStateException if a Stackdriver exporter is already created.
   */
  public static void createAndRegisterWithMonitoredResource(
      Duration exportInterval, MonitoredResource monitoredResource) throws IOException {
    checkNotNull(exportInterval, "exportInterval");
    checkNotNull(monitoredResource, "monitoredResource");
    createInternal(null, ServiceOptions.getDefaultProjectId(), exportInterval, monitoredResource);
  }

  // Use createInternal() (instead of constructor) to enforce singleton.
  private static void createInternal(
      @Nullable Credentials credentials,
      String projectId,
      Duration exportInterval,
      @Nullable MonitoredResource monitoredResource)
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
      if (monitoredResource == null) {
        monitoredResource = DEFAULT_RESOURCE;
      }
      exporter =
          new StackdriverStatsExporter(
              projectId,
              metricServiceClient,
              exportInterval,
              Stats.getViewManager(),
              monitoredResource);
      exporter.workerThread.start();
    }
  }

  // Resets exporter to null. Used only for unit tests.
  @VisibleForTesting
  static void unsafeResetExporter() {
    synchronized (monitor) {
      StackdriverStatsExporter.exporter = null;
    }
  }
}
