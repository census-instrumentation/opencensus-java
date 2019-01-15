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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.api.MonitoredResource;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider;
import com.google.api.gax.rpc.FixedHeaderProvider;
import com.google.api.gax.rpc.HeaderProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.ServiceOptions;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.cloud.monitoring.v3.MetricServiceSettings;
import com.google.common.annotations.VisibleForTesting;
import io.opencensus.common.Duration;
import io.opencensus.common.OpenCensusLibraryInformation;
import io.opencensus.exporter.metrics.util.IntervalMetricReader;
import io.opencensus.exporter.metrics.util.MetricReader;
import io.opencensus.metrics.Metrics;
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

  @VisibleForTesting static final Object monitor = new Object();

  @GuardedBy("monitor")
  @Nullable
  private static StackdriverStatsExporter instance = null;

  private static final String EXPORTER_SPAN_NAME = "ExportMetricsToStackdriver";

  // See io.grpc.internal.GrpcUtil.USER_AGENT_KEY
  private static final String USER_AGENT_KEY = "user-agent";
  private static final String USER_AGENT =
      "opencensus-java/" + OpenCensusLibraryInformation.VERSION;
  private static final HeaderProvider OPENCENSUS_USER_AGENT_HEADER_PROVIDER =
      FixedHeaderProvider.create(USER_AGENT_KEY, USER_AGENT);
  @VisibleForTesting static final Duration DEFAULT_INTERVAL = Duration.create(60, 0);

  private static final MonitoredResource DEFAULT_RESOURCE =
      StackdriverExportUtils.getDefaultResource();

  private final IntervalMetricReader intervalMetricReader;

  private StackdriverStatsExporter(
      String projectId,
      MetricServiceClient metricServiceClient,
      Duration exportInterval,
      MonitoredResource monitoredResource,
      @Nullable String metricNamePrefix) {
    IntervalMetricReader.Options.Builder intervalMetricReaderOptionsBuilder =
        IntervalMetricReader.Options.builder();
    if (exportInterval != null) {
      intervalMetricReaderOptionsBuilder.setExportInterval(exportInterval);
    }
    intervalMetricReader =
        IntervalMetricReader.create(
            new CreateMetricDescriptorExporter(
                projectId,
                metricServiceClient,
                metricNamePrefix,
                new CreateTimeSeriesExporter(
                    projectId, metricServiceClient, monitoredResource, metricNamePrefix)),
            MetricReader.create(
                MetricReader.Options.builder()
                    .setMetricProducerManager(
                        Metrics.getExportComponent().getMetricProducerManager())
                    .setSpanName(EXPORTER_SPAN_NAME)
                    .build()),
            intervalMetricReaderOptionsBuilder.build());
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
    createInternal(credentials, projectId, exportInterval, null, null);
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
    createInternal(null, projectId, exportInterval, null, null);
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
   * <p>If {@code metricNamePrefix} of the configuration is not set, the exporter will use the
   * default prefix "OpenCensus".
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
        configuration.getMonitoredResource(),
        configuration.getMetricNamePrefix());
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
   * <p>This method uses the default display name prefix "OpenCensus".
   *
   * @throws IllegalStateException if a Stackdriver exporter is already created.
   * @since 0.11.0
   */
  public static void createAndRegister() throws IOException {
    createInternal(null, null, null, null, null);
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
    createInternal(null, null, exportInterval, null, null);
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
    createInternal(null, projectId, exportInterval, monitoredResource, null);
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
    createInternal(null, null, exportInterval, monitoredResource, null);
  }

  // Use createInternal() (instead of constructor) to enforce singleton.
  private static void createInternal(
      @Nullable Credentials credentials,
      @Nullable String projectId,
      @Nullable Duration exportInterval,
      @Nullable MonitoredResource monitoredResource,
      @Nullable String metricNamePrefix)
      throws IOException {
    projectId =
        projectId == null
            // TODO(sebright): Handle null default project ID.
            ? castNonNull(ServiceOptions.getDefaultProjectId())
            : projectId;
    synchronized (monitor) {
      checkState(instance == null, "Stackdriver stats exporter is already created.");
      instance =
          new StackdriverStatsExporter(
              projectId,
              createMetricServiceClient(credentials),
              exportInterval == null ? DEFAULT_INTERVAL : exportInterval,
              monitoredResource == null ? DEFAULT_RESOURCE : monitoredResource,
              metricNamePrefix);
    }
  }

  // TODO(sebright): Remove this method.
  @SuppressWarnings("nullness")
  private static <T> T castNonNull(@javax.annotation.Nullable T arg) {
    return arg;
  }

  // Initialize MetricServiceClient inside lock to avoid creating multiple clients.
  @GuardedBy("monitor")
  @VisibleForTesting
  static MetricServiceClient createMetricServiceClient(@Nullable Credentials credentials)
      throws IOException {
    MetricServiceSettings.Builder settingsBuilder =
        MetricServiceSettings.newBuilder()
            .setTransportChannelProvider(
                InstantiatingGrpcChannelProvider.newBuilder()
                    .setHeaderProvider(OPENCENSUS_USER_AGENT_HEADER_PROVIDER)
                    .build());
    if (credentials != null) {
      settingsBuilder.setCredentialsProvider(FixedCredentialsProvider.create(credentials));
    }
    return MetricServiceClient.create(settingsBuilder.build());
  }

  // Resets exporter to null. Used only for unit tests.
  @VisibleForTesting
  static void unsafeResetExporter() {
    synchronized (monitor) {
      if (instance != null) {
        instance.intervalMetricReader.stop();
      }
      instance = null;
    }
  }
}
