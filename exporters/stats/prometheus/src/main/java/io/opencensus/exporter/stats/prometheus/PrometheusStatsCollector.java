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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.opencensus.common.Scope;
import io.opencensus.stats.Stats;
import io.opencensus.stats.View;
import io.opencensus.stats.ViewData;
import io.opencensus.stats.ViewManager;
import io.opencensus.trace.Span;
import io.opencensus.trace.Status;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * OpenCensus Stats {@link Collector} for Prometheus.
 *
 * @since 0.12
 */
public final class PrometheusStatsCollector extends Collector implements Collector.Describable {

  private static final Logger logger = Logger.getLogger(PrometheusStatsCollector.class.getName());
  private static final Tracer tracer = Tracing.getTracer();

  private final ViewManager viewManager;

  /**
   * Creates a {@link PrometheusStatsCollector} and registers it to Prometheus {@link
   * CollectorRegistry#defaultRegistry}.
   *
   * @throws IllegalArgumentException if a {@code PrometheusStatsCollector} has already been created
   *     and registered.
   * @since 0.12
   */
  public static void createAndRegister() {
    new PrometheusStatsCollector(Stats.getViewManager()).register();
  }

  @Override
  public List<MetricFamilySamples> collect() {
    List<MetricFamilySamples> samples = Lists.newArrayList();
    Span span = tracer.spanBuilder("ExportStatsToPrometheus").setRecordEvents(true).startSpan();
    span.addAnnotation("Collect Prometheus Metric Samples.");
    try (Scope scope = tracer.withSpan(span)) {
      for (View view : viewManager.getAllExportedViews()) {
        ViewData viewData = viewManager.getView(view.getName());
        if (viewData == null) {
          continue;
        } else {
          samples.add(PrometheusExportUtils.createMetricFamilySamples(viewData));
        }
      }
      span.addAnnotation("Finish collecting Prometheus Metric Samples.");
    } catch (Throwable e) {
      logger.log(Level.WARNING, "Exception thrown when collecting metric samples.", e);
      span.setStatus(
          Status.UNKNOWN.withDescription(
              "Exception thrown when collecting Prometheus Metric Samples: "
                  + exceptionMessage(e)));
    } finally {
      span.end();
    }
    return samples;
  }

  @Override
  public List<MetricFamilySamples> describe() {
    List<MetricFamilySamples> samples = Lists.newArrayList();
    Span span =
        tracer.spanBuilder("DescribeMetricsForPrometheus").setRecordEvents(true).startSpan();
    span.addAnnotation("Describe Prometheus Metrics.");
    try (Scope scope = tracer.withSpan(span)) {
      for (View view : viewManager.getAllExportedViews()) {
        samples.add(PrometheusExportUtils.createDescribableMetricFamilySamples(view));
      }
      span.addAnnotation("Finish describing Prometheus Metrics.");
    } catch (Throwable e) {
      logger.log(Level.WARNING, "Exception thrown when describing metrics.", e);
      span.setStatus(
          Status.UNKNOWN.withDescription(
              "Exception thrown when describing Prometheus Metrics: " + exceptionMessage(e)));
    } finally {
      span.end();
    }
    return samples;
  }

  @VisibleForTesting
  PrometheusStatsCollector(ViewManager viewManager) {
    this.viewManager = viewManager;
    Tracing.getExportComponent()
        .getSampledSpanStore()
        .registerSpanNamesForCollection(
            ImmutableList.of("DescribeMetricsForPrometheus", "ExportStatsToPrometheus"));
  }

  private static String exceptionMessage(Throwable e) {
    return e.getMessage() != null ? e.getMessage() : e.getClass().getName();
  }
}
