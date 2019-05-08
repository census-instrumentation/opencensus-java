/*
 * Copyright 2019, OpenCensus Authors
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

package io.opencensus.exporter.trace.jaeger;

import com.google.auto.value.AutoValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.jaegertracing.thrift.internal.senders.ThriftSender;
import io.opencensus.common.Duration;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Configurations for {@link JaegerTraceExporter}.
 *
 * @since 0.22
 */
@AutoValue
@Immutable
public abstract class JaegerExporterConfiguration {

  @VisibleForTesting static final Duration DEFAULT_DEADLINE = Duration.create(10, 0);

  JaegerExporterConfiguration() {}

  /**
   * Returns the service name.
   *
   * @return the service name.
   * @since 0.22
   */
  public abstract String getServiceName();

  /**
   * Returns the Thrift endpoint of your Jaeger instance.
   *
   * @return the Thrift endpoint.
   * @since 0.22
   */
  public abstract String getThriftEndpoint();

  /**
   * Returns the Thrift sender.
   *
   * @return the Thrift sender.
   * @since 0.22
   */
  @Nullable
  public abstract ThriftSender getThriftSender();

  /**
   * Returns the deadline for exporting to Jaeger.
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
   * @since 0.22
   */
  public static Builder builder() {
    return new AutoValue_JaegerExporterConfiguration.Builder()
        .setThriftEndpoint("")
        .setDeadline(DEFAULT_DEADLINE);
  }

  /**
   * Builder for {@link JaegerExporterConfiguration}.
   *
   * @since 0.22
   */
  @AutoValue.Builder
  public abstract static class Builder {

    @VisibleForTesting static final Duration ZERO = Duration.fromMillis(0);

    Builder() {}

    /**
     * Sets the service name.
     *
     * @param serviceName the service name.
     * @return this.
     * @since 0.22
     */
    public abstract Builder setServiceName(String serviceName);

    /**
     * Sets the Thrift endpoint of your Jaeger instance. e.g.: "http://127.0.0.1:14268/api/traces".
     *
     * <p>At least one of {@code ThriftEndpoint} and {@code ThriftSender} needs to be specified. If
     * both {@code ThriftEndpoint} and {@code ThriftSender} are set, {@code ThriftSender} takes
     * precedence.
     *
     * @param thriftEndpoint the Thrift endpoint.
     * @return this.
     * @since 0.22
     */
    public abstract Builder setThriftEndpoint(String thriftEndpoint);

    /**
     * Sets the Thrift sender.
     *
     * <p>At least one of {@code ThriftEndpoint} and {@code ThriftSender} needs to be specified. If
     * both {@code ThriftEndpoint} and {@code ThriftSender} are set, {@code ThriftSender} takes
     * precedence.
     *
     * @param sender the Thrift sender.
     * @return this.
     * @since 0.22
     */
    public abstract Builder setThriftSender(ThriftSender sender);

    /**
     * Sets the deadline for exporting to Jaeger.
     *
     * @param deadline the export deadline.
     * @return this
     * @since 0.22
     */
    public abstract Builder setDeadline(Duration deadline);

    abstract Duration getDeadline();

    abstract String getThriftEndpoint();

    @Nullable
    abstract ThriftSender getThriftSender();

    abstract JaegerExporterConfiguration autoBuild();

    /**
     * Builds a {@link JaegerExporterConfiguration}.
     *
     * @return a {@code JaegerExporterConfiguration}.
     * @since 0.22
     */
    public JaegerExporterConfiguration build() {
      Preconditions.checkArgument(getDeadline().compareTo(ZERO) > 0, "Deadline must be positive.");
      Preconditions.checkArgument(
          !getThriftEndpoint().isEmpty() || getThriftSender() != null,
          "Neither Thrift endpoint nor Thrift sender is specified.");
      return autoBuild();
    }
  }
}
