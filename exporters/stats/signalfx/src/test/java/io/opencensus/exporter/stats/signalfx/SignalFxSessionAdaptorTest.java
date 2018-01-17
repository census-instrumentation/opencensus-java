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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.signalfx.metrics.protobuf.SignalFxProtocolBuffers.DataPoint;
import com.signalfx.metrics.protobuf.SignalFxProtocolBuffers.Datum;
import com.signalfx.metrics.protobuf.SignalFxProtocolBuffers.Dimension;
import com.signalfx.metrics.protobuf.SignalFxProtocolBuffers.MetricType;
import io.opencensus.common.Duration;
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.AggregationData;
import io.opencensus.stats.AggregationData.CountData;
import io.opencensus.stats.AggregationData.DistributionData;
import io.opencensus.stats.AggregationData.MeanData;
import io.opencensus.stats.AggregationData.SumDataDouble;
import io.opencensus.stats.AggregationData.SumDataLong;
import io.opencensus.stats.BucketBoundaries;
import io.opencensus.stats.View;
import io.opencensus.stats.View.AggregationWindow;
import io.opencensus.stats.View.Name;
import io.opencensus.stats.ViewData;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SignalFxSessionAdaptorTest {

  private static final Duration ONE_SECOND = Duration.create(1, 0);

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Mock private View view;

  @Mock private ViewData viewData;

  @Before
  public void setUp() {
    Mockito.when(view.getName()).thenReturn(Name.create("view-name"));
    Mockito.when(view.getColumns()).thenReturn(ImmutableList.of(TagKey.create("animal")));
    Mockito.when(viewData.getView()).thenReturn(view);
  }

  @Test
  public void checkMetricTypeFromAggregation() {
    assertNull(SignalFxSessionAdaptor.getMetricTypeForAggregation(null, null));
    assertNull(
        SignalFxSessionAdaptor.getMetricTypeForAggregation(
            null, AggregationWindow.Cumulative.create()));
    assertEquals(
        MetricType.GAUGE,
        SignalFxSessionAdaptor.getMetricTypeForAggregation(
            Aggregation.Mean.create(), AggregationWindow.Cumulative.create()));
    assertEquals(
        MetricType.GAUGE,
        SignalFxSessionAdaptor.getMetricTypeForAggregation(
            Aggregation.Mean.create(), AggregationWindow.Interval.create(ONE_SECOND)));
    assertEquals(
        MetricType.CUMULATIVE_COUNTER,
        SignalFxSessionAdaptor.getMetricTypeForAggregation(
            Aggregation.Count.create(), AggregationWindow.Cumulative.create()));
    assertEquals(
        MetricType.CUMULATIVE_COUNTER,
        SignalFxSessionAdaptor.getMetricTypeForAggregation(
            Aggregation.Sum.create(), AggregationWindow.Cumulative.create()));
    assertNull(
        SignalFxSessionAdaptor.getMetricTypeForAggregation(Aggregation.Count.create(), null));
    assertNull(SignalFxSessionAdaptor.getMetricTypeForAggregation(Aggregation.Sum.create(), null));
    assertNull(
        SignalFxSessionAdaptor.getMetricTypeForAggregation(
            Aggregation.Count.create(), AggregationWindow.Interval.create(ONE_SECOND)));
    assertNull(
        SignalFxSessionAdaptor.getMetricTypeForAggregation(
            Aggregation.Sum.create(), AggregationWindow.Interval.create(ONE_SECOND)));
    assertNull(
        SignalFxSessionAdaptor.getMetricTypeForAggregation(
            Aggregation.Distribution.create(BucketBoundaries.create(ImmutableList.of(3.14d))),
            AggregationWindow.Cumulative.create()));
  }

  @Test
  public void createDimensionsWithNonMatchingListSizes() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("don't have the same size");
    SignalFxSessionAdaptor.createDimensions(
        ImmutableList.of(TagKey.create("animal"), TagKey.create("color")),
        ImmutableList.of(TagValue.create("dog")));
  }

  @Test
  public void createDimensionsIgnoresEmptyValues() {
    List<Dimension> dimensions =
        Lists.newArrayList(
            SignalFxSessionAdaptor.createDimensions(
                ImmutableList.of(TagKey.create("animal"), TagKey.create("color")),
                ImmutableList.of(TagValue.create("dog"), TagValue.create(""))));
    assertEquals(1, dimensions.size());
    assertEquals("animal", dimensions.get(0).getKey());
    assertEquals("dog", dimensions.get(0).getValue());
  }

  @Test
  public void createDimension() {
    Dimension dimension =
        SignalFxSessionAdaptor.createDimension(TagKey.create("animal"), TagValue.create("dog"));
    assertEquals("animal", dimension.getKey());
    assertEquals("dog", dimension.getValue());
  }

  @Test
  public void unsupportedAggregationYieldsNoDatapoints() {
    Mockito.when(view.getAggregation())
        .thenReturn(
            Aggregation.Distribution.create(BucketBoundaries.create(ImmutableList.of(3.14d))));
    Mockito.when(view.getWindow()).thenReturn(AggregationWindow.Cumulative.create());
    List<DataPoint> datapoints = SignalFxSessionAdaptor.adapt(viewData);
    assertEquals(0, datapoints.size());
  }

  @Test
  public void noAggregationDataYieldsNoDatapoints() {
    Mockito.when(view.getAggregation()).thenReturn(Aggregation.Count.create());
    Mockito.when(view.getWindow()).thenReturn(AggregationWindow.Cumulative.create());
    List<DataPoint> datapoints = SignalFxSessionAdaptor.adapt(viewData);
    assertEquals(0, datapoints.size());
  }

  @Test
  public void createDatumFromDoubleSum() {
    SumDataDouble data = SumDataDouble.create(3.14d);
    Datum datum = SignalFxSessionAdaptor.createDatum(data);
    assertTrue(datum.hasDoubleValue());
    assertFalse(datum.hasIntValue());
    assertFalse(datum.hasStrValue());
    assertEquals(3.14d, datum.getDoubleValue(), 0d);
  }

  @Test
  public void createDatumFromLongSum() {
    SumDataLong data = SumDataLong.create(42L);
    Datum datum = SignalFxSessionAdaptor.createDatum(data);
    assertFalse(datum.hasDoubleValue());
    assertTrue(datum.hasIntValue());
    assertFalse(datum.hasStrValue());
    assertEquals(42L, datum.getIntValue());
  }

  @Test
  public void createDatumFromCount() {
    CountData data = CountData.create(42L);
    Datum datum = SignalFxSessionAdaptor.createDatum(data);
    assertFalse(datum.hasDoubleValue());
    assertTrue(datum.hasIntValue());
    assertFalse(datum.hasStrValue());
    assertEquals(42L, datum.getIntValue());
  }

  @Test
  public void createDatumFromMean() {
    MeanData data = MeanData.create(3.14d, 2L);
    Datum datum = SignalFxSessionAdaptor.createDatum(data);
    assertTrue(datum.hasDoubleValue());
    assertFalse(datum.hasIntValue());
    assertFalse(datum.hasStrValue());
    assertEquals(3.14d, datum.getDoubleValue(), 0d);
  }

  @Test
  public void createDatumFromDistributionThrows() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Distribution aggregations are not supported");
    SignalFxSessionAdaptor.createDatum(
        DistributionData.create(5, 2, 0, 10, 40, ImmutableList.of(1L)));
  }

  @Test
  public void adaptViewIntoDatapoints() {
    Map<List<TagValue>, AggregationData> map =
        ImmutableMap.<List<TagValue>, AggregationData>of(
            ImmutableList.of(TagValue.create("dog")),
            SumDataLong.create(2L),
            ImmutableList.of(TagValue.create("cat")),
            SumDataLong.create(3L));
    Mockito.when(viewData.getAggregationMap()).thenReturn(map);
    Mockito.when(view.getAggregation()).thenReturn(Aggregation.Count.create());
    Mockito.when(view.getWindow()).thenReturn(AggregationWindow.Cumulative.create());

    List<DataPoint> datapoints = SignalFxSessionAdaptor.adapt(viewData);
    assertEquals(2, datapoints.size());
    for (DataPoint dp : datapoints) {
      assertEquals("view-name", dp.getMetric());
      assertEquals(MetricType.CUMULATIVE_COUNTER, dp.getMetricType());
      assertEquals(1, dp.getDimensionsCount());
      assertTrue(dp.hasValue());
      assertFalse(dp.hasSource());

      Datum datum = dp.getValue();
      assertTrue(datum.hasIntValue());
      assertFalse(datum.hasDoubleValue());
      assertFalse(datum.hasStrValue());

      Dimension dimension = dp.getDimensions(0);
      assertEquals("animal", dimension.getKey());
      switch (dimension.getValue()) {
        case "dog":
          assertEquals(2L, datum.getIntValue());
          break;
        case "cat":
          assertEquals(3L, datum.getIntValue());
          break;
        default:
          fail("unexpected dimension value");
      }
    }
  }

  @Test
  public void adaptViewWithEmptyTagValueIntoDatapoints() {
    Map<List<TagValue>, AggregationData> map =
        ImmutableMap.<List<TagValue>, AggregationData>of(
            ImmutableList.of(TagValue.create("dog")),
            SumDataLong.create(2L),
            ImmutableList.of(TagValue.create("")),
            SumDataLong.create(3L));
    Mockito.when(viewData.getAggregationMap()).thenReturn(map);
    Mockito.when(view.getAggregation()).thenReturn(Aggregation.Count.create());
    Mockito.when(view.getWindow()).thenReturn(AggregationWindow.Cumulative.create());

    List<DataPoint> datapoints = SignalFxSessionAdaptor.adapt(viewData);
    assertEquals(2, datapoints.size());
    for (DataPoint dp : datapoints) {
      assertEquals("view-name", dp.getMetric());
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
          assertEquals("animal", dimension.getKey());
          assertEquals("dog", dimension.getValue());
          assertEquals(2L, datum.getIntValue());
          break;
        default:
          fail("Unexpected number of dimensions on the created datapoint");
          break;
      }
    }
  }
}
