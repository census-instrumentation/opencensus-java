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

import com.google.api.MetricDescriptor;
import com.google.api.MonitoredResource;
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.monitoring.v3.CreateMetricDescriptorRequest;
import com.google.monitoring.v3.CreateTimeSeriesRequest;
import com.google.monitoring.v3.ProjectName;
import com.google.monitoring.v3.TimeSeries;
import io.opencensus.common.Duration;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.common.Scope;
import io.opencensus.common.Timestamp;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricDescriptor.Type;
import io.opencensus.metrics.export.MetricProducer;
import io.opencensus.metrics.export.MetricProducerManager;
import io.opencensus.metrics.export.Point;
import io.opencensus.metrics.export.Summary;
import io.opencensus.metrics.export.Summary.Snapshot;
import io.opencensus.metrics.export.Summary.Snapshot.ValueAtPercentile;
import io.opencensus.metrics.export.Value;
import io.opencensus.trace.EndSpanOptions;
import io.opencensus.trace.Sampler;
import io.opencensus.trace.Span;
import io.opencensus.trace.Status;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.samplers.Samplers;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.NotThreadSafe;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

/**
 * Worker {@code Runnable} that polls Metric from Metrics library and batch export to StackDriver.
 *
 * <p>{@code StackdriverExporterWorker} will be started in a daemon {@code Thread}.
 *
 * <p>The state of this class should only be accessed from the thread which {@link
 * StackdriverExporterWorker} resides in.
 */
@NotThreadSafe
final class StackdriverExporterWorker implements Runnable {

  private static final Logger logger = Logger.getLogger(StackdriverExporterWorker.class.getName());

  // Stackdriver Monitoring v3 only accepts up to 200 TimeSeries per CreateTimeSeries call.
  @VisibleForTesting static final int MAX_BATCH_EXPORT_SIZE = 200;

  @VisibleForTesting static final String DEFAULT_DISPLAY_NAME_PREFIX = "OpenCensus/";
  @VisibleForTesting static final String CUSTOM_METRIC_DOMAIN = "custom.googleapis.com/";

  @VisibleForTesting
  static final String CUSTOM_OPENCENSUS_DOMAIN = CUSTOM_METRIC_DOMAIN + "opencensus/";

  private static final String EXPORT_STATS_TO_STACKDRIVER_MONITORING =
      "ExportStatsToStackdriverMonitoring";
  private static final EndSpanOptions END_SPAN_OPTIONS =
      EndSpanOptions.builder().setSampleToLocalSpanStore(true).build();

  @VisibleForTesting
  static final LabelKey PERCENTILE_LABEL_KEY =
      LabelKey.create("percentile", "the value at a given percentile of a distribution");

  @VisibleForTesting
  static final String SNAPSHOT_SUFFIX_PERCENTILE = "_summary_snapshot_percentile";

  @VisibleForTesting static final String SUMMARY_SUFFIX_COUNT = "_summary_count";
  @VisibleForTesting static final String SUMMARY_SUFFIX_SUM = "_summary_sum";

  private final long scheduleDelayMillis;
  private final String projectId;
  private final ProjectName projectName;
  private final MetricServiceClient metricServiceClient;
  private final MetricProducerManager metricProducerManager;
  private final MonitoredResource monitoredResource;
  private final String domain;
  private final String displayNamePrefix;
  private final Map<String, io.opencensus.metrics.export.MetricDescriptor>
      registeredMetricDescriptors = new LinkedHashMap<>();

  private static final Tracer tracer = Tracing.getTracer();
  private static final Sampler probabilitySampler = Samplers.probabilitySampler(0.0001);

  StackdriverExporterWorker(
      String projectId,
      MetricServiceClient metricServiceClient,
      Duration exportInterval,
      MetricProducerManager metricProducerManager,
      MonitoredResource monitoredResource,
      @javax.annotation.Nullable String metricNamePrefix) {
    this.scheduleDelayMillis = exportInterval.toMillis();
    this.projectId = projectId;
    projectName = ProjectName.newBuilder().setProject(projectId).build();
    this.metricServiceClient = metricServiceClient;
    this.metricProducerManager = metricProducerManager;
    this.monitoredResource = monitoredResource;
    this.domain = getDomain(metricNamePrefix);
    this.displayNamePrefix = getDisplayNamePrefix(metricNamePrefix);
  }

  // Returns true if the given metricDescriptor is successfully registered to Stackdriver
  // Monitoring, or the
  // exact same metric has already been registered. Returns false otherwise.
  @VisibleForTesting
  boolean registerMetricDescriptor(io.opencensus.metrics.export.MetricDescriptor metricDescriptor) {
    io.opencensus.metrics.export.MetricDescriptor existingMetricDescriptor =
        registeredMetricDescriptors.get(metricDescriptor.getName());
    if (existingMetricDescriptor != null) {
      if (existingMetricDescriptor.equals(metricDescriptor)) {
        // Ignore metricDescriptor that are already registered.
        return true;
      } else {
        logger.log(
            Level.WARNING,
            "A different metric with the same name is already registered: "
                + existingMetricDescriptor);
        return false;
      }
    }
    registeredMetricDescriptors.put(metricDescriptor.getName(), metricDescriptor);

    Span span = tracer.getCurrentSpan();
    span.addAnnotation("Create Stackdriver Metric.");
    MetricDescriptor stackDriverMetricDescriptor =
        StackdriverExportUtils.createMetricDescriptor(
            metricDescriptor, projectId, domain, displayNamePrefix);

    CreateMetricDescriptorRequest request =
        CreateMetricDescriptorRequest.newBuilder()
            .setName(projectName.toString())
            .setMetricDescriptor(stackDriverMetricDescriptor)
            .build();
    try {
      metricServiceClient.createMetricDescriptor(request);
      span.addAnnotation("Finish creating MetricDescriptor.");
      return true;
    } catch (ApiException e) {
      logger.log(Level.WARNING, "ApiException thrown when creating MetricDescriptor.", e);
      span.setStatus(
          Status.CanonicalCode.valueOf(e.getStatusCode().getCode().name())
              .toStatus()
              .withDescription(
                  "ApiException thrown when creating MetricDescriptor: " + exceptionMessage(e)));
      return false;
    } catch (Throwable e) {
      logger.log(Level.WARNING, "Exception thrown when creating MetricDescriptor.", e);
      span.setStatus(
          Status.UNKNOWN.withDescription(
              "Exception thrown when creating MetricDescriptor: " + exceptionMessage(e)));
      return false;
    }
  }

  // Polls MetricProducerManager from Metrics library for all registered MetricDescriptors, and
  // upload them as TimeSeries to StackDriver.
  @VisibleForTesting
  void export() {
    ArrayList<Metric> metricsList = Lists.newArrayList();
    for (MetricProducer metricProducer : metricProducerManager.getAllMetricProducer()) {
      Collection<Metric> producerMetricsList = metricProducer.getMetrics();
      metricsList.ensureCapacity(metricsList.size() + producerMetricsList.size());
      for (Metric metric : producerMetricsList) {
        final io.opencensus.metrics.export.MetricDescriptor metricDescriptor =
            metric.getMetricDescriptor();
        if (metricDescriptor.getType() == Type.SUMMARY) {
          List<Metric> summaryToGaugeMetrics = summaryMetricToDoubleGaugeMetric(metric);
          metricsList.ensureCapacity(metricsList.size() + summaryToGaugeMetrics.size());
          for (Metric summaryToGaugeMetric : summaryToGaugeMetrics) {
            if (registerMetricDescriptor(summaryToGaugeMetric.getMetricDescriptor())) {
              metricsList.add(summaryToGaugeMetric);
            }
          }
        } else {
          if (registerMetricDescriptor(metric.getMetricDescriptor())) {
            metricsList.add(metric);
          }
        }
      }
    }

    List<TimeSeries> timeSeriesList = Lists.newArrayList();
    for (Metric metric : metricsList) {
      timeSeriesList.addAll(
          StackdriverExportUtils.createTimeSeriesList(metric, monitoredResource, domain));
    }

    for (List<TimeSeries> batchedTimeSeries :
        Lists.partition(timeSeriesList, MAX_BATCH_EXPORT_SIZE)) {
      Span span = tracer.getCurrentSpan();
      span.addAnnotation("Export Stackdriver TimeSeries.");
      try {
        CreateTimeSeriesRequest request =
            CreateTimeSeriesRequest.newBuilder()
                .setName(projectName.toString())
                .addAllTimeSeries(batchedTimeSeries)
                .build();
        metricServiceClient.createTimeSeries(request);
        span.addAnnotation("Finish exporting TimeSeries.");
      } catch (ApiException e) {
        logger.log(Level.WARNING, "ApiException thrown when exporting TimeSeries.", e);
        span.setStatus(
            Status.CanonicalCode.valueOf(e.getStatusCode().getCode().name())
                .toStatus()
                .withDescription(
                    "ApiException thrown when exporting TimeSeries: " + exceptionMessage(e)));
      } catch (Throwable e) {
        logger.log(Level.WARNING, "Exception thrown when exporting TimeSeries.", e);
        span.setStatus(
            Status.UNKNOWN.withDescription(
                "Exception thrown when exporting TimeSeries: " + exceptionMessage(e)));
      }
    }
  }

  @Override
  public void run() {
    while (true) {
      Span span =
          tracer
              .spanBuilder(EXPORT_STATS_TO_STACKDRIVER_MONITORING)
              .setRecordEvents(true)
              .setSampler(probabilitySampler)
              .startSpan();
      Scope scope = tracer.withSpan(span);
      try {
        export();
      } catch (Throwable e) {
        logger.log(Level.WARNING, "Exception thrown by the Stackdriver stats exporter.", e);
        span.setStatus(
            Status.UNKNOWN.withDescription(
                "Exception from Stackdriver Exporter: " + exceptionMessage(e)));
      } finally {
        scope.close();
        span.end(END_SPAN_OPTIONS);
      }
      try {
        Thread.sleep(scheduleDelayMillis);
      } catch (InterruptedException ie) {
        // Preserve the interruption status as per guidance and stop doing any work.
        Thread.currentThread().interrupt();
        return;
      }
    }
  }

  private static String exceptionMessage(Throwable e) {
    return e.getMessage() != null ? e.getMessage() : e.getClass().getName();
  }

  @VisibleForTesting
  static String getDomain(@javax.annotation.Nullable String metricNamePrefix) {
    String domain;
    if (Strings.isNullOrEmpty(metricNamePrefix)) {
      domain = CUSTOM_OPENCENSUS_DOMAIN;
    } else {
      if (!metricNamePrefix.endsWith("/")) {
        domain = metricNamePrefix + '/';
      } else {
        domain = metricNamePrefix;
      }
    }
    return domain;
  }

  @VisibleForTesting
  static String getDisplayNamePrefix(@javax.annotation.Nullable String metricNamePrefix) {
    if (metricNamePrefix == null) {
      return DEFAULT_DISPLAY_NAME_PREFIX;
    } else {
      if (!metricNamePrefix.endsWith("/") && !metricNamePrefix.isEmpty()) {
        metricNamePrefix += '/';
      }
      return metricNamePrefix;
    }
  }

  @VisibleForTesting
  static List<Metric> summaryMetricToDoubleGaugeMetric(Metric summaryMetric) {
    List<Metric> metricsList = Lists.newArrayList();
    final io.opencensus.metrics.export.MetricDescriptor metricDescriptor =
        summaryMetric.getMetricDescriptor();
    final List<LabelKey> labelKeysWithPercentile = new ArrayList<>(metricDescriptor.getLabelKeys());
    labelKeysWithPercentile.add(PERCENTILE_LABEL_KEY);
    io.opencensus.metrics.export.MetricDescriptor percentileMetricDescriptor =
        io.opencensus.metrics.export.MetricDescriptor.create(
            metricDescriptor.getName() + SNAPSHOT_SUFFIX_PERCENTILE,
            metricDescriptor.getDescription(),
            metricDescriptor.getUnit(),
            Type.GAUGE_DOUBLE,
            labelKeysWithPercentile);

    final List<io.opencensus.metrics.export.TimeSeries> percentileTimeSeries = new ArrayList<>();
    final List<io.opencensus.metrics.export.TimeSeries> summaryCountTimeSeries = new ArrayList<>();
    final List<io.opencensus.metrics.export.TimeSeries> summarySumTimeSeries = new ArrayList<>();
    for (final io.opencensus.metrics.export.TimeSeries timeSeries :
        summaryMetric.getTimeSeriesList()) {
      final List<LabelValue> labelValuesWithPercentile =
          new ArrayList<>(timeSeries.getLabelValues());
      final Timestamp timeSeriesTimestamp = timeSeries.getStartTimestamp();
      for (Point point : timeSeries.getPoints()) {
        final Timestamp pointTimestamp = point.getTimestamp();
        point
            .getValue()
            .match(
                Functions.<Void>returnNull(),
                Functions.<Void>returnNull(),
                Functions.<Void>returnNull(),
                new Function<Summary, Void>() {
                  @Override
                  public Void apply(Summary summary) {
                    Long count = summary.getCount();
                    if (count != null) {
                      summaryCountTimeSeries.add(
                          io.opencensus.metrics.export.TimeSeries.createWithOnePoint(
                              timeSeries.getLabelValues(),
                              Point.create(Value.longValue(count), pointTimestamp),
                              timeSeriesTimestamp));
                    }
                    Double sum = summary.getSum();
                    if (sum != null) {
                      summarySumTimeSeries.add(
                          io.opencensus.metrics.export.TimeSeries.createWithOnePoint(
                              timeSeries.getLabelValues(),
                              Point.create(Value.doubleValue(sum), pointTimestamp),
                              timeSeriesTimestamp));
                    }

                    Snapshot snapshot = summary.getSnapshot();
                    for (ValueAtPercentile valueAtPercentile : snapshot.getValueAtPercentiles()) {
                      labelValuesWithPercentile.add(
                          LabelValue.create(valueAtPercentile.getPercentile() + ""));
                      percentileTimeSeries.add(
                          io.opencensus.metrics.export.TimeSeries.createWithOnePoint(
                              labelValuesWithPercentile,
                              Point.create(
                                  Value.doubleValue(valueAtPercentile.getValue()), pointTimestamp),
                              timeSeriesTimestamp));
                      labelValuesWithPercentile.remove(labelValuesWithPercentile.size() - 1);
                    }
                    return null;
                  }
                },
                Functions.<Void>returnNull());
      }
    }

    // Metric for summary->count.
    if (summaryCountTimeSeries.size() > 0) {
      io.opencensus.metrics.export.MetricDescriptor summaryCountMetricDescriptor =
          io.opencensus.metrics.export.MetricDescriptor.create(
              metricDescriptor.getName() + SUMMARY_SUFFIX_COUNT,
              metricDescriptor.getDescription(),
              metricDescriptor.getUnit(),
              Type.CUMULATIVE_INT64,
              metricDescriptor.getLabelKeys());
      metricsList.add(Metric.create(summaryCountMetricDescriptor, summaryCountTimeSeries));
    }

    // Metric for summary->sum.
    if (summarySumTimeSeries.size() > 0) {
      io.opencensus.metrics.export.MetricDescriptor summarySumMetricDescriptor =
          io.opencensus.metrics.export.MetricDescriptor.create(
              metricDescriptor.getName() + SUMMARY_SUFFIX_SUM,
              metricDescriptor.getDescription(),
              metricDescriptor.getUnit(),
              Type.CUMULATIVE_DOUBLE,
              metricDescriptor.getLabelKeys());
      metricsList.add(Metric.create(summarySumMetricDescriptor, summarySumTimeSeries));
    }

    // Metric for summary->snapshot->percentiles.
    metricsList.add(Metric.create(percentileMetricDescriptor, percentileTimeSeries));
    return metricsList;
  }
}
