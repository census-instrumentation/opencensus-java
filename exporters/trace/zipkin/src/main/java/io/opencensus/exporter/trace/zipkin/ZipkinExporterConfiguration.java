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

package io.opencensus.exporter.trace.zipkin;

import com.google.auto.value.AutoValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.opencensus.common.Duration;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import zipkin2.codec.SpanBytesEncoder;
import zipkin2.reporter.Sender;

/**
 * Configurations for {@link ZipkinTraceExporter}.
 *
 * @since 0.22
 */
@AutoValue
@Immutable
public abstract class ZipkinExporterConfiguration {

  @VisibleForTesting static final Duration DEFAULT_DEADLINE = Duration.create(10, 0);

  ZipkinExporterConfiguration() {}

  /**
   * Returns the service name.
   *
   * @return the service name.
   * @since 0.22
   */
  public abstract String getServiceName();

  /**
   * Returns the Zipkin V2 URL.
   *
   * @return the Zipkin V2 URL.
   * @since 0.22
   */
  public abstract String getV2Url();

  /**
   * Returns the Zipkin sender.
   *
   * @return the Zipkin sender.
   * @since 0.22
   */
  @Nullable
  public abstract Sender getSender();

  /**
   * Returns the {@link SpanBytesEncoder}.
   *
   * <p>Default is {@link SpanBytesEncoder#JSON_V2}.
   *
   * @return the {@code SpanBytesEncoder}
   * @since 0.22
   */
  public abstract SpanBytesEncoder getEncoder();

  /**
   * Returns the deadline for exporting to Zipkin.
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
    return new AutoValue_ZipkinExporterConfiguration.Builder()
        .setV2Url("")
        .setEncoder(SpanBytesEncoder.JSON_V2)
        .setDeadline(DEFAULT_DEADLINE);
  }

  /**
   * Builder for {@link ZipkinExporterConfiguration}.
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
     * Sets the Zipkin V2 URL, e.g.: "http://127.0.0.1:9411/api/v2/spans".
     *
     * <p>At least one of {@code V2Url} and {@code Sender} needs to be specified. If both {@code
     * V2Url} and {@code Sender} are set, {@code Sender} takes precedence.
     *
     * @param v2Url the Zipkin V2 URL.
     * @return this.
     * @since 0.22
     */
    public abstract Builder setV2Url(String v2Url);

    /**
     * Sets the Zipkin sender.
     *
     * <p>At least one of {@code V2Url} and {@code Sender} needs to be specified. If both {@code
     * V2Url} and {@code Sender} are set, {@code Sender} takes precedence.
     *
     * @param sender the Zipkin sender.
     * @return this.
     * @since 0.22
     */
    public abstract Builder setSender(Sender sender);

    /**
     * Sets the {@link SpanBytesEncoder}.
     *
     * @param encoder the {@code SpanBytesEncoder}.
     * @return this
     * @since 0.22
     */
    public abstract Builder setEncoder(SpanBytesEncoder encoder);

    /**
     * Sets the deadline for exporting to Zipkin.
     *
     * @param deadline the export deadline.
     * @return this
     * @since 0.22
     */
    public abstract Builder setDeadline(Duration deadline);

    abstract Duration getDeadline();

    abstract String getV2Url();

    @Nullable
    abstract Sender getSender();

    abstract ZipkinExporterConfiguration autoBuild();

    /**
     * Builds a {@link ZipkinExporterConfiguration}.
     *
     * @return a {@code ZipkinExporterConfiguration}.
     * @since 0.22
     */
    public ZipkinExporterConfiguration build() {
      Preconditions.checkArgument(getDeadline().compareTo(ZERO) > 0, "Deadline must be positive.");
      Preconditions.checkArgument(
          !getV2Url().isEmpty() || getSender() != null,
          "Neither Zipkin V2 URL nor Zipkin sender is specified.");
      return autoBuild();
    }
  }
}
