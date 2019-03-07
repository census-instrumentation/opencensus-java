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

import com.google.protobuf.DoubleValue;
import com.google.protobuf.Int64Value;
import io.opencensus.common.AttachmentValue;
import io.opencensus.common.Exemplar;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.common.Timestamp;
import io.opencensus.metrics.export.Distribution;
import io.opencensus.metrics.export.Summary;
import io.opencensus.proto.metrics.v1.DistributionValue;
import io.opencensus.proto.metrics.v1.LabelKey;
import io.opencensus.proto.metrics.v1.LabelValue;
import io.opencensus.proto.metrics.v1.Metric;
import io.opencensus.proto.metrics.v1.MetricDescriptor;
import io.opencensus.proto.metrics.v1.Point;
import io.opencensus.proto.metrics.v1.SummaryValue;
import io.opencensus.proto.metrics.v1.TimeSeries;
import io.opencensus.proto.resource.v1.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;

/** Utilities for converting Metrics APIs in OpenCensus Java to OpenCensus Metrics Proto. */
final class MetricsProtoUtils {

  // TODO(songya): determine if we should make the optimization on not sending already-existed
  // MetricDescriptors.
  static Metric toMetricProto(
      io.opencensus.metrics.export.Metric metric,
      @Nullable io.opencensus.resource.Resource resource) {
    Metric.Builder builder = Metric.newBuilder();
    builder.setMetricDescriptor(toMetricDescriptorProto(metric.getMetricDescriptor()));
    for (io.opencensus.metrics.export.TimeSeries timeSeries : metric.getTimeSeriesList()) {
      builder.addTimeseries(toTimeSeriesProto(timeSeries));
    }
    if (resource != null) {
      builder.setResource(toResourceProto(resource));
    }
    return builder.build();
  }

  private static MetricDescriptor toMetricDescriptorProto(
      io.opencensus.metrics.export.MetricDescriptor metricDescriptor) {
    MetricDescriptor.Builder builder = MetricDescriptor.newBuilder();
    builder
        .setName(metricDescriptor.getName())
        .setDescription(metricDescriptor.getDescription())
        .setUnit(metricDescriptor.getUnit())
        .setType(toTypeProto(metricDescriptor.getType()));
    for (io.opencensus.metrics.LabelKey labelKey : metricDescriptor.getLabelKeys()) {
      builder.addLabelKeys(toLabelKeyProto(labelKey));
    }
    return builder.build();
  }

  private static MetricDescriptor.Type toTypeProto(
      io.opencensus.metrics.export.MetricDescriptor.Type type) {
    switch (type) {
      case CUMULATIVE_INT64:
        return MetricDescriptor.Type.CUMULATIVE_INT64;
      case CUMULATIVE_DOUBLE:
        return MetricDescriptor.Type.CUMULATIVE_DOUBLE;
      case CUMULATIVE_DISTRIBUTION:
        return MetricDescriptor.Type.CUMULATIVE_DISTRIBUTION;
      case GAUGE_INT64:
        return MetricDescriptor.Type.GAUGE_INT64;
      case GAUGE_DOUBLE:
        return MetricDescriptor.Type.GAUGE_DOUBLE;
      case GAUGE_DISTRIBUTION:
        return MetricDescriptor.Type.GAUGE_DISTRIBUTION;
      case SUMMARY:
        return MetricDescriptor.Type.SUMMARY;
    }
    return MetricDescriptor.Type.UNRECOGNIZED;
  }

  private static LabelKey toLabelKeyProto(io.opencensus.metrics.LabelKey labelKey) {
    return LabelKey.newBuilder()
        .setKey(labelKey.getKey())
        .setDescription(labelKey.getDescription())
        .build();
  }

  private static Resource toResourceProto(io.opencensus.resource.Resource resource) {
    Resource.Builder builder = Resource.newBuilder();
    if (resource.getType() != null) {
      builder.setType(resource.getType());
    }
    builder.putAllLabels(resource.getLabels());
    return builder.build();
  }

  private static TimeSeries toTimeSeriesProto(io.opencensus.metrics.export.TimeSeries timeSeries) {
    TimeSeries.Builder builder = TimeSeries.newBuilder();
    if (timeSeries.getStartTimestamp() != null) {
      builder.setStartTimestamp(toTimestampProto(timeSeries.getStartTimestamp()));
    }
    for (io.opencensus.metrics.LabelValue labelValue : timeSeries.getLabelValues()) {
      builder.addLabelValues(toLabelValueProto(labelValue));
    }
    for (io.opencensus.metrics.export.Point point : timeSeries.getPoints()) {
      builder.addPoints(toPointProto(point));
    }
    return builder.build();
  }

  private static LabelValue toLabelValueProto(io.opencensus.metrics.LabelValue labelValue) {
    LabelValue.Builder builder = LabelValue.newBuilder();
    if (labelValue.getValue() == null) {
      builder.setHasValue(false);
    } else {
      builder.setHasValue(true).setValue(labelValue.getValue());
    }
    return builder.build();
  }

  private static Point toPointProto(io.opencensus.metrics.export.Point point) {
    final Point.Builder builder = Point.newBuilder();
    builder.setTimestamp(toTimestampProto(point.getTimestamp()));
    point
        .getValue()
        .match(
            new Function<Double, Void>() {
              @Override
              public Void apply(Double arg) {
                builder.setDoubleValue(arg);
                return null;
              }
            },
            new Function<Long, Void>() {
              @Override
              public Void apply(Long arg) {
                builder.setInt64Value(arg);
                return null;
              }
            },
            new Function<Distribution, Void>() {
              @Override
              public Void apply(Distribution arg) {
                builder.setDistributionValue(toDistributionProto(arg));
                return null;
              }
            },
            new Function<Summary, Void>() {
              @Override
              public Void apply(Summary arg) {
                builder.setSummaryValue(toSummaryProto(arg));
                return null;
              }
            },
            Functions.<Void>throwAssertionError());
    return builder.build();
  }

  private static DistributionValue toDistributionProto(
      io.opencensus.metrics.export.Distribution distribution) {
    DistributionValue.Builder builder = DistributionValue.newBuilder();
    builder
        .setSum(distribution.getSum())
        .setCount(distribution.getCount())
        .setSumOfSquaredDeviation(distribution.getSumOfSquaredDeviations());
    if (distribution.getBucketOptions() != null) {
      builder.setBucketOptions(toBucketOptionsProto(distribution.getBucketOptions()));
    }
    for (io.opencensus.metrics.export.Distribution.Bucket bucket : distribution.getBuckets()) {
      builder.addBuckets(toBucketProto(bucket));
    }
    return builder.build();
  }

  // TODO(songya): determine if we should make the optimization on not sending already-existed
  // BucketOptions.
  private static DistributionValue.BucketOptions toBucketOptionsProto(
      Distribution.BucketOptions bucketOptions) {
    final DistributionValue.BucketOptions.Builder builder =
        DistributionValue.BucketOptions.newBuilder();
    bucketOptions.match(
        new Function<Distribution.BucketOptions.ExplicitOptions, Void>() {
          @Override
          public Void apply(Distribution.BucketOptions.ExplicitOptions arg) {
            builder.setExplicit(
                DistributionValue.BucketOptions.Explicit.newBuilder()
                    .addAllBounds(arg.getBucketBoundaries())
                    .build());
            return null;
          }
        },
        Functions.<Void>throwAssertionError());
    return builder.build();
  }

  private static DistributionValue.Bucket toBucketProto(
      io.opencensus.metrics.export.Distribution.Bucket bucket) {
    DistributionValue.Bucket.Builder builder =
        DistributionValue.Bucket.newBuilder().setCount(bucket.getCount());
    Exemplar exemplar = bucket.getExemplar();
    if (exemplar != null) {
      builder.setExemplar(toExemplarProto(exemplar));
    }
    return builder.build();
  }

  private static DistributionValue.Exemplar toExemplarProto(Exemplar exemplar) {
    Map<String, String> stringAttachments = new HashMap<>();
    for (Entry<String, AttachmentValue> entry : exemplar.getAttachments().entrySet()) {
      stringAttachments.put(entry.getKey(), entry.getValue().getValue());
    }
    return DistributionValue.Exemplar.newBuilder()
        .setValue(exemplar.getValue())
        .setTimestamp(toTimestampProto(exemplar.getTimestamp()))
        .putAllAttachments(stringAttachments)
        .build();
  }

  private static SummaryValue toSummaryProto(io.opencensus.metrics.export.Summary summary) {
    SummaryValue.Builder builder = SummaryValue.newBuilder();
    if (summary.getSum() != null) {
      builder.setSum(DoubleValue.of(summary.getSum()));
    }
    if (summary.getCount() != null) {
      builder.setCount(Int64Value.of(summary.getCount()));
    }
    builder.setSnapshot(toSnapshotProto(summary.getSnapshot()));
    return builder.build();
  }

  private static SummaryValue.Snapshot toSnapshotProto(
      io.opencensus.metrics.export.Summary.Snapshot snapshot) {
    SummaryValue.Snapshot.Builder builder = SummaryValue.Snapshot.newBuilder();
    if (snapshot.getSum() != null) {
      builder.setSum(DoubleValue.of(snapshot.getSum()));
    }
    if (snapshot.getCount() != null) {
      builder.setCount(Int64Value.of(snapshot.getCount()));
    }
    for (io.opencensus.metrics.export.Summary.Snapshot.ValueAtPercentile valueAtPercentile :
        snapshot.getValueAtPercentiles()) {
      builder.addPercentileValues(
          SummaryValue.Snapshot.ValueAtPercentile.newBuilder()
              .setValue(valueAtPercentile.getValue())
              .setPercentile(valueAtPercentile.getPercentile())
              .build());
    }
    return builder.build();
  }

  static com.google.protobuf.Timestamp toTimestampProto(Timestamp timestamp) {
    return com.google.protobuf.Timestamp.newBuilder()
        .setSeconds(timestamp.getSeconds())
        .setNanos(timestamp.getNanos())
        .build();
  }

  private MetricsProtoUtils() {}
}
