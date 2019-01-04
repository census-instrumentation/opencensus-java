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
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.mockito.Mockito.when;

import io.opencensus.common.Duration;
import io.opencensus.common.Timestamp;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricDescriptor.Type;
import io.opencensus.metrics.export.MetricProducer;
import io.opencensus.metrics.export.MetricProducerManager;
import io.opencensus.metrics.export.Point;
import io.opencensus.metrics.export.Value;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link IntervalMetricReader}. */
@RunWith(JUnit4.class)
public class IntervalMetricReaderTest {
  private static final String METRIC_NAME = "my metric";
  private static final String METRIC_DESCRIPTION = "metric description";
  private static final String METRIC_UNIT = "us";
  private static final List<LabelKey> LABEL_KEY =
      Collections.singletonList(LabelKey.create("KEY", "key description"));
  private static final List<LabelValue> LABEL_VALUE =
      Collections.singletonList(LabelValue.create("VALUE"));
  private static final io.opencensus.metrics.export.MetricDescriptor METRIC_DESCRIPTOR =
      io.opencensus.metrics.export.MetricDescriptor.create(
          METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, Type.CUMULATIVE_INT64, LABEL_KEY);

  private static final Value VALUE_LONG = Value.longValue(12345678);
  private static final Timestamp TIMESTAMP = Timestamp.fromMillis(3000);
  private static final Timestamp TIMESTAMP_2 = Timestamp.fromMillis(1000);
  private static final Point POINT = Point.create(VALUE_LONG, TIMESTAMP);

  private static final io.opencensus.metrics.export.TimeSeries CUMULATIVE_TIME_SERIES =
      io.opencensus.metrics.export.TimeSeries.createWithOnePoint(LABEL_VALUE, POINT, TIMESTAMP_2);

  private static final Metric METRIC =
      Metric.createWithOneTimeSeries(METRIC_DESCRIPTOR, CUMULATIVE_TIME_SERIES);

  @Mock private MetricProducerManager metricProducerManager;
  @Mock private MetricProducer metricProducer;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    Set<MetricProducer> metricProducerSet = new HashSet<>();
    metricProducerSet.add(metricProducer);
    when(metricProducer.getMetrics()).thenReturn(Collections.singletonList(METRIC));
    when(metricProducerManager.getAllMetricProducer()).thenReturn(metricProducerSet);
  }

  @Test
  public void testConstants() {
    assertThat(IntervalMetricReader.DEFAULT_INTERVAL).isEqualTo(Duration.create(60, 0));
  }

  @Test
  public void intervalExport() {
    FakeMetricExporter fakeMetricExporter = new FakeMetricExporter();
    IntervalMetricReader intervalMetricReader =
        IntervalMetricReader.create(
            fakeMetricExporter,
            MetricReader.create(
                MetricReader.Options.builder()
                    .setMetricProducerManager(metricProducerManager)
                    .build()),
            IntervalMetricReader.Options.builder()
                .setExportInterval(Duration.create(0, (int) MILLISECONDS.toNanos(100)))
                .build());
    assertThat(fakeMetricExporter.waitForNumberOfExports(1))
        .containsExactly(Collections.singletonList(METRIC));
    assertThat(fakeMetricExporter.waitForNumberOfExports(2))
        .containsExactly(Collections.singletonList(METRIC), Collections.singletonList(METRIC));
    intervalMetricReader.stop();
  }

  @Test
  public void exportAfterStop() {
    FakeMetricExporter fakeMetricExporter = new FakeMetricExporter();
    IntervalMetricReader intervalMetricReader =
        IntervalMetricReader.create(
            fakeMetricExporter,
            MetricReader.create(
                MetricReader.Options.builder()
                    .setMetricProducerManager(metricProducerManager)
                    .build()),
            IntervalMetricReader.Options.builder()
                .setExportInterval(Duration.create(10, 0))
                .build());
    // Rely that this will be called in less than 10 seconds.
    intervalMetricReader.stop();
    assertThat(fakeMetricExporter.waitForNumberOfExports(1))
        .containsExactly(Collections.singletonList(METRIC));
  }
}
