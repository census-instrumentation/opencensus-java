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
import static io.opencensus.exporter.stats.stackdriver.StackdriverExportUtils.DEFAULT_CONSTANT_LABELS;
import static io.opencensus.exporter.stats.stackdriver.StackdriverStatsConfiguration.DEFAULT_PROJECT_ID;
import static io.opencensus.exporter.stats.stackdriver.StackdriverStatsConfiguration.DEFAULT_RESOURCE;

import com.google.api.MetricDescriptor;
import com.google.api.MonitoredResource;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.grpc.InstantiatingGrpcChannelProvider;
import com.google.api.gax.retrying.RetrySettings;
import com.google.api.gax.rpc.FixedHeaderProvider;
import com.google.api.gax.rpc.HeaderProvider;
import com.google.api.gax.rpc.UnaryCallSettings;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.ServiceOptions;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.cloud.monitoring.v3.MetricServiceSettings;
import com.google.common.annotations.VisibleForTesting;
import com.google.monitoring.v3.CreateMetricDescriptorRequest;
import com.google.monitoring.v3.CreateTimeSeriesRequest;
import com.google.protobuf.Empty;
import io.opencensus.common.Duration;
import io.opencensus.common.OpenCensusLibraryInformation;
import io.opencensus.exporter.metrics.util.IntervalMetricReader;
import io.opencensus.exporter.metrics.util.MetricReader;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.Metrics;
import java.io.IOException;
import java.util.Map;
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

  private final IntervalMetricReader intervalMetricReader;

  private StackdriverStatsExporter(
      String projectId,
      MetricServiceClient metricServiceClient,
      Duration exportInterval,
      MonitoredResource monitoredResource,
      @Nullable String metricNamePrefix,
      Map<LabelKey, LabelValue> constantLabels) {
    IntervalMetricReader.Options.Builder intervalMetricReaderOptionsBuilder =
        IntervalMetricReader.Options.builder();
    intervalMetricReaderOptionsBuilder.setExportInterval(exportInterval);
    intervalMetricReader =
        IntervalMetricReader.create(
            new CreateMetricDescriptorExporter(
                projectId,
                metricServiceClient,
                metricNamePrefix,
                constantLabels,
                new CreateTimeSeriesExporter(
                    projectId,
                    metricServiceClient,
                    monitoredResource,
                    metricNamePrefix,
                    constantLabels)),
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
    createInternal(
        credentials,
        projectId,
        exportInterval,
        DEFAULT_RESOURCE,
        null,
        DEFAULT_CONSTANT_LABELS,
        null);
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
    createInternal(
        null, projectId, exportInterval, DEFAULT_RESOURCE, null, DEFAULT_CONSTANT_LABELS, null);
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
        configuration.getMetricNamePrefix(),
        configuration.getConstantLabels(),
        configuration.getDeadline());
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
    createAndRegister(StackdriverStatsConfiguration.builder().build());
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
    checkArgument(
        !DEFAULT_PROJECT_ID.isEmpty(), "Cannot find a project ID from application default.");
    createInternal(
        null,
        DEFAULT_PROJECT_ID,
        exportInterval,
        DEFAULT_RESOURCE,
        null,
        DEFAULT_CONSTANT_LABELS,
        null);
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
    createInternal(
        null, projectId, exportInterval, monitoredResource, null, DEFAULT_CONSTANT_LABELS, null);
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
    checkArgument(
        !DEFAULT_PROJECT_ID.isEmpty(), "Cannot find a project ID from application default.");
    createInternal(
        null,
        DEFAULT_PROJECT_ID,
        exportInterval,
        monitoredResource,
        null,
        DEFAULT_CONSTANT_LABELS,
        null);
  }

  // Use createInternal() (instead of constructor) to enforce singleton.
  private static void createInternal(
      @Nullable Credentials credentials,
      String projectId,
      Duration exportInterval,
      MonitoredResource monitoredResource,
      @Nullable String metricNamePrefix,
      Map<LabelKey, LabelValue> constantLabels,
      @Nullable Duration deadline)
      throws IOException {
    synchronized (monitor) {
      checkState(instance == null, "Stackdriver stats exporter is already created.");
      instance =
          new StackdriverStatsExporter(
              projectId,
              createMetricServiceClient(credentials, deadline),
              exportInterval,
              monitoredResource,
              metricNamePrefix,
              constantLabels);
    }
  }

  // Initialize MetricServiceClient inside lock to avoid creating multiple clients.
  @GuardedBy("monitor")
  @VisibleForTesting
  static MetricServiceClient createMetricServiceClient(
      @Nullable Credentials credentials, @Nullable Duration deadline) throws IOException {
    MetricServiceSettings.Builder settingsBuilder =
        MetricServiceSettings.newBuilder()
            .setTransportChannelProvider(
                InstantiatingGrpcChannelProvider.newBuilder()
                    .setHeaderProvider(OPENCENSUS_USER_AGENT_HEADER_PROVIDER)
                    .build());
    if (credentials != null) {
      settingsBuilder.setCredentialsProvider(FixedCredentialsProvider.create(credentials));
    }

    // We use createMetricDescriptor and createTimeSeries APIs in this exporter.
    UnaryCallSettings.Builder<CreateMetricDescriptorRequest, MetricDescriptor>
        createMetricDescriptorSettings = settingsBuilder.createMetricDescriptorSettings();
    UnaryCallSettings.Builder<CreateTimeSeriesRequest, Empty> createTimeSeriesSettings =
        settingsBuilder.createTimeSeriesSettings();
    if (deadline != null) {
      org.threeten.bp.Duration stackdriverDuration =
          org.threeten.bp.Duration.ofMillis(deadline.toMillis());
      createMetricDescriptorSettings.setSimpleTimeoutNoRetries(stackdriverDuration);
      createTimeSeriesSettings.setSimpleTimeoutNoRetries(stackdriverDuration);
    } else {
      /*
       * Default retry settings for Stackdriver Monitoring client is:
       * settings =
       *   RetrySettings.newBuilder()
       *       .setInitialRetryDelay(Duration.ofMillis(100L))
       *       .setRetryDelayMultiplier(1.3)
       *       .setMaxRetryDelay(Duration.ofMillis(60000L))
       *       .setInitialRpcTimeout(Duration.ofMillis(20000L))
       *       .setRpcTimeoutMultiplier(1.0)
       *       .setMaxRpcTimeout(Duration.ofMillis(20000L))
       *       .setTotalTimeout(Duration.ofMillis(600000L))
       *       .build();
       *
       * Override the default settings with settings that don't retry.
       */
      RetrySettings noRetrySettings = RetrySettings.newBuilder().build();
      createMetricDescriptorSettings.setRetryableCodes().setRetrySettings(noRetrySettings);
      createTimeSeriesSettings.setRetryableCodes().setRetrySettings(noRetrySettings);
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
