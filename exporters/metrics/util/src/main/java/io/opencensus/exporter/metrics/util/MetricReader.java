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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.auto.value.AutoValue;
import com.google.common.annotations.VisibleForTesting;
import io.opencensus.common.Scope;
import io.opencensus.metrics.Metrics;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricProducer;
import io.opencensus.metrics.export.MetricProducerManager;
import io.opencensus.trace.Sampler;
import io.opencensus.trace.Span;
import io.opencensus.trace.Status;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.samplers.Samplers;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.Immutable;

/**
 * Helper class to read all available {@link Metric}s from a {@link MetricProducerManager} and
 * exports them to a {@link MetricExporter}.
 *
 * @since 0.19
 */
public class MetricReader {
  private static final Tracer tracer = Tracing.getTracer();
  private static final Logger logger = Logger.getLogger(MetricReader.class.getName());
  private static final Sampler probabilitySampler = Samplers.probabilitySampler(0.0001);
  @VisibleForTesting static final String DEFAULT_SPAN_NAME = "ExportMetrics";

  private final MetricProducerManager metricProducerManager;
  private final String spanName;

  private MetricReader(MetricProducerManager metricProducerManager, String spanName) {
    this.metricProducerManager = metricProducerManager;
    this.spanName = spanName;
  }

  /**
   * Options for {@link MetricReader}.
   *
   * @since 0.19
   */
  @AutoValue
  @Immutable
  public abstract static class Options {

    Options() {}

    /**
     * Returns the {@link MetricProducerManager}.
     *
     * @return the {@link MetricProducerManager}.
     * @since 0.19
     */
    public abstract MetricProducerManager getMetricProducerManager();

    /**
     * Returns the span name for the {@link Span} created when data are read and exported.
     *
     * @return the span name for the {@link Span} created when data are read and exported.
     * @since 0.19
     */
    public abstract String getSpanName();

    /**
     * Returns a new {@link Options.Builder}.
     *
     * @return a {@code Builder}.
     * @since 0.19
     */
    public static Builder builder() {
      return new AutoValue_MetricReader_Options.Builder()
          .setMetricProducerManager(Metrics.getExportComponent().getMetricProducerManager())
          .setSpanName(DEFAULT_SPAN_NAME);
    }

    /**
     * Builder for {@link MetricReader.Options}.
     *
     * @since 0.19
     */
    @AutoValue.Builder
    public abstract static class Builder {
      /**
       * Sets the {@link MetricProducerManager}.
       *
       * @param metricProducerManager the {@link MetricProducerManager}.
       * @return this.
       * @since 0.19
       */
      public abstract Builder setMetricProducerManager(MetricProducerManager metricProducerManager);

      /**
       * Sets the span name for the {@link Span} created when data are read and exported.
       *
       * @param spanName the span name for the {@link Span} created when data are read and exported.
       * @return this.
       * @since 0.19
       */
      public abstract Builder setSpanName(String spanName);

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
   * Creates a new {@link MetricReader}.
   *
   * @param options the options for {@link MetricReader}.
   * @return a new {@link MetricReader}.
   * @since 0.19
   */
  public static MetricReader create(Options options) {
    checkNotNull(options, "options");
    return new MetricReader(
        checkNotNull(options.getMetricProducerManager(), "metricProducerManager"),
        checkNotNull(options.getSpanName(), "spanName"));
  }

  /**
   * Reads the metrics from the {@link MetricProducerManager} and exports them to the {@code
   * metricExporter}.
   *
   * @param metricExporter the exporter called to export the metrics read.
   * @since 0.19
   */
  public void readAndExport(MetricExporter metricExporter) {
    Span span =
        tracer
            .spanBuilder(spanName)
            .setRecordEvents(true)
            .setSampler(probabilitySampler)
            .startSpan();
    Scope scope = tracer.withSpan(span);
    try {
      ArrayList<Metric> metricsList = new ArrayList<>();
      for (MetricProducer metricProducer : metricProducerManager.getAllMetricProducer()) {
        metricsList.addAll(metricProducer.getMetrics());
      }
      metricExporter.export(metricsList);
    } catch (Throwable e) {
      logger.log(Level.WARNING, "Exception thrown by the metrics exporter.", e);
      span.setStatus(
          Status.UNKNOWN.withDescription("Exception when export metrics: " + exceptionMessage(e)));
    } finally {
      scope.close();
      span.end();
    }
  }

  private static String exceptionMessage(Throwable e) {
    return e.getMessage() != null ? e.getMessage() : e.getClass().getName();
  }
}
