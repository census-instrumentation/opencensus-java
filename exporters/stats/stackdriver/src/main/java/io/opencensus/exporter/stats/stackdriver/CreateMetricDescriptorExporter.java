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

import com.google.api.MetricDescriptor;
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.common.collect.ImmutableSet;
import com.google.monitoring.v3.CreateMetricDescriptorRequest;
import com.google.monitoring.v3.ProjectName;
import io.opencensus.exporter.metrics.util.MetricExporter;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricDescriptor.Type;
import io.opencensus.trace.Span;
import io.opencensus.trace.Status;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

final class CreateMetricDescriptorExporter extends MetricExporter {
  private static final Tracer tracer = Tracing.getTracer();
  private static final Logger logger =
      Logger.getLogger(CreateMetricDescriptorExporter.class.getName());
  private static final ImmutableSet<String> SUPPORTED_EXTERNAL_DOMAINS =
      ImmutableSet.<String>of(
          "custom.googleapis.com", "external.googleapis.com", "workload.googleapis.com");
  private static final String GOOGLE_APIS_DOMAIN_SUFFIX = "googleapis.com";

  private final String projectId;
  private final ProjectName projectName;
  private final MetricServiceClient metricServiceClient;
  private final String domain;
  private final String displayNamePrefix;
  private final Map<String, io.opencensus.metrics.export.MetricDescriptor>
      registeredMetricDescriptors = new LinkedHashMap<>();
  private final Map<LabelKey, LabelValue> constantLabels;
  private final MetricExporter nextExporter;

  CreateMetricDescriptorExporter(
      String projectId,
      MetricServiceClient metricServiceClient,
      @javax.annotation.Nullable String metricNamePrefix,
      @javax.annotation.Nullable String displayNamePrefix,
      Map<LabelKey, LabelValue> constantLabels,
      MetricExporter nextExporter) {
    this.projectId = projectId;
    projectName = ProjectName.newBuilder().setProject(projectId).build();
    this.metricServiceClient = metricServiceClient;
    this.domain = StackdriverExportUtils.getDomain(metricNamePrefix);
    this.displayNamePrefix =
        StackdriverExportUtils.getDisplayNamePrefix(
            displayNamePrefix == null ? metricNamePrefix : displayNamePrefix);
    this.constantLabels = constantLabels;
    this.nextExporter = nextExporter;
  }

  // Returns true if the given metricDescriptor is successfully registered to Stackdriver
  // Monitoring, or the
  // exact same metric has already been registered. Returns false otherwise.
  private boolean registerMetricDescriptor(
      io.opencensus.metrics.export.MetricDescriptor metricDescriptor) {
    String metricName = metricDescriptor.getName();
    io.opencensus.metrics.export.MetricDescriptor existingMetricDescriptor =
        registeredMetricDescriptors.get(metricName);
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
    registeredMetricDescriptors.put(metricName, metricDescriptor);
    if (isBuiltInMetric(metricName)) {
      return true; // skip creating metric descriptor for stackdriver built-in metrics.
    }

    Span span = tracer.getCurrentSpan();
    span.addAnnotation("Create Stackdriver Metric.");
    MetricDescriptor stackDriverMetricDescriptor =
        StackdriverExportUtils.createMetricDescriptor(
            metricDescriptor, projectId, domain, displayNamePrefix, constantLabels);

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
                  "ApiException thrown when creating MetricDescriptor: "
                      + StackdriverExportUtils.exceptionMessage(e)));
      return false;
    } catch (Throwable e) {
      logger.log(Level.WARNING, "Exception thrown when creating MetricDescriptor.", e);
      span.setStatus(
          Status.UNKNOWN.withDescription(
              "Exception thrown when creating MetricDescriptor: "
                  + StackdriverExportUtils.exceptionMessage(e)));
      return false;
    }
  }

  @Override
  public void export(Collection<Metric> metrics) {
    ArrayList<Metric> registeredMetrics = new ArrayList<>(metrics.size());
    for (Metric metric : metrics) {
      final io.opencensus.metrics.export.MetricDescriptor metricDescriptor =
          metric.getMetricDescriptor();
      if (metricDescriptor.getType() == Type.SUMMARY) {
        List<Metric> convertedMetrics = StackdriverExportUtils.convertSummaryMetric(metric);
        registeredMetrics.ensureCapacity(registeredMetrics.size() + convertedMetrics.size());
        for (Metric convertedMetric : convertedMetrics) {
          if (registerMetricDescriptor(convertedMetric.getMetricDescriptor())) {
            registeredMetrics.add(convertedMetric);
          }
        }
      } else {
        if (registerMetricDescriptor(metricDescriptor)) {
          registeredMetrics.add(metric);
        }
      }
    }
    nextExporter.export(registeredMetrics);
  }

  private static boolean isBuiltInMetric(String metricName) {
    int domainIndex = metricName.indexOf('/');
    if (domainIndex < 0) {
      return false;
    }
    String metricDomain = metricName.substring(0, domainIndex);
    if (!metricDomain.endsWith(GOOGLE_APIS_DOMAIN_SUFFIX)) {
      return false; // domains like "my.org" are not Stackdriver built-in metrics.
    }
    // All googleapis.com domains except "custom.googleapis.com", "external.googleapis.com",
    // or "workload.googleapis.com" are built-in metrics.
    return !SUPPORTED_EXTERNAL_DOMAINS.contains(metricDomain);
  }
}
