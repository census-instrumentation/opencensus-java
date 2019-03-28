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

import static com.google.common.truth.Truth.assertThat;
import static io.opencensus.exporter.stats.stackdriver.StackdriverExportUtils.CUSTOM_OPENCENSUS_DOMAIN;
import static io.opencensus.exporter.stats.stackdriver.StackdriverExportUtils.DEFAULT_DISPLAY_NAME_PREFIX;
import static io.opencensus.exporter.stats.stackdriver.StackdriverExportUtils.PERCENTILE_LABEL_KEY;
import static io.opencensus.exporter.stats.stackdriver.StackdriverExportUtils.SNAPSHOT_SUFFIX_PERCENTILE;
import static io.opencensus.exporter.stats.stackdriver.StackdriverExportUtils.STACKDRIVER_PROJECT_ID_KEY;
import static io.opencensus.exporter.stats.stackdriver.StackdriverExportUtils.SUMMARY_SUFFIX_COUNT;
import static io.opencensus.exporter.stats.stackdriver.StackdriverExportUtils.SUMMARY_SUFFIX_SUM;

import com.google.api.Distribution;
import com.google.api.Distribution.BucketOptions;
import com.google.api.Distribution.BucketOptions.Explicit;
import com.google.api.LabelDescriptor;
import com.google.api.LabelDescriptor.ValueType;
import com.google.api.Metric;
import com.google.api.MetricDescriptor;
import com.google.api.MetricDescriptor.MetricKind;
import com.google.api.MonitoredResource;
import com.google.common.collect.ImmutableMap;
import com.google.monitoring.v3.SpanContext;
import com.google.monitoring.v3.TimeInterval;
import com.google.monitoring.v3.TimeSeries;
import com.google.monitoring.v3.TypedValue;
import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import io.opencensus.common.Timestamp;
import io.opencensus.contrib.exemplar.util.AttachmentValueSpanContext;
import io.opencensus.contrib.exemplar.util.ExemplarUtils;
import io.opencensus.contrib.resource.util.CloudResource;
import io.opencensus.contrib.resource.util.ContainerResource;
import io.opencensus.contrib.resource.util.HostResource;
import io.opencensus.contrib.resource.util.K8sResource;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.data.AttachmentValue;
import io.opencensus.metrics.data.AttachmentValue.AttachmentValueString;
import io.opencensus.metrics.data.Exemplar;
import io.opencensus.metrics.export.Distribution.Bucket;
import io.opencensus.metrics.export.MetricDescriptor.Type;
import io.opencensus.metrics.export.Point;
import io.opencensus.metrics.export.Summary;
import io.opencensus.metrics.export.Summary.Snapshot;
import io.opencensus.metrics.export.Summary.Snapshot.ValueAtPercentile;
import io.opencensus.metrics.export.Value;
import io.opencensus.resource.Resource;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link StackdriverExportUtils}. */
@RunWith(JUnit4.class)
public class StackdriverExportUtilsTest {
  private static final String METRIC_NAME = "my measurement";
  private static final String METRIC_DESCRIPTION = "measure description";
  private static final String METRIC_UNIT = "us";
  private static final String METRIC_UNIT_2 = "1";
  private static final List<LabelKey> LABEL_KEY =
      Collections.singletonList(LabelKey.create("KEY1", "key description"));
  private static final List<LabelValue> LABEL_VALUE =
      Collections.singletonList(LabelValue.create("VALUE1"));
  private static final List<LabelValue> LABEL_VALUE_2 =
      Collections.singletonList(LabelValue.create("VALUE2"));
  private static final List<LabelKey> EMPTY_LABEL_KEY = new ArrayList<>();
  private static final List<LabelValue> EMPTY_LABEL_VALUE = new ArrayList<>();
  private static final io.opencensus.metrics.export.MetricDescriptor METRIC_DESCRIPTOR =
      io.opencensus.metrics.export.MetricDescriptor.create(
          METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, Type.CUMULATIVE_DOUBLE, LABEL_KEY);
  private static final io.opencensus.metrics.export.MetricDescriptor METRIC_DESCRIPTOR_2 =
      io.opencensus.metrics.export.MetricDescriptor.create(
          METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT_2, Type.CUMULATIVE_INT64, EMPTY_LABEL_KEY);
  private static final io.opencensus.metrics.export.MetricDescriptor GAUGE_METRIC_DESCRIPTOR =
      io.opencensus.metrics.export.MetricDescriptor.create(
          METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, Type.GAUGE_DOUBLE, LABEL_KEY);

  private static final List<Double> BUCKET_BOUNDARIES = Arrays.asList(1.0, 3.0, 5.0);
  private static final io.opencensus.metrics.export.Distribution.BucketOptions BUCKET_OPTIONS =
      io.opencensus.metrics.export.Distribution.BucketOptions.explicitOptions(BUCKET_BOUNDARIES);
  private static final Value VALUE_DOUBLE = Value.doubleValue(12345678.2);
  private static final Value VALUE_DOUBLE_2 = Value.doubleValue(133.79);

  private static final Timestamp TIMESTAMP = Timestamp.fromMillis(3000);
  private static final Timestamp TIMESTAMP_2 = Timestamp.fromMillis(1000);
  private static final Timestamp TIMESTAMP_3 = Timestamp.fromMillis(2000);
  private static final Point POINT = Point.create(VALUE_DOUBLE, TIMESTAMP);
  private static final Point POINT_2 = Point.create(VALUE_DOUBLE_2, TIMESTAMP_3);
  private static final io.opencensus.metrics.export.TimeSeries CUMULATIVE_TIME_SERIES =
      io.opencensus.metrics.export.TimeSeries.createWithOnePoint(LABEL_VALUE, POINT, TIMESTAMP_2);
  private static final io.opencensus.metrics.export.TimeSeries GAUGE_TIME_SERIES =
      io.opencensus.metrics.export.TimeSeries.createWithOnePoint(LABEL_VALUE, POINT, null);
  private static final io.opencensus.metrics.export.TimeSeries GAUGE_TIME_SERIES_2 =
      io.opencensus.metrics.export.TimeSeries.createWithOnePoint(LABEL_VALUE_2, POINT_2, null);
  private static final io.opencensus.metrics.export.Metric METRIC =
      io.opencensus.metrics.export.Metric.createWithOneTimeSeries(
          METRIC_DESCRIPTOR, CUMULATIVE_TIME_SERIES);
  private static final String PROJECT_ID = "id";
  private static final MonitoredResource DEFAULT_RESOURCE =
      MonitoredResource.newBuilder().setType("global").build();
  private static final MonitoredResource.Builder DEFAULT_RESOURCE_WITH_PROJECT_ID =
      MonitoredResource.newBuilder().putLabels(STACKDRIVER_PROJECT_ID_KEY, "proj1");

  private static final String DEFAULT_TASK_VALUE =
      "java-" + ManagementFactory.getRuntimeMXBean().getName();

  private static final io.opencensus.trace.SpanContext SPAN_CONTEXT_INVALID =
      io.opencensus.trace.SpanContext.INVALID;
  private static final Exemplar EXEMPLAR_1 =
      Exemplar.create(
          1.2,
          TIMESTAMP_2,
          Collections.<String, AttachmentValue>singletonMap(
              "key", AttachmentValueString.create("value")));
  private static final Exemplar EXEMPLAR_2 =
      Exemplar.create(
          5.6,
          TIMESTAMP_3,
          ImmutableMap.<String, AttachmentValue>of(
              ExemplarUtils.ATTACHMENT_KEY_SPAN_CONTEXT,
              AttachmentValueSpanContext.create(SPAN_CONTEXT_INVALID)));
  private static final io.opencensus.metrics.export.Distribution DISTRIBUTION =
      io.opencensus.metrics.export.Distribution.create(
          3,
          2,
          14,
          BUCKET_OPTIONS,
          Arrays.asList(
              Bucket.create(3),
              Bucket.create(1, EXEMPLAR_1),
              Bucket.create(2),
              Bucket.create(4, EXEMPLAR_2)));
  private static final Summary SUMMARY =
      Summary.create(
          10L,
          10.0,
          Snapshot.create(
              10L, 87.07, Collections.singletonList(ValueAtPercentile.create(0.98, 10.2))));
  private static final Value DOUBLE_VALUE = Value.doubleValue(1.1);
  private static final Value LONG_VALUE = Value.longValue(10000);
  private static final Value DISTRIBUTION_VALUE = Value.distributionValue(DISTRIBUTION);
  private static final Value SUMMARY_VALUE = Value.summaryValue(SUMMARY);

  private static final io.opencensus.metrics.export.MetricDescriptor HISTOGRAM_METRIC_DESCRIPTOR =
      io.opencensus.metrics.export.MetricDescriptor.create(
          METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, Type.CUMULATIVE_DISTRIBUTION, LABEL_KEY);

  private static final Point DISTRIBUTION_POINT = Point.create(DISTRIBUTION_VALUE, TIMESTAMP);
  private static final io.opencensus.metrics.export.TimeSeries DISTRIBUTION_TIME_SERIES =
      io.opencensus.metrics.export.TimeSeries.createWithOnePoint(
          LABEL_VALUE, DISTRIBUTION_POINT, null);
  private static final io.opencensus.metrics.export.Metric DISTRIBUTION_METRIC =
      io.opencensus.metrics.export.Metric.createWithOneTimeSeries(
          HISTOGRAM_METRIC_DESCRIPTOR, DISTRIBUTION_TIME_SERIES);
  private static final Summary SUMMARY_1 =
      Summary.create(
          22L,
          74.8,
          Snapshot.create(
              10L,
              87.07,
              Arrays.asList(
                  ValueAtPercentile.create(50, 6),
                  ValueAtPercentile.create(75, 10.2),
                  ValueAtPercentile.create(98, 4.6),
                  ValueAtPercentile.create(99, 1.2))));
  private static final Value SUMMARY_VALUE_1 = Value.summaryValue(SUMMARY_1);
  private static final Point SUMMARY_POINT = Point.create(SUMMARY_VALUE_1, TIMESTAMP);
  private static final Summary SUMMARY_NULL_SUM =
      Summary.create(
          22L,
          null,
          Snapshot.create(10L, 87.07, Collections.singletonList(ValueAtPercentile.create(50, 6))));
  private static final Value SUMMARY_VALUE_NULL_SUM = Value.summaryValue(SUMMARY_NULL_SUM);
  private static final Point SUMMARY_POINT_NULL_SUM =
      Point.create(SUMMARY_VALUE_NULL_SUM, TIMESTAMP);
  private static final io.opencensus.metrics.export.TimeSeries SUMMARY_TIME_SERIES_NULL_SUM =
      io.opencensus.metrics.export.TimeSeries.createWithOnePoint(
          LABEL_VALUE, SUMMARY_POINT_NULL_SUM, null);
  private static final io.opencensus.metrics.export.MetricDescriptor SUMMARY_METRIC_DESCRIPTOR =
      io.opencensus.metrics.export.MetricDescriptor.create(
          METRIC_NAME,
          METRIC_DESCRIPTION,
          METRIC_UNIT,
          io.opencensus.metrics.export.MetricDescriptor.Type.SUMMARY,
          LABEL_KEY);
  private static final io.opencensus.metrics.export.TimeSeries SUMMARY_TIME_SERIES =
      io.opencensus.metrics.export.TimeSeries.createWithOnePoint(LABEL_VALUE, SUMMARY_POINT, null);

  private static final io.opencensus.metrics.export.Metric SUMMARY_METRIC =
      io.opencensus.metrics.export.Metric.createWithOneTimeSeries(
          SUMMARY_METRIC_DESCRIPTOR, SUMMARY_TIME_SERIES);
  private static final io.opencensus.metrics.export.Metric SUMMARY_METRIC_NULL_SUM =
      io.opencensus.metrics.export.Metric.createWithOneTimeSeries(
          SUMMARY_METRIC_DESCRIPTOR, SUMMARY_TIME_SERIES_NULL_SUM);

  @Test
  public void testConstants() {
    assertThat(StackdriverExportUtils.OPENCENSUS_TASK).isEqualTo("opencensus_task");
    assertThat(StackdriverExportUtils.OPENCENSUS_TASK_DESCRIPTION)
        .isEqualTo("Opencensus task identifier");
    assertThat(StackdriverExportUtils.STACKDRIVER_PROJECT_ID_KEY).isEqualTo("project_id");
    assertThat(StackdriverExportUtils.MAX_BATCH_EXPORT_SIZE).isEqualTo(200);
    assertThat(StackdriverExportUtils.CUSTOM_METRIC_DOMAIN).isEqualTo("custom.googleapis.com/");
    assertThat(StackdriverExportUtils.CUSTOM_OPENCENSUS_DOMAIN)
        .isEqualTo("custom.googleapis.com/opencensus/");
    assertThat(StackdriverExportUtils.DEFAULT_DISPLAY_NAME_PREFIX).isEqualTo("OpenCensus/");
  }

  @Test
  public void createLabelDescriptor() {
    assertThat(StackdriverExportUtils.createLabelDescriptor(LabelKey.create("key", "desc")))
        .isEqualTo(
            LabelDescriptor.newBuilder()
                .setKey("key")
                .setDescription("desc")
                .setValueType(ValueType.STRING)
                .build());
  }

  @Test
  public void createMetricKind() {
    assertThat(StackdriverExportUtils.createMetricKind(Type.CUMULATIVE_INT64))
        .isEqualTo(MetricKind.CUMULATIVE);
    assertThat(StackdriverExportUtils.createMetricKind(Type.SUMMARY))
        .isEqualTo(MetricKind.UNRECOGNIZED);
    assertThat(StackdriverExportUtils.createMetricKind(Type.GAUGE_INT64))
        .isEqualTo(MetricKind.GAUGE);
    assertThat(StackdriverExportUtils.createMetricKind(Type.GAUGE_DOUBLE))
        .isEqualTo(MetricKind.GAUGE);
  }

  @Test
  public void createValueType() {
    assertThat(StackdriverExportUtils.createValueType(Type.GAUGE_DOUBLE))
        .isEqualTo(MetricDescriptor.ValueType.DOUBLE);
    assertThat(StackdriverExportUtils.createValueType(Type.CUMULATIVE_INT64))
        .isEqualTo(MetricDescriptor.ValueType.INT64);
    assertThat(StackdriverExportUtils.createValueType(Type.GAUGE_INT64))
        .isEqualTo(MetricDescriptor.ValueType.INT64);
    assertThat(StackdriverExportUtils.createValueType(Type.CUMULATIVE_DOUBLE))
        .isEqualTo(MetricDescriptor.ValueType.DOUBLE);
    assertThat(StackdriverExportUtils.createValueType(Type.GAUGE_DISTRIBUTION))
        .isEqualTo(MetricDescriptor.ValueType.DISTRIBUTION);
    assertThat(StackdriverExportUtils.createValueType(Type.CUMULATIVE_DISTRIBUTION))
        .isEqualTo(MetricDescriptor.ValueType.DISTRIBUTION);
  }

  @Test
  public void createMetric() {
    assertThat(
            StackdriverExportUtils.createMetric(
                METRIC_DESCRIPTOR, LABEL_VALUE, CUSTOM_OPENCENSUS_DOMAIN))
        .isEqualTo(
            Metric.newBuilder()
                .setType("custom.googleapis.com/opencensus/" + METRIC_NAME)
                .putLabels("KEY1", "VALUE1")
                .putLabels(StackdriverExportUtils.OPENCENSUS_TASK, DEFAULT_TASK_VALUE)
                .build());
  }

  @Test
  public void createMetric_WithExternalMetricDomain() {
    String prometheusDomain = "external.googleapis.com/prometheus/";
    assertThat(
            StackdriverExportUtils.createMetric(METRIC_DESCRIPTOR, LABEL_VALUE, prometheusDomain))
        .isEqualTo(
            Metric.newBuilder()
                .setType(prometheusDomain + METRIC_NAME)
                .putLabels("KEY1", "VALUE1")
                .putLabels(StackdriverExportUtils.OPENCENSUS_TASK, DEFAULT_TASK_VALUE)
                .build());
  }

  @Test
  public void createMetric_EmptyLabel() {
    assertThat(
            StackdriverExportUtils.createMetric(
                METRIC_DESCRIPTOR_2, EMPTY_LABEL_VALUE, CUSTOM_OPENCENSUS_DOMAIN))
        .isEqualTo(
            Metric.newBuilder()
                .setType("custom.googleapis.com/opencensus/" + METRIC_NAME)
                .putLabels(StackdriverExportUtils.OPENCENSUS_TASK, DEFAULT_TASK_VALUE)
                .build());
  }

  @Test
  public void convertTimestamp() {
    Timestamp censusTimestamp1 = Timestamp.create(100, 3000);
    assertThat(StackdriverExportUtils.convertTimestamp(censusTimestamp1))
        .isEqualTo(
            com.google.protobuf.Timestamp.newBuilder().setSeconds(100).setNanos(3000).build());

    // Stackdriver doesn't allow negative values, instead it will replace the negative values
    // by returning a default instance.
    Timestamp censusTimestamp2 = Timestamp.create(-100, 3000);
    assertThat(StackdriverExportUtils.convertTimestamp(censusTimestamp2))
        .isEqualTo(com.google.protobuf.Timestamp.newBuilder().build());
  }

  @Test
  public void createBucketOptions() {
    assertThat(StackdriverExportUtils.createBucketOptions(BUCKET_OPTIONS))
        .isEqualTo(
            BucketOptions.newBuilder()
                .setExplicitBuckets(
                    Explicit.newBuilder().addAllBounds(Arrays.asList(0.0, 1.0, 3.0, 5.0)))
                .build());
  }

  @Test
  public void createBucketOptions_Null() {
    assertThat(StackdriverExportUtils.createBucketOptions(null))
        .isEqualTo(BucketOptions.newBuilder().build());
  }

  @Test
  public void createDistribution() {
    StackdriverExportUtils.setCachedProjectIdForExemplar(null);
    assertThat(StackdriverExportUtils.createDistribution(DISTRIBUTION))
        .isEqualTo(
            Distribution.newBuilder()
                .setCount(3)
                .setMean(0.6666666666666666)
                .setBucketOptions(StackdriverExportUtils.createBucketOptions(BUCKET_OPTIONS))
                .addAllBucketCounts(Arrays.asList(0L, 3L, 1L, 2L, 4L))
                .setSumOfSquaredDeviation(14)
                .addAllExemplars(
                    Arrays.<Distribution.Exemplar>asList(
                        Distribution.Exemplar.newBuilder()
                            .setValue(1.2)
                            .setTimestamp(StackdriverExportUtils.convertTimestamp(TIMESTAMP_2))
                            .addAttachments(
                                Any.newBuilder()
                                    .setTypeUrl(
                                        StackdriverExportUtils.EXEMPLAR_ATTACHMENT_TYPE_STRING)
                                    .setValue(ByteString.copyFromUtf8("value"))
                                    .build())
                            .build(),
                        Distribution.Exemplar.newBuilder()
                            .setValue(5.6)
                            .setTimestamp(StackdriverExportUtils.convertTimestamp(TIMESTAMP_3))
                            // Cached project ID is set to null, so no SpanContext attachment will
                            // be created.
                            .build()))
                .build());
  }

  @Test
  public void createTypedValue() {
    assertThat(StackdriverExportUtils.createTypedValue(DOUBLE_VALUE))
        .isEqualTo(TypedValue.newBuilder().setDoubleValue(1.1).build());
    assertThat(StackdriverExportUtils.createTypedValue(LONG_VALUE))
        .isEqualTo(TypedValue.newBuilder().setInt64Value(10000).build());
    assertThat(StackdriverExportUtils.createTypedValue(DISTRIBUTION_VALUE))
        .isEqualTo(
            TypedValue.newBuilder()
                .setDistributionValue(StackdriverExportUtils.createDistribution(DISTRIBUTION))
                .build());
  }

  @Test
  public void createTypedValue_UnknownType() {
    assertThat(StackdriverExportUtils.createTypedValue(SUMMARY_VALUE))
        .isEqualTo(TypedValue.newBuilder().build());
  }

  @Test
  public void createPoint() {
    assertThat(StackdriverExportUtils.createPoint(POINT, null))
        .isEqualTo(
            com.google.monitoring.v3.Point.newBuilder()
                .setInterval(
                    TimeInterval.newBuilder()
                        .setEndTime(StackdriverExportUtils.convertTimestamp(TIMESTAMP))
                        .build())
                .setValue(StackdriverExportUtils.createTypedValue(VALUE_DOUBLE))
                .build());
  }

  @Test
  public void createPoint_Cumulative() {
    assertThat(StackdriverExportUtils.createPoint(POINT, TIMESTAMP_2))
        .isEqualTo(
            com.google.monitoring.v3.Point.newBuilder()
                .setInterval(
                    TimeInterval.newBuilder()
                        .setStartTime(StackdriverExportUtils.convertTimestamp(TIMESTAMP_2))
                        .setEndTime(StackdriverExportUtils.convertTimestamp(TIMESTAMP))
                        .build())
                .setValue(StackdriverExportUtils.createTypedValue(VALUE_DOUBLE))
                .build());
  }

  @Test
  public void createMetricDescriptor() {
    MetricDescriptor metricDescriptor =
        StackdriverExportUtils.createMetricDescriptor(
            METRIC_DESCRIPTOR, PROJECT_ID, "custom.googleapis.com/myorg/", "myorg/");
    assertThat(metricDescriptor.getName())
        .isEqualTo(
            "projects/"
                + PROJECT_ID
                + "/metricDescriptors/custom.googleapis.com/myorg/"
                + METRIC_NAME);
    assertThat(metricDescriptor.getDescription()).isEqualTo(METRIC_DESCRIPTION);
    assertThat(metricDescriptor.getDisplayName()).isEqualTo("myorg/" + METRIC_NAME);
    assertThat(metricDescriptor.getType()).isEqualTo("custom.googleapis.com/myorg/" + METRIC_NAME);
    assertThat(metricDescriptor.getUnit()).isEqualTo(METRIC_UNIT);
    assertThat(metricDescriptor.getMetricKind()).isEqualTo(MetricKind.CUMULATIVE);

    assertThat(metricDescriptor.getValueType()).isEqualTo(MetricDescriptor.ValueType.DOUBLE);
    assertThat(metricDescriptor.getLabelsList())
        .containsExactly(
            LabelDescriptor.newBuilder()
                .setKey(LABEL_KEY.get(0).getKey())
                .setDescription(LABEL_KEY.get(0).getDescription())
                .setValueType(ValueType.STRING)
                .build(),
            LabelDescriptor.newBuilder()
                .setKey(StackdriverExportUtils.OPENCENSUS_TASK)
                .setDescription(StackdriverExportUtils.OPENCENSUS_TASK_DESCRIPTION)
                .setValueType(ValueType.STRING)
                .build());
  }

  @Test
  public void createMetricDescriptor_cumulative() {
    MetricDescriptor metricDescriptor =
        StackdriverExportUtils.createMetricDescriptor(
            METRIC_DESCRIPTOR_2, PROJECT_ID, CUSTOM_OPENCENSUS_DOMAIN, DEFAULT_DISPLAY_NAME_PREFIX);
    assertThat(metricDescriptor.getName())
        .isEqualTo(
            "projects/"
                + PROJECT_ID
                + "/metricDescriptors/custom.googleapis.com/opencensus/"
                + METRIC_NAME);
    assertThat(metricDescriptor.getDescription()).isEqualTo(METRIC_DESCRIPTION);
    assertThat(metricDescriptor.getDisplayName()).isEqualTo("OpenCensus/" + METRIC_NAME);
    assertThat(metricDescriptor.getType())
        .isEqualTo("custom.googleapis.com/opencensus/" + METRIC_NAME);
    assertThat(metricDescriptor.getUnit()).isEqualTo("1");
    assertThat(metricDescriptor.getMetricKind()).isEqualTo(MetricKind.CUMULATIVE);
    assertThat(metricDescriptor.getValueType()).isEqualTo(MetricDescriptor.ValueType.INT64);
    assertThat(metricDescriptor.getLabelsList())
        .containsExactly(
            LabelDescriptor.newBuilder()
                .setKey(StackdriverExportUtils.OPENCENSUS_TASK)
                .setDescription(StackdriverExportUtils.OPENCENSUS_TASK_DESCRIPTION)
                .setValueType(ValueType.STRING)
                .build());
  }

  @Test
  public void createTimeSeriesList_Cumulative() {
    List<TimeSeries> timeSeriesList =
        StackdriverExportUtils.createTimeSeriesList(
            METRIC, DEFAULT_RESOURCE, CUSTOM_OPENCENSUS_DOMAIN, PROJECT_ID);
    assertThat(timeSeriesList).hasSize(1);
    TimeSeries expectedTimeSeries =
        TimeSeries.newBuilder()
            .setMetricKind(MetricKind.CUMULATIVE)
            .setValueType(MetricDescriptor.ValueType.DOUBLE)
            .setMetric(
                StackdriverExportUtils.createMetric(
                    METRIC_DESCRIPTOR, LABEL_VALUE, CUSTOM_OPENCENSUS_DOMAIN))
            .setResource(MonitoredResource.newBuilder().setType("global"))
            .addPoints(StackdriverExportUtils.createPoint(POINT, TIMESTAMP_2))
            .build();
    assertThat(timeSeriesList).containsExactly(expectedTimeSeries);
  }

  @Test
  public void createTimeSeriesList_Distribution() {
    List<TimeSeries> timeSeriesList =
        StackdriverExportUtils.createTimeSeriesList(
            DISTRIBUTION_METRIC, DEFAULT_RESOURCE, CUSTOM_OPENCENSUS_DOMAIN, PROJECT_ID);

    assertThat(timeSeriesList.size()).isEqualTo(1);
    TimeSeries timeSeries = timeSeriesList.get(0);
    assertThat(timeSeries.getPointsCount()).isEqualTo(1);
    String expectedSpanName =
        "projects/id/traces/00000000000000000000000000000000/spans/0000000000000000";
    assertThat(timeSeries.getPoints(0).getValue().getDistributionValue())
        .isEqualTo(
            com.google.api.Distribution.newBuilder()
                .setCount(3)
                .setMean(0.6666666666666666)
                .setBucketOptions(
                    BucketOptions.newBuilder()
                        .setExplicitBuckets(
                            Explicit.newBuilder()
                                .addAllBounds(Arrays.asList(0.0, 1.0, 3.0, 5.0))
                                .build())
                        .build())
                .addAllBucketCounts(Arrays.asList(0L, 3L, 1L, 2L, 4L))
                .setSumOfSquaredDeviation(14)
                .addAllExemplars(
                    Arrays.<Distribution.Exemplar>asList(
                        Distribution.Exemplar.newBuilder()
                            .setValue(1.2)
                            .setTimestamp(StackdriverExportUtils.convertTimestamp(TIMESTAMP_2))
                            .addAttachments(
                                Any.newBuilder()
                                    .setTypeUrl(
                                        StackdriverExportUtils.EXEMPLAR_ATTACHMENT_TYPE_STRING)
                                    .setValue(ByteString.copyFromUtf8("value"))
                                    .build())
                            .build(),
                        Distribution.Exemplar.newBuilder()
                            .setValue(5.6)
                            .setTimestamp(StackdriverExportUtils.convertTimestamp(TIMESTAMP_3))
                            .addAttachments(
                                Any.newBuilder()
                                    .setTypeUrl(
                                        StackdriverExportUtils
                                            .EXEMPLAR_ATTACHMENT_TYPE_SPAN_CONTEXT)
                                    .setValue(
                                        SpanContext.newBuilder()
                                            .setSpanName(expectedSpanName)
                                            .build()
                                            .toByteString())
                                    .build())
                            .build()))
                .build());
  }

  @Test
  public void createTimeSeriesList_Gauge() {
    io.opencensus.metrics.export.Metric metric =
        io.opencensus.metrics.export.Metric.create(
            GAUGE_METRIC_DESCRIPTOR, Arrays.asList(GAUGE_TIME_SERIES, GAUGE_TIME_SERIES_2));

    List<TimeSeries> timeSeriesList =
        StackdriverExportUtils.createTimeSeriesList(
            metric, DEFAULT_RESOURCE, CUSTOM_OPENCENSUS_DOMAIN, PROJECT_ID);
    assertThat(timeSeriesList).hasSize(2);
    TimeSeries expected1 =
        TimeSeries.newBuilder()
            .setMetricKind(MetricKind.GAUGE)
            .setValueType(MetricDescriptor.ValueType.DOUBLE)
            .setMetric(
                StackdriverExportUtils.createMetric(
                    GAUGE_METRIC_DESCRIPTOR, LABEL_VALUE, CUSTOM_OPENCENSUS_DOMAIN))
            .setResource(MonitoredResource.newBuilder().setType("global"))
            .addPoints(StackdriverExportUtils.createPoint(POINT, null))
            .build();
    TimeSeries expected2 =
        TimeSeries.newBuilder()
            .setMetricKind(MetricKind.GAUGE)
            .setValueType(MetricDescriptor.ValueType.DOUBLE)
            .setMetric(
                StackdriverExportUtils.createMetric(
                    GAUGE_METRIC_DESCRIPTOR, LABEL_VALUE_2, CUSTOM_OPENCENSUS_DOMAIN))
            .setResource(MonitoredResource.newBuilder().setType("global"))
            .addPoints(StackdriverExportUtils.createPoint(POINT_2, null))
            .build();
    assertThat(timeSeriesList).containsExactly(expected1, expected2);
  }

  @Test
  public void createTimeSeriesList_withCustomMonitoredResource() {
    MonitoredResource resource =
        MonitoredResource.newBuilder().setType("global").putLabels("key", "value").build();
    List<TimeSeries> timeSeriesList =
        StackdriverExportUtils.createTimeSeriesList(
            METRIC, resource, CUSTOM_OPENCENSUS_DOMAIN, PROJECT_ID);
    assertThat(timeSeriesList)
        .containsExactly(
            TimeSeries.newBuilder()
                .setMetricKind(MetricKind.CUMULATIVE)
                .setValueType(MetricDescriptor.ValueType.DOUBLE)
                .setMetric(
                    StackdriverExportUtils.createMetric(
                        METRIC_DESCRIPTOR, LABEL_VALUE, CUSTOM_OPENCENSUS_DOMAIN))
                .setResource(resource)
                .addPoints(StackdriverExportUtils.createPoint(POINT, TIMESTAMP_2))
                .build());
  }

  @Test
  public void convertSummaryMetric() {
    io.opencensus.metrics.export.MetricDescriptor expectedMetricDescriptor1 =
        io.opencensus.metrics.export.MetricDescriptor.create(
            METRIC_NAME + SUMMARY_SUFFIX_COUNT,
            METRIC_DESCRIPTION,
            METRIC_UNIT_2,
            Type.CUMULATIVE_INT64,
            LABEL_KEY);
    io.opencensus.metrics.export.MetricDescriptor expectedMetricDescriptor2 =
        io.opencensus.metrics.export.MetricDescriptor.create(
            METRIC_NAME + SUMMARY_SUFFIX_SUM,
            METRIC_DESCRIPTION,
            METRIC_UNIT,
            Type.CUMULATIVE_DOUBLE,
            LABEL_KEY);
    List<LabelKey> labelKeys = new ArrayList<>(LABEL_KEY);
    labelKeys.add(PERCENTILE_LABEL_KEY);
    io.opencensus.metrics.export.MetricDescriptor expectedMetricDescriptor3 =
        io.opencensus.metrics.export.MetricDescriptor.create(
            METRIC_NAME + SNAPSHOT_SUFFIX_PERCENTILE,
            METRIC_DESCRIPTION,
            METRIC_UNIT,
            Type.GAUGE_DOUBLE,
            labelKeys);
    List<io.opencensus.metrics.export.TimeSeries> expectedTimeSeries1 =
        Collections.singletonList(
            io.opencensus.metrics.export.TimeSeries.createWithOnePoint(
                LABEL_VALUE, Point.create(Value.longValue(22), TIMESTAMP), null));
    List<io.opencensus.metrics.export.TimeSeries> expectedTimeSeries2 =
        Collections.singletonList(
            io.opencensus.metrics.export.TimeSeries.createWithOnePoint(
                LABEL_VALUE, Point.create(Value.doubleValue(74.8), TIMESTAMP), null));
    LabelValue existingLabelValues = LABEL_VALUE.get(0);
    List<io.opencensus.metrics.export.TimeSeries> expectedTimeSeries3 =
        Arrays.asList(
            io.opencensus.metrics.export.TimeSeries.createWithOnePoint(
                Arrays.asList(existingLabelValues, LabelValue.create("50.0")),
                Point.create(Value.doubleValue(6), TIMESTAMP),
                null),
            io.opencensus.metrics.export.TimeSeries.createWithOnePoint(
                Arrays.asList(existingLabelValues, LabelValue.create("75.0")),
                Point.create(Value.doubleValue(10.2), TIMESTAMP),
                null),
            io.opencensus.metrics.export.TimeSeries.createWithOnePoint(
                Arrays.asList(existingLabelValues, LabelValue.create("98.0")),
                Point.create(Value.doubleValue(4.6), TIMESTAMP),
                null),
            io.opencensus.metrics.export.TimeSeries.createWithOnePoint(
                Arrays.asList(existingLabelValues, LabelValue.create("99.0")),
                Point.create(Value.doubleValue(1.2), TIMESTAMP),
                null));
    List<io.opencensus.metrics.export.Metric> metrics =
        StackdriverExportUtils.convertSummaryMetric(SUMMARY_METRIC);

    assertThat(metrics).isNotEmpty();
    assertThat(metrics.size()).isEqualTo(3);
    assertThat(metrics.get(0).getMetricDescriptor()).isEqualTo(expectedMetricDescriptor1);
    assertThat(metrics.get(0).getTimeSeriesList()).isNotEmpty();
    assertThat(metrics.get(0).getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metrics.get(0).getTimeSeriesList()).containsExactlyElementsIn(expectedTimeSeries1);
    assertThat(metrics.get(1).getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metrics.get(1).getTimeSeriesList()).containsExactlyElementsIn(expectedTimeSeries2);
    assertThat(metrics.get(1).getTimeSeriesList()).isNotEmpty();
    assertThat(metrics.get(1).getMetricDescriptor()).isEqualTo(expectedMetricDescriptor2);
    assertThat(metrics.get(2).getTimeSeriesList()).isNotEmpty();
    assertThat(metrics.get(2).getMetricDescriptor()).isEqualTo(expectedMetricDescriptor3);
    assertThat(metrics.get(2).getTimeSeriesList().size()).isEqualTo(4);
    assertThat(metrics.get(2).getTimeSeriesList()).containsExactlyElementsIn(expectedTimeSeries3);
  }

  @Test
  public void convertSummaryMetricWithNullSum() {
    io.opencensus.metrics.export.MetricDescriptor expectedMetricDescriptor1 =
        io.opencensus.metrics.export.MetricDescriptor.create(
            METRIC_NAME + SUMMARY_SUFFIX_COUNT,
            METRIC_DESCRIPTION,
            METRIC_UNIT_2,
            Type.CUMULATIVE_INT64,
            LABEL_KEY);
    List<LabelKey> labelKeys = new ArrayList<>(LABEL_KEY);
    labelKeys.add(PERCENTILE_LABEL_KEY);
    io.opencensus.metrics.export.MetricDescriptor expectedMetricDescriptor2 =
        io.opencensus.metrics.export.MetricDescriptor.create(
            METRIC_NAME + SNAPSHOT_SUFFIX_PERCENTILE,
            METRIC_DESCRIPTION,
            METRIC_UNIT,
            Type.GAUGE_DOUBLE,
            labelKeys);
    List<io.opencensus.metrics.export.TimeSeries> expectedTimeSeries1 =
        Collections.singletonList(
            io.opencensus.metrics.export.TimeSeries.createWithOnePoint(
                LABEL_VALUE, Point.create(Value.longValue(22), TIMESTAMP), null));
    LabelValue existingLabelValues = LABEL_VALUE.get(0);
    List<io.opencensus.metrics.export.TimeSeries> expectedTimeSeries2 =
        Collections.singletonList(
            io.opencensus.metrics.export.TimeSeries.createWithOnePoint(
                Arrays.asList(existingLabelValues, LabelValue.create("50.0")),
                Point.create(Value.doubleValue(6), TIMESTAMP),
                null));
    List<io.opencensus.metrics.export.Metric> metrics =
        StackdriverExportUtils.convertSummaryMetric(SUMMARY_METRIC_NULL_SUM);

    assertThat(metrics).isNotEmpty();
    assertThat(metrics.size()).isEqualTo(2);
    assertThat(metrics.get(0).getMetricDescriptor()).isEqualTo(expectedMetricDescriptor1);
    assertThat(metrics.get(0).getTimeSeriesList()).isNotEmpty();
    assertThat(metrics.get(0).getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metrics.get(0).getTimeSeriesList()).containsExactlyElementsIn(expectedTimeSeries1);
    assertThat(metrics.get(1).getTimeSeriesList()).isNotEmpty();
    assertThat(metrics.get(1).getMetricDescriptor()).isEqualTo(expectedMetricDescriptor2);
    assertThat(metrics.get(1).getTimeSeriesList().size()).isEqualTo(1);
    assertThat(metrics.get(1).getTimeSeriesList()).containsExactlyElementsIn(expectedTimeSeries2);
  }

  @Test
  public void setResourceForBuilder_GcpInstanceType() {
    MonitoredResource.Builder monitoredResourceBuilder = DEFAULT_RESOURCE_WITH_PROJECT_ID.clone();
    Map<String, String> resourceLabels = new HashMap<String, String>();
    resourceLabels.put(CloudResource.ACCOUNT_ID_KEY, "proj1");
    resourceLabels.put(CloudResource.PROVIDER_KEY, CloudResource.PROVIDER_GCP);
    resourceLabels.put(HostResource.ID_KEY, "inst1");
    resourceLabels.put(CloudResource.ZONE_KEY, "zone1");
    resourceLabels.put("extra_key", "must be ignored");
    Map<String, String> expectedResourceLabels = new HashMap<String, String>();
    expectedResourceLabels.put("project_id", "proj1");
    expectedResourceLabels.put("instance_id", "inst1");
    expectedResourceLabels.put("zone", "zone1");
    Resource resource = Resource.create(HostResource.TYPE, resourceLabels);

    StackdriverExportUtils.setResourceForBuilder(monitoredResourceBuilder, resource);

    assertThat(monitoredResourceBuilder.getType()).isNotNull();
    assertThat(monitoredResourceBuilder.getLabelsMap()).isNotEmpty();
    assertThat(monitoredResourceBuilder.getType()).isEqualTo("gce_instance");
    assertThat(monitoredResourceBuilder.getLabelsMap().size()).isEqualTo(3);
    assertThat(monitoredResourceBuilder.getLabelsMap())
        .containsExactlyEntriesIn(expectedResourceLabels);
  }

  @Test
  public void setResourceForBuilder_K8sInstanceType() {
    MonitoredResource.Builder monitoredResourceBuilder = DEFAULT_RESOURCE_WITH_PROJECT_ID.clone();
    Map<String, String> resourceLabels = new HashMap<String, String>();
    resourceLabels.put(CloudResource.ZONE_KEY, "zone1");
    resourceLabels.put(HostResource.ID_KEY, "instance1");
    resourceLabels.put(K8sResource.CLUSTER_NAME_KEY, "cluster1");
    resourceLabels.put(ContainerResource.NAME_KEY, "container1");
    resourceLabels.put(K8sResource.NAMESPACE_NAME_KEY, "namespace1");
    resourceLabels.put(K8sResource.POD_NAME_KEY, "pod1");
    resourceLabels.put("extra_key", "must be ignored");
    Map<String, String> expectedResourceLabels = new HashMap<String, String>();
    expectedResourceLabels.put("project_id", "proj1");
    expectedResourceLabels.put("location", "zone1");
    expectedResourceLabels.put("instance_id", "instance1");
    expectedResourceLabels.put("cluster_name", "cluster1");
    expectedResourceLabels.put("namespace_name", "namespace1");
    expectedResourceLabels.put("pod_name", "pod1");
    expectedResourceLabels.put("container_name", "container1");
    Resource resource = Resource.create(ContainerResource.TYPE, resourceLabels);

    StackdriverExportUtils.setResourceForBuilder(monitoredResourceBuilder, resource);

    assertThat(monitoredResourceBuilder.getType()).isNotNull();
    assertThat(monitoredResourceBuilder.getLabelsMap()).isNotEmpty();
    assertThat(monitoredResourceBuilder.getType()).isEqualTo("k8s_container");
    assertThat(monitoredResourceBuilder.getLabelsMap().size()).isEqualTo(7);
    assertThat(monitoredResourceBuilder.getLabelsMap())
        .containsExactlyEntriesIn(expectedResourceLabels);
  }

  @Test
  public void setResourceForBuilder_AwsInstanceType() {
    MonitoredResource.Builder monitoredResourceBuilder = DEFAULT_RESOURCE_WITH_PROJECT_ID.clone();
    Map<String, String> resourceLabels = new HashMap<String, String>();
    resourceLabels.put(CloudResource.REGION_KEY, "region1");
    resourceLabels.put(CloudResource.PROVIDER_KEY, CloudResource.PROVIDER_AWS);
    resourceLabels.put(CloudResource.ACCOUNT_ID_KEY, "account1");
    resourceLabels.put(HostResource.ID_KEY, "instance1");
    resourceLabels.put("extra_key", "must be ignored");
    Map<String, String> expectedResourceLabels = new HashMap<String, String>();
    expectedResourceLabels.put("project_id", "proj1");
    expectedResourceLabels.put("instance_id", "instance1");
    expectedResourceLabels.put(
        "region", StackdriverExportUtils.AWS_REGION_VALUE_PREFIX + "region1");
    expectedResourceLabels.put("aws_account", "account1");

    Resource resource = Resource.create(HostResource.TYPE, resourceLabels);

    StackdriverExportUtils.setResourceForBuilder(monitoredResourceBuilder, resource);

    assertThat(monitoredResourceBuilder.getType()).isNotNull();
    assertThat(monitoredResourceBuilder.getLabelsMap()).isNotEmpty();
    assertThat(monitoredResourceBuilder.getType()).isEqualTo("aws_ec2_instance");
    assertThat(monitoredResourceBuilder.getLabelsMap().size()).isEqualTo(4);
    assertThat(monitoredResourceBuilder.getLabelsMap())
        .containsExactlyEntriesIn(expectedResourceLabels);
  }

  @Test
  public void getDomain() {
    assertThat(StackdriverExportUtils.getDomain(null))
        .isEqualTo("custom.googleapis.com/opencensus/");
    assertThat(StackdriverExportUtils.getDomain("")).isEqualTo("custom.googleapis.com/opencensus/");
    assertThat(StackdriverExportUtils.getDomain("custom.googleapis.com/myorg/"))
        .isEqualTo("custom.googleapis.com/myorg/");
    assertThat(StackdriverExportUtils.getDomain("external.googleapis.com/prometheus/"))
        .isEqualTo("external.googleapis.com/prometheus/");
    assertThat(StackdriverExportUtils.getDomain("myorg")).isEqualTo("myorg/");
  }

  @Test
  public void getDisplayNamePrefix() {
    assertThat(StackdriverExportUtils.getDisplayNamePrefix(null)).isEqualTo("OpenCensus/");
    assertThat(StackdriverExportUtils.getDisplayNamePrefix("")).isEqualTo("");
    assertThat(StackdriverExportUtils.getDisplayNamePrefix("custom.googleapis.com/myorg/"))
        .isEqualTo("custom.googleapis.com/myorg/");
    assertThat(StackdriverExportUtils.getDisplayNamePrefix("external.googleapis.com/prometheus/"))
        .isEqualTo("external.googleapis.com/prometheus/");
    assertThat(StackdriverExportUtils.getDisplayNamePrefix("myorg")).isEqualTo("myorg/");
  }
}
