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

package io.opencensus.exporter.stats.signalfx;

import com.google.auto.value.AutoValue;
import io.opencensus.common.Duration;
import java.net.URI;
import java.net.URISyntaxException;
import javax.annotation.concurrent.Immutable;

/** Configurations for {@link SignalFxStatsExporter}. */
@AutoValue
@Immutable
// Suppress Checker Framework warning about missing @Nullable in generated equals method.
@AutoValue.CopyAnnotations
@SuppressWarnings("nullness")
public abstract class SignalFxStatsConfiguration {

  /** The default SignalFx ingest API URL. */
  public static final String DEFAULT_SIGNALFX_ENDPOINT = "https://ingest.signalfx.com";

  /** The default stats export interval. */
  public static final Duration DEFAULT_EXPORT_INTERVAL = Duration.create(1, 0);

  SignalFxStatsConfiguration() {}

  /**
   * Returns the SignalFx ingest API URL.
   *
   * @return the SignalFx ingest API URL.
   */
  public abstract URI getIngestEndpoint();

  /**
   * Returns the authentication token.
   *
   * @return the authentication token.
   */
  public abstract String getToken();

  /**
   * Returns the export interval between pushes to SignalFx.
   *
   * @return the export interval.
   */
  public abstract Duration getExportInterval();

  /**
   * Returns a new {@link Builder}.
   *
   * @return a {@code Builder}.
   */
  public static Builder builder() {
    try {
      return new AutoValue_SignalFxStatsConfiguration.Builder()
          .setIngestEndpoint(new URI(DEFAULT_SIGNALFX_ENDPOINT))
          .setExportInterval(DEFAULT_EXPORT_INTERVAL);
    } catch (URISyntaxException e) {
      // This shouldn't happen if DEFAULT_SIGNALFX_ENDPOINT was typed in correctly.
      throw new IllegalArgumentException(e);
    }
  }

  /** Builder for {@link SignalFxStatsConfiguration}. */
  @AutoValue.Builder
  public abstract static class Builder {

    Builder() {}

    /**
     * Sets the given SignalFx ingest API URL.
     *
     * @param url the SignalFx ingest API URL.
     * @return this.
     */
    public abstract Builder setIngestEndpoint(URI url);

    /**
     * Sets the given authentication token.
     *
     * @param token the authentication token.
     * @return this.
     */
    public abstract Builder setToken(String token);

    /**
     * Sets the export interval.
     *
     * @param exportInterval the export interval between pushes to SignalFx.
     * @return this.
     */
    public abstract Builder setExportInterval(Duration exportInterval);

    /**
     * Builds a new {@link SignalFxStatsConfiguration} with current settings.
     *
     * @return a {@code SignalFxStatsConfiguration}.
     */
    public abstract SignalFxStatsConfiguration build();
  }
}
