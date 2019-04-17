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

package io.opencensus.exporter.stats.stackdriver;

import static io.opencensus.exporter.stats.stackdriver.StackdriverExportUtils.MAX_BATCH_EXPORT_SIZE;

import com.google.api.MonitoredResource;
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.common.collect.Lists;
import com.google.monitoring.v3.CreateTimeSeriesRequest;
import com.google.monitoring.v3.ProjectName;
import com.google.monitoring.v3.TimeSeries;
import io.opencensus.exporter.metrics.util.MetricExporter;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.export.Metric;
import io.opencensus.trace.Span;
import io.opencensus.trace.Status;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

final class CreateTimeSeriesExporter extends MetricExporter {
  private static final Tracer tracer = Tracing.getTracer();
  private static final Logger logger = Logger.getLogger(CreateTimeSeriesExporter.class.getName());

  private final ProjectName projectName;
  private final MetricServiceClient metricServiceClient;
  private final MonitoredResource monitoredResource;
  private final String domain;
  private final Map<LabelKey, LabelValue> constantLabels;

  CreateTimeSeriesExporter(
      String projectId,
      MetricServiceClient metricServiceClient,
      MonitoredResource monitoredResource,
      @javax.annotation.Nullable String metricNamePrefix,
      Map<LabelKey, LabelValue> constantLabels) {
    projectName = ProjectName.newBuilder().setProject(projectId).build();
    this.metricServiceClient = metricServiceClient;
    this.monitoredResource = monitoredResource;
    this.domain = StackdriverExportUtils.getDomain(metricNamePrefix);
    this.constantLabels = constantLabels;
  }

  @Override
  public void export(Collection<Metric> metrics) {
    List<TimeSeries> timeSeriesList = new ArrayList<>(metrics.size());
    for (Metric metric : metrics) {
      timeSeriesList.addAll(
          StackdriverExportUtils.createTimeSeriesList(
              metric, monitoredResource, domain, projectName.getProject(), constantLabels));
    }

    Span span = tracer.getCurrentSpan();
    for (List<TimeSeries> batchedTimeSeries :
        Lists.partition(timeSeriesList, MAX_BATCH_EXPORT_SIZE)) {
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
                    "ApiException thrown when exporting TimeSeries: "
                        + StackdriverExportUtils.exceptionMessage(e)));
      } catch (Throwable e) {
        logger.log(Level.WARNING, "Exception thrown when exporting TimeSeries.", e);
        span.setStatus(
            Status.UNKNOWN.withDescription(
                "Exception thrown when exporting TimeSeries: "
                    + StackdriverExportUtils.exceptionMessage(e)));
      }
    }
  }
}
