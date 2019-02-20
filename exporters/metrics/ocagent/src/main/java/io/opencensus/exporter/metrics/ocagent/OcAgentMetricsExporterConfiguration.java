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
import io.netty.handler.ssl.SslContext;
import io.opencensus.common.Duration;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Configurations for {@code OcAgentMetricsExporter}.
 *
 * @since 0.19
 */
@AutoValue
@Immutable
public abstract class OcAgentMetricsExporterConfiguration {

  OcAgentMetricsExporterConfiguration() {}

  /**
   * Returns the end point of OC-Agent. The end point can be dns, ip:port, etc.
   *
   * @return the end point of OC-Agent.
   * @since 0.19
   */
  @Nullable
  public abstract String getEndPoint();

  /**
   * Returns whether to disable client transport security for the exporter's gRPC connection or not.
   *
   * @return whether to disable client transport security for the exporter's gRPC connection or not.
   * @since 0.19
   */
  @Nullable
  public abstract Boolean getUseInsecure();

  /**
   * Returns the {@link SslContext} for secure TLS gRPC connection.
   *
   * @return the {@code SslContext}.
   * @since 0.20
   */
  @Nullable
  public abstract SslContext getSslContext();

  /**
   * Returns the service name to be used for the {@code OcAgentMetricsExporter}.
   *
   * @return the service name.
   * @since 0.19
   */
  @Nullable
  public abstract String getServiceName();

  /**
   * Returns the retry time interval when trying to connect to Agent.
   *
   * @return the retry time interval.
   * @since 0.19
   */
  @Nullable
  public abstract Duration getRetryInterval();

  /**
   * Returns the export interval between pushes to Agent.
   *
   * @return the export interval.
   * @since 0.19
   */
  @Nullable
  public abstract Duration getExportInterval();

  /**
   * Returns a new {@link Builder}.
   *
   * @return a {@code Builder}.
   * @since 0.19
   */
  public static Builder builder() {
    return new AutoValue_OcAgentMetricsExporterConfiguration.Builder().setUseInsecure(true);
  }

  /**
   * Builder for {@link OcAgentMetricsExporterConfiguration}.
   *
   * @since 0.19
   */
  @AutoValue.Builder
  public abstract static class Builder {

    Builder() {}

    /**
     * Sets the end point of OC-Agent server.
     *
     * @param endPoint the end point of OC-Agent.
     * @return this.
     * @since 0.19
     */
    public abstract Builder setEndPoint(String endPoint);

    /**
     * Sets whether to disable client transport security for the exporter's gRPC connection or not.
     *
     * @param useInsecure whether disable client transport security for the exporter's gRPC
     *     connection.
     * @return this.
     * @since 0.19
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
     * @since 0.19
     */
    public abstract Builder setServiceName(String serviceName);

    /**
     * Sets the retry time interval when trying to connect to Agent.
     *
     * @param retryInterval the retry time interval.
     * @return this.
     * @since 0.19
     */
    public abstract Builder setRetryInterval(Duration retryInterval);

    /**
     * Sets the export time interval between pushes to Agent.
     *
     * @param exportInterval the export time interval.
     * @return this.
     * @since 0.19
     */
    public abstract Builder setExportInterval(Duration exportInterval);

    // TODO(songya): add an option that controls whether to always keep the RPC connection alive.

    /**
     * Builds a {@link OcAgentMetricsExporterConfiguration}.
     *
     * @return a {@code OcAgentMetricsExporterConfiguration}.
     * @since 0.19
     */
    public abstract OcAgentMetricsExporterConfiguration build();
  }
}
