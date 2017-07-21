/*
 * Copyright 2016, Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.common.Timestamp;
import io.opencensus.stats.Aggregation.Count;
import io.opencensus.stats.Aggregation.Histogram;
import io.opencensus.stats.Aggregation.Mean;
import io.opencensus.stats.Aggregation.Range;
import io.opencensus.stats.Aggregation.StdDev;
import io.opencensus.stats.Aggregation.Sum;
import io.opencensus.stats.AggregationData.CountData;
import io.opencensus.stats.AggregationData.HistogramData;
import io.opencensus.stats.AggregationData.MeanData;
import io.opencensus.stats.AggregationData.RangeData;
import io.opencensus.stats.AggregationData.StdDevData;
import io.opencensus.stats.AggregationData.SumData;
import io.opencensus.stats.View.Window.CumulativeWindow;
import io.opencensus.stats.ViewData.WindowData;
import io.opencensus.stats.ViewData.WindowData.CumulativeWindowData;
import io.opencensus.stats.ViewData.WindowData.IntervalWindowData;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for class {@link ViewData}. */
@RunWith(JUnit4.class)
public final class ViewDataTest {
  
  @Test
  public void testDistributionViewData() {
    final View view =
        View.create(name, description, measure, AGGREGATIONS, tagKeys, CumulativeWindow.create());
    final Timestamp start = Timestamp.fromMillis(1000);
    final Timestamp end = Timestamp.fromMillis(2000);
    final WindowData windowData = CumulativeWindowData.create(start, end);
    final ViewData viewData = ViewData.create(view, ENTRIES, windowData);
    assertThat(viewData.getView()).isEqualTo(view);
    assertThat(viewData.getAggregationMap()).isEqualTo(ENTRIES);
    viewData.getWindowData().match(
        new Function<CumulativeWindowData, Void>() {
          @Override
          public Void apply(CumulativeWindowData windowData) {
            assertThat(windowData.getStart()).isEqualTo(start);
            assertThat(windowData.getEnd()).isEqualTo(end);
            return null;
          }
        },
        new Function<IntervalWindowData, Void>() {
          @Override
          public Void apply(IntervalWindowData windowData) {
            fail("CumulativeWindowData expected.");
            return null;
          }
        },
        Functions.<Void>throwIllegalArgumentException());
  }

  @Test
  public void testIntervalViewData() {
    final View view = View.create(
        name, description, measure, AGGREGATIONS, tagKeys, RpcViewConstants.INTERVAL_HOUR);
    final Timestamp end = Timestamp.fromMillis(2000);
    final WindowData windowData = IntervalWindowData.create(end);
    final ViewData viewData = ViewData.create(view, ENTRIES, windowData);
    assertThat(viewData.getView()).isEqualTo(view);
    assertThat(viewData.getAggregationMap()).isEqualTo(ENTRIES);
    viewData.getWindowData().match(
        new Function<CumulativeWindowData, Void>() {
          @Override
          public Void apply(CumulativeWindowData windowData) {
            fail("IntervalWindowData expected.");
            return null;
          }
        },
        new Function<IntervalWindowData, Void>() {
          @Override
          public Void apply(IntervalWindowData windowData) {
            assertThat(windowData.getEnd()).isEqualTo(end);
            return null;
          }
        },
        Functions.<Void>throwIllegalArgumentException());
  }

  @Test
  public void testViewDataEquals() {
    View dView = 
        View.create(name, description, measure, AGGREGATIONS, tagKeys, RpcViewConstants.CUMULATIVE);
    View iView =
        View.create(
            name, description, measure, AGGREGATIONS, tagKeys, RpcViewConstants.INTERVAL_HOUR);

    new EqualsTester()
        .addEqualityGroup(
            ViewData.create(
                dView, ENTRIES,
                CumulativeWindowData.create(
                    Timestamp.fromMillis(1000), Timestamp.fromMillis(2000))),
            ViewData.create(
                dView,
                ENTRIES,
                CumulativeWindowData.create(
                    Timestamp.fromMillis(1000), Timestamp.fromMillis(2000))))
        .addEqualityGroup(
            ViewData.create(
                dView,
                ENTRIES,
                CumulativeWindowData.create(
                    Timestamp.fromMillis(1000), Timestamp.fromMillis(3000))))
        .addEqualityGroup(
            ViewData.create(
                iView, ENTRIES,
                IntervalWindowData.create(Timestamp.fromMillis(2000))),
            ViewData.create(
                iView, ENTRIES,
                IntervalWindowData.create(Timestamp.fromMillis(2000))))
        .addEqualityGroup(
            ViewData.create(
                iView, Collections.<List<TagValue>, List<AggregationData>>emptyMap(),
                IntervalWindowData.create(Timestamp.fromMillis(2000))))
        .testEquals();
  }

  // tag keys
  private static final TagKey K1 = TagKey.create("k1");
  private static final TagKey K2 = TagKey.create("k2");
  private final List<TagKey> tagKeys = Arrays.asList(K1, K2);

  // tag values
  private static final TagValue V1 = TagValue.create("v1");
  private static final TagValue V2 = TagValue.create("v2");
  private static final TagValue V10 = TagValue.create("v10");
  private static final TagValue V20 = TagValue.create("v20");

  private static final BucketBoundaries BUCKET_BOUNDARIES = BucketBoundaries.create(
      Arrays.asList(10.0, 20.0, 30.0, 40.0));
  
  private static final List<Aggregation> AGGREGATIONS = Collections.unmodifiableList(Arrays.asList(
      Sum.create(), Count.create(), Range.create(), Histogram.create(BUCKET_BOUNDARIES), 
      Mean.create(), StdDev.create()));
  
  private static final ImmutableMap<List<TagValue>, List<AggregationData>> ENTRIES = ImmutableMap.of(
      Arrays.asList(V1, V2),
      Arrays.asList(SumData.create(0), CountData.create(1), HistogramData.create(1, 0, 0, 0, 0),
          RangeData.create(0, 0), MeanData.create(0), StdDevData.create(0)),
      Arrays.asList(V10, V20),
      Arrays.asList(SumData.create(50), CountData.create(2), HistogramData.create(0, 0, 2, 0, 0),
          RangeData.create(25, 25), MeanData.create(25), StdDevData.create(0)));

  // name
  private final View.Name name = View.Name.create("test-view");
  // description
  private final String description = "test-view-descriptor description";
  // measurement descriptor
  private final Measure measure = Measure.MeasureDouble.create(
      "measure",
      "measure description",
      "1");
}
