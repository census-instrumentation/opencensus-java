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

package io.opencensus.exporter.trace.datadog;

import com.google.auto.value.AutoValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.opencensus.common.Duration;
import javax.annotation.concurrent.Immutable;

/**
 * Configuration for {@link DatadogTraceExporter}.
 *
 * @since 0.19
 */
@AutoValue
@Immutable
public abstract class DatadogTraceConfiguration {

  @VisibleForTesting static final Duration DEFAULT_DEADLINE = Duration.create(10, 0);
  @VisibleForTesting static final Duration ZERO = Duration.fromMillis(0);

  DatadogTraceConfiguration() {}

  /**
   * Returns the URL of the Datadog agent.
   *
   * @return the URL of the Datadog agent.
   * @since 0.19
   */
  public abstract String getAgentEndpoint();

  /**
   * Returns the name of the service being traced.
   *
   * @return the name of the service being traced.
   * @since 0.19
   */
  public abstract String getService();

  /**
   * Return the type of service being traced.
   *
   * @return the type of service being traced.
   * @since 0.19
   */
  public abstract String getType();

  /**
   * Returns the deadline for exporting to Datadog.
   *
   * <p>Default value is 10 seconds.
   *
   * @return the export deadline.
   * @since 0.22
   */
  public abstract Duration getDeadline();

  /**
   * Return a new {@link Builder}.
   *
   * @return a {@code Builder}
   * @since 0.19
   */
  public static Builder builder() {
    return new AutoValue_DatadogTraceConfiguration.Builder().setDeadline(DEFAULT_DEADLINE);
  }

  /**
   * Builder for {@link DatadogTraceConfiguration}.
   *
   * @since 0.19
   */
  @AutoValue.Builder
  public abstract static class Builder {

    Builder() {}

    /**
     * Sets the URL to send traces to.
     *
     * @param agentEndpoint the URL of the agent.
     * @return this.
     * @since 0.19
     */
    public abstract Builder setAgentEndpoint(String agentEndpoint);

    /**
     * Sets the name of service being traced.
     *
     * @param service the name of the service being traced.
     * @return this.
     * @since 0.19
     */
    public abstract Builder setService(String service);

    /**
     * Sets the type of the service being traced.
     *
     * @param type the type of service being traced.
     * @return this.
     * @since 0.19
     */
    public abstract Builder setType(String type);

    /**
     * Sets the deadline for exporting to Datadog.
     *
     * @param deadline the export deadline.
     * @return this
     * @since 0.22
     */
    public abstract Builder setDeadline(Duration deadline);

    abstract Duration getDeadline();

    abstract DatadogTraceConfiguration autoBuild();

    /**
     * Builds a {@link DatadogTraceConfiguration}.
     *
     * @return a {@code DatadogTraceConfiguration}.
     * @since 0.22
     */
    public DatadogTraceConfiguration build() {
      Preconditions.checkArgument(getDeadline().compareTo(ZERO) > 0, "Deadline must be positive.");
      return autoBuild();
    }
  }
}
