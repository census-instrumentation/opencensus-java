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

package io.opencensus.exporter.metrics.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.auto.value.AutoValue;
import com.google.common.annotations.VisibleForTesting;
import io.opencensus.common.Duration;
import javax.annotation.concurrent.Immutable;

/**
 * Wrapper of the {@link MetricReader} which automatically reads and exports the metrics every
 * export interval.
 *
 * @since 0.19
 */
public final class IntervalMetricReader {
  @VisibleForTesting static final Duration DEFAULT_INTERVAL = Duration.create(60, 0);
  private static final Duration ZERO = Duration.create(0, 0);

  private final Thread workerThread;
  private final Worker worker;

  private IntervalMetricReader(Worker worker) {
    this.worker = worker;
    this.workerThread = new DaemonThreadFactory().newThread(worker);
    workerThread.start();
  }

  /**
   * Options for {@link IntervalMetricReader}.
   *
   * @since 0.19
   */
  @AutoValue
  @Immutable
  public abstract static class Options {

    Options() {}

    /**
     * Returns the export interval between pushes to StackDriver.
     *
     * @return the export interval.
     * @since 0.19
     */
    public abstract Duration getExportInterval();

    /**
     * Returns a new {@link Builder}.
     *
     * @return a {@code Builder}.
     * @since 0.19
     */
    public static Builder builder() {
      return new AutoValue_IntervalMetricReader_Options.Builder()
          .setExportInterval(DEFAULT_INTERVAL);
    }

    /**
     * Builder for {@link Options}.
     *
     * @since 0.19
     */
    @AutoValue.Builder
    public abstract static class Builder {
      /**
       * Sets the export interval.
       *
       * @param exportInterval the export interval between pushes to StackDriver.
       * @return this.
       * @since 0.19
       */
      public abstract Builder setExportInterval(Duration exportInterval);

      /**
       * Builds a new {@link Options} with current settings.
       *
       * @return a {@code Options}.
       * @since 0.19
       */
      public abstract Options build();
    }
  }

  /**
   * Creates a new {@link IntervalMetricReader}.
   *
   * @param metricExporter the {@link MetricExporter} to be called after.
   * @param metricReader the {@link MetricReader} to be used to read metrics.
   * @param options the {@link Options} for the new {@link IntervalMetricReader}.
   * @return a new {@link IntervalMetricReader}.
   * @since 0.19
   */
  public static IntervalMetricReader create(
      MetricExporter metricExporter, MetricReader metricReader, Options options) {
    checkNotNull(options, "options");
    Duration exportInterval = checkNotNull(options.getExportInterval(), "exportInterval");
    checkArgument(exportInterval.compareTo(ZERO) > 0, "Export interval must be positive");

    return new IntervalMetricReader(
        new Worker(
            checkNotNull(metricExporter, "metricExporter"),
            exportInterval.toMillis(),
            checkNotNull(metricReader, "metricReader")));
  }

  /**
   * Reads and exports data immediately.
   *
   * @since 0.19
   */
  public void readAndExportNow() {
    worker.readAndExport();
  }

  /**
   * Stops the worker thread by calling {@link Thread#interrupt()}.
   *
   * @since 0.19
   */
  public void stop() {
    workerThread.interrupt();
  }

  private static final class Worker implements Runnable {

    private final MetricExporter metricExporter;
    private final long exportInterval;
    private final MetricReader metricReader;

    private Worker(MetricExporter metricExporter, long exportInterval, MetricReader metricReader) {
      this.metricExporter = metricExporter;
      this.exportInterval = exportInterval;
      this.metricReader = metricReader;
    }

    @Override
    public void run() {
      while (true) {
        try {
          Thread.sleep(exportInterval);
        } catch (InterruptedException ie) {
          // Preserve the interruption status as per guidance and stop doing any work.
          Thread.currentThread().interrupt();
          break;
        }
        readAndExport();
      }
      // Do one last readAndExport before stop.
      readAndExport();
    }

    private void readAndExport() {
      metricReader.readAndExport(metricExporter);
    }
  }
}
