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
import io.opencensus.stats.Aggregation.Distribution;
import io.opencensus.stats.AggregationData.DistributionData;
import io.opencensus.stats.View.AggregationWindow;
import io.opencensus.stats.View.AggregationWindow.Cumulative;
import io.opencensus.stats.View.AggregationWindow.Interval;
import io.opencensus.stats.ViewData.AggregationWindowData;
import io.opencensus.stats.ViewData.AggregationWindowData.CumulativeData;
import io.opencensus.stats.ViewData.AggregationWindowData.IntervalData;
import io.opencensus.tags.TagKey.TagKeyString;
import io.opencensus.tags.TagValue;
import io.opencensus.tags.TagValue.TagValueString;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for class {@link ViewData}. */
@RunWith(JUnit4.class)
public final class ViewDataTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testCumulativeViewData() {
    View view = View.create(NAME, DESCRIPTION, MEASURE, DISTRIBUTION, tagKeys, CUMULATIVE);
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
    View view = View.create(NAME, DESCRIPTION, MEASURE, DISTRIBUTION, tagKeys, INTERVAL_HOUR);
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
        View.create(NAME, DESCRIPTION, MEASURE, DISTRIBUTION, tagKeys, CUMULATIVE);
    View intervalView =
        View.create(NAME, DESCRIPTION, MEASURE, DISTRIBUTION, tagKeys, INTERVAL_HOUR);

    new EqualsTester()
        .addEqualityGroup(
            ViewData.create(
                cumulativeView,
                ENTRIES,
                CumulativeData.create(
                    Timestamp.fromMillis(1000), Timestamp.fromMillis(2000))),
            ViewData.create(
                cumulativeView,
                ENTRIES,
                CumulativeData.create(
                    Timestamp.fromMillis(1000), Timestamp.fromMillis(2000))))
        .addEqualityGroup(
            ViewData.create(
                cumulativeView,
                ENTRIES,
                CumulativeData.create(
                    Timestamp.fromMillis(1000), Timestamp.fromMillis(3000))))
        .addEqualityGroup(
            ViewData.create(
                intervalView,
                ENTRIES,
                IntervalData.create(Timestamp.fromMillis(2000))),
            ViewData.create(
                intervalView,
                ENTRIES,
                IntervalData.create(Timestamp.fromMillis(2000))))
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
    ViewData.create(
        View.create(NAME, DESCRIPTION, MEASURE, DISTRIBUTION, tagKeys, INTERVAL_HOUR),
        ENTRIES,
        CumulativeData.create(
            Timestamp.fromMillis(1000), Timestamp.fromMillis(2000)));
  }

  @Test
  public void preventWindowAndAggregationWindowDataMismatch2() {
    thrown.expect(IllegalArgumentException.class);
    ViewData.create(
        View.create(NAME, DESCRIPTION, MEASURE, DISTRIBUTION, tagKeys, CUMULATIVE),
        ENTRIES,
        IntervalData.create(Timestamp.fromMillis(1000)));
  }

  @Test
  public void preventStartTimeLaterThanEndTime() {
    thrown.expect(IllegalArgumentException.class);
    CumulativeData.create(Timestamp.fromMillis(3000), Timestamp.fromMillis(2000));
  }

  // tag keys
  private static final TagKeyString K1 = TagKeyString.create("k1");
  private static final TagKeyString K2 = TagKeyString.create("k2");
  private final List<TagKeyString> tagKeys = Arrays.asList(K1, K2);

  // tag values
  private static final TagValueString V1 = TagValueString.create("v1");
  private static final TagValueString V2 = TagValueString.create("v2");
  private static final TagValueString V10 = TagValueString.create("v10");
  private static final TagValueString V20 = TagValueString.create("v20");

  private static final AggregationWindow CUMULATIVE = Cumulative.create();
  private static final AggregationWindow INTERVAL_HOUR = Interval.create(Duration.create(3600, 0));

  private static final BucketBoundaries BUCKET_BOUNDARIES = BucketBoundaries.create(
      Arrays.asList(10.0, 20.0, 30.0, 40.0));

  private static final Aggregation DISTRIBUTION = Distribution.create(BUCKET_BOUNDARIES);
  
  private static final ImmutableMap<List<TagValueString>, DistributionData> ENTRIES =
      ImmutableMap.of(
          Arrays.asList(V1, V2),
          DistributionData.create(1, 1, 1, 1, 0, 0, 1, 0),
          Arrays.asList(V10, V20),
          DistributionData.create(1, 1, 1, 1, 0, 0, 1, 0));

  // name
  private static final View.Name NAME = View.Name.create("test-view");
  // description
  private static final String DESCRIPTION = "test-view-descriptor description";
  // measure
  private static final Measure MEASURE = Measure.MeasureDouble.create(
      "measure",
      "measure description",
      "1");
}
