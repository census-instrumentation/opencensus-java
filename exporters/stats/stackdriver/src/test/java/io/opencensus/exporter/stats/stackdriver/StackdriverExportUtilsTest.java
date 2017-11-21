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

import com.google.api.Distribution.BucketOptions;
import com.google.api.Distribution.BucketOptions.Explicit;
import com.google.api.LabelDescriptor;
import com.google.api.LabelDescriptor.ValueType;
import com.google.api.Metric;
import com.google.api.MetricDescriptor;
import com.google.api.MetricDescriptor.MetricKind;
import com.google.api.MonitoredResource;
import com.google.common.collect.ImmutableMap;
import com.google.monitoring.v3.Point;
import com.google.monitoring.v3.TimeInterval;
import com.google.monitoring.v3.TimeSeries;
import com.google.monitoring.v3.TypedValue;
import io.opencensus.common.Duration;
import io.opencensus.common.Timestamp;
import io.opencensus.stats.Aggregation.Count;
import io.opencensus.stats.Aggregation.Distribution;
import io.opencensus.stats.Aggregation.Mean;
import io.opencensus.stats.Aggregation.Sum;
import io.opencensus.stats.AggregationData.CountData;
import io.opencensus.stats.AggregationData.DistributionData;
import io.opencensus.stats.AggregationData.MeanData;
import io.opencensus.stats.AggregationData.SumDataDouble;
import io.opencensus.stats.AggregationData.SumDataLong;
import io.opencensus.stats.BucketBoundaries;
import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.stats.Measure.MeasureLong;
import io.opencensus.stats.View;
import io.opencensus.stats.View.AggregationWindow.Cumulative;
import io.opencensus.stats.View.AggregationWindow.Interval;
import io.opencensus.stats.View.Name;
import io.opencensus.stats.ViewData;
import io.opencensus.stats.ViewData.AggregationWindowData.CumulativeData;
import io.opencensus.stats.ViewData.AggregationWindowData.IntervalData;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link StackdriverExportUtils}. */
@RunWith(JUnit4.class)
public class StackdriverExportUtilsTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  private static final TagKey KEY = TagKey.create("KEY");
  private static final TagValue VALUE_1 = TagValue.create("VALUE1");
  private static final TagValue VALUE_2 = TagValue.create("VALUE2");
  private static final String MEASURE_UNIT = "us";
  private static final String MEASURE_DESCRIPTION = "measure description";
  private static final MeasureDouble MEASURE_DOUBLE =
      MeasureDouble.create("measure1", MEASURE_DESCRIPTION, MEASURE_UNIT);
  private static final MeasureLong MEASURE_LONG =
      MeasureLong.create("measure2", MEASURE_DESCRIPTION, MEASURE_UNIT);
  private static final String VIEW_NAME = "view";
  private static final String VIEW_DESCRIPTION = "view description";
  private static final Duration TEN_SECONDS = Duration.create(10, 0);
  private static final Cumulative CUMULATIVE = Cumulative.create();
  private static final Interval INTERVAL = Interval.create(TEN_SECONDS);
  private static final BucketBoundaries BUCKET_BOUNDARIES =
      BucketBoundaries.create(Arrays.asList(0.0, 1.0, 3.0, 5.0));
  private static final Sum SUM = Sum.create();
  private static final Count COUNT = Count.create();
  private static final Mean MEAN = Mean.create();
  private static final Distribution DISTRIBUTION = Distribution.create(BUCKET_BOUNDARIES);
  private static final String PROJECT_ID = "id";

  @Test
  public void testConstant() {
    assertThat(StackdriverExportUtils.LABEL_DESCRIPTION).isEqualTo("OpenCensus TagKey");
  }

  @Test
  public void createLabelDescriptor() {
    assertThat(StackdriverExportUtils.createLabelDescriptor(TagKey.create("string")))
        .isEqualTo(
            LabelDescriptor.newBuilder()
                .setKey("string")
                .setDescription(StackdriverExportUtils.LABEL_DESCRIPTION)
                .setValueType(ValueType.STRING)
                .build());
  }

  @Test
  public void createMetricKind() {
    assertThat(StackdriverExportUtils.createMetricKind(CUMULATIVE))
        .isEqualTo(MetricKind.CUMULATIVE);
    assertThat(StackdriverExportUtils.createMetricKind(INTERVAL))
        .isEqualTo(MetricKind.UNRECOGNIZED);
  }

  @Test
  public void createValueType() {
    assertThat(StackdriverExportUtils.createValueType(SUM, MEASURE_DOUBLE))
        .isEqualTo(MetricDescriptor.ValueType.DOUBLE);
    assertThat(StackdriverExportUtils.createValueType(SUM, MEASURE_LONG))
        .isEqualTo(MetricDescriptor.ValueType.INT64);
    assertThat(StackdriverExportUtils.createValueType(COUNT, MEASURE_DOUBLE))
        .isEqualTo(MetricDescriptor.ValueType.INT64);
    assertThat(StackdriverExportUtils.createValueType(COUNT, MEASURE_LONG))
        .isEqualTo(MetricDescriptor.ValueType.INT64);
    assertThat(StackdriverExportUtils.createValueType(MEAN, MEASURE_DOUBLE))
        .isEqualTo(MetricDescriptor.ValueType.DOUBLE);
    assertThat(StackdriverExportUtils.createValueType(MEAN, MEASURE_LONG))
        .isEqualTo(MetricDescriptor.ValueType.DOUBLE);
    assertThat(StackdriverExportUtils.createValueType(DISTRIBUTION, MEASURE_DOUBLE))
        .isEqualTo(MetricDescriptor.ValueType.DISTRIBUTION);
    assertThat(StackdriverExportUtils.createValueType(DISTRIBUTION, MEASURE_LONG))
        .isEqualTo(MetricDescriptor.ValueType.DISTRIBUTION);
  }

  @Test
  public void createMetric() {
    View view =
        View.create(
            Name.create(VIEW_NAME),
            VIEW_DESCRIPTION,
            MEASURE_DOUBLE,
            DISTRIBUTION,
            Arrays.asList(KEY),
            CUMULATIVE);
    assertThat(StackdriverExportUtils.createMetric(view, Arrays.asList(VALUE_1)))
        .isEqualTo(
            Metric.newBuilder()
                .setType("custom.googleapis.com/opencensus/" + VIEW_NAME)
                .putLabels("KEY", "VALUE1")
                .build());
  }

  @Test
  public void convertTimestamp() {
    Timestamp censusTimestamp1 = Timestamp.create(100, 3000);
    assertThat(StackdriverExportUtils.convertTimestamp(censusTimestamp1))
        .isEqualTo(
            com.google.protobuf.Timestamp.newBuilder().setSeconds(100).setNanos(3000).build());

    // Proto timestamp doesn't allow negative values, instead it will replace the negative values
    // by returning a default instance.
    Timestamp censusTimestamp2 = Timestamp.create(-100, -3000);
    assertThat(StackdriverExportUtils.convertTimestamp(censusTimestamp2))
        .isEqualTo(com.google.protobuf.Timestamp.newBuilder().build());
  }

  @Test
  public void createTimeInterval_cumulative() {
    Timestamp censusTimestamp1 = Timestamp.create(100, 3000);
    Timestamp censusTimestamp2 = Timestamp.create(200, 0);
    assertThat(
            StackdriverExportUtils.createTimeInterval(
                CumulativeData.create(censusTimestamp1, censusTimestamp2)))
        .isEqualTo(
            TimeInterval.newBuilder()
                .setStartTime(StackdriverExportUtils.convertTimestamp(censusTimestamp1))
                .setEndTime(StackdriverExportUtils.convertTimestamp(censusTimestamp2))
                .build());
  }

  @Test
  public void createTimeInterval_interval() {
    IntervalData intervalData = IntervalData.create(Timestamp.create(200, 0));
    // Only Cumulative view will supported in this version.
    thrown.expect(IllegalArgumentException.class);
    StackdriverExportUtils.createTimeInterval(intervalData);
  }

  @Test
  public void createBucketOptions() {
    assertThat(StackdriverExportUtils.createBucketOptions(BUCKET_BOUNDARIES))
        .isEqualTo(
            BucketOptions.newBuilder()
                .setExplicitBuckets(
                    Explicit.newBuilder().addAllBounds(Arrays.asList(0.0, 1.0, 3.0, 5.0)))
                .build());
  }

  @Test
  public void createDistribution() {
    DistributionData distributionData =
        DistributionData.create(2, 3, 0, 5, 14, Arrays.asList(0L, 1L, 1L, 0L, 1L));
    assertThat(StackdriverExportUtils.createDistribution(distributionData, BUCKET_BOUNDARIES))
        .isEqualTo(
            com.google.api.Distribution.newBuilder()
                .setMean(2)
                .setCount(3)
                .setRange(
                    com.google.api.Distribution.Range.newBuilder().setMin(0).setMax(5).build())
                .setBucketOptions(StackdriverExportUtils.createBucketOptions(BUCKET_BOUNDARIES))
                .addAllBucketCounts(Arrays.asList(0L, 1L, 1L, 0L, 1L))
                .setSumOfSquaredDeviation(14)
                .build());
  }

  @Test
  public void createTypedValue() {
    assertThat(StackdriverExportUtils.createTypedValue(SUM, SumDataDouble.create(1.1)))
        .isEqualTo(TypedValue.newBuilder().setDoubleValue(1.1).build());
    assertThat(StackdriverExportUtils.createTypedValue(SUM, SumDataLong.create(10000)))
        .isEqualTo(TypedValue.newBuilder().setInt64Value(10000).build());
    assertThat(StackdriverExportUtils.createTypedValue(COUNT, CountData.create(55)))
        .isEqualTo(TypedValue.newBuilder().setInt64Value(55).build());
    assertThat(StackdriverExportUtils.createTypedValue(MEAN, MeanData.create(7.7, 8)))
        .isEqualTo(TypedValue.newBuilder().setDoubleValue(7.7).build());
    DistributionData distributionData =
        DistributionData.create(2, 3, 0, 5, 14, Arrays.asList(0L, 1L, 1L, 0L, 1L));
    assertThat(StackdriverExportUtils.createTypedValue(DISTRIBUTION, distributionData))
        .isEqualTo(
            TypedValue.newBuilder()
                .setDistributionValue(
                    StackdriverExportUtils.createDistribution(distributionData, BUCKET_BOUNDARIES))
                .build());
  }

  @Test
  public void createPoint_cumulative() {
    Timestamp censusTimestamp1 = Timestamp.create(100, 3000);
    Timestamp censusTimestamp2 = Timestamp.create(200, 0);
    CumulativeData cumulativeData = CumulativeData.create(censusTimestamp1, censusTimestamp2);
    SumDataDouble sumDataDouble = SumDataDouble.create(33.3);

    assertThat(StackdriverExportUtils.createPoint(sumDataDouble, cumulativeData, SUM))
        .isEqualTo(
            Point.newBuilder()
                .setInterval(StackdriverExportUtils.createTimeInterval(cumulativeData))
                .setValue(StackdriverExportUtils.createTypedValue(SUM, sumDataDouble))
                .build());
  }

  @Test
  public void createPoint_interval() {
    IntervalData intervalData = IntervalData.create(Timestamp.create(200, 0));
    SumDataDouble sumDataDouble = SumDataDouble.create(33.3);
    // Only Cumulative view will supported in this version.
    thrown.expect(IllegalArgumentException.class);
    StackdriverExportUtils.createPoint(sumDataDouble, intervalData, SUM);
  }

  @Test
  public void createMetricDescriptor_cumulative() {
    View view =
        View.create(
            Name.create(VIEW_NAME),
            VIEW_DESCRIPTION,
            MEASURE_DOUBLE,
            DISTRIBUTION,
            Arrays.asList(KEY),
            CUMULATIVE);
    MetricDescriptor metricDescriptor =
        StackdriverExportUtils.createMetricDescriptor(view, PROJECT_ID);
    assertThat(metricDescriptor.getName())
        .isEqualTo(
            "projects/"
                + PROJECT_ID
                + "/metricDescriptors/custom.googleapis.com/opencensus/"
                + VIEW_NAME);
    assertThat(metricDescriptor.getDescription()).isEqualTo(VIEW_DESCRIPTION);
    assertThat(metricDescriptor.getDisplayName()).isEqualTo("OpenCensus/" + VIEW_NAME);
    assertThat(metricDescriptor.getType())
        .isEqualTo("custom.googleapis.com/opencensus/" + VIEW_NAME);
    assertThat(metricDescriptor.getUnit()).isEqualTo(MEASURE_UNIT);
    assertThat(metricDescriptor.getMetricKind()).isEqualTo(MetricKind.CUMULATIVE);
    assertThat(metricDescriptor.getValueType()).isEqualTo(MetricDescriptor.ValueType.DISTRIBUTION);
    assertThat(metricDescriptor.getLabelsList())
        .containsExactly(
            LabelDescriptor.newBuilder()
                .setKey(KEY.getName())
                .setDescription(StackdriverExportUtils.LABEL_DESCRIPTION)
                .setValueType(ValueType.STRING)
                .build());
  }

  @Test
  public void createMetricDescriptor_interval() {
    View view =
        View.create(
            Name.create(VIEW_NAME),
            VIEW_DESCRIPTION,
            MEASURE_DOUBLE,
            DISTRIBUTION,
            Arrays.asList(KEY),
            INTERVAL);
    assertThat(StackdriverExportUtils.createMetricDescriptor(view, PROJECT_ID)).isNull();
  }

  @Test
  public void createTimeSeriesList_cumulative() {
    View view =
        View.create(
            Name.create(VIEW_NAME),
            VIEW_DESCRIPTION,
            MEASURE_DOUBLE,
            DISTRIBUTION,
            Arrays.asList(KEY),
            CUMULATIVE);
    DistributionData distributionData1 =
        DistributionData.create(2, 3, 0, 5, 14, Arrays.asList(0L, 1L, 1L, 0L, 1L));
    DistributionData distributionData2 =
        DistributionData.create(-1, 1, -1, -1, 0, Arrays.asList(1L, 0L, 0L, 0L, 0L));
    Map<List<TagValue>, DistributionData> aggregationMap =
        ImmutableMap.of(
            Arrays.asList(VALUE_1), distributionData1, Arrays.asList(VALUE_2), distributionData2);
    CumulativeData cumulativeData =
        CumulativeData.create(Timestamp.fromMillis(1000), Timestamp.fromMillis(2000));
    ViewData viewData = ViewData.create(view, aggregationMap, cumulativeData);
    List<TimeSeries> timeSeriesList =
        StackdriverExportUtils.createTimeSeriesList(viewData, PROJECT_ID);
    assertThat(timeSeriesList).hasSize(2);
    TimeSeries expected1 =
        TimeSeries.newBuilder()
            .setMetricKind(MetricKind.CUMULATIVE)
            .setValueType(MetricDescriptor.ValueType.DISTRIBUTION)
            .setMetric(StackdriverExportUtils.createMetric(view, Arrays.asList(VALUE_1)))
            .setResource(
                MonitoredResource.newBuilder()
                    .setType("global")
                    .putLabels("project_id", PROJECT_ID))
            .addPoints(
                StackdriverExportUtils.createPoint(distributionData1, cumulativeData, DISTRIBUTION))
            .build();
    TimeSeries expected2 =
        TimeSeries.newBuilder()
            .setMetricKind(MetricKind.CUMULATIVE)
            .setValueType(MetricDescriptor.ValueType.DISTRIBUTION)
            .setMetric(StackdriverExportUtils.createMetric(view, Arrays.asList(VALUE_2)))
            .setResource(
                MonitoredResource.newBuilder()
                    .setType("global")
                    .putLabels("project_id", PROJECT_ID))
            .addPoints(
                StackdriverExportUtils.createPoint(distributionData2, cumulativeData, DISTRIBUTION))
            .build();
    assertThat(timeSeriesList).containsExactly(expected1, expected2);
  }

  @Test
  public void createTimeSeriesList_interval() {
    View view =
        View.create(
            Name.create(VIEW_NAME),
            VIEW_DESCRIPTION,
            MEASURE_DOUBLE,
            DISTRIBUTION,
            Arrays.asList(KEY),
            INTERVAL);
    Map<List<TagValue>, DistributionData> aggregationMap =
        ImmutableMap.of(
            Arrays.asList(VALUE_1),
            DistributionData.create(2, 3, 0, 5, 14, Arrays.asList(0L, 1L, 1L, 0L, 1L)),
            Arrays.asList(VALUE_2),
            DistributionData.create(-1, 1, -1, -1, 0, Arrays.asList(1L, 0L, 0L, 0L, 0L)));
    ViewData viewData =
        ViewData.create(view, aggregationMap, IntervalData.create(Timestamp.fromMillis(2000)));
    assertThat(StackdriverExportUtils.createTimeSeriesList(viewData, PROJECT_ID)).isEmpty();
  }
}
