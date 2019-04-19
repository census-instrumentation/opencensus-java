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

import static io.opencensus.exporter.stats.prometheus.PrometheusExportUtils.containsDisallowedLeLabelForHistogram;
import static io.opencensus.exporter.stats.prometheus.PrometheusExportUtils.containsDisallowedQuantileLabelForSummary;
import static io.opencensus.exporter.stats.prometheus.PrometheusExportUtils.convertToLabelNames;
import static io.opencensus.exporter.stats.prometheus.PrometheusExportUtils.getType;

import com.google.common.annotations.VisibleForTesting;
import io.opencensus.exporter.metrics.util.MetricExporter;
import io.opencensus.exporter.metrics.util.MetricReader;
import io.opencensus.metrics.Metrics;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricDescriptor;
import io.opencensus.metrics.export.MetricProducerManager;
import io.opencensus.trace.Status;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * OpenCensus Metrics {@link Collector} for Prometheus.
 *
 * @since 0.12
 */
public final class PrometheusStatsCollector extends Collector implements Collector.Describable {

  private static final Logger logger = Logger.getLogger(PrometheusStatsCollector.class.getName());
  private static final Tracer tracer = Tracing.getTracer();
  private static final String DESCRIBE_METRICS_FOR_PROMETHEUS = "DescribeMetricsToPrometheus";
  private static final String EXPORT_METRICS_TO_PROMETHEUS = "ExportMetricsToPrometheus";
  private final MetricReader collectMetricReader;
  private final MetricReader describeMetricReader;
  private final String namespace;

  /**
   * Creates a {@link PrometheusStatsCollector} and registers it to Prometheus {@link
   * CollectorRegistry#defaultRegistry}.
   *
   * <p>This is equivalent with:
   *
   * <pre>{@code
   * PrometheusStatsCollector.createAndRegister(PrometheusStatsConfiguration.builder().build());
   * }</pre>
   *
   * @throws IllegalArgumentException if a {@code PrometheusStatsCollector} has already been created
   *     and registered.
   * @since 0.12
   */
  public static void createAndRegister() {
    new PrometheusStatsCollector(Metrics.getExportComponent().getMetricProducerManager(), "")
        .register();
  }

  /**
   * Creates a {@link PrometheusStatsCollector} and registers it to the given Prometheus {@link
   * CollectorRegistry} in the {@link PrometheusStatsConfiguration}.
   *
   * <p>If {@code CollectorRegistry} of the configuration is not set, the collector will use {@link
   * CollectorRegistry#defaultRegistry}.
   *
   * @throws IllegalArgumentException if a {@code PrometheusStatsCollector} has already been created
   *     and registered.
   * @since 0.13
   */
  public static void createAndRegister(PrometheusStatsConfiguration configuration) {
    CollectorRegistry registry = configuration.getRegistry();
    if (registry == null) {
      registry = CollectorRegistry.defaultRegistry;
    }
    new PrometheusStatsCollector(
            Metrics.getExportComponent().getMetricProducerManager(), configuration.getNamespace())
        .register(registry);
  }

  private static final class ExportMetricExporter extends MetricExporter {
    private final ArrayList<MetricFamilySamples> samples = new ArrayList<>();
    private final String namespace;

    private ExportMetricExporter(String namespace) {
      this.namespace = namespace;
    }

    @Override
    public void export(Collection<Metric> metrics) {
      samples.ensureCapacity(metrics.size());
      for (Metric metric : metrics) {
        MetricDescriptor metricDescriptor = metric.getMetricDescriptor();
        if (containsDisallowedLeLabelForHistogram(
                convertToLabelNames(metricDescriptor.getLabelKeys()),
                getType(metricDescriptor.getType()))
            || containsDisallowedQuantileLabelForSummary(
                convertToLabelNames(metricDescriptor.getLabelKeys()),
                getType(metricDescriptor.getType()))) {
          // silently skip Distribution metricdescriptor with "le" label key and Summary
          // metricdescriptor with "quantile" label key
          continue;
        }
        try {
          samples.add(PrometheusExportUtils.createMetricFamilySamples(metric, namespace));
        } catch (Throwable e) {
          logger.log(Level.WARNING, "Exception thrown when collecting metric samples.", e);
          tracer
              .getCurrentSpan()
              .setStatus(
                  Status.UNKNOWN.withDescription(
                      "Exception thrown when collecting Prometheus Metric Samples: "
                          + exceptionMessage(e)));
        }
      }
    }
  }

  @Override
  public List<MetricFamilySamples> collect() {
    ExportMetricExporter exportMetricExporter = new ExportMetricExporter(namespace);
    collectMetricReader.readAndExport(exportMetricExporter);
    return exportMetricExporter.samples;
  }

  private static final class DescribeMetricExporter extends MetricExporter {
    private final ArrayList<MetricFamilySamples> samples = new ArrayList<>();
    private final String namespace;

    private DescribeMetricExporter(String namespace) {
      this.namespace = namespace;
    }

    @Override
    public void export(Collection<Metric> metrics) {
      samples.ensureCapacity(metrics.size());
      for (Metric metric : metrics) {
        try {
          samples.add(
              PrometheusExportUtils.createDescribableMetricFamilySamples(
                  metric.getMetricDescriptor(), namespace));
        } catch (Throwable e) {
          logger.log(Level.WARNING, "Exception thrown when describing metrics.", e);
          tracer
              .getCurrentSpan()
              .setStatus(
                  Status.UNKNOWN.withDescription(
                      "Exception thrown when describing Prometheus Metrics: "
                          + exceptionMessage(e)));
        }
      }
    }
  }

  @Override
  public List<MetricFamilySamples> describe() {
    DescribeMetricExporter describeMetricExporter = new DescribeMetricExporter(namespace);
    describeMetricReader.readAndExport(describeMetricExporter);
    return describeMetricExporter.samples;
  }

  @VisibleForTesting
  PrometheusStatsCollector(MetricProducerManager metricProducerManager, String namespace) {
    this.collectMetricReader =
        MetricReader.create(
            MetricReader.Options.builder()
                .setMetricProducerManager(metricProducerManager)
                .setSpanName(EXPORT_METRICS_TO_PROMETHEUS)
                .build());
    this.describeMetricReader =
        MetricReader.create(
            MetricReader.Options.builder()
                .setMetricProducerManager(metricProducerManager)
                .setSpanName(DESCRIBE_METRICS_FOR_PROMETHEUS)
                .build());
    this.namespace = namespace;
  }

  private static String exceptionMessage(Throwable e) {
    return e.getMessage() != null ? e.getMessage() : e.getClass().getName();
  }
}
