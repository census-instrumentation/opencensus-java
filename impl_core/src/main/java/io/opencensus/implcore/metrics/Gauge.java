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

package io.opencensus.implcore.metrics;

import static com.google.common.base.Preconditions.checkNotNull;

import io.opencensus.common.Clock;
import io.opencensus.common.ToDoubleFunction;
import io.opencensus.common.ToLongFunction;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricDescriptor;
import io.opencensus.metrics.export.MetricDescriptor.Type;
import io.opencensus.metrics.export.Point;
import io.opencensus.metrics.export.TimeSeries;
import io.opencensus.metrics.export.Value;
import java.util.Collections;
import java.util.List;

abstract class Gauge {
  private final MetricDescriptor metricDescriptor;
  private final List<LabelValue> labelValues;

  final Metric getMetric(Clock clock) {
    return Metric.create(metricDescriptor, Collections.singletonList(getTimeSeries(clock)));
  }

  abstract TimeSeries getTimeSeries(Clock clock);

  static final class DoubleGauge<T> extends Gauge {
    private final T obj;
    private final ToDoubleFunction<T> function;

    DoubleGauge(
        String name,
        String description,
        String unit,
        List<LabelKey> labelKeys,
        List<LabelValue> labelValues,
        T obj,
        ToDoubleFunction<T> function) {
      super(
          MetricDescriptor.create(name, description, unit, Type.GAUGE_DOUBLE, labelKeys),
          labelValues);
      this.obj = obj;
      this.function = function;
    }

    @Override
    TimeSeries getTimeSeries(Clock clock) {
      return TimeSeries.create(
          getLabelValues(),
          Collections.singletonList(
              Point.create(Value.doubleValue(function.applyAsDouble(obj)), clock.now())),
          null);
    }
  }

  static final class LongGauge<T> extends Gauge {
    private final T obj;
    private final ToLongFunction<T> function;

    LongGauge(
        String name,
        String description,
        String unit,
        List<LabelKey> labelKeys,
        List<LabelValue> labelValues,
        T obj,
        ToLongFunction<T> function) {
      super(
          MetricDescriptor.create(name, description, unit, Type.GAUGE_INT64, labelKeys),
          labelValues);
      this.obj = obj;
      this.function = function;
    }

    @Override
    TimeSeries getTimeSeries(Clock clock) {
      return TimeSeries.create(
          getLabelValues(),
          Collections.singletonList(
              Point.create(Value.longValue(function.applyAsLong(obj)), clock.now())),
          null);
    }
  }

  List<LabelValue> getLabelValues() {
    return labelValues;
  }

  Gauge(MetricDescriptor metricDescriptor, List<LabelValue> labelValues) {
    this.metricDescriptor = checkNotNull(metricDescriptor, "metricDescriptor");
    this.labelValues = Collections.unmodifiableList(labelValues);
  }
}
