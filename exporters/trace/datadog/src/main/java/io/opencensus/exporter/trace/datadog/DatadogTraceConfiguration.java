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
import javax.annotation.concurrent.Immutable;

/**
 * Configuration for {@link DatadogTraceExporter}.
 *
 * @since 0.19
 */
@AutoValue
@Immutable
public abstract class DatadogTraceConfiguration {

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
   * Return a new {@link Builder}.
   *
   * @return a {@code Builder}
   * @since 0.19
   */
  public static Builder builder() {
    return new AutoValue_DatadogTraceConfiguration.Builder();
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

    public abstract DatadogTraceConfiguration build();
  }
}
