/*
 * Copyright 2017, Google Inc.
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

import com.google.common.collect.ImmutableMap;
import io.opencensus.common.Duration;
import io.opencensus.common.Timestamp;
import io.opencensus.impl.tags.TagContextsImpl;
import io.opencensus.internal.SimpleEventQueue;
import io.opencensus.stats.Aggregation.Count;
import io.opencensus.stats.Aggregation.Histogram;
import io.opencensus.stats.Aggregation.Mean;
import io.opencensus.stats.Aggregation.Range;
import io.opencensus.stats.Aggregation.StdDev;
import io.opencensus.stats.Aggregation.Sum;
import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.stats.View.Window.Cumulative;
import io.opencensus.stats.View.Window.Interval;
import io.opencensus.stats.ViewData.WindowData.CumulativeData;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagContexts;
import io.opencensus.tags.TagKey.TagKeyString;
import io.opencensus.tags.TagValueString;
import io.opencensus.testing.common.TestClock;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link ViewManagerImpl}. */
@RunWith(JUnit4.class)
public class ViewManagerImplTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  private static final TagKeyString KEY = TagKeyString.create("KEY");

  private static final TagValueString VALUE = TagValueString.create("VALUE");
  private static final TagValueString VALUE_2 = TagValueString.create("VALUE_2");

  private static final String MEASURE_NAME = "my measurement";

  private static final String MEASURE_NAME_2 = "my measurement 2";

  private static final String MEASURE_UNIT = "us";

  private static final String MEASURE_DESCRIPTION = "measure description";

  private static final MeasureDouble MEASURE =
      Measure.MeasureDouble.create(MEASURE_NAME, MEASURE_DESCRIPTION, MEASURE_UNIT);

  private static final View.Name VIEW_NAME = View.Name.create("my view");
  private static final View.Name VIEW_NAME_2 = View.Name.create("my view 2");

  private static final String VIEW_DESCRIPTION = "view description";

  private static final Cumulative CUMULATIVE = Cumulative.create();

  private static final int MILLIS_PER_SECOND = 1000;
  private static final Duration TEN_SECONDS = Duration.create(10, 0);

  private static final double EPSILON = 1e-7;

  private static final BucketBoundaries BUCKET_BOUNDARIES =
      BucketBoundaries.create(
          Arrays.asList(
              0.0, 0.2, 0.5, 1.0, 2.0, 3.0, 4.0, 5.0, 7.0, 10.0, 15.0, 20.0, 30.0, 40.0, 50.0));

  private static final List<Aggregation> AGGREGATIONS =
      Collections.unmodifiableList(
          Arrays.asList(
              Sum.create(),
              Count.create(),
              Range.create(),
              Histogram.create(BUCKET_BOUNDARIES),
              Mean.create(),
              StdDev.create()));

  private static final List<Aggregation> AGGREGATIONS_V1 =
      Collections.unmodifiableList(Arrays.asList(
          Sum.create(), Count.create(), Histogram.create(BUCKET_BOUNDARIES)));

  private final TestClock clock = TestClock.create();

  private final StatsComponentImplBase statsComponent =
      new StatsComponentImplBase(new SimpleEventQueue(), clock);

  private final TagContexts tagContexts = new TagContextsImpl();
  private final ViewManagerImpl viewManager = statsComponent.getViewManager();
  private final StatsRecorder statsRecorder = statsComponent.getStatsRecorder();

  private static View createCumulativeView() {
    return createCumulativeView(VIEW_NAME, MEASURE, AGGREGATIONS, Arrays.asList(KEY));
  }

  private static View createCumulativeView(
      View.Name name, Measure measure, List<Aggregation> aggregations, List<TagKeyString> keys) {
    return View.create(name, VIEW_DESCRIPTION, measure, aggregations, keys, CUMULATIVE);
  }

  @Test
  public void testRegisterAndGetCumulativeView() {
    View view = createCumulativeView();
    viewManager.registerView(view);
    assertThat(viewManager.getView(VIEW_NAME).getView()).isEqualTo(view);
  }

  @Test
  public void testRegisterAndGetIntervalView() {
    View intervalView =
        View.create(
            VIEW_NAME,
            VIEW_DESCRIPTION,
            MEASURE,
            AGGREGATIONS,
            Arrays.asList(KEY),
            Interval.create(Duration.create(60, 0)));
    viewManager.registerView(intervalView);
    assertThat(viewManager.getView(VIEW_NAME).getView()).isEqualTo(intervalView);
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
            VIEW_NAME, "View description.", MEASURE, AGGREGATIONS, Arrays.asList(KEY), CUMULATIVE);
    viewManager.registerView(view1);
    View view2 =
        View.create(
            VIEW_NAME,
            "This is a different description.",
            MEASURE,
            AGGREGATIONS,
            Arrays.asList(KEY),
            CUMULATIVE);
    try {
      thrown.expect(IllegalArgumentException.class);
      thrown.expectMessage("A different view with the same name is already registered");
      viewManager.registerView(view2);
    } finally {
      assertThat(viewManager.getView(VIEW_NAME).getView()).isEqualTo(view1);
    }
  }

  @Test
  public void disallowGettingNonexistentViewData() {
    thrown.expect(IllegalArgumentException.class);
    viewManager.getView(VIEW_NAME);
  }

  @Test
  public void testRecordCumulative() {
    View view = createCumulativeView(VIEW_NAME, MEASURE, AGGREGATIONS, Arrays.asList(KEY));
    clock.setTime(Timestamp.create(1, 2));
    viewManager.registerView(view);
    TagContext tags = tagContexts.emptyBuilder().set(KEY, VALUE).build();
    double[] values = {10.0, 20.0, 30.0, 40.0};
    for (double val : values) {
      statsRecorder.record(tags, MeasureMap.builder().set(MEASURE, val).build());
    }
    clock.setTime(Timestamp.create(3, 4));
    ViewData viewData = viewManager.getView(VIEW_NAME);
    assertThat(viewData.getView()).isEqualTo(view);
    assertThat(viewData.getWindowData())
        .isEqualTo(CumulativeData.create(Timestamp.create(1, 2), Timestamp.create(3, 4)));
    StatsTestUtil.assertAggregationMapEquals(
        viewData.getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE), StatsTestUtil.createAggregationData(AGGREGATIONS, values)),
        EPSILON);
  }

  @Test
  public void testRecordInterval() {
    // The interval is 10 seconds, i.e. values should expire after 10 seconds.
    View view = View.create(
        VIEW_NAME, VIEW_DESCRIPTION, MEASURE, AGGREGATIONS_V1, Arrays.asList(KEY),
        Interval.create(TEN_SECONDS));
    clock.setTime(Timestamp.fromMillis(100));
    viewManager.registerView(view);

    TagContext tags = tagContexts.emptyBuilder().set(KEY, VALUE).build();

    double[] values = {20.0, -1.0, 1.0, -5.0, 5.0};
    for (int i = 1; i <= 5; i++) {
      // Add each value in sequence, at 1st second, 2nd second, 3rd second, etc.
      clock.setTime(Timestamp.fromMillis(i * MILLIS_PER_SECOND));
      statsRecorder.record(tags, MeasureMap.builder().set(MEASURE, values[i - 1]).build());
    }

    clock.setTime(Timestamp.fromMillis(8 * MILLIS_PER_SECOND));
    // 8s, no values should have expired
    StatsTestUtil.assertAggregationMapEquals(
        viewManager.getView(VIEW_NAME).getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE),
            StatsTestUtil.createAggregationData(AGGREGATIONS_V1, values)),
        EPSILON);

    clock.setTime(Timestamp.fromMillis((long) (11.1 * MILLIS_PER_SECOND)));
    // 11.1s, the first value should have expired
    StatsTestUtil.assertAggregationMapEquals(
        viewManager.getView(VIEW_NAME).getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE),
            StatsTestUtil.createAggregationData(AGGREGATIONS_V1, -1.0, 1.0, -5.0, 5.0)),
        EPSILON);

    clock.setTime(Timestamp.fromMillis((long) (13.1 * MILLIS_PER_SECOND)));
    // 13.1s, the 1st, 2nd and 3rd value should have expired
    StatsTestUtil.assertAggregationMapEquals(
        viewManager.getView(VIEW_NAME).getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE),
            StatsTestUtil.createAggregationData(AGGREGATIONS_V1, -5.0, 5.0)),
        EPSILON);

    clock.setTime(Timestamp.fromMillis((long) 13.2 * MILLIS_PER_SECOND));
    // 13.2s, add a new value 9.0
    statsRecorder.record(tags, MeasureMap.builder().set(MEASURE, 9.0).build());

    clock.setTime(Timestamp.fromMillis((long) (13.5 * MILLIS_PER_SECOND)));
    // 13.5s, after adding value 9.0
    StatsTestUtil.assertAggregationMapEquals(
        viewManager.getView(VIEW_NAME).getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE),
            StatsTestUtil.createAggregationData(AGGREGATIONS_V1, -5.0, 5.0, 9.0)),
        EPSILON);

    clock.setTime(Timestamp.fromMillis((long) (60.0 * MILLIS_PER_SECOND)));
    // 60s, all values should have expired
    StatsTestUtil.assertAggregationMapEquals(
        viewManager.getView(VIEW_NAME).getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE),
            StatsTestUtil.createAggregationData(AGGREGATIONS_V1)),
        EPSILON);
  }

  @Test
  public void getViewDoesNotClearStats() {
    View view = createCumulativeView(VIEW_NAME, MEASURE, AGGREGATIONS, Arrays.asList(KEY));
    clock.setTime(Timestamp.create(10, 0));
    viewManager.registerView(view);
    TagContext tags = tagContexts.emptyBuilder().set(KEY, VALUE).build();
    statsRecorder.record(tags, MeasureMap.builder().set(MEASURE, 0.1).build());
    clock.setTime(Timestamp.create(11, 0));
    ViewData viewData1 = viewManager.getView(VIEW_NAME);
    assertThat(viewData1.getWindowData())
        .isEqualTo(CumulativeData.create(Timestamp.create(10, 0), Timestamp.create(11, 0)));
    StatsTestUtil.assertAggregationMapEquals(
        viewData1.getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE), StatsTestUtil.createAggregationData(AGGREGATIONS, 0.1)),
        EPSILON);

    statsRecorder.record(tags, MeasureMap.builder().set(MEASURE, 0.2).build());
    clock.setTime(Timestamp.create(12, 0));
    ViewData viewData2 = viewManager.getView(VIEW_NAME);

    // The second view should have the same start time as the first view, and it should include both
    // recorded values:
    assertThat(viewData2.getWindowData())
        .isEqualTo(CumulativeData.create(Timestamp.create(10, 0), Timestamp.create(12, 0)));
    StatsTestUtil.assertAggregationMapEquals(
        viewData2.getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE), StatsTestUtil.createAggregationData(AGGREGATIONS, 0.1, 0.2)),
        EPSILON);
  }

  @Test
  public void testRecordCumulativeMultipleTagValues() {
    viewManager.registerView(
        createCumulativeView(VIEW_NAME, MEASURE, AGGREGATIONS, Arrays.asList(KEY)));
    statsRecorder.record(
        tagContexts.emptyBuilder().set(KEY, VALUE).build(),
        MeasureMap.builder().set(MEASURE, 10.0).build());
    statsRecorder.record(
        tagContexts.emptyBuilder().set(KEY, VALUE_2).build(),
        MeasureMap.builder().set(MEASURE, 30.0).build());
    statsRecorder.record(
        tagContexts.emptyBuilder().set(KEY, VALUE_2).build(),
        MeasureMap.builder().set(MEASURE, 50.0).build());
    ViewData viewData = viewManager.getView(VIEW_NAME);
    StatsTestUtil.assertAggregationMapEquals(
        viewData.getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE),
            StatsTestUtil.createAggregationData(AGGREGATIONS, 10.0),
            Arrays.asList(VALUE_2),
            StatsTestUtil.createAggregationData(AGGREGATIONS, 30.0, 50.0)),
        EPSILON);
  }

  @Test
  public void testRecordIntervalMultipleTagValues() {
    // The interval is 10 seconds, i.e. values should expire after 10 seconds.
    View view = View.create(
        VIEW_NAME, VIEW_DESCRIPTION, MEASURE, AGGREGATIONS_V1, Arrays.asList(KEY),
        Interval.create(TEN_SECONDS));
    clock.setTime(Timestamp.fromMillis(100));
    viewManager.registerView(view);

    // record for TagValue1 at 1st second
    clock.setTime(Timestamp.fromMillis(1 * MILLIS_PER_SECOND));
    statsRecorder.record(
        tagContexts.emptyBuilder().set(KEY, VALUE).build(),
        MeasureMap.builder().set(MEASURE, 10.0).build());

    // record for TagValue2 at 5th second
    clock.setTime(Timestamp.fromMillis(5 * MILLIS_PER_SECOND));
    statsRecorder.record(
        tagContexts.emptyBuilder().set(KEY, VALUE_2).build(),
        MeasureMap.builder().set(MEASURE, 30.0).build());
    statsRecorder.record(
        tagContexts.emptyBuilder().set(KEY, VALUE_2).build(),
        MeasureMap.builder().set(MEASURE, 50.0).build());

    // get ViewData at 9th second, no stats should have expired.
    clock.setTime(Timestamp.fromMillis(9 * MILLIS_PER_SECOND));
    ViewData viewData1 = viewManager.getView(VIEW_NAME);
    StatsTestUtil.assertAggregationMapEquals(
        viewData1.getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE),
            StatsTestUtil.createAggregationData(AGGREGATIONS_V1, 10.0),
            Arrays.asList(VALUE_2),
            StatsTestUtil.createAggregationData(AGGREGATIONS_V1, 30.0, 50.0)),
        EPSILON);

    // get ViewData at 12th second, stats for TagValue1 should have expired.
    clock.setTime(Timestamp.fromMillis(12 * MILLIS_PER_SECOND));
    ViewData viewData2 = viewManager.getView(VIEW_NAME);
    StatsTestUtil.assertAggregationMapEquals(
        viewData2.getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE),
            StatsTestUtil.createAggregationData(AGGREGATIONS_V1),
            Arrays.asList(VALUE_2),
            StatsTestUtil.createAggregationData(AGGREGATIONS_V1, 30.0, 50.0)),
        EPSILON);
  }

  // This test checks that StatsRecorder.record(...) does not throw an exception when no views are
  // registered.
  @Test
  public void allowRecordingWithoutRegisteringMatchingViewData() {
    statsRecorder.record(
        tagContexts.emptyBuilder().set(KEY, VALUE).build(),
        MeasureMap.builder().set(MEASURE, 10).build());
  }

  @Test
  public void testRecordWithEmptyStatsContext() {
    viewManager.registerView(
        createCumulativeView(VIEW_NAME, MEASURE, AGGREGATIONS, Arrays.asList(KEY)));
    // DEFAULT doesn't have tags, but the view has tag key "KEY".
    statsRecorder.record(tagContexts.empty(), MeasureMap.builder().set(MEASURE, 10.0).build());
    ViewData viewData = viewManager.getView(VIEW_NAME);
    StatsTestUtil.assertAggregationMapEquals(
        viewData.getAggregationMap(),
        ImmutableMap.of(
            // Tag is missing for associated measureValues, should use default tag value
            // "unknown/not set".
            Arrays.asList(MutableViewData.UNKNOWN_TAG_VALUE),
            // Should record stats with default tag value: "KEY" : "unknown/not set".
            StatsTestUtil.createAggregationData(AGGREGATIONS, 10.0)),
        EPSILON);
  }

  @Test
  public void testRecordWithNonExistentMeasurement() {
    viewManager.registerView(
        createCumulativeView(
            VIEW_NAME,
            Measure.MeasureDouble.create(MEASURE_NAME, "measure", MEASURE_UNIT),
            AGGREGATIONS,
            Arrays.asList(KEY)));
    MeasureDouble measure2 = Measure.MeasureDouble.create(MEASURE_NAME_2, "measure", MEASURE_UNIT);
    statsRecorder.record(
        tagContexts.emptyBuilder().set(KEY, VALUE).build(),
        MeasureMap.builder().set(measure2, 10.0).build());
    ViewData view = viewManager.getView(VIEW_NAME);
    assertThat(view.getAggregationMap()).isEmpty();
  }

  @Test
  public void testRecordWithTagsThatDoNotMatchViewData() {
    viewManager.registerView(
        createCumulativeView(VIEW_NAME, MEASURE, AGGREGATIONS, Arrays.asList(KEY)));
    statsRecorder.record(
        tagContexts.emptyBuilder().set(TagKeyString.create("wrong key"), VALUE).build(),
        MeasureMap.builder().set(MEASURE, 10.0).build());
    statsRecorder.record(
        tagContexts.emptyBuilder().set(TagKeyString.create("another wrong key"), VALUE).build(),
        MeasureMap.builder().set(MEASURE, 50.0).build());
    ViewData viewData = viewManager.getView(VIEW_NAME);
    StatsTestUtil.assertAggregationMapEquals(
        viewData.getAggregationMap(),
        ImmutableMap.of(
            // Won't record the unregistered tag key, for missing registered keys will use default
            // tag value : "unknown/not set".
            Arrays.asList(MutableViewData.UNKNOWN_TAG_VALUE),
            // Should record stats with default tag value: "KEY" : "unknown/not set".
            StatsTestUtil.createAggregationData(AGGREGATIONS, 10.0, 50.0)),
        EPSILON);
  }

  @Test
  public void testViewDataWithMultipleTagKeys() {
    TagKeyString key1 = TagKeyString.create("Key-1");
    TagKeyString key2 = TagKeyString.create("Key-2");
    viewManager.registerView(
        createCumulativeView(VIEW_NAME, MEASURE, AGGREGATIONS, Arrays.asList(key1, key2)));
    statsRecorder.record(
        tagContexts
            .emptyBuilder()
            .set(key1, TagValueString.create("v1"))
            .set(key2, TagValueString.create("v10"))
            .build(),
        MeasureMap.builder().set(MEASURE, 1.1).build());
    statsRecorder.record(
        tagContexts
            .emptyBuilder()
            .set(key1, TagValueString.create("v1"))
            .set(key2, TagValueString.create("v20"))
            .build(),
        MeasureMap.builder().set(MEASURE, 2.2).build());
    statsRecorder.record(
        tagContexts
            .emptyBuilder()
            .set(key1, TagValueString.create("v2"))
            .set(key2, TagValueString.create("v10"))
            .build(),
        MeasureMap.builder().set(MEASURE, 3.3).build());
    statsRecorder.record(
        tagContexts
            .emptyBuilder()
            .set(key1, TagValueString.create("v1"))
            .set(key2, TagValueString.create("v10"))
            .build(),
        MeasureMap.builder().set(MEASURE, 4.4).build());
    ViewData viewData = viewManager.getView(VIEW_NAME);
    StatsTestUtil.assertAggregationMapEquals(
        viewData.getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(TagValueString.create("v1"), TagValueString.create("v10")),
            StatsTestUtil.createAggregationData(AGGREGATIONS, 1.1, 4.4),
            Arrays.asList(TagValueString.create("v1"), TagValueString.create("v20")),
            StatsTestUtil.createAggregationData(AGGREGATIONS, 2.2),
            Arrays.asList(TagValueString.create("v2"), TagValueString.create("v10")),
            StatsTestUtil.createAggregationData(AGGREGATIONS, 3.3)),
        EPSILON);
  }

  @Test
  public void testMultipleViewSameMeasure() {
    final View view1 = createCumulativeView(VIEW_NAME, MEASURE, AGGREGATIONS, Arrays.asList(KEY));
    final View view2 = createCumulativeView(VIEW_NAME_2, MEASURE, AGGREGATIONS, Arrays.asList(KEY));
    clock.setTime(Timestamp.create(1, 1));
    viewManager.registerView(view1);
    clock.setTime(Timestamp.create(2, 2));
    viewManager.registerView(view2);
    statsRecorder.record(
        tagContexts.emptyBuilder().set(KEY, VALUE).build(),
        MeasureMap.builder().set(MEASURE, 5.0).build());
    clock.setTime(Timestamp.create(3, 3));
    ViewData viewData1 = viewManager.getView(VIEW_NAME);
    clock.setTime(Timestamp.create(4, 4));
    ViewData viewData2 = viewManager.getView(VIEW_NAME_2);
    assertThat(viewData1.getWindowData())
        .isEqualTo(CumulativeData.create(Timestamp.create(1, 1), Timestamp.create(3, 3)));
    StatsTestUtil.assertAggregationMapEquals(
        viewData1.getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE), StatsTestUtil.createAggregationData(AGGREGATIONS, 5.0)),
        EPSILON);
    assertThat(viewData2.getWindowData())
        .isEqualTo(CumulativeData.create(Timestamp.create(2, 2), Timestamp.create(4, 4)));
    StatsTestUtil.assertAggregationMapEquals(
        viewData2.getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE), StatsTestUtil.createAggregationData(AGGREGATIONS, 5.0)),
        EPSILON);
  }

  @Test
  public void testMultipleViewsDifferentMeasures() {
    MeasureDouble measure1 =
        Measure.MeasureDouble.create(MEASURE_NAME, MEASURE_DESCRIPTION, MEASURE_UNIT);
    MeasureDouble measure2 =
        Measure.MeasureDouble.create(MEASURE_NAME_2, MEASURE_DESCRIPTION, MEASURE_UNIT);
    final View view1 = createCumulativeView(VIEW_NAME, measure1, AGGREGATIONS, Arrays.asList(KEY));
    final View view2 =
        createCumulativeView(VIEW_NAME_2, measure2, AGGREGATIONS, Arrays.asList(KEY));
    clock.setTime(Timestamp.create(1, 0));
    viewManager.registerView(view1);
    clock.setTime(Timestamp.create(2, 0));
    viewManager.registerView(view2);
    statsRecorder.record(
        tagContexts.emptyBuilder().set(KEY, VALUE).build(),
        MeasureMap.builder().set(measure1, 1.1).set(measure2, 2.2).build());
    clock.setTime(Timestamp.create(3, 0));
    ViewData viewData1 = viewManager.getView(VIEW_NAME);
    clock.setTime(Timestamp.create(4, 0));
    ViewData viewData2 = viewManager.getView(VIEW_NAME_2);
    assertThat(viewData1.getWindowData())
        .isEqualTo(CumulativeData.create(Timestamp.create(1, 0), Timestamp.create(3, 0)));
    StatsTestUtil.assertAggregationMapEquals(
        viewData1.getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE), StatsTestUtil.createAggregationData(AGGREGATIONS, 1.1)),
        EPSILON);
    assertThat(viewData2.getWindowData())
        .isEqualTo(CumulativeData.create(Timestamp.create(2, 0), Timestamp.create(4, 0)));
    StatsTestUtil.assertAggregationMapEquals(
        viewData2.getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE), StatsTestUtil.createAggregationData(AGGREGATIONS, 2.2)),
        EPSILON);
  }

  @Test
  public void testGetCumulativeViewDataWithoutBucketBoundaries() {
    List<Aggregation> aggregationsNoHistogram =
        Arrays.asList(Sum.create(), Count.create(), Mean.create(), Range.create(), StdDev.create());
    View view =
        createCumulativeView(VIEW_NAME, MEASURE, aggregationsNoHistogram, Arrays.asList(KEY));
    clock.setTime(Timestamp.create(1, 0));
    viewManager.registerView(view);
    statsRecorder.record(
        tagContexts.emptyBuilder().set(KEY, VALUE).build(),
        MeasureMap.builder().set(MEASURE, 1.1).build());
    clock.setTime(Timestamp.create(3, 0));
    ViewData viewData = viewManager.getView(VIEW_NAME);
    assertThat(viewData.getWindowData())
        .isEqualTo(CumulativeData.create(Timestamp.create(1, 0), Timestamp.create(3, 0)));
    StatsTestUtil.assertAggregationMapEquals(
        viewData.getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE),
            StatsTestUtil.createAggregationData(aggregationsNoHistogram, 1.1)),
        EPSILON);
  }
}
