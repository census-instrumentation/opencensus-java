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
import static io.opencensus.exporter.stats.stackdriver.StackdriverExporterWorker.CUSTOM_OPENCENSUS_DOMAIN;
import static io.opencensus.exporter.stats.stackdriver.StackdriverExporterWorker.DEFAULT_DISPLAY_NAME_PREFIX;

import com.google.api.Distribution.BucketOptions;
import com.google.api.Distribution.BucketOptions.Explicit;
import com.google.api.LabelDescriptor;
import com.google.api.LabelDescriptor.ValueType;
import com.google.api.Metric;
import com.google.api.MetricDescriptor;
import com.google.api.MetricDescriptor.MetricKind;
import com.google.api.MonitoredResource;
import com.google.monitoring.v3.TimeInterval;
import com.google.monitoring.v3.TimeSeries;
import com.google.monitoring.v3.TypedValue;
import io.opencensus.common.Timestamp;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.export.Distribution.Bucket;
import io.opencensus.metrics.export.MetricDescriptor.Type;
import io.opencensus.metrics.export.Point;
import io.opencensus.metrics.export.Summary;
import io.opencensus.metrics.export.Summary.Snapshot;
import io.opencensus.metrics.export.Summary.Snapshot.ValueAtPercentile;
import io.opencensus.metrics.export.Value;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link StackdriverExportUtils}. */
@RunWith(JUnit4.class)
public class StackdriverExportUtilsTest {
  @Rule public final ExpectedException thrown = ExpectedException.none();

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
  private static final String DEFAULT_TASK_VALUE =
      "java-" + ManagementFactory.getRuntimeMXBean().getName();

  private static final io.opencensus.metrics.export.Distribution DISTRIBUTION =
      io.opencensus.metrics.export.Distribution.create(
          3,
          2,
          14,
          BUCKET_OPTIONS,
          Arrays.asList(Bucket.create(3), Bucket.create(1), Bucket.create(2), Bucket.create(4)));
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
  public void createMetric_throwWhenTagKeysAndValuesHaveDifferentSize() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("LabelKeys and LabelValues don't have same size.");
    StackdriverExportUtils.createMetric(
        METRIC_DESCRIPTOR, EMPTY_LABEL_VALUE, CUSTOM_OPENCENSUS_DOMAIN);
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
                    Explicit.newBuilder().addAllBounds(Arrays.asList(1.0, 3.0, 5.0)))
                .build());
  }

  @Test
  public void createBucketOptions_Null() {
    assertThat(StackdriverExportUtils.createBucketOptions(null))
        .isEqualTo(BucketOptions.newBuilder().build());
  }

  @Test
  public void createDistribution() {
    assertThat(StackdriverExportUtils.createDistribution(DISTRIBUTION))
        .isEqualTo(
            com.google.api.Distribution.newBuilder()
                .setCount(3)
                .setBucketOptions(StackdriverExportUtils.createBucketOptions(BUCKET_OPTIONS))
                .addAllBucketCounts(Arrays.asList(3L, 1L, 2L, 4L))
                .setSumOfSquaredDeviation(14)
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
  public void createMetricDescriptor_NotNull() {
    assertThat(
            StackdriverExportUtils.createMetricDescriptor(
                METRIC_DESCRIPTOR,
                PROJECT_ID,
                CUSTOM_OPENCENSUS_DOMAIN,
                DEFAULT_DISPLAY_NAME_PREFIX))
        .isNotNull();
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
            METRIC, DEFAULT_RESOURCE, CUSTOM_OPENCENSUS_DOMAIN);
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
  public void createTimeSeriesList_Gauge() {
    io.opencensus.metrics.export.Metric metric =
        io.opencensus.metrics.export.Metric.create(
            GAUGE_METRIC_DESCRIPTOR, Arrays.asList(GAUGE_TIME_SERIES, GAUGE_TIME_SERIES_2));

    List<TimeSeries> timeSeriesList =
        StackdriverExportUtils.createTimeSeriesList(
            metric, DEFAULT_RESOURCE, CUSTOM_OPENCENSUS_DOMAIN);
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
        StackdriverExportUtils.createTimeSeriesList(METRIC, resource, CUSTOM_OPENCENSUS_DOMAIN);
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
}
