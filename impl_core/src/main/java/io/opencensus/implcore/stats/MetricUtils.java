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
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.Measure;
import io.opencensus.stats.View;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import io.opencensus.temporary.metrics.LabelKey;
import io.opencensus.temporary.metrics.LabelValue;
import io.opencensus.temporary.metrics.MetricDescriptor;
import io.opencensus.temporary.metrics.MetricDescriptor.Type;
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
                TYPE_CUMULATIVE_DOUBLE_FUNCTION, // Sum Double
                TYPE_CUMULATIVE_INT64_FUNCTION, // Sum Int64
                TYPE_UNRECOGNIZED_FUNCTION)),
        TYPE_CUMULATIVE_INT64_FUNCTION, // Count
        TYPE_CUMULATIVE_DISTRIBUTION_FUNCTION, // Distribution
        Functions.returnConstant(
            measure.match(
                TYPE_GAUGE_DOUBLE_FUNCTION, // LastValue Double
                TYPE_GAUGE_INT64_FUNCTION, // LastValue Long
                TYPE_UNRECOGNIZED_FUNCTION)),
        AGGREGATION_TYPE_DEFAULT_FUNCTION);
  }

  static List<LabelValue> tagValuesToLabelValues(List</*@Nullable*/ TagValue> tagValues) {
    List<LabelValue> labelValues = new ArrayList<LabelValue>();
    for (/*@Nullable*/ TagValue tagValue : tagValues) {
      labelValues.add(LabelValue.create(tagValue == null ? null : tagValue.asString()));
    }
    return labelValues;
  }

  private static final Function<Object, Type> TYPE_CUMULATIVE_DOUBLE_FUNCTION =
      Functions.returnConstant(Type.CUMULATIVE_DOUBLE);

  private static final Function<Object, Type> TYPE_CUMULATIVE_INT64_FUNCTION =
      Functions.returnConstant(Type.CUMULATIVE_INT64);

  private static final Function<Object, Type> TYPE_CUMULATIVE_DISTRIBUTION_FUNCTION =
      Functions.returnConstant(Type.CUMULATIVE_DISTRIBUTION);

  private static final Function<Object, Type> TYPE_GAUGE_DOUBLE_FUNCTION =
      Functions.returnConstant(Type.GAUGE_DOUBLE);

  private static final Function<Object, Type> TYPE_GAUGE_INT64_FUNCTION =
      Functions.returnConstant(Type.GAUGE_INT64);

  private static final Function<Object, Type> TYPE_UNRECOGNIZED_FUNCTION =
      Functions.<Type>throwAssertionError();

  private static final Function<Aggregation, Type> AGGREGATION_TYPE_DEFAULT_FUNCTION =
      new Function<Aggregation, Type>() {
        @Override
        public Type apply(Aggregation arg) {
          if (arg instanceof Aggregation.Mean) {
            return Type.CUMULATIVE_DOUBLE; // Mean
          }
          throw new AssertionError();
        }
      };

  private MetricUtils() {}
}
