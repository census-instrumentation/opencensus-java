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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
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

  public static final String SIGNALFX_TOKEN_PROPERTY = "signalfx.auth.token";
  public static final String SIGNALFX_HOST_PROPERTY = "signalfx.ingest.url";
  public static final String DEFAULT_SIGNALFX_ENDPOINT = "https://ingest.signalfx.com";

  private static final Duration ZERO = Duration.create(0, 0);
  private static final Duration DEFAULT_INTERVAL = Duration.create(1, 0);
  private static final Object monitor = new Object();

  private final SignalFxStatsExporterWorkerThread workerThread;

  @GuardedBy("monitor")
  private static SignalFxStatsExporter exporter = null;

  private SignalFxStatsExporter(URL url, String token, Duration interval, ViewManager viewManager) {
    Preconditions.checkArgument(!Strings.isNullOrEmpty(token), "Invalid SignalFx token");
    Preconditions.checkNotNull(interval, "Interval must not be null");
    Preconditions.checkArgument(interval.compareTo(ZERO) > 0, "Interval duration must be positive");
    this.workerThread =
        new SignalFxStatsExporterWorkerThread(
            SignalFxMetricsSenderFactory.DEFAULT, url, token, interval, viewManager);
  }

  /**
   * Creates a SignalFx Stats exporter that gets its authentication token and the SignalFx ingest
   * URL from system properties and uses the default reporting interval.
   *
   * <p>Only one SignalFx Stats exporter can be created.
   *
   * @throws IllegalStateException if a SignalFx exporter already exists.
   */
  public static void create() {
    create(System.getProperties());
  }

  /**
   * Creates a SignalFx Stats exporter that gets its authentication token and the SignalFx ingest
   * URL from the given Java properties and uses the default reporting interval.
   *
   * <p>Only one SignalFx Stats exporter can be created.
   *
   * @param properties a {@code Properties} object containing the {@link #SIGNALFX_TOKEN_PROPERTY}
   *     and {@link #SIGNALFX_HOST_PROPERTY}.
   * @throws IllegalStateException if a SignalFx exporter already exists.
   */
  public static void create(Properties properties) {
    Preconditions.checkNotNull(properties, "Given properties shouldn't be null");
    String host = properties.getProperty(SIGNALFX_HOST_PROPERTY, DEFAULT_SIGNALFX_ENDPOINT);
    String token = properties.getProperty(SIGNALFX_TOKEN_PROPERTY);
    create(host, token, DEFAULT_INTERVAL);
  }

  /**
   * Creates a SignalFx Stats exporter with an explicit authentication token and reporting interval.
   *
   * <p>Only one SignalFx Stats exporter can be created.
   *
   * @param host the SignalFx ingest URL.
   * @param token a SignalFx ingest token.
   * @param interval the interval at which stats are reported to SignalFx.
   * @throws IllegalStateException if a SignalFx exporter already exists.
   */
  public static void create(String host, String token, Duration interval) {
    URL url;
    try {
      url = new URL(host);
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Invalid SignalFx endpoint URL", e);
    }

    synchronized (monitor) {
      Preconditions.checkState(exporter == null, "SignalFx stats exporter is already created.");
      exporter = new SignalFxStatsExporter(url, token, interval, Stats.getViewManager());
      exporter.workerThread.start();
    }
  }

  @VisibleForTesting
  static void unsafeResetExporter() {
    synchronized (monitor) {
      if (exporter != null) {
        try {
          exporter.workerThread.interrupt();
          exporter.workerThread.join();
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } finally {
          exporter = null;
        }
      }
    }
  }
}
