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

package io.opencensus.implcore.stats;

import com.google.common.annotations.VisibleForTesting;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.common.Timestamp;
import io.opencensus.metrics.Distribution;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.MetricDescriptor;
import io.opencensus.metrics.MetricDescriptor.Type;
import io.opencensus.metrics.Point;
import io.opencensus.metrics.Value;
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.AggregationData;
import io.opencensus.stats.Measure;
import io.opencensus.stats.View;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import java.util.ArrayList;
import java.util.List;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

@SuppressWarnings("deprecation")
// Utils to convert Stats data models to Metric data models.
final class MetricUtils {

  @javax.annotation.Nullable
  static MetricDescriptor viewToMetricDescriptor(View view) {
    if (view.getWindow() instanceof View.AggregationWindow.Interval) {
      // Only creates Metric for cumulative stats.
      return null;
    }
    List<LabelKey> labelKeys = new ArrayList<LabelKey>();
    for (TagKey tagKey : view.getColumns()) {
      // TODO: add description
      labelKeys.add(LabelKey.create(tagKey.getName(), ""));
    }
    Measure measure = view.getMeasure();
    return MetricDescriptor.create(
        view.getName().asString(),
        view.getDescription(),
        measure.getUnit(),
        getType(measure, view.getAggregation()),
        labelKeys);
  }

  @VisibleForTesting
  static Type getType(Measure measure, Aggregation aggregation) {
    return aggregation.match(
        Functions.returnConstant(
            measure.match(
                Functions.returnConstant(Type.CUMULATIVE_DOUBLE), // Sum Double
                Functions.returnConstant(Type.CUMULATIVE_INT64), // Sum Int64
                Functions.<Type>throwAssertionError())),
        Functions.returnConstant(Type.CUMULATIVE_INT64), // Count
        Functions.returnConstant(Type.CUMULATIVE_DISTRIBUTION), // Distribution
        Functions.returnConstant(
            measure.match(
                Functions.returnConstant(Type.GAUGE_DOUBLE), // LastValue Double
                Functions.returnConstant(Type.GAUGE_INT64), // LastValue Long
                Functions.<Type>throwAssertionError())),
        new Function<Aggregation, Type>() {
          @Override
          public Type apply(Aggregation arg) {
            if (arg instanceof Aggregation.Mean) {
              return Type.CUMULATIVE_DOUBLE; // Mean
            }
            throw new AssertionError();
          }
        });
  }

  static List<LabelValue> tagValuesToLabelValues(List</*@Nullable*/ TagValue> tagValues) {
    List<LabelValue> labelValues = new ArrayList<LabelValue>();
    for (/*@Nullable*/ TagValue tagValue : tagValues) {
      labelValues.add(LabelValue.create(tagValue == null ? null : tagValue.asString()));
    }
    return labelValues;
  }

  static Point mutableAggregationToPoint(
      MutableAggregation mutableAggregation, final Timestamp timestamp, final Type type) {
    return mutableAggregation.match(
        new Function<MutableAggregation.MutableSum, Point>() {
          @Override
          public Point apply(MutableAggregation.MutableSum arg) {
            return Point.create(toDoubleOrInt64Value(arg.getSum(), type), timestamp);
          }
        },
        new Function<MutableAggregation.MutableCount, Point>() {
          @Override
          public Point apply(MutableAggregation.MutableCount arg) {
            return Point.create(Value.longValue(arg.getCount()), timestamp);
          }
        },
        new Function<MutableAggregation.MutableMean, Point>() {
          @Override
          public Point apply(MutableAggregation.MutableMean arg) {
            return Point.create(toDoubleOrInt64Value(arg.getMean(), type), timestamp);
          }
        },
        new Function<MutableAggregation.MutableDistribution, Point>() {
          @Override
          public Point apply(MutableAggregation.MutableDistribution arg) {
            return Point.create(toDistributionValue(arg), timestamp);
          }
        },
        new Function<MutableAggregation.MutableLastValue, Point>() {
          @Override
          public Point apply(MutableAggregation.MutableLastValue arg) {
            return Point.create(toDoubleOrInt64Value(arg.getLastValue(), type), timestamp);
          }
        });
  }

  private static Value toDoubleOrInt64Value(double valueInDouble, Type type) {
    switch (type) {
      case CUMULATIVE_INT64:
      case GAUGE_INT64:
        return Value.longValue(Math.round(valueInDouble));
      case CUMULATIVE_DOUBLE:
      case GAUGE_DOUBLE:
        return Value.doubleValue(valueInDouble);
      case CUMULATIVE_DISTRIBUTION:
        throw new AssertionError();
    }
    throw new AssertionError();
  }

  private static Value toDistributionValue(MutableAggregation.MutableDistribution distribution) {
    List<Distribution.Bucket> buckets = new ArrayList<Distribution.Bucket>();
    for (int bucket = 0; bucket < distribution.getBucketCounts().length; bucket++) {
      long bucketCount = distribution.getBucketCounts()[bucket];
      AggregationData.DistributionData.Exemplar exemplar = null;
      if (distribution.getExemplars() != null) {
        exemplar = distribution.getExemplars()[bucket];
      }

      Distribution.Bucket metricBucket;
      if (exemplar != null) {
        // Bucket with an Exemplar.
        metricBucket =
            Distribution.Bucket.create(
                bucketCount,
                Distribution.Exemplar.create(
                    exemplar.getValue(), exemplar.getTimestamp(), exemplar.getAttachments()));
      } else {
        // Bucket with no Exemplar.
        metricBucket = Distribution.Bucket.create(bucketCount);
      }
      buckets.add(metricBucket);
    }
    return Value.distributionValue(
        Distribution.create(
            distribution.getMean(),
            distribution.getCount(),
            distribution.getSumOfSquaredDeviations(),
            distribution.getBucketBoundaries().getBoundaries(),
            buckets));
  }

  private MetricUtils() {}
}
