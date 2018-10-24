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

package io.opencensus.exporter.stats.signalfx;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.signalfx.metrics.protobuf.SignalFxProtocolBuffers.DataPoint;
import com.signalfx.metrics.protobuf.SignalFxProtocolBuffers.Datum;
import com.signalfx.metrics.protobuf.SignalFxProtocolBuffers.Dimension;
import com.signalfx.metrics.protobuf.SignalFxProtocolBuffers.MetricType;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.export.Distribution;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricDescriptor;
import io.opencensus.metrics.export.MetricDescriptor.Type;
import io.opencensus.metrics.export.Point;
import io.opencensus.metrics.export.Summary;
import io.opencensus.metrics.export.TimeSeries;
import io.opencensus.metrics.export.Value;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Adapter for a {@code Metric}'s contents into SignalFx datapoints. */
final class SignalFxSessionAdaptor {

  // Constant functions for Datum.
  private static final Function<Double, Datum> datumDoubleFunction =
      new Function<Double, Datum>() {
        @Override
        public Datum apply(Double arg) {
          Datum.Builder builder = Datum.newBuilder();
          builder.setDoubleValue(arg);
          return builder.build();
        }
      };
  private static final Function<Long, Datum> datumLongFunction =
      new Function<Long, Datum>() {
        @Override
        public Datum apply(Long arg) {
          Datum.Builder builder = Datum.newBuilder();
          builder.setIntValue(arg);
          return builder.build();
        }
      };
  private static final Function<Distribution, Datum> datumDistributionFunction =
      new Function<Distribution, Datum>() {
        @Override
        public Datum apply(Distribution arg) {
          // Signal doesn't handle Distribution value.
          // TODO(mayurkale): decide what to do with Distribution value.
          throw new IllegalArgumentException("Distribution type are not supported");
        }
      };
  private static final Function<Summary, Datum> datumSummaryFunction =
      new Function<Summary, Datum>() {
        @Override
        public Datum apply(Summary arg) {
          // Signal doesn't handle Summary value.
          // TODO(mayurkale): decide what to do with Summary value.
          throw new IllegalArgumentException("Summary type are not supported");
        }
      };

  private SignalFxSessionAdaptor() {}

  /**
   * Converts the given Metric into datapoints that can be sent to SignalFx.
   *
   * @param metric The {@link Metric} containing the timeseries of each combination of label values.
   * @return A list of datapoints for the corresponding metric timeseries of this metric.
   */
  static List<DataPoint> adapt(Metric metric) {
    MetricDescriptor metricDescriptor = metric.getMetricDescriptor();
    MetricType metricType = getType(metricDescriptor.getType());
    if (metricType == null) {
      return Collections.emptyList();
    }

    DataPoint.Builder shared = DataPoint.newBuilder();
    shared.setMetric(metricDescriptor.getName());
    shared.setMetricType(metricType);

    ArrayList<DataPoint> datapoints = Lists.newArrayList();
    for (TimeSeries timeSeries : metric.getTimeSeriesList()) {
      DataPoint.Builder builder = shared.clone();
      builder.addAllDimensions(
          createDimensions(metricDescriptor.getLabelKeys(), timeSeries.getLabelValues()));

      List<Point> points = timeSeries.getPoints();
      datapoints.ensureCapacity(datapoints.size() + points.size());
      for (Point point : points) {
        datapoints.add(builder.setValue(createDatum(point.getValue())).build());
      }
    }
    return datapoints;
  }

  @VisibleForTesting
  @javax.annotation.Nullable
  static MetricType getType(Type type) {
    if (type == Type.GAUGE_INT64 || type == Type.GAUGE_DOUBLE) {
      return MetricType.GAUGE;
    } else if (type == Type.CUMULATIVE_INT64 || type == Type.CUMULATIVE_DOUBLE) {
      return MetricType.CUMULATIVE_COUNTER;
    }
    // TODO(mayurkale): decide what to do with Distribution and Summary types.
    return null;
  }

  @VisibleForTesting
  static Iterable<Dimension> createDimensions(List<LabelKey> keys, List<LabelValue> values) {
    List<Dimension> dimensions = new ArrayList<>(keys.size());
    for (int i = 0; i < values.size(); i++) {
      LabelValue value = values.get(i);
      if (value == null || Strings.isNullOrEmpty(value.getValue())) {
        continue;
      }
      dimensions.add(createDimension(keys.get(i), value));
    }
    return dimensions;
  }

  @VisibleForTesting
  static Dimension createDimension(LabelKey labelKey, LabelValue labelValue) {
    Dimension.Builder builder = Dimension.newBuilder();
    String value = labelValue.getValue();
    if (!Strings.isNullOrEmpty(value)) {
      builder.setKey(labelKey.getKey()).setValue(value);
    }
    return builder.build();
  }

  @VisibleForTesting
  static Datum createDatum(Value value) {
    return value.match(
        datumDoubleFunction,
        datumLongFunction,
        datumDistributionFunction,
        datumSummaryFunction,
        Functions.<Datum>throwIllegalArgumentException());
  }
}
