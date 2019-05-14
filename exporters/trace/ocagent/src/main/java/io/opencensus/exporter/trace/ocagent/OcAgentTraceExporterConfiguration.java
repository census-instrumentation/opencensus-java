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

package io.opencensus.exporter.trace.ocagent;

import com.google.auto.value.AutoValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.netty.handler.ssl.SslContext;
import io.opencensus.common.Duration;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Configurations for {@link OcAgentTraceExporter}.
 *
 * @since 0.20
 */
@AutoValue
@Immutable
public abstract class OcAgentTraceExporterConfiguration {

  @VisibleForTesting static final String DEFAULT_END_POINT = "localhost:55678";
  @VisibleForTesting static final String DEFAULT_SERVICE_NAME = "OpenCensus";
  @VisibleForTesting static final Duration DEFAULT_RETRY_INTERVAL = Duration.create(300, 0);
  @VisibleForTesting static final Duration DEFAULT_DEADLINE = Duration.create(10, 0);
  @VisibleForTesting static final Duration ZERO = Duration.create(0, 0);

  OcAgentTraceExporterConfiguration() {}

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
   * Returns the service name to be used for this {@link OcAgentTraceExporter}.
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
   * Returns whether the {@link OcAgentTraceExporter} should handle the config streams.
   *
   * <p>Config service is enabled by default.
   *
   * @return whether the {@code OcAgentTraceExporter} should handle the config streams.
   * @since 0.20
   */
  public abstract boolean getEnableConfig();

  /**
   * Returns the deadline for exporting to Agent/Collector.
   *
   * <p>Default value is 10 seconds.
   *
   * @return the export deadline.
   * @since 0.22
   */
  public abstract Duration getDeadline();

  /**
   * Returns a new {@link Builder}.
   *
   * @return a {@code Builder}.
   * @since 0.20
   */
  public static Builder builder() {
    return new AutoValue_OcAgentTraceExporterConfiguration.Builder()
        .setEndPoint(DEFAULT_END_POINT)
        .setServiceName(DEFAULT_SERVICE_NAME)
        .setEnableConfig(true)
        .setUseInsecure(true)
        .setRetryInterval(DEFAULT_RETRY_INTERVAL)
        .setDeadline(DEFAULT_DEADLINE);
  }

  /**
   * Builder for {@link OcAgentTraceExporterConfiguration}.
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
     * Sets the service name to be used for this {@link OcAgentTraceExporter}.
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
     * Sets whether {@link OcAgentTraceExporter} should handle the config streams.
     *
     * @param enableConfig whether {@code OcAgentTraceExporter} should handle the config streams.
     * @return this.
     * @since 0.20
     */
    public abstract Builder setEnableConfig(boolean enableConfig);

    /**
     * Sets the deadline for exporting to Agent/Collector.
     *
     * @param deadline the export deadline.
     * @return this
     * @since 0.22
     */
    public abstract Builder setDeadline(Duration deadline);

    // TODO(songya): add an option that controls whether to always keep the RPC connection alive.

    abstract Duration getRetryInterval();

    abstract OcAgentTraceExporterConfiguration autoBuild();

    abstract Duration getDeadline();

    /**
     * Builds a {@link OcAgentTraceExporterConfiguration}.
     *
     * @return a {@code OcAgentTraceExporterConfiguration}.
     * @since 0.20
     */
    public OcAgentTraceExporterConfiguration build() {
      Preconditions.checkArgument(getDeadline().compareTo(ZERO) > 0, "Deadline must be positive.");
      Preconditions.checkArgument(
          getRetryInterval().compareTo(ZERO) > 0, "Retry interval must be positive.");
      return autoBuild();
    }
  }
}
