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
import com.google.common.base.Strings;
import io.opencensus.common.Duration;
import io.opencensus.stats.Stats;
import io.opencensus.stats.ViewManager;
import java.net.URI;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

/**
 * Exporter to SignalFx.
 *
 * <p>Example of usage:
 *
 * <pre><code>
 * public static void main(String[] args) {
 *   SignalFxStatsExporter.create();
 *   ... // Do work.
 * }
 * </code></pre>
 */
public final class SignalFxStatsExporter {

  private static final Duration ZERO = Duration.create(0, 0);
  private static final Object monitor = new Object();

  private final SignalFxStatsConfiguration configuration;
  private final SignalFxStatsExporterWorkerThread workerThread;

  @GuardedBy("monitor")
  @Nullable
  private static SignalFxStatsExporter exporter = null;

  private SignalFxStatsExporter(SignalFxStatsConfiguration configuration, ViewManager viewManager) {
    Preconditions.checkNotNull(configuration, "SignalFx stats exporter configuration");
    this.configuration = configuration;

    String token = configuration.getToken();
    Preconditions.checkArgument(!Strings.isNullOrEmpty(token), "Invalid SignalFx token");

    Duration interval = configuration.getExportInterval();
    Preconditions.checkArgument(interval.compareTo(ZERO) > 0, "Interval duration must be positive");

    URI endpoint = configuration.getIngestEndpoint();
    this.workerThread =
        new SignalFxStatsExporterWorkerThread(
            SignalFxMetricsSenderFactory.DEFAULT, endpoint, token, interval, viewManager);
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
   */
  public static void create(SignalFxStatsConfiguration configuration) {
    synchronized (monitor) {
      Preconditions.checkState(exporter == null, "SignalFx stats exporter is already created.");
      exporter = new SignalFxStatsExporter(configuration, Stats.getViewManager());
      exporter.workerThread.start();
    }
  }

  @VisibleForTesting
  static void unsafeResetExporter() {
    synchronized (monitor) {
      if (exporter != null) {
        SignalFxStatsExporterWorkerThread workerThread = exporter.workerThread;
        if (workerThread != null && workerThread.isAlive()) {
          try {
            workerThread.interrupt();
            workerThread.join();
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
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
