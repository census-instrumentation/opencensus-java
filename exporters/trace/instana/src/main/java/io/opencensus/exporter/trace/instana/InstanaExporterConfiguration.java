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

package io.opencensus.exporter.trace.instana;

import com.google.auto.value.AutoValue;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.opencensus.common.Duration;
import javax.annotation.concurrent.Immutable;

/**
 * Configuration for {@link InstanaTraceExporter}.
 *
 * @since 0.22
 */
@AutoValue
@Immutable
public abstract class InstanaExporterConfiguration {

  @VisibleForTesting static final Duration DEFAULT_DEADLINE = Duration.create(10, 0);
  @VisibleForTesting static final Duration ZERO = Duration.fromMillis(0);

  InstanaExporterConfiguration() {}

  /**
   * Returns the endpoint of the Instana agent.
   *
   * @return the endpoint of the Instana agent.
   * @since 0.22
   */
  public abstract String getAgentEndpoint();

  /**
   * Returns the deadline for exporting to Instana.
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
   * @since 0.22
   */
  public static Builder builder() {
    return new AutoValue_InstanaExporterConfiguration.Builder().setDeadline(DEFAULT_DEADLINE);
  }

  /**
   * Builder for {@link InstanaExporterConfiguration}.
   *
   * @since 0.22
   */
  @AutoValue.Builder
  public abstract static class Builder {

    Builder() {}

    /**
     * Sets the endpoint of Instana agent to send traces to. E.g
     * http://localhost:42699/com.instana.plugin.generic.trace
     *
     * @param agentEndpoint the endpoint of the agent.
     * @return this.
     * @since 0.22
     */
    public abstract Builder setAgentEndpoint(String agentEndpoint);

    /**
     * Sets the deadline for exporting to Instana.
     *
     * @param deadline the export deadline.
     * @return this
     * @since 0.22
     */
    public abstract Builder setDeadline(Duration deadline);

    abstract Duration getDeadline();

    abstract InstanaExporterConfiguration autoBuild();

    /**
     * Builds a {@link InstanaExporterConfiguration}.
     *
     * @return a {@code InstanaExporterConfiguration}.
     * @since 0.22
     */
    public InstanaExporterConfiguration build() {
      Preconditions.checkArgument(getDeadline().compareTo(ZERO) > 0, "Deadline must be positive.");
      return autoBuild();
    }
  }
}
