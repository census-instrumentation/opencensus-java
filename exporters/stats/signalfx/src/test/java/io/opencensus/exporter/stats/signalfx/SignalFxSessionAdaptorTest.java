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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.signalfx.metrics.protobuf.SignalFxProtocolBuffers.DataPoint;
import com.signalfx.metrics.protobuf.SignalFxProtocolBuffers.Datum;
import com.signalfx.metrics.protobuf.SignalFxProtocolBuffers.Dimension;
import com.signalfx.metrics.protobuf.SignalFxProtocolBuffers.MetricType;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link SignalFxSessionAdaptor}. */
@RunWith(JUnit4.class)
public class SignalFxSessionAdaptorTest {
  @Rule public final ExpectedException thrown = ExpectedException.none();

  private static final String METRIC_NAME = "metric-name";
  private static final String METRIC_DESCRIPTION = "metric-description";
  private static final String METRIC_UNIT = "1";
  private static final LabelKey LABEL_KEY_1 = LabelKey.create("key1", "description");
  private static final LabelKey LABEL_KEY_2 = LabelKey.create("key2", "description");
  private static final LabelValue LABEL_VALUE_1 = LabelValue.create("value1");
  private static final LabelValue LABEL_VALUE_2 = LabelValue.create("value2");
  private static final LabelValue EMPTY_LABEL_VALUE = LabelValue.create("");
  private static final MetricDescriptor METRIC_DESCRIPTOR =
      MetricDescriptor.create(
          METRIC_NAME,
          METRIC_DESCRIPTION,
          METRIC_UNIT,
          Type.CUMULATIVE_INT64,
          Collections.singletonList(LABEL_KEY_1));
  private static final MetricDescriptor DISTRIBUTION_METRIC_DESCRIPTOR =
      MetricDescriptor.create(
          METRIC_NAME,
          METRIC_DESCRIPTION,
          METRIC_UNIT,
          Type.CUMULATIVE_DISTRIBUTION,
          Collections.singletonList(LABEL_KEY_1));
  private static final List<Double> BUCKET_BOUNDARIES = Arrays.asList(1.0, 3.0, 5.0);
  private static final Distribution DISTRIBUTION =
      Distribution.create(
          3,
          2,
          14,
          BucketOptions.explicitOptions(BUCKET_BOUNDARIES),
          Arrays.asList(Bucket.create(3), Bucket.create(1), Bucket.create(2), Bucket.create(4)));
  private static final Summary SUMMARY =
      Summary.create(
          10L,
          10.0,
          Snapshot.create(
              10L, 87.07, Collections.singletonList(ValueAtPercentile.create(0.98, 10.2))));
  private static final Value VALUE_LONG = Value.longValue(42L);
  private static final Value VALUE_DOUBLE = Value.doubleValue(12.2);
  private static final Value VALUE_DISTRIBUTION = Value.distributionValue(DISTRIBUTION);
  private static final Value VALUE_SUMMARY = Value.summaryValue(SUMMARY);
  private static final Timestamp TIMESTAMP = Timestamp.fromMillis(3000);
  private static final Point POINT_1 = Point.create(Value.longValue(2L), TIMESTAMP);
  private static final Point POINT_2 = Point.create(Value.longValue(3L), TIMESTAMP);
  private static final TimeSeries TIME_SERIES_1 =
      TimeSeries.createWithOnePoint(Collections.singletonList(LABEL_VALUE_1), POINT_1, null);
  private static final TimeSeries TIME_SERIES_2 =
      TimeSeries.createWithOnePoint(Collections.singletonList(LABEL_VALUE_2), POINT_2, null);
  private static final TimeSeries TIME_SERIES_3 =
      TimeSeries.createWithOnePoint(Collections.singletonList(EMPTY_LABEL_VALUE), POINT_2, null);
  private static final Metric METRIC =
      Metric.create(METRIC_DESCRIPTOR, Arrays.asList(TIME_SERIES_1, TIME_SERIES_2));
  private static final Metric METRIC_1 =
      Metric.create(METRIC_DESCRIPTOR, Arrays.asList(TIME_SERIES_1, TIME_SERIES_3));
  private static final Metric DISTRIBUTION_METRIC =
      Metric.create(DISTRIBUTION_METRIC_DESCRIPTOR, Collections.<TimeSeries>emptyList());

  @Test
  public void checkMetricType() {
    assertNull(SignalFxSessionAdaptor.getType(null));
    assertEquals(MetricType.GAUGE, SignalFxSessionAdaptor.getType(Type.GAUGE_INT64));
    assertEquals(MetricType.GAUGE, SignalFxSessionAdaptor.getType(Type.GAUGE_DOUBLE));
    assertEquals(
        MetricType.CUMULATIVE_COUNTER, SignalFxSessionAdaptor.getType(Type.CUMULATIVE_INT64));
    assertEquals(
        MetricType.CUMULATIVE_COUNTER, SignalFxSessionAdaptor.getType(Type.CUMULATIVE_DOUBLE));
    assertNull(SignalFxSessionAdaptor.getType(Type.SUMMARY));
    assertNull(SignalFxSessionAdaptor.getType(Type.GAUGE_DISTRIBUTION));
    assertNull(SignalFxSessionAdaptor.getType(Type.CUMULATIVE_DISTRIBUTION));
  }

  @Test
  public void createDimensionsIgnoresEmptyValues() {
    List<Dimension> dimensions =
        Lists.newArrayList(
            SignalFxSessionAdaptor.createDimensions(
                ImmutableList.of(LABEL_KEY_1, LABEL_KEY_2),
                ImmutableList.of(LABEL_VALUE_1, EMPTY_LABEL_VALUE)));
    assertEquals(1, dimensions.size());
    assertEquals(LABEL_KEY_1.getKey(), dimensions.get(0).getKey());
    assertEquals(LABEL_VALUE_1.getValue(), dimensions.get(0).getValue());
  }

  @Test
  public void createDimension() {
    Dimension dimension = SignalFxSessionAdaptor.createDimension(LABEL_KEY_1, LABEL_VALUE_1);
    assertEquals(LABEL_KEY_1.getKey(), dimension.getKey());
    assertEquals(LABEL_VALUE_1.getValue(), dimension.getValue());
  }

  @Test
  public void adoptMetricNoDatapoints() {
    List<DataPoint> datapoints = SignalFxSessionAdaptor.adapt(DISTRIBUTION_METRIC);
    assertEquals(0, datapoints.size());
  }

  @Test
  public void createDatumFromValueLong() {
    Datum datum = SignalFxSessionAdaptor.createDatum(VALUE_LONG);
    assertFalse(datum.hasDoubleValue());
    assertTrue(datum.hasIntValue());
    assertFalse(datum.hasStrValue());
    assertEquals(42L, datum.getIntValue());
  }

  @Test
  public void createDatumFromValueDistribution() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Distribution type are not supported");
    SignalFxSessionAdaptor.createDatum(VALUE_DISTRIBUTION);
  }

  @Test
  public void createDatumFromValueSummary() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Summary type are not supported");
    SignalFxSessionAdaptor.createDatum(VALUE_SUMMARY);
  }

  @Test
  public void createDatumFromValueDouble() {
    Datum datum = SignalFxSessionAdaptor.createDatum(VALUE_DOUBLE);
    assertTrue(datum.hasDoubleValue());
    assertFalse(datum.hasIntValue());
    assertFalse(datum.hasStrValue());
    assertEquals(12.2, datum.getDoubleValue(), 0d);
  }

  @Test
  public void adaptMetricIntoDatapoints() {
    List<DataPoint> datapoints = SignalFxSessionAdaptor.adapt(METRIC);
    assertEquals(2, datapoints.size());
    for (DataPoint dp : datapoints) {
      assertEquals(METRIC_NAME, dp.getMetric());
      assertEquals(MetricType.CUMULATIVE_COUNTER, dp.getMetricType());
      assertEquals(1, dp.getDimensionsCount());
      assertTrue(dp.hasValue());
      assertFalse(dp.hasSource());

      Datum datum = dp.getValue();
      assertTrue(datum.hasIntValue());
      assertFalse(datum.hasDoubleValue());
      assertFalse(datum.hasStrValue());

      Dimension dimension = dp.getDimensions(0);
      assertEquals(LABEL_KEY_1.getKey(), dimension.getKey());
      switch (dimension.getValue()) {
        case "value1":
          assertEquals(2L, datum.getIntValue());
          break;
        case "value2":
          assertEquals(3L, datum.getIntValue());
          break;
        default:
          fail("unexpected dimension value");
      }
    }
  }

  @Test
  public void adaptMetricWithEmptyLabelValueIntoDatapoints() {
    List<DataPoint> datapoints = SignalFxSessionAdaptor.adapt(METRIC_1);
    assertEquals(2, datapoints.size());
    for (DataPoint dp : datapoints) {
      assertEquals(METRIC_NAME, dp.getMetric());
      assertEquals(MetricType.CUMULATIVE_COUNTER, dp.getMetricType());
      assertTrue(dp.hasValue());
      assertFalse(dp.hasSource());

      Datum datum = dp.getValue();
      assertTrue(datum.hasIntValue());
      assertFalse(datum.hasDoubleValue());
      assertFalse(datum.hasStrValue());

      switch (dp.getDimensionsCount()) {
        case 0:
          assertEquals(3L, datum.getIntValue());
          break;
        case 1:
          Dimension dimension = dp.getDimensions(0);
          assertEquals(LABEL_KEY_1.getKey(), dimension.getKey());
          assertEquals(LABEL_VALUE_1.getValue(), dimension.getValue());
          assertEquals(2L, datum.getIntValue());
          break;
        default:
          fail("Unexpected number of dimensions on the created datapoint");
          break;
      }
    }
  }
}
