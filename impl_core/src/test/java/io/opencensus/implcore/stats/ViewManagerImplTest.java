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

package io.opencensus.implcore.stats;

import static com.google.common.truth.Truth.assertThat;
import static io.opencensus.implcore.stats.StatsTestUtil.assertAggregationMapEquals;
import static io.opencensus.implcore.stats.StatsTestUtil.createAggregationData;
import static io.opencensus.implcore.stats.StatsTestUtil.createEmptyViewData;

import com.google.common.collect.ImmutableMap;
import io.opencensus.common.Duration;
import io.opencensus.common.Timestamp;
import io.opencensus.implcore.internal.SimpleEventQueue;
import io.opencensus.implcore.tags.TagsComponentImplBase;
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.Aggregation.Distribution;
import io.opencensus.stats.Aggregation.LastValue;
import io.opencensus.stats.Aggregation.Mean;
import io.opencensus.stats.Aggregation.Sum;
import io.opencensus.stats.AggregationData;
import io.opencensus.stats.AggregationData.LastValueDataDouble;
import io.opencensus.stats.AggregationData.LastValueDataLong;
import io.opencensus.stats.AggregationData.MeanData;
import io.opencensus.stats.AggregationData.SumDataDouble;
import io.opencensus.stats.AggregationData.SumDataLong;
import io.opencensus.stats.BucketBoundaries;
import io.opencensus.stats.Measure;
import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.stats.Measure.MeasureLong;
import io.opencensus.stats.MeasureMap;
import io.opencensus.stats.StatsCollectionState;
import io.opencensus.stats.View;
import io.opencensus.stats.View.AggregationWindow.Cumulative;
import io.opencensus.stats.View.AggregationWindow.Interval;
import io.opencensus.stats.View.Name;
import io.opencensus.stats.ViewData;
import io.opencensus.stats.ViewData.AggregationWindowData;
import io.opencensus.stats.ViewData.AggregationWindowData.CumulativeData;
import io.opencensus.stats.ViewData.AggregationWindowData.IntervalData;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import io.opencensus.tags.Tagger;
import io.opencensus.tags.TagsComponent;
import io.opencensus.testing.common.TestClock;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link ViewManagerImpl}. */
@RunWith(JUnit4.class)
public class ViewManagerImplTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  private static final TagKey KEY = TagKey.create("KEY");

  private static final TagValue VALUE = TagValue.create("VALUE");
  private static final TagValue VALUE_2 = TagValue.create("VALUE_2");

  private static final String MEASURE_NAME = "my measurement";

  private static final String MEASURE_NAME_2 = "my measurement 2";

  private static final String MEASURE_UNIT = "us";

  private static final String MEASURE_DESCRIPTION = "measure description";

  private static final MeasureDouble MEASURE_DOUBLE =
      MeasureDouble.create(MEASURE_NAME, MEASURE_DESCRIPTION, MEASURE_UNIT);

  private static final MeasureLong MEASURE_LONG =
      MeasureLong.create(MEASURE_NAME_2, MEASURE_DESCRIPTION, MEASURE_UNIT);

  private static final Name VIEW_NAME = Name.create("my view");
  private static final Name VIEW_NAME_2 = Name.create("my view 2");

  private static final String VIEW_DESCRIPTION = "view description";

  private static final Cumulative CUMULATIVE = Cumulative.create();

  private static final double EPSILON = 1e-7;
  private static final long MILLIS_PER_SECOND = 1000;
  private static final Duration TEN_SECONDS = Duration.create(10, 0);
  private static final Interval INTERVAL = Interval.create(TEN_SECONDS);

  private static final BucketBoundaries BUCKET_BOUNDARIES =
      BucketBoundaries.create(
          Arrays.asList(
              0.0, 0.2, 0.5, 1.0, 2.0, 3.0, 4.0, 5.0, 7.0, 10.0, 15.0, 20.0, 30.0, 40.0, 50.0));

  private static final Sum SUM = Sum.create();
  private static final Mean MEAN = Mean.create();
  private static final Distribution DISTRIBUTION = Distribution.create(BUCKET_BOUNDARIES);
  private static final LastValue LAST_VALUE = LastValue.create();

  private final TestClock clock = TestClock.create();

  private final StatsComponentImplBase statsComponent =
      new StatsComponentImplBase(new SimpleEventQueue(), clock);
  private final TagsComponent tagsComponent = new TagsComponentImplBase();

  private final Tagger tagger = tagsComponent.getTagger();
  private final ViewManagerImpl viewManager = statsComponent.getViewManager();
  private final StatsRecorderImpl statsRecorder = statsComponent.getStatsRecorder();

  private static View createCumulativeView() {
    return createCumulativeView(VIEW_NAME, MEASURE_DOUBLE, DISTRIBUTION, Arrays.asList(KEY));
  }

  private static View createCumulativeView(
      View.Name name, Measure measure, Aggregation aggregation, List<TagKey> keys) {
    return View.create(name, VIEW_DESCRIPTION, measure, aggregation, keys, CUMULATIVE);
  }

  @Test
  public void testRegisterAndGetCumulativeView() {
    View view = createCumulativeView();
    viewManager.registerView(view);
    assertThat(viewManager.getView(VIEW_NAME).getView()).isEqualTo(view);
    assertThat(viewManager.getView(VIEW_NAME).getAggregationMap()).isEmpty();
    assertThat(viewManager.getView(VIEW_NAME).getWindowData()).isInstanceOf(CumulativeData.class);
  }

  @Test
  public void testGetAllExportedViews() {
    assertThat(viewManager.getAllExportedViews()).isEmpty();
    View cumulativeView1 =
        createCumulativeView(
            View.Name.create("View 1"), MEASURE_DOUBLE, DISTRIBUTION, Arrays.asList(KEY));
    View cumulativeView2 =
        createCumulativeView(
            View.Name.create("View 2"), MEASURE_DOUBLE, DISTRIBUTION, Arrays.asList(KEY));
    View intervalView =
        View.create(
            View.Name.create("View 3"),
            VIEW_DESCRIPTION,
            MEASURE_DOUBLE,
            DISTRIBUTION,
            Arrays.asList(KEY),
            INTERVAL);
    viewManager.registerView(cumulativeView1);
    viewManager.registerView(cumulativeView2);
    viewManager.registerView(intervalView);

    // Only cumulative views should be exported.
    assertThat(viewManager.getAllExportedViews()).containsExactly(cumulativeView1, cumulativeView2);
  }

  @Test
  public void getAllExportedViewsResultIsUnmodifiable() {
    View view1 =
        View.create(
            View.Name.create("View 1"),
            VIEW_DESCRIPTION,
            MEASURE_DOUBLE,
            DISTRIBUTION,
            Arrays.asList(KEY),
            CUMULATIVE);
    viewManager.registerView(view1);
    Set<View> exported = viewManager.getAllExportedViews();

    View view2 =
        View.create(
            View.Name.create("View 2"),
            VIEW_DESCRIPTION,
            MEASURE_DOUBLE,
            DISTRIBUTION,
            Arrays.asList(KEY),
            CUMULATIVE);
    thrown.expect(UnsupportedOperationException.class);
    exported.add(view2);
  }

  @Test
  public void testRegisterAndGetIntervalView() {
    View intervalView =
        View.create(
            VIEW_NAME,
            VIEW_DESCRIPTION,
            MEASURE_DOUBLE,
            DISTRIBUTION,
            Arrays.asList(KEY),
            INTERVAL);
    viewManager.registerView(intervalView);
    assertThat(viewManager.getView(VIEW_NAME).getView()).isEqualTo(intervalView);
    assertThat(viewManager.getView(VIEW_NAME).getAggregationMap()).isEmpty();
    assertThat(viewManager.getView(VIEW_NAME).getWindowData()).isInstanceOf(IntervalData.class);
  }

  @Test
  public void allowRegisteringSameViewTwice() {
    View view = createCumulativeView();
    viewManager.registerView(view);
    viewManager.registerView(view);
    assertThat(viewManager.getView(VIEW_NAME).getView()).isEqualTo(view);
  }

  @Test
  public void preventRegisteringDifferentViewWithSameName() {
    View view1 =
        View.create(
            VIEW_NAME,
            "View description.",
            MEASURE_DOUBLE,
            DISTRIBUTION,
            Arrays.asList(KEY),
            CUMULATIVE);
    View view2 =
        View.create(
            VIEW_NAME,
            "This is a different description.",
            MEASURE_DOUBLE,
            DISTRIBUTION,
            Arrays.asList(KEY),
            CUMULATIVE);
    testFailedToRegisterView(
        view1, view2, "A different view with the same name is already registered");
  }

  @Test
  public void preventRegisteringDifferentMeasureWithSameName() {
    MeasureDouble measure1 = MeasureDouble.create("measure", "description", "1");
    MeasureLong measure2 = MeasureLong.create("measure", "description", "1");
    View view1 =
        View.create(
            VIEW_NAME, VIEW_DESCRIPTION, measure1, DISTRIBUTION, Arrays.asList(KEY), CUMULATIVE);
    View view2 =
        View.create(
            VIEW_NAME_2, VIEW_DESCRIPTION, measure2, DISTRIBUTION, Arrays.asList(KEY), CUMULATIVE);
    testFailedToRegisterView(
        view1, view2, "A different measure with the same name is already registered");
  }

  private void testFailedToRegisterView(View view1, View view2, String message) {
    viewManager.registerView(view1);
    try {
      thrown.expect(IllegalArgumentException.class);
      thrown.expectMessage(message);
      viewManager.registerView(view2);
    } finally {
      assertThat(viewManager.getView(VIEW_NAME).getView()).isEqualTo(view1);
    }
  }

  @Test
  public void returnNullWhenGettingNonexistentViewData() {
    assertThat(viewManager.getView(VIEW_NAME)).isNull();
  }

  @Test
  public void testRecordDouble_distribution_cumulative() {
    testRecordCumulative(MEASURE_DOUBLE, DISTRIBUTION, 10.0, 20.0, 30.0, 40.0);
  }

  @Test
  public void testRecordLong_distribution_cumulative() {
    testRecordCumulative(MEASURE_LONG, DISTRIBUTION, 1000, 2000, 3000, 4000);
  }

  @Test
  public void testRecordDouble_sum_cumulative() {
    testRecordCumulative(MEASURE_DOUBLE, SUM, 11.1, 22.2, 33.3, 44.4);
  }

  @Test
  public void testRecordLong_sum_cumulative() {
    testRecordCumulative(MEASURE_LONG, SUM, 1000, 2000, 3000, 4000);
  }

  @Test
  public void testRecordDouble_lastvalue_cumulative() {
    testRecordCumulative(MEASURE_DOUBLE, LAST_VALUE, 11.1, 22.2, 33.3, 44.4);
  }

  @Test
  public void testRecordLong_lastvalue_cumulative() {
    testRecordCumulative(MEASURE_LONG, LAST_VALUE, 1000, 2000, 3000, 4000);
  }

  private void testRecordCumulative(Measure measure, Aggregation aggregation, double... values) {
    View view = createCumulativeView(VIEW_NAME, measure, aggregation, Arrays.asList(KEY));
    clock.setTime(Timestamp.create(1, 2));
    viewManager.registerView(view);
    TagContext tags = tagger.emptyBuilder().put(KEY, VALUE).build();
    for (double val : values) {
      putToMeasureMap(statsRecorder.newMeasureMap(), measure, val).record(tags);
    }
    clock.setTime(Timestamp.create(3, 4));
    ViewData viewData = viewManager.getView(VIEW_NAME);
    assertThat(viewData.getView()).isEqualTo(view);
    assertThat(viewData.getWindowData())
        .isEqualTo(CumulativeData.create(Timestamp.create(1, 2), Timestamp.create(3, 4)));
    StatsTestUtil.assertAggregationMapEquals(
        viewData.getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE),
            StatsTestUtil.createAggregationData(aggregation, measure, values)),
        EPSILON);
  }

  @Test
  public void testRecordDouble_mean_interval() {
    testRecordInterval(
        MEASURE_DOUBLE,
        MEAN,
        new double[] {20.0, -1.0, 1.0, -5.0, 5.0},
        9.0,
        30.0,
        MeanData.create((19 * 0.6 + 1) / 4, 4),
        MeanData.create(0.2 * 5 + 9, 1),
        MeanData.create(30.0, 1));
  }

  @Test
  public void testRecordLong_mean_interval() {
    testRecordInterval(
        MEASURE_LONG,
        MEAN,
        new double[] {1000, 2000, 3000, 4000, 5000},
        -5000,
        30,
        MeanData.create((3000 * 0.6 + 12000) / 4, 4),
        MeanData.create(-4000, 1),
        MeanData.create(30, 1));
  }

  @Test
  public void testRecordDouble_sum_interval() {
    testRecordInterval(
        MEASURE_DOUBLE,
        SUM,
        new double[] {20.0, -1.0, 1.0, -5.0, 5.0},
        9.0,
        30.0,
        SumDataDouble.create(19 * 0.6 + 1),
        SumDataDouble.create(0.2 * 5 + 9),
        SumDataDouble.create(30.0));
  }

  @Test
  public void testRecordLong_sum_interval() {
    testRecordInterval(
        MEASURE_LONG,
        SUM,
        new double[] {10, 24, 30, 40, 50},
        -50,
        30,
        SumDataLong.create(Math.round(34 * 0.6 + 120)),
        SumDataLong.create(-40),
        SumDataLong.create(30));
  }

  @Test
  public void testRecordDouble_lastvalue_interval() {
    testRecordInterval(
        MEASURE_DOUBLE,
        LAST_VALUE,
        new double[] {20.0, -1.0, 1.0, -5.0, 5.0},
        9.0,
        30.0,
        LastValueDataDouble.create(5.0),
        LastValueDataDouble.create(9.0),
        LastValueDataDouble.create(30.0));
  }

  @Test
  public void testRecordLong_lastvalue_interval() {
    testRecordInterval(
        MEASURE_LONG,
        LAST_VALUE,
        new double[] {1000, 2000, 3000, 4000, 5000},
        -5000,
        30,
        LastValueDataLong.create(5000),
        LastValueDataLong.create(-5000),
        LastValueDataLong.create(30));
  }

  private final void testRecordInterval(
      Measure measure,
      Aggregation aggregation,
      double[] initialValues, /* There are 5 initial values recorded before we call getView(). */
      double value6,
      double value7,
      AggregationData expectedValues1,
      AggregationData expectedValues2,
      AggregationData expectedValues3) {
    // The interval is 10 seconds, i.e. values should expire after 10 seconds.
    // Each bucket has a duration of 2.5 seconds.
    View view =
        View.create(
            VIEW_NAME,
            VIEW_DESCRIPTION,
            measure,
            aggregation,
            Arrays.asList(KEY),
            Interval.create(TEN_SECONDS));
    long startTimeMillis = 30 * MILLIS_PER_SECOND; // start at 30s
    clock.setTime(Timestamp.fromMillis(startTimeMillis));
    viewManager.registerView(view);

    TagContext tags = tagger.emptyBuilder().put(KEY, VALUE).build();

    for (int i = 1; i <= 5; i++) {
      /*
       * Add each value in sequence, at 31s, 32s, 33s, etc.
       * 1st and 2nd values should fall into the first bucket [30.0, 32.5),
       * 3rd and 4th values should fall into the second bucket [32.5, 35.0),
       * 5th value should fall into the third bucket [35.0, 37.5).
       */
      clock.setTime(Timestamp.fromMillis(startTimeMillis + i * MILLIS_PER_SECOND));
      putToMeasureMap(statsRecorder.newMeasureMap(), measure, initialValues[i - 1]).record(tags);
    }

    clock.setTime(Timestamp.fromMillis(startTimeMillis + 8 * MILLIS_PER_SECOND));
    // 38s, no values should have expired
    StatsTestUtil.assertAggregationMapEquals(
        viewManager.getView(VIEW_NAME).getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE),
            StatsTestUtil.createAggregationData(aggregation, measure, initialValues)),
        EPSILON);

    clock.setTime(Timestamp.fromMillis(startTimeMillis + 11 * MILLIS_PER_SECOND));
    // 41s, 40% of the values in the first bucket should have expired (1 / 2.5 = 0.4).
    StatsTestUtil.assertAggregationMapEquals(
        viewManager.getView(VIEW_NAME).getAggregationMap(),
        ImmutableMap.of(Arrays.asList(VALUE), expectedValues1),
        EPSILON);

    clock.setTime(Timestamp.fromMillis(startTimeMillis + 12 * MILLIS_PER_SECOND));
    // 42s, add a new value value1, should fall into bucket [40.0, 42.5)
    putToMeasureMap(statsRecorder.newMeasureMap(), measure, value6).record(tags);

    clock.setTime(Timestamp.fromMillis(startTimeMillis + 17 * MILLIS_PER_SECOND));
    // 47s, values in the first and second bucket should have expired, and 80% of values in the
    // third bucket should have expired. The new value should persist.
    StatsTestUtil.assertAggregationMapEquals(
        viewManager.getView(VIEW_NAME).getAggregationMap(),
        ImmutableMap.of(Arrays.asList(VALUE), expectedValues2),
        EPSILON);

    clock.setTime(Timestamp.fromMillis(60 * MILLIS_PER_SECOND));
    // 60s, all previous values should have expired, add another value value2
    putToMeasureMap(statsRecorder.newMeasureMap(), measure, value7).record(tags);
    StatsTestUtil.assertAggregationMapEquals(
        viewManager.getView(VIEW_NAME).getAggregationMap(),
        ImmutableMap.of(Arrays.asList(VALUE), expectedValues3),
        EPSILON);

    clock.setTime(Timestamp.fromMillis(100 * MILLIS_PER_SECOND));
    // 100s, all values should have expired
    assertThat(viewManager.getView(VIEW_NAME).getAggregationMap()).isEmpty();
  }

  @Test
  public void getViewDoesNotClearStats() {
    View view = createCumulativeView(VIEW_NAME, MEASURE_DOUBLE, DISTRIBUTION, Arrays.asList(KEY));
    clock.setTime(Timestamp.create(10, 0));
    viewManager.registerView(view);
    TagContext tags = tagger.emptyBuilder().put(KEY, VALUE).build();
    statsRecorder.newMeasureMap().put(MEASURE_DOUBLE, 0.1).record(tags);
    clock.setTime(Timestamp.create(11, 0));
    ViewData viewData1 = viewManager.getView(VIEW_NAME);
    assertThat(viewData1.getWindowData())
        .isEqualTo(CumulativeData.create(Timestamp.create(10, 0), Timestamp.create(11, 0)));
    StatsTestUtil.assertAggregationMapEquals(
        viewData1.getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE),
            StatsTestUtil.createAggregationData(DISTRIBUTION, MEASURE_DOUBLE, 0.1)),
        EPSILON);

    statsRecorder.newMeasureMap().put(MEASURE_DOUBLE, 0.2).record(tags);
    clock.setTime(Timestamp.create(12, 0));
    ViewData viewData2 = viewManager.getView(VIEW_NAME);

    // The second view should have the same start time as the first view, and it should include both
    // recorded values:
    assertThat(viewData2.getWindowData())
        .isEqualTo(CumulativeData.create(Timestamp.create(10, 0), Timestamp.create(12, 0)));
    StatsTestUtil.assertAggregationMapEquals(
        viewData2.getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE),
            StatsTestUtil.createAggregationData(DISTRIBUTION, MEASURE_DOUBLE, 0.1, 0.2)),
        EPSILON);
  }

  @Test
  public void testRecordCumulativeMultipleTagValues() {
    viewManager.registerView(
        createCumulativeView(VIEW_NAME, MEASURE_DOUBLE, DISTRIBUTION, Arrays.asList(KEY)));
    statsRecorder
        .newMeasureMap()
        .put(MEASURE_DOUBLE, 10.0)
        .record(tagger.emptyBuilder().put(KEY, VALUE).build());
    statsRecorder
        .newMeasureMap()
        .put(MEASURE_DOUBLE, 30.0)
        .record(tagger.emptyBuilder().put(KEY, VALUE_2).build());
    statsRecorder
        .newMeasureMap()
        .put(MEASURE_DOUBLE, 50.0)
        .record(tagger.emptyBuilder().put(KEY, VALUE_2).build());
    ViewData viewData = viewManager.getView(VIEW_NAME);
    assertAggregationMapEquals(
        viewData.getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE),
            createAggregationData(DISTRIBUTION, MEASURE_DOUBLE, 10.0),
            Arrays.asList(VALUE_2),
            createAggregationData(DISTRIBUTION, MEASURE_DOUBLE, 30.0, 50.0)),
        EPSILON);
  }

  @Test
  public void testRecordIntervalMultipleTagValues() {
    // The interval is 10 seconds, i.e. values should expire after 10 seconds.
    View view =
        View.create(
            VIEW_NAME,
            VIEW_DESCRIPTION,
            MEASURE_DOUBLE,
            DISTRIBUTION,
            Arrays.asList(KEY),
            Interval.create(TEN_SECONDS));
    clock.setTime(Timestamp.create(10, 0)); // Start at 10s
    viewManager.registerView(view);

    // record for TagValue1 at 11s
    clock.setTime(Timestamp.fromMillis(11 * MILLIS_PER_SECOND));
    statsRecorder
        .newMeasureMap()
        .put(MEASURE_DOUBLE, 10.0)
        .record(tagger.emptyBuilder().put(KEY, VALUE).build());

    // record for TagValue2 at 15s
    clock.setTime(Timestamp.fromMillis(15 * MILLIS_PER_SECOND));
    statsRecorder
        .newMeasureMap()
        .put(MEASURE_DOUBLE, 30.0)
        .record(tagger.emptyBuilder().put(KEY, VALUE_2).build());
    statsRecorder
        .newMeasureMap()
        .put(MEASURE_DOUBLE, 50.0)
        .record(tagger.emptyBuilder().put(KEY, VALUE_2).build());

    // get ViewData at 19s, no stats should have expired.
    clock.setTime(Timestamp.fromMillis(19 * MILLIS_PER_SECOND));
    ViewData viewData1 = viewManager.getView(VIEW_NAME);
    StatsTestUtil.assertAggregationMapEquals(
        viewData1.getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE),
            StatsTestUtil.createAggregationData(DISTRIBUTION, MEASURE_DOUBLE, 10.0),
            Arrays.asList(VALUE_2),
            StatsTestUtil.createAggregationData(DISTRIBUTION, MEASURE_DOUBLE, 30.0, 50.0)),
        EPSILON);

    // record for TagValue2 again at 20s
    clock.setTime(Timestamp.fromMillis(20 * MILLIS_PER_SECOND));
    statsRecorder
        .newMeasureMap()
        .put(MEASURE_DOUBLE, 40.0)
        .record(tagger.emptyBuilder().put(KEY, VALUE_2).build());

    // get ViewData at 25s, stats for TagValue1 should have expired.
    clock.setTime(Timestamp.fromMillis(25 * MILLIS_PER_SECOND));
    ViewData viewData2 = viewManager.getView(VIEW_NAME);
    StatsTestUtil.assertAggregationMapEquals(
        viewData2.getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE_2),
            StatsTestUtil.createAggregationData(DISTRIBUTION, MEASURE_DOUBLE, 30.0, 50.0, 40.0)),
        EPSILON);

    // get ViewData at 30s, the first two values for TagValue2 should have expired.
    clock.setTime(Timestamp.fromMillis(30 * MILLIS_PER_SECOND));
    ViewData viewData3 = viewManager.getView(VIEW_NAME);
    StatsTestUtil.assertAggregationMapEquals(
        viewData3.getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE_2),
            StatsTestUtil.createAggregationData(DISTRIBUTION, MEASURE_DOUBLE, 40.0)),
        EPSILON);

    // get ViewData at 40s, all stats should have expired.
    clock.setTime(Timestamp.fromMillis(40 * MILLIS_PER_SECOND));
    ViewData viewData4 = viewManager.getView(VIEW_NAME);
    assertThat(viewData4.getAggregationMap()).isEmpty();
  }

  // This test checks that MeasureMaper.record(...) does not throw an exception when no views are
  // registered.
  @Test
  public void allowRecordingWithoutRegisteringMatchingViewData() {
    statsRecorder
        .newMeasureMap()
        .put(MEASURE_DOUBLE, 10)
        .record(tagger.emptyBuilder().put(KEY, VALUE).build());
  }

  @Test
  public void testRecordWithEmptyStatsContext() {
    viewManager.registerView(
        createCumulativeView(VIEW_NAME, MEASURE_DOUBLE, DISTRIBUTION, Arrays.asList(KEY)));
    // DEFAULT doesn't have tags, but the view has tag key "KEY".
    statsRecorder.newMeasureMap().put(MEASURE_DOUBLE, 10.0).record(tagger.empty());
    ViewData viewData = viewManager.getView(VIEW_NAME);
    assertAggregationMapEquals(
        viewData.getAggregationMap(),
        ImmutableMap.of(
            // Tag is missing for associated measureValues, should use default tag value
            // "unknown/not set".
            Arrays.asList(RecordUtils.UNKNOWN_TAG_VALUE),
            // Should record stats with default tag value: "KEY" : "unknown/not set".
            createAggregationData(DISTRIBUTION, MEASURE_DOUBLE, 10.0)),
        EPSILON);
  }

  @Test
  public void testRecord_MeasureNameNotMatch() {
    testRecord_MeasureNotMatch(
        MeasureDouble.create(MEASURE_NAME, "measure", MEASURE_UNIT),
        MeasureDouble.create(MEASURE_NAME_2, "measure", MEASURE_UNIT),
        10.0);
  }

  @Test
  public void testRecord_MeasureTypeNotMatch() {
    testRecord_MeasureNotMatch(
        MeasureLong.create(MEASURE_NAME, "measure", MEASURE_UNIT),
        MeasureDouble.create(MEASURE_NAME, "measure", MEASURE_UNIT),
        10.0);
  }

  private void testRecord_MeasureNotMatch(Measure measure1, Measure measure2, double value) {
    viewManager.registerView(createCumulativeView(VIEW_NAME, measure1, MEAN, Arrays.asList(KEY)));
    TagContext tags = tagger.emptyBuilder().put(KEY, VALUE).build();
    putToMeasureMap(statsRecorder.newMeasureMap(), measure2, value).record(tags);
    ViewData view = viewManager.getView(VIEW_NAME);
    assertThat(view.getAggregationMap()).isEmpty();
  }

  @Test
  public void testRecordWithTagsThatDoNotMatchViewData() {
    viewManager.registerView(
        createCumulativeView(VIEW_NAME, MEASURE_DOUBLE, DISTRIBUTION, Arrays.asList(KEY)));
    statsRecorder
        .newMeasureMap()
        .put(MEASURE_DOUBLE, 10.0)
        .record(tagger.emptyBuilder().put(TagKey.create("wrong key"), VALUE).build());
    statsRecorder
        .newMeasureMap()
        .put(MEASURE_DOUBLE, 50.0)
        .record(tagger.emptyBuilder().put(TagKey.create("another wrong key"), VALUE).build());
    ViewData viewData = viewManager.getView(VIEW_NAME);
    assertAggregationMapEquals(
        viewData.getAggregationMap(),
        ImmutableMap.of(
            // Won't record the unregistered tag key, for missing registered keys will use default
            // tag value : "unknown/not set".
            Arrays.asList(RecordUtils.UNKNOWN_TAG_VALUE),
            // Should record stats with default tag value: "KEY" : "unknown/not set".
            createAggregationData(DISTRIBUTION, MEASURE_DOUBLE, 10.0, 50.0)),
        EPSILON);
  }

  @Test
  public void testViewDataWithMultipleTagKeys() {
    TagKey key1 = TagKey.create("Key-1");
    TagKey key2 = TagKey.create("Key-2");
    viewManager.registerView(
        createCumulativeView(VIEW_NAME, MEASURE_DOUBLE, DISTRIBUTION, Arrays.asList(key1, key2)));
    statsRecorder
        .newMeasureMap()
        .put(MEASURE_DOUBLE, 1.1)
        .record(
            tagger
                .emptyBuilder()
                .put(key1, TagValue.create("v1"))
                .put(key2, TagValue.create("v10"))
                .build());
    statsRecorder
        .newMeasureMap()
        .put(MEASURE_DOUBLE, 2.2)
        .record(
            tagger
                .emptyBuilder()
                .put(key1, TagValue.create("v1"))
                .put(key2, TagValue.create("v20"))
                .build());
    statsRecorder
        .newMeasureMap()
        .put(MEASURE_DOUBLE, 3.3)
        .record(
            tagger
                .emptyBuilder()
                .put(key1, TagValue.create("v2"))
                .put(key2, TagValue.create("v10"))
                .build());
    statsRecorder
        .newMeasureMap()
        .put(MEASURE_DOUBLE, 4.4)
        .record(
            tagger
                .emptyBuilder()
                .put(key1, TagValue.create("v1"))
                .put(key2, TagValue.create("v10"))
                .build());
    ViewData viewData = viewManager.getView(VIEW_NAME);
    assertAggregationMapEquals(
        viewData.getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(TagValue.create("v1"), TagValue.create("v10")),
            StatsTestUtil.createAggregationData(DISTRIBUTION, MEASURE_DOUBLE, 1.1, 4.4),
            Arrays.asList(TagValue.create("v1"), TagValue.create("v20")),
            StatsTestUtil.createAggregationData(DISTRIBUTION, MEASURE_DOUBLE, 2.2),
            Arrays.asList(TagValue.create("v2"), TagValue.create("v10")),
            StatsTestUtil.createAggregationData(DISTRIBUTION, MEASURE_DOUBLE, 3.3)),
        EPSILON);
  }

  @Test
  public void testMultipleViewSameMeasure() {
    final View view1 =
        createCumulativeView(VIEW_NAME, MEASURE_DOUBLE, DISTRIBUTION, Arrays.asList(KEY));
    final View view2 =
        createCumulativeView(VIEW_NAME_2, MEASURE_DOUBLE, DISTRIBUTION, Arrays.asList(KEY));
    clock.setTime(Timestamp.create(1, 1));
    viewManager.registerView(view1);
    clock.setTime(Timestamp.create(2, 2));
    viewManager.registerView(view2);
    statsRecorder
        .newMeasureMap()
        .put(MEASURE_DOUBLE, 5.0)
        .record(tagger.emptyBuilder().put(KEY, VALUE).build());
    clock.setTime(Timestamp.create(3, 3));
    ViewData viewData1 = viewManager.getView(VIEW_NAME);
    clock.setTime(Timestamp.create(4, 4));
    ViewData viewData2 = viewManager.getView(VIEW_NAME_2);
    assertThat(viewData1.getWindowData())
        .isEqualTo(CumulativeData.create(Timestamp.create(1, 1), Timestamp.create(3, 3)));
    StatsTestUtil.assertAggregationMapEquals(
        viewData1.getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE),
            StatsTestUtil.createAggregationData(DISTRIBUTION, MEASURE_DOUBLE, 5.0)),
        EPSILON);
    assertThat(viewData2.getWindowData())
        .isEqualTo(CumulativeData.create(Timestamp.create(2, 2), Timestamp.create(4, 4)));
    StatsTestUtil.assertAggregationMapEquals(
        viewData2.getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE),
            StatsTestUtil.createAggregationData(DISTRIBUTION, MEASURE_DOUBLE, 5.0)),
        EPSILON);
  }

  @Test
  public void testMultipleViews_DifferentMeasureNames() {
    testMultipleViews_DifferentMeasures(
        MeasureDouble.create(MEASURE_NAME, MEASURE_DESCRIPTION, MEASURE_UNIT),
        MeasureDouble.create(MEASURE_NAME_2, MEASURE_DESCRIPTION, MEASURE_UNIT),
        1.1,
        2.2);
  }

  @Test
  public void testMultipleViews_DifferentMeasureTypes() {
    testMultipleViews_DifferentMeasures(
        MeasureDouble.create(MEASURE_NAME, MEASURE_DESCRIPTION, MEASURE_UNIT),
        MeasureLong.create(MEASURE_NAME_2, MEASURE_DESCRIPTION, MEASURE_UNIT),
        1.1,
        5000);
  }

  private void testMultipleViews_DifferentMeasures(
      Measure measure1, Measure measure2, double value1, double value2) {
    final View view1 = createCumulativeView(VIEW_NAME, measure1, DISTRIBUTION, Arrays.asList(KEY));
    final View view2 =
        createCumulativeView(VIEW_NAME_2, measure2, DISTRIBUTION, Arrays.asList(KEY));
    clock.setTime(Timestamp.create(1, 0));
    viewManager.registerView(view1);
    clock.setTime(Timestamp.create(2, 0));
    viewManager.registerView(view2);
    TagContext tags = tagger.emptyBuilder().put(KEY, VALUE).build();
    MeasureMap measureMap = statsRecorder.newMeasureMap();
    putToMeasureMap(measureMap, measure1, value1);
    putToMeasureMap(measureMap, measure2, value2);
    measureMap.record(tags);
    clock.setTime(Timestamp.create(3, 0));
    ViewData viewData1 = viewManager.getView(VIEW_NAME);
    clock.setTime(Timestamp.create(4, 0));
    ViewData viewData2 = viewManager.getView(VIEW_NAME_2);
    assertThat(viewData1.getWindowData())
        .isEqualTo(CumulativeData.create(Timestamp.create(1, 0), Timestamp.create(3, 0)));
    StatsTestUtil.assertAggregationMapEquals(
        viewData1.getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE),
            StatsTestUtil.createAggregationData(DISTRIBUTION, measure1, value1)),
        EPSILON);
    assertThat(viewData2.getWindowData())
        .isEqualTo(CumulativeData.create(Timestamp.create(2, 0), Timestamp.create(4, 0)));
    StatsTestUtil.assertAggregationMapEquals(
        viewData2.getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE),
            StatsTestUtil.createAggregationData(DISTRIBUTION, measure2, value2)),
        EPSILON);
  }

  @Test
  public void testGetCumulativeViewDataWithEmptyBucketBoundaries() {
    Aggregation noHistogram =
        Distribution.create(BucketBoundaries.create(Collections.<Double>emptyList()));
    View view = createCumulativeView(VIEW_NAME, MEASURE_DOUBLE, noHistogram, Arrays.asList(KEY));
    clock.setTime(Timestamp.create(1, 0));
    viewManager.registerView(view);
    statsRecorder
        .newMeasureMap()
        .put(MEASURE_DOUBLE, 1.1)
        .record(tagger.emptyBuilder().put(KEY, VALUE).build());
    clock.setTime(Timestamp.create(3, 0));
    ViewData viewData = viewManager.getView(VIEW_NAME);
    assertThat(viewData.getWindowData())
        .isEqualTo(CumulativeData.create(Timestamp.create(1, 0), Timestamp.create(3, 0)));
    StatsTestUtil.assertAggregationMapEquals(
        viewData.getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE),
            StatsTestUtil.createAggregationData(noHistogram, MEASURE_DOUBLE, 1.1)),
        EPSILON);
  }

  @Test
  public void testGetCumulativeViewDataWithoutBucketBoundaries() {
    View view = createCumulativeView(VIEW_NAME, MEASURE_DOUBLE, MEAN, Arrays.asList(KEY));
    clock.setTime(Timestamp.create(1, 0));
    viewManager.registerView(view);
    statsRecorder
        .newMeasureMap()
        .put(MEASURE_DOUBLE, 1.1)
        .record(tagger.emptyBuilder().put(KEY, VALUE).build());
    clock.setTime(Timestamp.create(3, 0));
    ViewData viewData = viewManager.getView(VIEW_NAME);
    assertThat(viewData.getWindowData())
        .isEqualTo(CumulativeData.create(Timestamp.create(1, 0), Timestamp.create(3, 0)));
    StatsTestUtil.assertAggregationMapEquals(
        viewData.getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE), StatsTestUtil.createAggregationData(MEAN, MEASURE_DOUBLE, 1.1)),
        EPSILON);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void registerRecordAndGetView_StatsDisabled() {
    statsComponent.setState(StatsCollectionState.DISABLED);
    View view = createCumulativeView(VIEW_NAME, MEASURE_DOUBLE, MEAN, Arrays.asList(KEY));
    viewManager.registerView(view);
    statsRecorder
        .newMeasureMap()
        .put(MEASURE_DOUBLE, 1.1)
        .record(tagger.emptyBuilder().put(KEY, VALUE).build());
    assertThat(viewManager.getView(VIEW_NAME)).isEqualTo(createEmptyViewData(view));
  }

  @Test
  @SuppressWarnings("deprecation")
  public void registerRecordAndGetView_StatsReenabled() {
    statsComponent.setState(StatsCollectionState.DISABLED);
    statsComponent.setState(StatsCollectionState.ENABLED);
    View view = createCumulativeView(VIEW_NAME, MEASURE_DOUBLE, MEAN, Arrays.asList(KEY));
    viewManager.registerView(view);
    statsRecorder
        .newMeasureMap()
        .put(MEASURE_DOUBLE, 1.1)
        .record(tagger.emptyBuilder().put(KEY, VALUE).build());
    StatsTestUtil.assertAggregationMapEquals(
        viewManager.getView(VIEW_NAME).getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE), StatsTestUtil.createAggregationData(MEAN, MEASURE_DOUBLE, 1.1)),
        EPSILON);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void registerViewWithStatsDisabled_RecordAndGetViewWithStatsEnabled() {
    statsComponent.setState(StatsCollectionState.DISABLED);
    View view = createCumulativeView(VIEW_NAME, MEASURE_DOUBLE, MEAN, Arrays.asList(KEY));
    viewManager.registerView(view); // view will still be registered.

    statsComponent.setState(StatsCollectionState.ENABLED);
    statsRecorder
        .newMeasureMap()
        .put(MEASURE_DOUBLE, 1.1)
        .record(tagger.emptyBuilder().put(KEY, VALUE).build());
    StatsTestUtil.assertAggregationMapEquals(
        viewManager.getView(VIEW_NAME).getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE), StatsTestUtil.createAggregationData(MEAN, MEASURE_DOUBLE, 1.1)),
        EPSILON);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void registerDifferentViewWithSameNameWithStatsDisabled() {
    statsComponent.setState(StatsCollectionState.DISABLED);
    View view1 =
        View.create(
            VIEW_NAME,
            "View description.",
            MEASURE_DOUBLE,
            DISTRIBUTION,
            Arrays.asList(KEY),
            CUMULATIVE);
    View view2 =
        View.create(
            VIEW_NAME,
            "This is a different description.",
            MEASURE_DOUBLE,
            DISTRIBUTION,
            Arrays.asList(KEY),
            CUMULATIVE);
    testFailedToRegisterView(
        view1, view2, "A different view with the same name is already registered");
  }

  @Test
  public void settingStateToDisabledWillClearStats_Cumulative() {
    View cumulativeView = createCumulativeView(VIEW_NAME, MEASURE_DOUBLE, MEAN, Arrays.asList(KEY));
    settingStateToDisabledWillClearStats(cumulativeView);
  }

  @Test
  public void settingStateToDisabledWillClearStats_Interval() {
    View intervalView =
        View.create(
            VIEW_NAME_2,
            VIEW_DESCRIPTION,
            MEASURE_DOUBLE,
            MEAN,
            Arrays.asList(KEY),
            Interval.create(Duration.create(60, 0)));
    settingStateToDisabledWillClearStats(intervalView);
  }

  @SuppressWarnings("deprecation")
  private void settingStateToDisabledWillClearStats(View view) {
    Timestamp timestamp1 = Timestamp.create(1, 0);
    clock.setTime(timestamp1);
    viewManager.registerView(view);
    statsRecorder
        .newMeasureMap()
        .put(MEASURE_DOUBLE, 1.1)
        .record(tagger.emptyBuilder().put(KEY, VALUE).build());
    StatsTestUtil.assertAggregationMapEquals(
        viewManager.getView(view.getName()).getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE),
            StatsTestUtil.createAggregationData(view.getAggregation(), view.getMeasure(), 1.1)),
        EPSILON);

    Timestamp timestamp2 = Timestamp.create(2, 0);
    clock.setTime(timestamp2);
    statsComponent.setState(StatsCollectionState.DISABLED); // This will clear stats.
    assertThat(viewManager.getView(view.getName())).isEqualTo(createEmptyViewData(view));

    Timestamp timestamp3 = Timestamp.create(3, 0);
    clock.setTime(timestamp3);
    statsComponent.setState(StatsCollectionState.ENABLED);

    Timestamp timestamp4 = Timestamp.create(4, 0);
    clock.setTime(timestamp4);
    // This ViewData does not have any stats, but it should not be an empty ViewData, since it has
    // non-zero TimeStamps.
    ViewData viewData = viewManager.getView(view.getName());
    assertThat(viewData.getAggregationMap()).isEmpty();
    AggregationWindowData windowData = viewData.getWindowData();
    if (windowData instanceof CumulativeData) {
      assertThat(windowData).isEqualTo(CumulativeData.create(timestamp3, timestamp4));
    } else {
      assertThat(windowData).isEqualTo(IntervalData.create(timestamp4));
    }
  }

  private static MeasureMap putToMeasureMap(MeasureMap measureMap, Measure measure, double value) {
    if (measure instanceof MeasureDouble) {
      return measureMap.put((MeasureDouble) measure, value);
    } else if (measure instanceof MeasureLong) {
      return measureMap.put((MeasureLong) measure, Math.round(value));
    } else {
      // Future measures.
      throw new AssertionError();
    }
  }
}
