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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import io.opencensus.exporter.metrics.util.IntervalMetricReader;
import io.opencensus.exporter.metrics.util.MetricReader;
import io.opencensus.metrics.Metrics;
import io.opencensus.metrics.export.MetricProducerManager;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

/**
 * Exporter to SignalFx.
 *
 * <p>Example of usage:
 *
 * <pre><code>
 * public static void main(String[] args) {
 *   SignalFxStatsExporter.create(SignalFxStatsConfiguration.builder().build());
 *   ... // Do work.
 * }
 * </code></pre>
 *
 * @since 0.11
 */
public final class SignalFxStatsExporter {

  private static final Object monitor = new Object();

  private final SignalFxStatsConfiguration configuration;
  private final IntervalMetricReader intervalMetricReader;

  private static final String EXPORTER_SPAN_NAME = "ExportMetricsToSignalFX";

  @GuardedBy("monitor")
  @Nullable
  private static SignalFxStatsExporter exporter = null;

  private SignalFxStatsExporter(
      SignalFxStatsConfiguration configuration, MetricProducerManager metricProducerManager) {
    this.configuration = Preconditions.checkNotNull(configuration, "configuration");
    this.intervalMetricReader =
        IntervalMetricReader.create(
            new SignalFxMetricExporter(
                SignalFxMetricsSenderFactory.DEFAULT,
                configuration.getIngestEndpoint(),
                configuration.getToken()),
            MetricReader.create(
                MetricReader.Options.builder()
                    .setMetricProducerManager(metricProducerManager)
                    .setSpanName(EXPORTER_SPAN_NAME)
                    .build()),
            IntervalMetricReader.Options.builder()
                .setExportInterval(configuration.getExportInterval())
                .build());
  }

  /**
   * Creates a SignalFx Stats exporter from the given {@link SignalFxStatsConfiguration}.
   *
   * <p>If {@code ingestEndpoint} is not set on the configuration, the exporter will use {@link
   * SignalFxStatsConfiguration#DEFAULT_SIGNALFX_ENDPOINT}.
   *
   * <p>If {@code exportInterval} is not set on the configuration, the exporter will use {@link
   * SignalFxStatsConfiguration#DEFAULT_EXPORT_INTERVAL}.
   *
   * @param configuration the {@code SignalFxStatsConfiguration}.
   * @throws IllegalStateException if a SignalFx exporter is already created.
   * @since 0.11
   */
  public static void create(SignalFxStatsConfiguration configuration) {
    synchronized (monitor) {
      Preconditions.checkState(exporter == null, "SignalFx stats exporter is already created.");
      exporter =
          new SignalFxStatsExporter(
              configuration, Metrics.getExportComponent().getMetricProducerManager());
    }
  }

  @VisibleForTesting
  static void unsafeResetExporter() {
    synchronized (monitor) {
      if (exporter != null) {
        if (exporter.intervalMetricReader != null) {
          exporter.intervalMetricReader.stop();
        }
        exporter = null;
      }
    }
  }

  @VisibleForTesting
  @Nullable
  static SignalFxStatsConfiguration unsafeGetConfig() {
    synchronized (monitor) {
      return exporter != null ? exporter.configuration : null;
    }
  }
}
