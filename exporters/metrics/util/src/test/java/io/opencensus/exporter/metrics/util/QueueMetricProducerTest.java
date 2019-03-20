/*
 * Copyright 2019, OpenCensus Authors
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

package io.opencensus.exporter.metrics.util;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.common.Timestamp;
import io.opencensus.exporter.metrics.util.QueueMetricProducer.Options;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricDescriptor;
import io.opencensus.metrics.export.Point;
import io.opencensus.metrics.export.Value;
import java.util.Collections;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link QueueMetricProducer}. */
@RunWith(JUnit4.class)
public class QueueMetricProducerTest {

  private static final String METRIC_NAME = "test_metric";
  private static final String METRIC_DESCRIPTION = "test_description";
  private static final String METRIC_UNIT = "us";
  private static final List<LabelKey> LABEL_KEY =
      Collections.singletonList(LabelKey.create("test_key", "test_description"));
  private static final List<LabelValue> LABEL_VALUE =
      Collections.singletonList(LabelValue.create("test_value"));
  private static final io.opencensus.metrics.export.MetricDescriptor METRIC_DESCRIPTOR =
      io.opencensus.metrics.export.MetricDescriptor.create(
          METRIC_NAME,
          METRIC_DESCRIPTION,
          METRIC_UNIT,
          MetricDescriptor.Type.CUMULATIVE_INT64,
          LABEL_KEY);

  private static final Value VALUE_LONG = Value.longValue(12345678);
  private static final Value VALUE_LONG_2 = Value.longValue(23456789);
  private static final Timestamp TIMESTAMP = Timestamp.fromMillis(3000);
  private static final Timestamp TIMESTAMP_2 = Timestamp.fromMillis(4000);
  private static final Timestamp TIMESTAMP_3 = Timestamp.fromMillis(4000);
  private static final Point POINT = Point.create(VALUE_LONG, TIMESTAMP);
  private static final Point POINT_2 = Point.create(VALUE_LONG_2, TIMESTAMP);

  private static final io.opencensus.metrics.export.TimeSeries CUMULATIVE_TIME_SERIES =
      io.opencensus.metrics.export.TimeSeries.createWithOnePoint(LABEL_VALUE, POINT, TIMESTAMP_2);
  private static final io.opencensus.metrics.export.TimeSeries CUMULATIVE_TIME_SERIES_2 =
      io.opencensus.metrics.export.TimeSeries.createWithOnePoint(LABEL_VALUE, POINT_2, TIMESTAMP_3);

  private static final Metric METRIC_1 =
      Metric.createWithOneTimeSeries(METRIC_DESCRIPTOR, CUMULATIVE_TIME_SERIES);
  private static final Metric METRIC_2 =
      Metric.createWithOneTimeSeries(METRIC_DESCRIPTOR, CUMULATIVE_TIME_SERIES_2);

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void createWithNegativeBufferSize() {
    Options options = Options.builder().setBufferSize(-1).build();
    thrown.expect(IllegalArgumentException.class);
    QueueMetricProducer.create(options);
  }

  @Test
  public void createWithZeroBufferSize() {
    Options options = Options.builder().setBufferSize(0).build();
    thrown.expect(IllegalArgumentException.class);
    QueueMetricProducer.create(options);
  }

  @Test
  public void pushMetrics() {
    Options options = Options.builder().setBufferSize(1).build();
    QueueMetricProducer producer = QueueMetricProducer.create(options);
    producer.pushMetrics(Collections.singleton(METRIC_1));
    assertThat(producer.getMetrics()).containsExactly(METRIC_1);
    assertThat(producer.getMetrics()).isEmpty(); // Flush after each getMetrics().
  }

  @Test
  public void pushMetrics_ExceedBufferSize() {
    Options options = Options.builder().setBufferSize(1).build();
    QueueMetricProducer producer = QueueMetricProducer.create(options);
    producer.pushMetrics(Collections.singleton(METRIC_1));
    producer.pushMetrics(Collections.singleton(METRIC_2));
    assertThat(producer.getMetrics()).containsExactly(METRIC_2);
  }
}
