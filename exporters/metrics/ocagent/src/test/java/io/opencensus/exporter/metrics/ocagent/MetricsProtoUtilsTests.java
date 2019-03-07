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

package io.opencensus.exporter.metrics.ocagent;

import static com.google.common.truth.Truth.assertThat;

import com.google.protobuf.DoubleValue;
import com.google.protobuf.Int64Value;
import io.opencensus.common.AttachmentValue;
import io.opencensus.common.Exemplar;
import io.opencensus.common.Timestamp;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.export.Distribution;
import io.opencensus.metrics.export.Distribution.Bucket;
import io.opencensus.metrics.export.Distribution.BucketOptions;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricDescriptor;
import io.opencensus.metrics.export.MetricDescriptor.Type;
import io.opencensus.metrics.export.Point;
import io.opencensus.metrics.export.Summary;
import io.opencensus.metrics.export.Summary.Snapshot;
import io.opencensus.metrics.export.Summary.Snapshot.ValueAtPercentile;
import io.opencensus.metrics.export.TimeSeries;
import io.opencensus.metrics.export.Value;
import io.opencensus.proto.metrics.v1.DistributionValue;
import io.opencensus.proto.metrics.v1.DistributionValue.BucketOptions.Explicit;
import io.opencensus.proto.metrics.v1.SummaryValue;
import io.opencensus.resource.Resource;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link MetricsProtoUtils}. */
@RunWith(JUnit4.class)
public class MetricsProtoUtilsTests {

  private static final LabelKey KEY_1 = LabelKey.create("key1", "");
  private static final LabelKey KEY_2 = LabelKey.create("key2", "");
  private static final LabelValue VALUE_1 = LabelValue.create("value1");
  private static final LabelValue VALUE_2 = LabelValue.create("value2");
  private static final LabelValue VALUE_NULL = LabelValue.create(null);
  private static final String UNIT = "ms";
  private static final String METRIC_NAME_1 = "metric1";
  private static final String METRIC_NAME_2 = "metric2";
  private static final String METRIC_DESCRIPTION = "description";
  private static final MetricDescriptor DESCRIPTOR_1 =
      MetricDescriptor.create(
          METRIC_NAME_1,
          METRIC_DESCRIPTION,
          UNIT,
          Type.CUMULATIVE_DISTRIBUTION,
          Collections.<LabelKey>singletonList(KEY_1));
  private static final MetricDescriptor DESCRIPTOR_2 =
      MetricDescriptor.create(
          METRIC_NAME_2,
          METRIC_DESCRIPTION,
          UNIT,
          Type.SUMMARY,
          Arrays.<LabelKey>asList(KEY_1, KEY_2));
  private static final Timestamp TIMESTAMP_1 = Timestamp.create(10, 0);
  private static final Timestamp TIMESTAMP_2 = Timestamp.create(25, 0);
  private static final Timestamp TIMESTAMP_3 = Timestamp.create(30, 0);
  private static final Timestamp TIMESTAMP_4 = Timestamp.create(50, 0);
  private static final Timestamp TIMESTAMP_5 = Timestamp.create(100, 0);
  private static final Distribution DISTRIBUTION =
      Distribution.create(
          5,
          24.0,
          321.5,
          BucketOptions.explicitOptions(Arrays.<Double>asList(5.0, 10.0, 15.0)),
          Arrays.<Bucket>asList(
              Bucket.create(2),
              Bucket.create(1),
              Bucket.create(
                  2,
                  Exemplar.create(
                      11, TIMESTAMP_1, Collections.<String, AttachmentValue>emptyMap())),
              Bucket.create(0)));
  private static final Summary SUMMARY =
      Summary.create(
          5L,
          45.0,
          Snapshot.create(
              3L, 25.0, Arrays.<ValueAtPercentile>asList(ValueAtPercentile.create(0.4, 11))));
  private static final Point POINT_DISTRIBUTION =
      Point.create(Value.distributionValue(DISTRIBUTION), TIMESTAMP_2);
  private static final Point POINT_SUMMARY = Point.create(Value.summaryValue(SUMMARY), TIMESTAMP_3);
  private static final TimeSeries TIME_SERIES_1 =
      TimeSeries.createWithOnePoint(
          Collections.<LabelValue>singletonList(VALUE_1), POINT_DISTRIBUTION, TIMESTAMP_4);
  private static final TimeSeries TIME_SERIES_2 =
      TimeSeries.createWithOnePoint(
          Arrays.<LabelValue>asList(VALUE_2, VALUE_NULL), POINT_SUMMARY, TIMESTAMP_5);
  private static final Resource RESOURCE =
      Resource.create("env", Collections.<String, String>singletonMap("env_key", "env_val"));

  @Test
  public void toMetricProto_Distribution() {
    Metric metric = Metric.create(DESCRIPTOR_1, Collections.singletonList(TIME_SERIES_1));
    io.opencensus.proto.metrics.v1.Metric expected =
        io.opencensus.proto.metrics.v1.Metric.newBuilder()
            .setMetricDescriptor(
                io.opencensus.proto.metrics.v1.MetricDescriptor.newBuilder()
                    .setName(METRIC_NAME_1)
                    .setDescription(METRIC_DESCRIPTION)
                    .setUnit(UNIT)
                    .setType(
                        io.opencensus.proto.metrics.v1.MetricDescriptor.Type
                            .CUMULATIVE_DISTRIBUTION)
                    .addLabelKeys(
                        io.opencensus.proto.metrics.v1.LabelKey.newBuilder()
                            .setKey(KEY_1.getKey())
                            .setDescription(KEY_1.getDescription())
                            .build())
                    .build())
            .setResource(
                io.opencensus.proto.resource.v1.Resource.newBuilder()
                    .setType(RESOURCE.getType())
                    .putAllLabels(RESOURCE.getLabels())
                    .build())
            .addTimeseries(
                io.opencensus.proto.metrics.v1.TimeSeries.newBuilder()
                    .setStartTimestamp(MetricsProtoUtils.toTimestampProto(TIMESTAMP_4))
                    .addLabelValues(
                        io.opencensus.proto.metrics.v1.LabelValue.newBuilder()
                            .setHasValue(true)
                            .setValue(VALUE_1.getValue())
                            .build())
                    .addPoints(
                        io.opencensus.proto.metrics.v1.Point.newBuilder()
                            .setTimestamp(MetricsProtoUtils.toTimestampProto(TIMESTAMP_2))
                            .setDistributionValue(
                                DistributionValue.newBuilder()
                                    .setCount(5)
                                    .setSum(24.0)
                                    .setSumOfSquaredDeviation(321.5)
                                    .setBucketOptions(
                                        DistributionValue.BucketOptions.newBuilder()
                                            .setExplicit(
                                                Explicit.newBuilder()
                                                    .addAllBounds(
                                                        Arrays.<Double>asList(5.0, 10.0, 15.0))
                                                    .build())
                                            .build())
                                    .addBuckets(
                                        DistributionValue.Bucket.newBuilder().setCount(2).build())
                                    .addBuckets(
                                        DistributionValue.Bucket.newBuilder().setCount(1).build())
                                    .addBuckets(
                                        DistributionValue.Bucket.newBuilder()
                                            .setCount(2)
                                            .setExemplar(
                                                DistributionValue.Exemplar.newBuilder()
                                                    .setTimestamp(
                                                        MetricsProtoUtils.toTimestampProto(
                                                            TIMESTAMP_1))
                                                    .setValue(11)
                                                    .build())
                                            .build())
                                    .addBuckets(
                                        DistributionValue.Bucket.newBuilder().setCount(0).build())
                                    .build())
                            .build())
                    .build())
            .build();
    io.opencensus.proto.metrics.v1.Metric actual =
        MetricsProtoUtils.toMetricProto(metric, RESOURCE);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void toMetricProto_Summary() {
    Metric metric = Metric.create(DESCRIPTOR_2, Collections.singletonList(TIME_SERIES_2));
    io.opencensus.proto.metrics.v1.Metric expected =
        io.opencensus.proto.metrics.v1.Metric.newBuilder()
            .setMetricDescriptor(
                io.opencensus.proto.metrics.v1.MetricDescriptor.newBuilder()
                    .setName(METRIC_NAME_2)
                    .setDescription(METRIC_DESCRIPTION)
                    .setUnit(UNIT)
                    .setType(io.opencensus.proto.metrics.v1.MetricDescriptor.Type.SUMMARY)
                    .addLabelKeys(
                        io.opencensus.proto.metrics.v1.LabelKey.newBuilder()
                            .setKey(KEY_1.getKey())
                            .setDescription(KEY_1.getDescription())
                            .build())
                    .addLabelKeys(
                        io.opencensus.proto.metrics.v1.LabelKey.newBuilder()
                            .setKey(KEY_2.getKey())
                            .setDescription(KEY_2.getDescription())
                            .build())
                    .build())
            .addTimeseries(
                io.opencensus.proto.metrics.v1.TimeSeries.newBuilder()
                    .setStartTimestamp(MetricsProtoUtils.toTimestampProto(TIMESTAMP_5))
                    .addLabelValues(
                        io.opencensus.proto.metrics.v1.LabelValue.newBuilder()
                            .setHasValue(true)
                            .setValue(VALUE_2.getValue())
                            .build())
                    .addLabelValues(
                        io.opencensus.proto.metrics.v1.LabelValue.newBuilder()
                            .setHasValue(false)
                            .build())
                    .addPoints(
                        io.opencensus.proto.metrics.v1.Point.newBuilder()
                            .setTimestamp(MetricsProtoUtils.toTimestampProto(TIMESTAMP_3))
                            .setSummaryValue(
                                SummaryValue.newBuilder()
                                    .setCount(Int64Value.of(5))
                                    .setSum(DoubleValue.of(45.0))
                                    .setSnapshot(
                                        SummaryValue.Snapshot.newBuilder()
                                            .setCount(Int64Value.of(3))
                                            .setSum(DoubleValue.of(25.0))
                                            .addPercentileValues(
                                                SummaryValue.Snapshot.ValueAtPercentile.newBuilder()
                                                    .setValue(11)
                                                    .setPercentile(0.4)
                                                    .build())
                                            .build())
                                    .build())
                            .build())
                    .build())
            .build();
    io.opencensus.proto.metrics.v1.Metric actual = MetricsProtoUtils.toMetricProto(metric, null);
    assertThat(actual).isEqualTo(expected);
  }
}
