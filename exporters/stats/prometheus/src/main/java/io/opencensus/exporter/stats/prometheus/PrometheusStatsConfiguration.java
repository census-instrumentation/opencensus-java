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

package io.opencensus.exporter.stats.prometheus;

import com.google.auto.value.AutoValue;
import io.prometheus.client.CollectorRegistry;
import javax.annotation.concurrent.Immutable;

/**
 * Configurations for {@link PrometheusStatsCollector}.
 *
 * @since 0.13
 */
@AutoValue
@Immutable
public abstract class PrometheusStatsConfiguration {

  PrometheusStatsConfiguration() {}

  /**
   * Returns the Prometheus {@link CollectorRegistry}.
   *
   * @return the Prometheus {@code CollectorRegistry}.
   * @since 0.13
   */
  public abstract CollectorRegistry getRegistry();

  /**
   * Returns the namespace used for Prometheus metrics.
   *
   * @return the namespace.
   * @since 0.21
   */
  public abstract String getNamespace();

  /**
   * Returns a new {@link Builder}.
   *
   * @return a {@code Builder}.
   * @since 0.13
   */
  public static Builder builder() {
    return new AutoValue_PrometheusStatsConfiguration.Builder()
        .setRegistry(CollectorRegistry.defaultRegistry)
        .setNamespace("");
  }

  /**
   * Builder for {@link PrometheusStatsConfiguration}.
   *
   * @since 0.13
   */
  @AutoValue.Builder
  public abstract static class Builder {

    Builder() {}

    /**
     * Sets the given Prometheus {@link CollectorRegistry}.
     *
     * @param registry the Prometheus {@code CollectorRegistry}.
     * @return this.
     * @since 0.13
     */
    public abstract Builder setRegistry(CollectorRegistry registry);

    /**
     * Sets the namespace used for Prometheus metrics.
     *
     * @param namespace the namespace.
     * @return this.
     * @since 0.21
     */
    public abstract Builder setNamespace(String namespace);

    /**
     * Builds a new {@link PrometheusStatsConfiguration} with current settings.
     *
     * @return a {@code PrometheusStatsConfiguration}.
     * @since 0.13
     */
    public abstract PrometheusStatsConfiguration build();
  }
}
