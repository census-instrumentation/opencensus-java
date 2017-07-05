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
import static io.opencensus.stats.StatsTestUtil.createContext;

import io.opencensus.common.Duration;
import io.opencensus.common.Timestamp;
import io.opencensus.internal.SimpleEventQueue;
import io.opencensus.stats.View.DistributionView;
import io.opencensus.stats.View.IntervalView;
import io.opencensus.stats.ViewData.DistributionViewData;
import io.opencensus.testing.common.TestClock;
import java.util.Arrays;
import java.util.Collection;
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

  private static final TagKey KEY = TagKey.create("KEY");

  private static final TagValue VALUE = TagValue.create("VALUE");
  private static final TagValue VALUE_2 = TagValue.create("VALUE_2");

  private static final String MEASURE_NAME = "my measurement";

  private static final String MEASURE_NAME_2 = "my measurement 2";

  private static final String MEASURE_UNIT = "us";

  private static final String MEASURE_DESCRIPTION = "measure description";

  private static final Measure MEASURE =
      Measure.DoubleMeasure.create(MEASURE_NAME, MEASURE_DESCRIPTION, MEASURE_UNIT);

  private static final View.Name VIEW_NAME = View.Name.create("my view");
  private static final View.Name VIEW_NAME_2 = View.Name.create("my view 2");

  private static final String VIEW_DESCRIPTION = "view description";

  private static final BucketBoundaries BUCKET_BOUNDARIES =
      BucketBoundaries.create(
          Arrays.asList(
              0.0, 0.2, 0.5, 1.0, 2.0, 3.0, 4.0, 5.0, 7.0, 10.0, 15.0, 20.0, 30.0, 40.0, 50.0));

  private static final DistributionAggregationDescriptor DISTRIBUTION_AGGREGATION_DESCRIPTOR =
      DistributionAggregationDescriptor.create(BUCKET_BOUNDARIES.getBoundaries());

  private final TestClock clock = TestClock.create();

  private final StatsComponentImplBase statsComponent =
      new StatsComponentImplBase(new SimpleEventQueue(), clock);

  private final StatsContextFactoryImpl factory = statsComponent.getStatsContextFactory();
  private final ViewManagerImpl viewManager = statsComponent.getViewManager();
  private final StatsRecorder statsRecorder = statsComponent.getStatsRecorder();

  private static DistributionView createDistributionView() {
    return createDistributionView(
        VIEW_NAME, MEASURE, DISTRIBUTION_AGGREGATION_DESCRIPTOR, Arrays.asList(KEY));
  }

  private static DistributionView createDistributionView(
      View.Name name,
      Measure measure,
      DistributionAggregationDescriptor aggDescr,
      List<TagKey> keys) {
    return DistributionView.create(name, VIEW_DESCRIPTION, measure, aggDescr, keys);
  }

  @Test
  public void testRegisterAndGetView() {
    DistributionView view = createDistributionView();
    viewManager.registerView(view);
    assertThat(viewManager.getView(VIEW_NAME).getView()).isEqualTo(view);
  }

  @Test
  public void preventRegisteringIntervalView() {
    View intervalView =
        IntervalView.create(
            VIEW_NAME,
            VIEW_DESCRIPTION,
            MEASURE,
            IntervalAggregationDescriptor.create(Arrays.asList(Duration.fromMillis(1000))),
            Arrays.asList(KEY));
    thrown.expect(UnsupportedOperationException.class);
    viewManager.registerView(intervalView);
  }

  @Test
  public void allowRegisteringSameViewTwice() {
    DistributionView view = createDistributionView();
    viewManager.registerView(view);
    viewManager.registerView(view);
    assertThat(viewManager.getView(VIEW_NAME).getView()).isEqualTo(view);
  }

  @Test
  public void preventRegisteringDifferentViewWithSameName() {
    View view1 =
        DistributionView.create(
            VIEW_NAME,
            "View description.",
            MEASURE,
            DISTRIBUTION_AGGREGATION_DESCRIPTOR,
            Arrays.asList(KEY));
    viewManager.registerView(view1);
    View view2 =
        DistributionView.create(
            VIEW_NAME,
            "This is a different description.",
            MEASURE,
            DISTRIBUTION_AGGREGATION_DESCRIPTOR,
            Arrays.asList(KEY));
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
  public void testRecord() {
    DistributionView view =
        createDistributionView(
            VIEW_NAME,
            MEASURE,
            DISTRIBUTION_AGGREGATION_DESCRIPTOR,
            Arrays.asList(KEY));
    clock.setTime(Timestamp.create(1, 2));
    viewManager.registerView(view);
    StatsContextImpl tags = createContext(factory, KEY, VALUE);
    for (double val : Arrays.asList(10.0, 20.0, 30.0, 40.0)) {
      statsRecorder.record(tags, MeasurementMap.of(MEASURE, val));
    }
    clock.setTime(Timestamp.create(3, 4));
    DistributionViewData viewData = (DistributionViewData) viewManager.getView(VIEW_NAME);
    assertThat(viewData.getView()).isEqualTo(view);
    assertThat(viewData.getStart()).isEqualTo(Timestamp.create(1, 2));
    assertThat(viewData.getEnd()).isEqualTo(Timestamp.create(3, 4));
    assertDistributionAggregationsEquivalent(
        viewData.getDistributionAggregations(),
        Arrays.asList(
            StatsTestUtil.createDistributionAggregation(
                Arrays.asList(Tag.create(KEY, VALUE)),
                BUCKET_BOUNDARIES,
                Arrays.asList(10.0, 20.0, 30.0, 40.0))));
  }

  @Test
  public void getViewDoesNotClearStats() {
    DistributionView view =
        createDistributionView(
            VIEW_NAME,
            MEASURE,
            DISTRIBUTION_AGGREGATION_DESCRIPTOR,
            Arrays.asList(KEY));
    clock.setTime(Timestamp.create(10, 0));
    viewManager.registerView(view);
    StatsContextImpl tags = createContext(factory, KEY, VALUE);
    statsRecorder.record(tags, MeasurementMap.of(MEASURE, 0.1));
    clock.setTime(Timestamp.create(11, 0));
    DistributionViewData viewData1 = (DistributionViewData) viewManager.getView(VIEW_NAME);
    assertThat(viewData1.getStart()).isEqualTo(Timestamp.create(10, 0));
    assertThat(viewData1.getEnd()).isEqualTo(Timestamp.create(11, 0));
    assertDistributionAggregationsEquivalent(
        viewData1.getDistributionAggregations(),
        Arrays.asList(
            StatsTestUtil.createDistributionAggregation(
                Arrays.asList(Tag.create(KEY, VALUE)), BUCKET_BOUNDARIES, Arrays.asList(0.1))));
    statsRecorder.record(tags, MeasurementMap.of(MEASURE, 0.2));
    clock.setTime(Timestamp.create(12, 0));
    DistributionViewData viewData2 = (DistributionViewData) viewManager.getView(VIEW_NAME);

    // The second view should have the same start time as the first view, and it should include both
    // recorded values:
    assertThat(viewData2.getStart()).isEqualTo(Timestamp.create(10, 0));
    assertThat(viewData2.getEnd()).isEqualTo(Timestamp.create(12, 0));
    assertDistributionAggregationsEquivalent(
        viewData2.getDistributionAggregations(),
        Arrays.asList(
            StatsTestUtil.createDistributionAggregation(
                Arrays.asList(Tag.create(KEY, VALUE)),
                BUCKET_BOUNDARIES,
                Arrays.asList(0.1, 0.2))));
  }

  @Test
  public void testRecordMultipleTagValues() {
    viewManager.registerView(
        createDistributionView(
            VIEW_NAME,
            MEASURE,
            DISTRIBUTION_AGGREGATION_DESCRIPTOR,
            Arrays.asList(KEY)));
    statsRecorder.record(
        createContext(factory, KEY, VALUE), MeasurementMap.of(MEASURE, 10.0));
    statsRecorder.record(
        createContext(factory, KEY, VALUE_2), MeasurementMap.of(MEASURE, 30.0));
    statsRecorder.record(
        createContext(factory, KEY, VALUE_2), MeasurementMap.of(MEASURE, 50.0));
    DistributionViewData viewData = (DistributionViewData) viewManager.getView(VIEW_NAME);
    assertDistributionAggregationsEquivalent(
        viewData.getDistributionAggregations(),
        Arrays.asList(
            StatsTestUtil.createDistributionAggregation(
                Arrays.asList(Tag.create(KEY, VALUE)), BUCKET_BOUNDARIES, Arrays.asList(10.0)),
            StatsTestUtil.createDistributionAggregation(
                Arrays.asList(Tag.create(KEY, VALUE_2)),
                BUCKET_BOUNDARIES,
                Arrays.asList(30.0, 50.0))));
  }

  // This test checks that StatsRecorder.record(...) does not throw an exception when no views are
  // registered.
  @Test
  public void allowRecordingWithoutRegisteringMatchingViewData() {
    statsRecorder.record(
        createContext(factory, KEY, VALUE), MeasurementMap.of(MEASURE, 10));
  }

  @Test
  public void testRecordWithEmptyStatsContext() {
    viewManager.registerView(
        createDistributionView(
            VIEW_NAME,
            MEASURE,
            DISTRIBUTION_AGGREGATION_DESCRIPTOR,
            Arrays.asList(KEY)));
    // DEFAULT doesn't have tags, but the view has tag key "KEY".
    statsRecorder.record(factory.getDefault(), MeasurementMap.of(MEASURE, 10.0));
    DistributionViewData view = (DistributionViewData) viewManager.getView(VIEW_NAME);
    assertDistributionAggregationsEquivalent(
        view.getDistributionAggregations(),
        Arrays.asList(
            StatsTestUtil.createDistributionAggregation(
                // Tag is missing for associated measureValues, should use default tag value
                // "unknown/not set"
                Arrays.asList(Tag.create(KEY, MutableViewData.UNKNOWN_TAG_VALUE)),
                BUCKET_BOUNDARIES,
                // Should record stats with default tag value: "KEY" : "unknown/not set".
                Arrays.asList(10.0))));
  }

  @Test
  public void testRecordWithNonExistentMeasurementDescriptor() {
    viewManager.registerView(
        createDistributionView(
            VIEW_NAME,
            Measure.DoubleMeasure.create(MEASURE_NAME, "measure", MEASURE_UNIT),
            DISTRIBUTION_AGGREGATION_DESCRIPTOR,
            Arrays.asList(KEY)));
    Measure measure2 =
        Measure.DoubleMeasure.create(MEASURE_NAME_2, "measure", MEASURE_UNIT);
    statsRecorder.record(createContext(factory, KEY, VALUE), MeasurementMap.of(measure2, 10.0));
    DistributionViewData view = (DistributionViewData) viewManager.getView(VIEW_NAME);
    assertThat(view.getDistributionAggregations()).isEmpty();
  }

  @Test
  public void testRecordWithTagsThatDoNotMatchViewData() {
    viewManager.registerView(
        createDistributionView(
            VIEW_NAME,
            MEASURE,
            DISTRIBUTION_AGGREGATION_DESCRIPTOR,
            Arrays.asList(KEY)));
    statsRecorder.record(
        createContext(factory, TagKey.create("wrong key"), VALUE),
        MeasurementMap.of(MEASURE, 10.0));
    statsRecorder.record(
        createContext(factory, TagKey.create("another wrong key"), VALUE),
        MeasurementMap.of(MEASURE, 50.0));
    DistributionViewData view = (DistributionViewData) viewManager.getView(VIEW_NAME);
    assertDistributionAggregationsEquivalent(
        view.getDistributionAggregations(),
        Arrays.asList(
            StatsTestUtil.createDistributionAggregation(
                // Won't record the unregistered tag key, will use default tag instead:
                // "KEY" : "unknown/not set".
                Arrays.asList(Tag.create(KEY, MutableViewData.UNKNOWN_TAG_VALUE)),
                BUCKET_BOUNDARIES,
                // Should record stats with default tag value: "KEY" : "unknown/not set".
                Arrays.asList(10.0, 50.0))));
  }

  @Test
  public void testViewDataWithMultipleTagKeys() {
    TagKey key1 = TagKey.create("Key-1");
    TagKey key2 = TagKey.create("Key-2");
    viewManager.registerView(
        createDistributionView(
            VIEW_NAME,
            MEASURE,
            DISTRIBUTION_AGGREGATION_DESCRIPTOR,
            Arrays.asList(key1, key2)));
    statsRecorder.record(
        createContext(factory, key1, TagValue.create("v1"), key2, TagValue.create("v10")),
        MeasurementMap.of(MEASURE, 1.1));
    statsRecorder.record(
        createContext(factory, key1, TagValue.create("v1"), key2, TagValue.create("v20")),
        MeasurementMap.of(MEASURE, 2.2));
    statsRecorder.record(
        createContext(factory, key1, TagValue.create("v2"), key2, TagValue.create("v10")),
        MeasurementMap.of(MEASURE, 3.3));
    statsRecorder.record(
        createContext(factory, key1, TagValue.create("v1"), key2, TagValue.create("v10")),
        MeasurementMap.of(MEASURE, 4.4));
    DistributionViewData view = (DistributionViewData) viewManager.getView(VIEW_NAME);
    assertDistributionAggregationsEquivalent(
        view.getDistributionAggregations(),
        Arrays.asList(
            StatsTestUtil.createDistributionAggregation(
                Arrays.asList(
                    Tag.create(key1, TagValue.create("v1")),
                    Tag.create(key2, TagValue.create("v10"))),
                BUCKET_BOUNDARIES,
                Arrays.asList(1.1, 4.4)),
            StatsTestUtil.createDistributionAggregation(
                Arrays.asList(
                    Tag.create(key1, TagValue.create("v1")),
                    Tag.create(key2, TagValue.create("v20"))),
                BUCKET_BOUNDARIES,
                Arrays.asList(2.2)),
            StatsTestUtil.createDistributionAggregation(
                Arrays.asList(
                    Tag.create(key1, TagValue.create("v2")),
                    Tag.create(key2, TagValue.create("v10"))),
                BUCKET_BOUNDARIES,
                Arrays.asList(3.3))));
  }

  @Test
  public void testMultipleViewDatasSameMeasure() {
    View view1 =
        createDistributionView(
            VIEW_NAME,
            MEASURE,
            DISTRIBUTION_AGGREGATION_DESCRIPTOR,
            Arrays.asList(KEY));
    View view2 =
        createDistributionView(
            VIEW_NAME_2,
            MEASURE,
            DISTRIBUTION_AGGREGATION_DESCRIPTOR,
            Arrays.asList(KEY));
    clock.setTime(Timestamp.create(1, 1));
    viewManager.registerView(view1);
    clock.setTime(Timestamp.create(2, 2));
    viewManager.registerView(view2);
    statsRecorder.record(
        createContext(factory, KEY, VALUE), MeasurementMap.of(MEASURE, 5.0));
    List<DistributionAggregation> expectedAggs =
        Arrays.asList(
            StatsTestUtil.createDistributionAggregation(
                Arrays.asList(Tag.create(KEY, VALUE)), BUCKET_BOUNDARIES, Arrays.asList(5.0)));
    clock.setTime(Timestamp.create(3, 3));
    DistributionViewData viewData1 = (DistributionViewData) viewManager.getView(VIEW_NAME);
    clock.setTime(Timestamp.create(4, 4));
    DistributionViewData viewData2 = (DistributionViewData) viewManager.getView(VIEW_NAME_2);
    assertThat(viewData1.getStart()).isEqualTo(Timestamp.create(1, 1));
    assertThat(viewData1.getEnd()).isEqualTo(Timestamp.create(3, 3));
    assertDistributionAggregationsEquivalent(viewData1.getDistributionAggregations(), expectedAggs);
    assertThat(viewData2.getStart()).isEqualTo(Timestamp.create(2, 2));
    assertThat(viewData2.getEnd()).isEqualTo(Timestamp.create(4, 4));
    assertDistributionAggregationsEquivalent(viewData2.getDistributionAggregations(), expectedAggs);
  }

  @Test
  public void testMultipleViewDatasDifferentMeasures() {
    Measure measure1 =
        Measure.DoubleMeasure.create(MEASURE_NAME, MEASURE_DESCRIPTION, MEASURE_UNIT);
    Measure measure2 =
        Measure.DoubleMeasure.create(MEASURE_NAME_2, MEASURE_DESCRIPTION, MEASURE_UNIT);
    View view1 =
        createDistributionView(
            VIEW_NAME, measure1, DISTRIBUTION_AGGREGATION_DESCRIPTOR, Arrays.asList(KEY));
    View view2 =
        createDistributionView(
            VIEW_NAME_2, measure2, DISTRIBUTION_AGGREGATION_DESCRIPTOR, Arrays.asList(KEY));
    clock.setTime(Timestamp.create(1, 0));
    viewManager.registerView(view1);
    clock.setTime(Timestamp.create(2, 0));
    viewManager.registerView(view2);
    statsRecorder.record(
        createContext(factory, KEY, VALUE),
        MeasurementMap.of(measure1, 1.1, measure2, 2.2));
    clock.setTime(Timestamp.create(3, 0));
    DistributionViewData viewData1 = (DistributionViewData) viewManager.getView(VIEW_NAME);
    clock.setTime(Timestamp.create(4, 0));
    DistributionViewData viewData2 = (DistributionViewData) viewManager.getView(VIEW_NAME_2);
    assertThat(viewData1.getStart()).isEqualTo(Timestamp.create(1, 0));
    assertThat(viewData1.getEnd()).isEqualTo(Timestamp.create(3, 0));
    assertDistributionAggregationsEquivalent(
        viewData1.getDistributionAggregations(),
        Arrays.asList(
            StatsTestUtil.createDistributionAggregation(
                Arrays.asList(Tag.create(KEY, VALUE)), BUCKET_BOUNDARIES, Arrays.asList(1.1))));
    assertThat(viewData2.getStart()).isEqualTo(Timestamp.create(2, 0));
    assertThat(viewData2.getEnd()).isEqualTo(Timestamp.create(4, 0));
    assertDistributionAggregationsEquivalent(
        viewData2.getDistributionAggregations(),
        Arrays.asList(
            StatsTestUtil.createDistributionAggregation(
                Arrays.asList(Tag.create(KEY, VALUE)), BUCKET_BOUNDARIES, Arrays.asList(2.2))));
  }

  @Test
  public void testGetDistributionViewDataWithoutBucketBoundaries() {
    View view =
        createDistributionView(
            VIEW_NAME, MEASURE, DistributionAggregationDescriptor.create(),
            Arrays.asList(KEY));
    clock.setTime(Timestamp.create(1, 0));
    viewManager.registerView(view);
    statsRecorder.record(
        createContext(factory, KEY, VALUE), MeasurementMap.of(MEASURE, 1.1));
    clock.setTime(Timestamp.create(3, 0));
    DistributionViewData viewData = (DistributionViewData) viewManager.getView(VIEW_NAME);
    assertThat(viewData.getStart()).isEqualTo(Timestamp.create(1, 0));
    assertThat(viewData.getEnd()).isEqualTo(Timestamp.create(3, 0));
    assertDistributionAggregationsEquivalent(
        viewData.getDistributionAggregations(),
        Arrays.asList(
            StatsTestUtil.createDistributionAggregation(
                Arrays.asList(Tag.create(KEY, VALUE)), Arrays.asList(1.1))));
  }

  // TODO(sebright) Consider making this helper method work with larger ranges of double values and
  // moving it to StatsTestUtil.
  private static void assertDistributionAggregationsEquivalent(
      Collection<DistributionAggregation> actual, Collection<DistributionAggregation> expected) {
    StatsTestUtil.assertDistributionAggregationsEquivalent(1e-6, actual, expected);
  }
}
