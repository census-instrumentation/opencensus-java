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
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.signalfx.metrics.protobuf.SignalFxProtocolBuffers.DataPoint;
import com.signalfx.metrics.protobuf.SignalFxProtocolBuffers.Datum;
import com.signalfx.metrics.protobuf.SignalFxProtocolBuffers.Dimension;
import com.signalfx.metrics.protobuf.SignalFxProtocolBuffers.MetricType;
import io.opencensus.common.Function;
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.AggregationData;
import io.opencensus.stats.AggregationData.CountData;
import io.opencensus.stats.AggregationData.DistributionData;
import io.opencensus.stats.AggregationData.LastValueDataDouble;
import io.opencensus.stats.AggregationData.LastValueDataLong;
import io.opencensus.stats.AggregationData.SumDataDouble;
import io.opencensus.stats.AggregationData.SumDataLong;
import io.opencensus.stats.View;
import io.opencensus.stats.ViewData;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

/** Adapter for a {@code ViewData}'s contents into SignalFx datapoints. */
@SuppressWarnings("deprecation")
final class SignalFxSessionAdaptor {

  private SignalFxSessionAdaptor() {}

  /**
   * Converts the given view data into datapoints that can be sent to SignalFx.
   *
   * <p>The view name is used as the metric name, and the aggregation type and aggregation window
   * type determine the metric type.
   *
   * @param data The {@link ViewData} containing the aggregation data of each combination of tag
   *     values.
   * @return A list of datapoints for the corresponding metric timeseries of this view's metric.
   */
  static List<DataPoint> adapt(ViewData data) {
    View view = data.getView();
    List<TagKey> keys = view.getColumns();

    MetricType metricType = getMetricTypeForAggregation(view.getAggregation(), view.getWindow());
    if (metricType == null) {
      return Collections.emptyList();
    }

    List<DataPoint> datapoints = new ArrayList<>(data.getAggregationMap().size());
    for (Map.Entry<List</*@Nullable*/ TagValue>, AggregationData> entry :
        data.getAggregationMap().entrySet()) {
      datapoints.add(
          DataPoint.newBuilder()
              .setMetric(view.getName().asString())
              .setMetricType(metricType)
              .addAllDimensions(createDimensions(keys, entry.getKey()))
              .setValue(createDatum(entry.getValue()))
              .build());
    }
    return datapoints;
  }

  @VisibleForTesting
  @javax.annotation.Nullable
  static MetricType getMetricTypeForAggregation(
      Aggregation aggregation, View.AggregationWindow window) {
    if (aggregation instanceof Aggregation.Mean || aggregation instanceof Aggregation.LastValue) {
      return MetricType.GAUGE;
    } else if (aggregation instanceof Aggregation.Count || aggregation instanceof Aggregation.Sum) {
      if (window instanceof View.AggregationWindow.Cumulative) {
        return MetricType.CUMULATIVE_COUNTER;
      }
      // TODO(mpetazzoni): support incremental counters when AggregationWindow.Interval is ready
    }

    // TODO(mpetazzoni): add support for histograms (Aggregation.Distribution).
    return null;
  }

  @VisibleForTesting
  static Iterable<Dimension> createDimensions(
      List<TagKey> keys, List</*@Nullable*/ TagValue> values) {
    Preconditions.checkArgument(
        keys.size() == values.size(), "TagKeys and TagValues don't have the same size.");
    List<Dimension> dimensions = new ArrayList<>(keys.size());
    for (ListIterator<TagKey> it = keys.listIterator(); it.hasNext(); ) {
      TagKey key = it.next();
      TagValue value = values.get(it.previousIndex());
      if (value == null || Strings.isNullOrEmpty(value.asString())) {
        continue;
      }
      dimensions.add(createDimension(key, value));
    }
    return dimensions;
  }

  @VisibleForTesting
  static Dimension createDimension(TagKey key, TagValue value) {
    return Dimension.newBuilder().setKey(key.getName()).setValue(value.asString()).build();
  }

  @VisibleForTesting
  static Datum createDatum(AggregationData data) {
    final Datum.Builder builder = Datum.newBuilder();
    data.match(
        new Function<SumDataDouble, Void>() {
          @Override
          public Void apply(SumDataDouble arg) {
            builder.setDoubleValue(arg.getSum());
            return null;
          }
        },
        new Function<SumDataLong, Void>() {
          @Override
          public Void apply(SumDataLong arg) {
            builder.setIntValue(arg.getSum());
            return null;
          }
        },
        new Function<CountData, Void>() {
          @Override
          public Void apply(CountData arg) {
            builder.setIntValue(arg.getCount());
            return null;
          }
        },
        new Function<DistributionData, Void>() {
          @Override
          public Void apply(DistributionData arg) {
            // TODO(mpetazzoni): add histogram support.
            throw new IllegalArgumentException("Distribution aggregations are not supported");
          }
        },
        new Function<LastValueDataDouble, Void>() {
          @Override
          public Void apply(LastValueDataDouble arg) {
            builder.setDoubleValue(arg.getLastValue());
            return null;
          }
        },
        new Function<LastValueDataLong, Void>() {
          @Override
          public Void apply(LastValueDataLong arg) {
            builder.setIntValue(arg.getLastValue());
            return null;
          }
        },
        new Function<AggregationData, Void>() {
          @Override
          public Void apply(AggregationData arg) {
            if (arg instanceof AggregationData.MeanData) {
              builder.setDoubleValue(((AggregationData.MeanData) arg).getMean());
              return null;
            }
            throw new IllegalArgumentException("Unknown Aggregation.");
          }
        });
    return builder.build();
  }
}
