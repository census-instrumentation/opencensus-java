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
import com.google.common.util.concurrent.MoreExecutors;
import io.opencensus.common.Duration;
import io.opencensus.stats.Stats;
import io.opencensus.stats.ViewManager;
import java.io.IOException;
import java.util.concurrent.ThreadFactory;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

/**
 * Exporter to Stackdriver Monitoring Client API v3.
 *
 * <p>Example of usage on Google Cloud VMs:
 *
 * <pre><code>
 *   public static void main(String[] args) {
 *     StackdriverStatsExporter.createAndRegister(
 *         StackdriverStatsConfiguration
 *             .builder()
 *             .setProjectId("MyStackdriverProjectId")
 *             .setExportInterval(Duration.fromMillis(100000))
 *             .build());
 *     ... // Do work.
 *   }
 * </code></pre>
 *
 * @since 0.9
 */
public final class StackdriverStatsExporter {

  private static final Object monitor = new Object();

  private final Thread workerThread;

  @GuardedBy("monitor")
  @Nullable
  private static StackdriverStatsExporter exporter = null;

  private static final Duration ZERO = Duration.create(0, 0);

  @VisibleForTesting static final Duration DEFAULT_INTERVAL = Duration.create(60, 0);

  @VisibleForTesting
  static final MonitoredResource DEFAULT_RESOURCE = StackdriverExportUtils.getResource();

  @VisibleForTesting
  StackdriverStatsExporter(
      String projectId,
      MetricServiceClient metricServiceClient,
      Duration exportInterval,
      ViewManager viewManager,
      MonitoredResource monitoredResource) {
    checkArgument(exportInterval.compareTo(ZERO) > 0, "Duration must be positive");
    StackdriverExporterWorker worker =
        new StackdriverExporterWorker(
            projectId, metricServiceClient, exportInterval, viewManager, monitoredResource);
    this.workerThread = new DaemonThreadFactory().newThread(worker);
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
   * @deprecated in favor of {@link #createAndRegister(StackdriverStatsConfiguration)}.
   * @since 0.9
   */
  @Deprecated
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
   * @deprecated in favor of {@link #createAndRegister(StackdriverStatsConfiguration)}.
   * @since 0.9
   */
  @Deprecated
  public static void createAndRegisterWithProjectId(String projectId, Duration exportInterval)
      throws IOException {
    checkNotNull(projectId, "projectId");
    checkNotNull(exportInterval, "exportInterval");
    createInternal(null, projectId, exportInterval, null);
  }

  /**
   * Creates a Stackdriver Stats exporter with a {@link StackdriverStatsConfiguration}.
   *
   * <p>Only one Stackdriver exporter can be created.
   *
   * <p>If {@code credentials} of the configuration is not set, the exporter will use the default
   * application credentials. See {@link GoogleCredentials#getApplicationDefault}.
   *
   * <p>If {@code projectId} of the configuration is not set, the exporter will use the default
   * project ID configured. See {@link ServiceOptions#getDefaultProjectId}.
   *
   * <p>If {@code exportInterval} of the configuration is not set, the exporter will use the default
   * interval of one minute.
   *
   * <p>If {@code monitoredResources} of the configuration is not set, the exporter will try to
   * create an appropriate {@code monitoredResources} based on the environment variables. In
   * addition, please refer to
   * cloud.google.com/monitoring/custom-metrics/creating-metrics#which-resource for a list of valid
   * {@code MonitoredResource}s.
   *
   * @param configuration the {@code StackdriverStatsConfiguration}.
   * @throws IllegalStateException if a Stackdriver exporter is already created.
   * @since 0.11.0
   */
  public static void createAndRegister(StackdriverStatsConfiguration configuration)
      throws IOException {
    checkNotNull(configuration, "configuration");
    createInternal(
        configuration.getCredentials(),
        configuration.getProjectId(),
        configuration.getExportInterval(),
        configuration.getMonitoredResource());
  }

  /**
   * Creates a Stackdriver Stats exporter with default settings.
   *
   * <p>Only one Stackdriver exporter can be created.
   *
   * <p>This is equivalent with:
   *
   * <pre>{@code
   * StackdriverStatsExporter.createAndRegister(StackdriverStatsConfiguration.builder().build());
   * }</pre>
   *
   * <p>This method uses the default application credentials. See {@link
   * GoogleCredentials#getApplicationDefault}.
   *
   * <p>This method uses the default project ID configured. See {@link
   * ServiceOptions#getDefaultProjectId}.
   *
   * <p>This method uses the default interval of one minute.
   *
   * <p>This method uses the default resource created from the environment variables.
   *
   * @throws IllegalStateException if a Stackdriver exporter is already created.
   * @since 0.11.0
   */
  public static void createAndRegister() throws IOException {
    createInternal(null, null, null, null);
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
   * @deprecated in favor of {@link #createAndRegister(StackdriverStatsConfiguration)}.
   * @since 0.9
   */
  @Deprecated
  public static void createAndRegister(Duration exportInterval) throws IOException {
    checkNotNull(exportInterval, "exportInterval");
    createInternal(null, null, exportInterval, null);
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
   * @deprecated in favor of {@link #createAndRegister(StackdriverStatsConfiguration)}.
   * @since 0.10
   */
  @Deprecated
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
   * @deprecated in favor of {@link #createAndRegister(StackdriverStatsConfiguration)}.
   * @since 0.10
   */
  @Deprecated
  public static void createAndRegisterWithMonitoredResource(
      Duration exportInterval, MonitoredResource monitoredResource) throws IOException {
    checkNotNull(exportInterval, "exportInterval");
    checkNotNull(monitoredResource, "monitoredResource");
    createInternal(null, null, exportInterval, monitoredResource);
  }

  // Use createInternal() (instead of constructor) to enforce singleton.
  private static void createInternal(
      @Nullable Credentials credentials,
      @Nullable String projectId,
      @Nullable Duration exportInterval,
      @Nullable MonitoredResource monitoredResource)
      throws IOException {
    projectId = projectId == null ? ServiceOptions.getDefaultProjectId() : projectId;
    exportInterval = exportInterval == null ? DEFAULT_INTERVAL : exportInterval;
    monitoredResource = monitoredResource == null ? DEFAULT_RESOURCE : monitoredResource;
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

  /** A lightweight {@link ThreadFactory} to spawn threads in a GAE-Java7-compatible way. */
  // TODO(Hailong): Remove this once we use a callback to implement the exporter.
  static final class DaemonThreadFactory implements ThreadFactory {
    // AppEngine runtimes have constraints on threading and socket handling
    // that need to be accommodated.
    public static final boolean IS_RESTRICTED_APPENGINE =
        System.getProperty("com.google.appengine.runtime.environment") != null
            && "1.7".equals(System.getProperty("java.specification.version"));
    private static final ThreadFactory threadFactory = MoreExecutors.platformThreadFactory();

    @Override
    public Thread newThread(Runnable r) {
      Thread thread = threadFactory.newThread(r);
      if (!IS_RESTRICTED_APPENGINE) {
        thread.setName("ExportWorkerThread");
        thread.setDaemon(true);
      }
      return thread;
    }
  }
}
