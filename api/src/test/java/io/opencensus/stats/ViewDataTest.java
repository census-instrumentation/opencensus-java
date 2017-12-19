/*
 * Copyright 2016-17, OpenCensus Authors
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

package io.opencensus.stats;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableMap;
import com.google.common.testing.EqualsTester;
import io.opencensus.common.Duration;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.common.Timestamp;
import io.opencensus.stats.Aggregation.Count;
import io.opencensus.stats.Aggregation.Distribution;
import io.opencensus.stats.Aggregation.Mean;
import io.opencensus.stats.Aggregation.Sum;
import io.opencensus.stats.AggregationData.CountData;
import io.opencensus.stats.AggregationData.DistributionData;
import io.opencensus.stats.AggregationData.SumDataDouble;
import io.opencensus.stats.AggregationData.SumDataLong;
import io.opencensus.stats.View.AggregationWindow;
import io.opencensus.stats.View.AggregationWindow.Cumulative;
import io.opencensus.stats.View.AggregationWindow.Interval;
import io.opencensus.stats.ViewData.AggregationWindowData;
import io.opencensus.stats.ViewData.AggregationWindowData.CumulativeData;
import io.opencensus.stats.ViewData.AggregationWindowData.IntervalData;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for class {@link ViewData}. */
@RunWith(JUnit4.class)
public final class ViewDataTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testCumulativeViewData() {
    View view = View.create(NAME, DESCRIPTION, MEASURE_DOUBLE, DISTRIBUTION, TAG_KEYS, CUMULATIVE);
    Timestamp start = Timestamp.fromMillis(1000);
    Timestamp end = Timestamp.fromMillis(2000);
    AggregationWindowData windowData = CumulativeData.create(start, end);
    ViewData viewData = ViewData.create(view, ENTRIES, windowData);
    assertThat(viewData.getView()).isEqualTo(view);
    assertThat(viewData.getAggregationMap()).isEqualTo(ENTRIES);
    assertThat(viewData.getWindowData()).isEqualTo(windowData);
  }

  @Test
  public void testIntervalViewData() {
    View view =
        View.create(NAME, DESCRIPTION, MEASURE_DOUBLE, DISTRIBUTION, TAG_KEYS, INTERVAL_HOUR);
    Timestamp end = Timestamp.fromMillis(2000);
    AggregationWindowData windowData = IntervalData.create(end);
    ViewData viewData = ViewData.create(view, ENTRIES, windowData);
    assertThat(viewData.getView()).isEqualTo(view);
    assertThat(viewData.getAggregationMap()).isEqualTo(ENTRIES);
    assertThat(viewData.getWindowData()).isEqualTo(windowData);
  }

  @Test
  public void testViewDataEquals() {
    View cumulativeView =
        View.create(NAME, DESCRIPTION, MEASURE_DOUBLE, DISTRIBUTION, TAG_KEYS, CUMULATIVE);
    View intervalView =
        View.create(NAME, DESCRIPTION, MEASURE_DOUBLE, DISTRIBUTION, TAG_KEYS, INTERVAL_HOUR);

    new EqualsTester()
        .addEqualityGroup(
            ViewData.create(
                cumulativeView,
                ENTRIES,
                CumulativeData.create(Timestamp.fromMillis(1000), Timestamp.fromMillis(2000))),
            ViewData.create(
                cumulativeView,
                ENTRIES,
                CumulativeData.create(Timestamp.fromMillis(1000), Timestamp.fromMillis(2000))))
        .addEqualityGroup(
            ViewData.create(
                cumulativeView,
                ENTRIES,
                CumulativeData.create(Timestamp.fromMillis(1000), Timestamp.fromMillis(3000))))
        .addEqualityGroup(
            ViewData.create(intervalView, ENTRIES, IntervalData.create(Timestamp.fromMillis(2000))),
            ViewData.create(intervalView, ENTRIES, IntervalData.create(Timestamp.fromMillis(2000))))
        .addEqualityGroup(
            ViewData.create(
                intervalView,
                Collections.<List<TagValue>, AggregationData>emptyMap(),
                IntervalData.create(Timestamp.fromMillis(2000))))
        .testEquals();
  }

  @Test
  public void testAggregationWindowDataMatch() {
    final Timestamp start = Timestamp.fromMillis(1000);
    final Timestamp end = Timestamp.fromMillis(2000);
    final AggregationWindowData windowData1 = CumulativeData.create(start, end);
    final AggregationWindowData windowData2 = IntervalData.create(end);
    windowData1.match(
        new Function<CumulativeData, Void>() {
          @Override
          public Void apply(CumulativeData windowData) {
            assertThat(windowData.getStart()).isEqualTo(start);
            assertThat(windowData.getEnd()).isEqualTo(end);
            return null;
          }
        },
        new Function<IntervalData, Void>() {
          @Override
          public Void apply(IntervalData windowData) {
            fail("CumulativeData expected.");
            return null;
          }
        },
        Functions.<Void>throwIllegalArgumentException());
    windowData2.match(
        new Function<CumulativeData, Void>() {
          @Override
          public Void apply(CumulativeData windowData) {
            fail("IntervalData expected.");
            return null;
          }
        },
        new Function<IntervalData, Void>() {
          @Override
          public Void apply(IntervalData windowData) {
            assertThat(windowData.getEnd()).isEqualTo(end);
            return null;
          }
        },
        Functions.<Void>throwIllegalArgumentException());
  }

  @Test
  public void preventWindowAndAggregationWindowDataMismatch() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("AggregationWindow and AggregationWindowData types mismatch. ");
    ViewData.create(
        View.create(NAME, DESCRIPTION, MEASURE_DOUBLE, DISTRIBUTION, TAG_KEYS, INTERVAL_HOUR),
        ENTRIES,
        CumulativeData.create(Timestamp.fromMillis(1000), Timestamp.fromMillis(2000)));
  }

  @Test
  public void preventWindowAndAggregationWindowDataMismatch2() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("AggregationWindow and AggregationWindowData types mismatch. ");
    ViewData.create(
        View.create(NAME, DESCRIPTION, MEASURE_DOUBLE, DISTRIBUTION, TAG_KEYS, CUMULATIVE),
        ENTRIES,
        IntervalData.create(Timestamp.fromMillis(1000)));
  }

  @Test
  public void preventStartTimeLaterThanEndTime() {
    thrown.expect(IllegalArgumentException.class);
    CumulativeData.create(Timestamp.fromMillis(3000), Timestamp.fromMillis(2000));
  }

  @Test
  public void preventAggregationAndAggregationDataMismatch_SumDouble_SumLong() {
    aggregationAndAggregationDataMismatch(
        createView(Sum.create(), MEASURE_DOUBLE),
        ImmutableMap.<List<TagValue>, AggregationData>of(
            Arrays.asList(V1, V2), SumDataLong.create(100)));
  }

  @Test
  public void preventAggregationAndAggregationDataMismatch_SumLong_SumDouble() {
    aggregationAndAggregationDataMismatch(
        createView(Sum.create(), MEASURE_LONG),
        ImmutableMap.<List<TagValue>, AggregationData>of(
            Arrays.asList(V1, V2), SumDataDouble.create(100)));
  }

  @Test
  public void preventAggregationAndAggregationDataMismatch_Count_Distribution() {
    aggregationAndAggregationDataMismatch(createView(Count.create()), ENTRIES);
  }

  @Test
  public void preventAggregationAndAggregationDataMismatch_Mean_Distribution() {
    aggregationAndAggregationDataMismatch(createView(Mean.create()), ENTRIES);
  }

  @Test
  public void preventAggregationAndAggregationDataMismatch_Distribution_Count() {
    aggregationAndAggregationDataMismatch(
        createView(DISTRIBUTION),
        ImmutableMap.of(
            Arrays.asList(V1, V2),
            DistributionData.create(1, 1, 1, 1, 0, Arrays.asList(0L, 1L, 0L)),
            Arrays.asList(V10, V20),
            CountData.create(100)));
  }

  private static View createView(Aggregation aggregation) {
    return createView(aggregation, MEASURE_DOUBLE);
  }

  private static View createView(Aggregation aggregation, Measure measure) {
    return View.create(NAME, DESCRIPTION, measure, aggregation, TAG_KEYS, CUMULATIVE);
  }

  private void aggregationAndAggregationDataMismatch(
      View view, Map<List<TagValue>, ? extends AggregationData> entries) {
    CumulativeData cumulativeData =
        CumulativeData.create(Timestamp.fromMillis(1000), Timestamp.fromMillis(2000));
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Aggregation and AggregationData types mismatch. ");
    ViewData.create(view, entries, cumulativeData);
  }

  // tag keys
  private static final TagKey K1 = TagKey.create("k1");
  private static final TagKey K2 = TagKey.create("k2");
  private static final List<TagKey> TAG_KEYS = Arrays.asList(K1, K2);

  // tag values
  private static final TagValue V1 = TagValue.create("v1");
  private static final TagValue V2 = TagValue.create("v2");
  private static final TagValue V10 = TagValue.create("v10");
  private static final TagValue V20 = TagValue.create("v20");

  private static final AggregationWindow CUMULATIVE = Cumulative.create();
  private static final AggregationWindow INTERVAL_HOUR = Interval.create(Duration.create(3600, 0));

  private static final BucketBoundaries BUCKET_BOUNDARIES =
      BucketBoundaries.create(Arrays.asList(10.0, 20.0, 30.0, 40.0));

  private static final Aggregation DISTRIBUTION = Distribution.create(BUCKET_BOUNDARIES);

  private static final ImmutableMap<List<TagValue>, DistributionData> ENTRIES =
      ImmutableMap.of(
          Arrays.asList(V1, V2),
          DistributionData.create(1, 1, 1, 1, 0, Arrays.asList(0L, 1L, 0L)),
          Arrays.asList(V10, V20),
          DistributionData.create(-5, 6, -20, 5, 100.1, Arrays.asList(5L, 0L, 1L)));

  // name
  private static final View.Name NAME = View.Name.create("test-view");
  // description
  private static final String DESCRIPTION = "test-view-descriptor description";
  // measure
  private static final Measure MEASURE_DOUBLE =
      Measure.MeasureDouble.create("measure1", "measure description", "1");
  private static final Measure MEASURE_LONG =
      Measure.MeasureLong.create("measure2", "measure description", "1");
}
