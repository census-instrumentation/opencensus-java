/*
 * Copyright 2018, OpenCensus Authors
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

package io.opencensus.exporter.metrics.ocagent;

import com.google.auto.value.AutoValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.netty.handler.ssl.SslContext;
import io.opencensus.common.Duration;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Configurations for {@code OcAgentMetricsExporter}.
 *
 * @since 0.20
 */
@AutoValue
@Immutable
public abstract class OcAgentMetricsExporterConfiguration {

  @VisibleForTesting static final String DEFAULT_END_POINT = "localhost:55678";
  @VisibleForTesting static final String DEFAULT_SERVICE_NAME = "OpenCensus";
  @VisibleForTesting static final Duration DEFAULT_RETRY_INTERVAL = Duration.create(300, 0);
  @VisibleForTesting static final Duration DEFAULT_EXPORT_INTERVAL = Duration.create(60, 0);
  @VisibleForTesting static final Duration ZERO = Duration.create(0, 0);

  OcAgentMetricsExporterConfiguration() {}

  /**
   * Returns the end point of OC-Agent. The end point can be dns, ip:port, etc.
   *
   * <p>Default value is "localhost:55678" if not set.
   *
   * @return the end point of OC-Agent.
   * @since 0.20
   */
  public abstract String getEndPoint();

  /**
   * Returns whether to disable client transport security for the exporter's gRPC connection or not.
   *
   * <p>Default value is true if not set.
   *
   * @return whether to disable client transport security for the exporter's gRPC connection or not.
   * @since 0.20
   */
  public abstract Boolean getUseInsecure();

  /**
   * Returns the {@link SslContext} for secure TLS gRPC connection.
   *
   * <p>If not set OcAgent exporter will use insecure connection by default.
   *
   * @return the {@code SslContext}.
   * @since 0.20
   */
  @Nullable
  public abstract SslContext getSslContext();

  /**
   * Returns the service name to be used for the {@code OcAgentMetricsExporter}.
   *
   * <p>Default value is "OpenCensus" if not set.
   *
   * @return the service name.
   * @since 0.20
   */
  public abstract String getServiceName();

  /**
   * Returns the retry time interval when trying to connect to Agent.
   *
   * <p>Default value is 5 minutes.
   *
   * @return the retry time interval.
   * @since 0.20
   */
  public abstract Duration getRetryInterval();

  /**
   * Returns the export interval between pushes to Agent.
   *
   * <p>Default value is 1 minute.
   *
   * @return the export interval.
   * @since 0.20
   */
  public abstract Duration getExportInterval();

  /**
   * Returns a new {@link Builder}.
   *
   * @return a {@code Builder}.
   * @since 0.20
   */
  public static Builder builder() {
    return new AutoValue_OcAgentMetricsExporterConfiguration.Builder()
        .setEndPoint(DEFAULT_END_POINT)
        .setServiceName(DEFAULT_SERVICE_NAME)
        .setRetryInterval(DEFAULT_RETRY_INTERVAL)
        .setExportInterval(DEFAULT_EXPORT_INTERVAL)
        .setUseInsecure(true);
  }

  /**
   * Builder for {@link OcAgentMetricsExporterConfiguration}.
   *
   * @since 0.20
   */
  @AutoValue.Builder
  public abstract static class Builder {

    Builder() {}

    /**
     * Sets the end point of OC-Agent server.
     *
     * @param endPoint the end point of OC-Agent.
     * @return this.
     * @since 0.20
     */
    public abstract Builder setEndPoint(String endPoint);

    /**
     * Sets whether to disable client transport security for the exporter's gRPC connection or not.
     *
     * @param useInsecure whether disable client transport security for the exporter's gRPC
     *     connection.
     * @return this.
     * @since 0.20
     */
    public abstract Builder setUseInsecure(Boolean useInsecure);

    /**
     * Sets the {@link SslContext} for secure TLS gRPC connection.
     *
     * @param sslContext the {@code SslContext}.
     * @return this.
     * @since 0.20
     */
    public abstract Builder setSslContext(SslContext sslContext);

    /**
     * Sets the service name to be used for the {@code OcAgentMetricsExporter}.
     *
     * @param serviceName the service name.
     * @return this.
     * @since 0.20
     */
    public abstract Builder setServiceName(String serviceName);

    /**
     * Sets the retry time interval when trying to connect to Agent.
     *
     * @param retryInterval the retry time interval.
     * @return this.
     * @since 0.20
     */
    public abstract Builder setRetryInterval(Duration retryInterval);

    /**
     * Sets the export time interval between pushes to Agent.
     *
     * @param exportInterval the export time interval.
     * @return this.
     * @since 0.20
     */
    public abstract Builder setExportInterval(Duration exportInterval);

    // TODO(songya): add an option that controls whether to always keep the RPC connection alive.

    abstract Duration getRetryInterval();

    abstract Duration getExportInterval();

    abstract OcAgentMetricsExporterConfiguration autoBuild();

    /**
     * Builds a {@link OcAgentMetricsExporterConfiguration}.
     *
     * @return a {@code OcAgentMetricsExporterConfiguration}.
     * @since 0.20
     */
    public OcAgentMetricsExporterConfiguration build() {
      Preconditions.checkArgument(
          getRetryInterval().compareTo(ZERO) > 0, "Retry interval must be positive.");
      Preconditions.checkArgument(
          getExportInterval().compareTo(ZERO) > 0, "Export interval must be positive.");
      return autoBuild();
    }
  }
}
