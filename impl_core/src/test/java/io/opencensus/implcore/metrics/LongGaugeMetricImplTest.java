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

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.common.Timestamp;
import io.opencensus.common.ToLongFunction;
import io.opencensus.implcore.metrics.LongGaugeMetricImpl.PointImpl;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.LongGaugeMetric.Point;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricDescriptor;
import io.opencensus.metrics.export.MetricDescriptor.Type;
import io.opencensus.metrics.export.TimeSeries;
import io.opencensus.metrics.export.Value;
import io.opencensus.testing.common.TestClock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link LongGaugeMetricImpl}. */
@RunWith(JUnit4.class)
public class LongGaugeMetricImplTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  private static final String METRIC_NAME = "name";
  private static final String METRIC_DESCRIPTION = "description";
  private static final String METRIC_UNIT = "1";
  private static final LabelKey LABEL_KEY = LabelKey.create("key", "key description");
  private static final LabelValue LABEL_VALUES = LabelValue.create("value");
  private static final LabelValue LABEL_VALUES_1 = LabelValue.create("value1");
  private final List<LabelKey> labelKeys = new ArrayList<LabelKey>();
  private final List<LabelValue> labelValues = new ArrayList<LabelValue>();
  private final List<LabelValue> labelValues1 = new ArrayList<LabelValue>();

  private static final Timestamp TEST_TIME = Timestamp.create(1234, 123);
  private final TestClock testClock = TestClock.create(TEST_TIME);

  private LongGaugeMetricImpl longGaugeMetric;

  @Before
  public void setUp() {
    labelKeys.add(LABEL_KEY);
    labelValues.add(LABEL_VALUES);
    labelValues1.add(LABEL_VALUES_1);

    longGaugeMetric =
        new LongGaugeMetricImpl(METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, labelKeys);
  }

  // helper class
  public static class JobsInQueue {
    public long getValue() {
      return 7;
    }
  }

  @Test
  public void addPoint_with_obj_function() {
    longGaugeMetric.addPoint(
        labelValues,
        new JobsInQueue(),
        new ToLongFunction<JobsInQueue>() {
          @Override
          public long applyAsLong(JobsInQueue jobsInQueue) {
            return jobsInQueue.getValue();
          }
        });

    assertThat(longGaugeMetric.getMetric(testClock))
        .isEqualTo(
            Metric.create(
                MetricDescriptor.create(
                    METRIC_NAME,
                    METRIC_DESCRIPTION,
                    METRIC_UNIT,
                    Type.GAUGE_INT64,
                    Collections.unmodifiableList(Collections.singletonList(LABEL_KEY))),
                Collections.singletonList(
                    TimeSeries.create(
                        Collections.unmodifiableList(Collections.singletonList(LABEL_VALUES)),
                        Collections.singletonList(
                            io.opencensus.metrics.export.Point.create(
                                Value.longValue(7), TEST_TIME)),
                        null))));
  }

  @Test
  public void empty_GetMetrics() {
    assertThat(longGaugeMetric.getMetric(testClock))
        .isEqualTo(
            Metric.create(
                MetricDescriptor.create(
                    METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, Type.GAUGE_INT64, labelKeys),
                Collections.<TimeSeries>emptyList()));
  }

  @Test
  public void addPoint_only_with_labels() {
    Point point = longGaugeMetric.addPoint(labelValues);

    assertThat(((PointImpl) point).get()).isEqualTo(0);
    point.inc();
    assertThat(((PointImpl) point).get()).isEqualTo(1);
    point.inc(120);
    assertThat(((PointImpl) point).get()).isEqualTo(121);
    point.dec();
    assertThat(((PointImpl) point).get()).isEqualTo(120);
    point.dec(100);
    assertThat(((PointImpl) point).get()).isEqualTo(20);
    point.set(500);
    assertThat(((PointImpl) point).get()).isEqualTo(500);

    assertThat(longGaugeMetric.getMetric(testClock))
        .isEqualTo(
            Metric.create(
                MetricDescriptor.create(
                    METRIC_NAME,
                    METRIC_DESCRIPTION,
                    METRIC_UNIT,
                    Type.GAUGE_INT64,
                    Collections.unmodifiableList(Collections.singletonList(LABEL_KEY))),
                Collections.singletonList(
                    TimeSeries.create(
                        Collections.unmodifiableList(Collections.singletonList(LABEL_VALUES)),
                        Collections.singletonList(
                            io.opencensus.metrics.export.Point.create(
                                Value.longValue(500), TEST_TIME)),
                        null))));
  }

  @Test
  public void getDefaultPoint() {
    Point point = longGaugeMetric.getDefaultPoint();

    assertThat(((PointImpl) point).get()).isEqualTo(0);
    point.inc();
    assertThat(((PointImpl) point).get()).isEqualTo(1);
    point.inc(120);
    assertThat(((PointImpl) point).get()).isEqualTo(121);
    point.dec();
    assertThat(((PointImpl) point).get()).isEqualTo(120);
    point.dec(100);
    assertThat(((PointImpl) point).get()).isEqualTo(20);
    point.set(500);
    assertThat(((PointImpl) point).get()).isEqualTo(500);

    assertThat(longGaugeMetric.getMetric(testClock))
        .isEqualTo(
            Metric.create(
                MetricDescriptor.create(
                    METRIC_NAME,
                    METRIC_DESCRIPTION,
                    METRIC_UNIT,
                    Type.GAUGE_INT64,
                    Collections.unmodifiableList(Collections.singletonList(LABEL_KEY))),
                Collections.singletonList(
                    TimeSeries.create(
                        Collections.unmodifiableList(Collections.<LabelValue>emptyList()),
                        Collections.singletonList(
                            io.opencensus.metrics.export.Point.create(
                                Value.longValue(500), TEST_TIME)),
                        null))));
  }

  @Test
  public void multipleMetrics_GetMetric() {
    Point point = longGaugeMetric.addPoint(labelValues);
    point.inc();
    point.inc();
    point.inc();

    Point point1 = longGaugeMetric.getDefaultPoint();
    point1.set(100);

    longGaugeMetric.addPoint(
        labelValues1,
        new JobsInQueue(),
        new ToLongFunction<JobsInQueue>() {
          @Override
          public long applyAsLong(JobsInQueue jobsInQueue) {
            return jobsInQueue.getValue();
          }
        });

    List<TimeSeries> timeSeriesList = new ArrayList<TimeSeries>();
    timeSeriesList.add(
        TimeSeries.create(
            Collections.singletonList(LABEL_VALUES),
            Collections.singletonList(
                io.opencensus.metrics.export.Point.create(Value.longValue(3), TEST_TIME)),
            null));
    timeSeriesList.add(
        TimeSeries.create(
            Collections.<LabelValue>emptyList(),
            Collections.singletonList(
                io.opencensus.metrics.export.Point.create(Value.longValue(100), TEST_TIME)),
            null));
    timeSeriesList.add(
        TimeSeries.create(
            Collections.singletonList(LABEL_VALUES_1),
            Collections.singletonList(
                io.opencensus.metrics.export.Point.create(Value.longValue(7), TEST_TIME)),
            null));

    Metric metric = longGaugeMetric.getMetric(testClock);
    assertThat(metric.getMetricDescriptor())
        .isEqualTo(
            MetricDescriptor.create(
                METRIC_NAME,
                METRIC_DESCRIPTION,
                METRIC_UNIT,
                Type.GAUGE_INT64,
                Collections.unmodifiableList(Collections.singletonList(LABEL_KEY))));

    assertThat(metric.getTimeSeriesList()).containsExactlyElementsIn(timeSeriesList);
  }

  @Test
  public void multipleMetrics_GetMetric_samePoint() {
    Point point = longGaugeMetric.addPoint(labelValues);
    point.inc();
    point.inc();
    point.inc();

    Point point1 = longGaugeMetric.addPoint(labelValues);
    point1.inc();

    assertThat(longGaugeMetric.getMetric(testClock))
        .isEqualTo(
            Metric.create(
                MetricDescriptor.create(
                    METRIC_NAME,
                    METRIC_DESCRIPTION,
                    METRIC_UNIT,
                    Type.GAUGE_INT64,
                    Collections.unmodifiableList(Collections.singletonList(LABEL_KEY))),
                Collections.singletonList(
                    TimeSeries.create(
                        Collections.unmodifiableList(Collections.singletonList(LABEL_VALUES)),
                        Collections.singletonList(
                            io.opencensus.metrics.export.Point.create(
                                Value.longValue(4), TEST_TIME)),
                        null))));
  }

  @Test
  public void addPoint_Incorrect_Labels() {
    thrown.expect(IllegalArgumentException.class);
    longGaugeMetric.addPoint(new ArrayList<LabelValue>());
  }

  @Test
  public void addPoint_Incorrect_Labels_With_Obj() {
    thrown.expect(IllegalArgumentException.class);
    longGaugeMetric.addPoint(
        new ArrayList<LabelValue>(),
        null,
        new ToLongFunction<JobsInQueue>() {
          @Override
          public long applyAsLong(JobsInQueue jobsInQueue) {
            return jobsInQueue.getValue();
          }
        });
  }
}
