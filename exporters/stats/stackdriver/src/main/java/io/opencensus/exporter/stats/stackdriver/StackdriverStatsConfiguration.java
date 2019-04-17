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

import static io.opencensus.exporter.stats.stackdriver.StackdriverExportUtils.DEFAULT_CONSTANT_LABELS;

import com.google.api.MonitoredResource;
import com.google.auth.Credentials;
import com.google.auto.value.AutoValue;
import com.google.cloud.ServiceOptions;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.opencensus.common.Duration;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Configurations for {@link StackdriverStatsExporter}.
 *
 * @since 0.11
 */
@AutoValue
@Immutable
public abstract class StackdriverStatsConfiguration {

  static final Duration DEFAULT_INTERVAL = Duration.create(60, 0);
  static final MonitoredResource DEFAULT_RESOURCE = StackdriverExportUtils.getDefaultResource();
  static final String DEFAULT_PROJECT_ID =
      Strings.nullToEmpty(ServiceOptions.getDefaultProjectId());

  StackdriverStatsConfiguration() {}

  /**
   * Returns the {@link Credentials}.
   *
   * @return the {@code Credentials}.
   * @since 0.11
   */
  @Nullable
  public abstract Credentials getCredentials();

  /**
   * Returns the project id.
   *
   * @return the project id.
   * @since 0.11
   */
  public abstract String getProjectId();

  /**
   * Returns the export interval between pushes to StackDriver.
   *
   * @return the export interval.
   * @since 0.11
   */
  public abstract Duration getExportInterval();

  /**
   * Returns the Stackdriver {@link MonitoredResource}.
   *
   * @return the {@code MonitoredResource}.
   * @since 0.11
   */
  public abstract MonitoredResource getMonitoredResource();

  /**
   * Returns the name prefix for Stackdriver metrics.
   *
   * @return the metric name prefix.
   * @since 0.16
   */
  @Nullable
  public abstract String getMetricNamePrefix();

  /**
   * Returns the constant labels that will be applied to every Stackdriver metric.
   *
   * @return the constant labels.
   * @since 0.21
   */
  public abstract Map<LabelKey, LabelValue> getConstantLabels();

  /**
   * Returns a new {@link Builder}.
   *
   * @return a {@code Builder}.
   * @since 0.11
   */
  public static Builder builder() {
    return new AutoValue_StackdriverStatsConfiguration.Builder()
        .setProjectId(DEFAULT_PROJECT_ID)
        .setConstantLabels(DEFAULT_CONSTANT_LABELS)
        .setExportInterval(DEFAULT_INTERVAL)
        .setMonitoredResource(DEFAULT_RESOURCE);
  }

  /**
   * Builder for {@link StackdriverStatsConfiguration}.
   *
   * @since 0.11
   */
  @AutoValue.Builder
  public abstract static class Builder {

    Builder() {}

    /**
     * Sets the given {@link Credentials}.
     *
     * @param credentials the {@code Credentials}.
     * @return this.
     * @since 0.11
     */
    public abstract Builder setCredentials(Credentials credentials);

    /**
     * Sets the given project id.
     *
     * @param projectId the cloud project id.
     * @return this.
     * @since 0.11
     */
    public abstract Builder setProjectId(String projectId);

    /**
     * Sets the export interval.
     *
     * @param exportInterval the export interval between pushes to StackDriver.
     * @return this.
     * @since 0.11
     */
    public abstract Builder setExportInterval(Duration exportInterval);

    /**
     * Sets the {@link MonitoredResource}.
     *
     * @param monitoredResource the Stackdriver {@code MonitoredResource}.
     * @return this.
     * @since 0.11
     */
    public abstract Builder setMonitoredResource(MonitoredResource monitoredResource);

    /**
     * Sets the the name prefix for Stackdriver metrics.
     *
     * <p>It is suggested to use prefix with custom or external domain name, for example
     * "custom.googleapis.com/myorg/" or "external.googleapis.com/prometheus/". If the given prefix
     * doesn't start with a valid domain, we will add "custom.googleapis.com/" before the prefix.
     *
     * @param prefix the metric name prefix.
     * @return this.
     * @since 0.16
     */
    public abstract Builder setMetricNamePrefix(String prefix);

    /**
     * Sets the constant labels that will be applied to every Stackdriver metric. This default
     * ensures that the set of labels together with the default resource (global) are unique to this
     * process, as required by stackdriver.
     *
     * <p>If not set, the exporter will use the "opencensus_task" label.
     *
     * <p>If you set constant labels, make sure that the monitored resource together with these
     * labels is unique to the current process. This is to ensure that there is only a single writer
     * to each time series in Stackdriver.
     *
     * <p>Set constant labels to empty to avoid getting the default "opencensus_task" label. You
     * should only do this if you know that the monitored resource uniquely identifies this process.
     *
     * @param constantLabels constant labels that will be applied to every Stackdriver metric.
     * @return this
     * @since 0.21
     */
    public abstract Builder setConstantLabels(Map<LabelKey, LabelValue> constantLabels);

    abstract String getProjectId();

    abstract Map<LabelKey, LabelValue> getConstantLabels();

    abstract StackdriverStatsConfiguration autoBuild();

    /**
     * Builds a new {@link StackdriverStatsConfiguration} with current settings.
     *
     * @return a {@code StackdriverStatsConfiguration}.
     * @since 0.11
     */
    public StackdriverStatsConfiguration build() {
      // Make a defensive copy of constant labels.
      setConstantLabels(
          Collections.unmodifiableMap(
              new LinkedHashMap<LabelKey, LabelValue>(getConstantLabels())));
      Preconditions.checkArgument(
          !Strings.isNullOrEmpty(getProjectId()),
          "Cannot find a project ID from either configurations or application default.");
      for (Map.Entry<LabelKey, LabelValue> constantLabel : getConstantLabels().entrySet()) {
        Preconditions.checkNotNull(constantLabel.getKey(), "constant label key");
        Preconditions.checkNotNull(constantLabel.getValue(), "constant label value");
      }
      return autoBuild();
    }
  }
}
