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

import com.google.api.MonitoredResource;
import com.google.auth.Credentials;
import com.google.auto.value.AutoValue;
import io.opencensus.common.Duration;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/** Configurations for {@link StackdriverStatsExporter}. */
@AutoValue
@Immutable
public abstract class StackdriverConfiguration {

  StackdriverConfiguration() {}

  /**
   * Returns the {@link Credentials}.
   *
   * @return the {@code Credentials}.
   */
  @Nullable
  public abstract Credentials getCredentials();

  /**
   * Returns the project id.
   *
   * @return the project id.
   */
  @Nullable
  public abstract String getProjectId();

  /**
   * Returns the export interval between pushes to StackDriver.
   *
   * @return the export interval.
   */
  @Nullable
  public abstract Duration getExportInterval();

  /**
   * Returns the Stackdriver {@link MonitoredResource}.
   *
   * @return the {@code MonitoredResource}.
   */
  @Nullable
  public abstract MonitoredResource getMonitoredResource();

  /**
   * Returns a new {@link Builder}.
   *
   * @return a {@code Builder}.
   */
  public static Builder builder() {
    return new AutoValue_StackdriverConfiguration.Builder();
  }

  /** Builder for {@link StackdriverConfiguration}. */
  @AutoValue.Builder
  public abstract static class Builder {

    Builder() {}

    /**
     * Sets the given {@link Credentials}.
     *
     * @param credentials the {@code Credentials}.
     * @return this.
     */
    public abstract Builder setCredentials(Credentials credentials);

    /**
     * Sets the given project id.
     *
     * @param projectId the cloud project id.
     * @return this.
     */
    public abstract Builder setProjectId(String projectId);

    /**
     * Sets the export interval.
     *
     * @param exportInterval the export interval between pushes to StackDriver.
     * @return this.
     */
    public abstract Builder setExportInterval(Duration exportInterval);

    /**
     * Sets the {@link MonitoredResource}.
     *
     * @param monitoredResource the Stackdriver {@code MonitoredResource}.
     * @return this.
     */
    public abstract Builder setMonitoredResource(MonitoredResource monitoredResource);

    /**
     * Builds a new {@link StackdriverConfiguration} with current settings.
     *
     * @return a {@code StackdriverConfiguration}.
     */
    public abstract StackdriverConfiguration build();
  }
}
