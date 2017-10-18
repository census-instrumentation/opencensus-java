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

import static com.google.api.client.util.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static io.opencensus.exporter.stats.stackdriver.StackdriverExportUtils.createMetricDescriptor;
import static io.opencensus.exporter.stats.stackdriver.StackdriverExportUtils.createTimeSeriesList;

import com.google.api.MetricDescriptor;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.ServiceOptions;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.cloud.monitoring.v3.MetricServiceSettings;
import com.google.common.annotations.VisibleForTesting;
import com.google.monitoring.v3.CreateMetricDescriptorRequest;
import com.google.monitoring.v3.CreateTimeSeriesRequest;
import io.opencensus.stats.View;
import io.opencensus.stats.ViewData;
import io.opencensus.stats.ViewManager;
import java.io.IOException;
import java.util.Collection;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

/**
 * Export handler to Stackdriver Monitoring, using Monitoring Client API v3.
 *
 * <p>Example of usage on Google Cloud VMs:
 *
 * <pre><code>
 *   public static void main(String[] args) {
 *     StackdriverExportHandler handler =
 *       StackdriverExportHandler.createWithProjectId("MyStackdriverProjectId");
 *     viewManager.registerView(myView, Arrays.asList(handler));
 *     ... // Do work.
 *   }
 * </code></pre>
 */
public final class StackdriverExportHandler extends ViewManager.Handler {

  private final String projectId;
  private final MetricServiceClient metricServiceClient;

  private static final Object monitor = new Object();

  @GuardedBy("monitor")
  private static StackdriverExportHandler handler = null;

  @VisibleForTesting
  StackdriverExportHandler(String projectId, MetricServiceClient metricServiceClient) {
    this.projectId = checkNotNull(projectId, "projectId");
    this.metricServiceClient = metricServiceClient;
  }

  /**
   * Creates a StackdriverExportHandler for an explicit project ID and using explicit credentials.
   *
   * <p>Only one Stackdriver export handler can be created at any point.
   *
   * @param credentials a credentials used to authenticate API calls.
   * @param projectId the cloud project id.
   * @throws IllegalStateException if a Stackdriver exporter already exists.
   */
  public static StackdriverExportHandler createWithCredentialsAndProjectId(
      Credentials credentials, String projectId) throws IOException {
    checkNotNull(credentials, "credentials");
    synchronized (monitor) {
      checkState(handler == null, "Stackdriver exporter is already created.");
      handler = createInternal(credentials, projectId);
      return handler;
    }
  }

  /**
   * Creates a Stackdriver Stats exporter for an explicit project ID.
   *
   * <p>Only one Stackdriver exporter can be created at any point.
   *
   * <p>This uses the default application credentials. See {@link
   * GoogleCredentials#getApplicationDefault}.
   *
   * <p>This is equivalent with:
   *
   * <pre>{@code
   * StackdriverExportHandler.createWithCredentialsAndProjectId(
   *     GoogleCredentials.getApplicationDefault(), projectId);
   * }</pre>
   *
   * @param projectId the cloud project id.
   * @throws IllegalStateException if a Stackdriver exporter is already created.
   */
  public static StackdriverExportHandler createWithProjectId(String projectId) throws IOException {
    synchronized (monitor) {
      checkState(handler == null, "Stackdriver exporter is already created.");
      handler = createInternal(null, projectId);
      return handler;
    }
  }

  /**
   * Creates a Stackdriver Stats exporter.
   *
   * <p>Only one Stackdriver exporter can be created at any point.
   *
   * <p>This uses the default application credentials. See {@link
   * GoogleCredentials#getApplicationDefault}.
   *
   * <p>This uses the default project ID configured see {@link ServiceOptions#getDefaultProjectId}.
   *
   * <p>This is equivalent with:
   *
   * <pre>{@code
   * StackdriverExportHandler.createWithProjectId(ServiceOptions.getDefaultProjectId());
   * }</pre>
   *
   * @throws IllegalStateException if a Stackdriver exporter is already created.
   */
  public static StackdriverExportHandler create() throws IOException {
    synchronized (monitor) {
      checkState(handler == null, "Stackdriver exporter is already created.");
      handler = createInternal(null, ServiceOptions.getDefaultProjectId());
      return handler;
    }
  }

  private static StackdriverExportHandler createInternal(
      @Nullable Credentials credentials, String projectId) throws IOException {
    MetricServiceClient metricServiceClient;
    if (credentials == null) {
      metricServiceClient = MetricServiceClient.create();
    } else {
      metricServiceClient = MetricServiceClient.create(
          MetricServiceSettings.newBuilder()
              .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
              .build());
    }
    return new StackdriverExportHandler(projectId, metricServiceClient);
  }

  @Override
  public void registerView(View view) {
    // This is required only for custom view definitions. Canonical views should be pre-registered.
    MetricDescriptor metricDescriptor = createMetricDescriptor(view, projectId);
    if (metricDescriptor != null) {
      metricServiceClient.createMetricDescriptor(
          CreateMetricDescriptorRequest
              .newBuilder()
              .setMetricDescriptor(metricDescriptor)
              .build());
    }
  }

  @Override
  public void export(Collection<ViewData> viewDataList) {
    CreateTimeSeriesRequest.Builder builder = CreateTimeSeriesRequest.newBuilder();
    for (ViewData viewData : viewDataList) {
      builder.addAllTimeSeries(createTimeSeriesList(viewData, projectId));
    }
    if (!builder.getTimeSeriesList().isEmpty()) {
      metricServiceClient.createTimeSeries(builder.build());
    }
  }
}
