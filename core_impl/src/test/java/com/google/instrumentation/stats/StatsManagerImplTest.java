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

package com.google.instrumentation.stats;

import static com.google.common.truth.Truth.assertThat;
import static com.google.instrumentation.stats.StatsTestUtil.assertDistributionAggregationsEquivalent;
import static com.google.instrumentation.stats.StatsTestUtil.createContext;

import com.google.instrumentation.common.Duration;
import com.google.instrumentation.common.SimpleEventQueue;
import com.google.instrumentation.common.Timestamp;
import com.google.instrumentation.internal.TestClock;
import com.google.instrumentation.stats.MeasurementDescriptor.BasicUnit;
import com.google.instrumentation.stats.MeasurementDescriptor.MeasurementUnit;
import com.google.instrumentation.stats.View.DistributionView;
import com.google.instrumentation.stats.ViewDescriptor.DistributionViewDescriptor;
import com.google.instrumentation.stats.ViewDescriptor.IntervalViewDescriptor;
import java.util.Arrays;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link StatsManagerImplBase}. */
@RunWith(JUnit4.class)
public class StatsManagerImplTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  private static final double TOLERANCE = 1e-6;

  private static final TagKey KEY = TagKey.create("KEY");

  private static final TagValue VALUE = TagValue.create("VALUE");
  private static final TagValue VALUE2 = TagValue.create("VALUE2");

  private static final MeasurementDescriptor.Name MEASUREMENT_NAME =
      MeasurementDescriptor.Name.create("my measurement");

  private static final MeasurementDescriptor.Name MEASUREMENT_NAME2 =
      MeasurementDescriptor.Name.create("my measurement 2");

  private static final MeasurementUnit MEASUREMENT_UNIT =
      MeasurementUnit.create(-6, Arrays.asList(BasicUnit.SECONDS));

  private static final String MEASUREMENT_DESCRIPTION = "measurement description";

  private static final MeasurementDescriptor MEASUREMENT_DESCRIPTOR =
      MeasurementDescriptor.create(MEASUREMENT_NAME, MEASUREMENT_DESCRIPTION, MEASUREMENT_UNIT);

  private static final ViewDescriptor.Name VIEW_NAME = ViewDescriptor.Name.create("my view");
  private static final ViewDescriptor.Name VIEW_NAME2 = ViewDescriptor.Name.create("my view 2");

  private static final String VIEW_DESCRIPTION = "view description";

  private static final BucketBoundaries BUCKET_BOUNDARIES =
      BucketBoundaries.create(
          Arrays.asList(
              0.0, 0.2, 0.5, 1.0, 2.0, 3.0, 4.0, 5.0, 7.0, 10.0, 15.0, 20.0, 30.0, 40.0, 50.0));

  private static final DistributionAggregationDescriptor DISTRIBUTION_AGGREGATION_DESCRIPTOR =
      DistributionAggregationDescriptor.create(BUCKET_BOUNDARIES.getBoundaries());

  private final TestClock clock = TestClock.create();

  private final StatsManagerImplBase statsManager =
      new StatsManagerImplBase(new SimpleEventQueue(), clock);

  private final StatsContextFactoryImpl factory = new StatsContextFactoryImpl(statsManager);

  @Test
  public void testRegisterAndGetView() {
    DistributionViewDescriptor viewDescr =
        DistributionViewDescriptor.create(
            VIEW_NAME,
            VIEW_DESCRIPTION,
            MEASUREMENT_DESCRIPTOR,
            DISTRIBUTION_AGGREGATION_DESCRIPTOR,
            Arrays.asList(KEY));
    statsManager.registerView(viewDescr);
    assertThat(statsManager.getView(VIEW_NAME).getViewDescriptor()).isEqualTo(viewDescr);
  }

  @Test
  public void preventRegisteringIntervalView() {
    ViewDescriptor intervalView =
        IntervalViewDescriptor.create(
            VIEW_NAME,
            VIEW_DESCRIPTION,
            MEASUREMENT_DESCRIPTOR,
            IntervalAggregationDescriptor.create(Arrays.asList(Duration.fromMillis(1000))),
            Arrays.asList(KEY));
    thrown.expect(UnsupportedOperationException.class);
    statsManager.registerView(intervalView);
  }

  @Test
  public void allowRegisteringSameViewDescriptorTwice() {
    DistributionViewDescriptor viewDescr =
        DistributionViewDescriptor.create(
            VIEW_NAME,
            VIEW_DESCRIPTION,
            MEASUREMENT_DESCRIPTOR,
            DISTRIBUTION_AGGREGATION_DESCRIPTOR,
            Arrays.asList(KEY));
    statsManager.registerView(viewDescr);
    statsManager.registerView(viewDescr);
    assertThat(statsManager.getView(VIEW_NAME).getViewDescriptor()).isEqualTo(viewDescr);
  }

  @Test
  public void preventRegisteringDifferentViewDescriptorWithSameName() {
    ViewDescriptor view1 =
        DistributionViewDescriptor.create(
            VIEW_NAME,
            "View description.",
            MEASUREMENT_DESCRIPTOR,
            DISTRIBUTION_AGGREGATION_DESCRIPTOR,
            Arrays.asList(KEY));
    statsManager.registerView(view1);
    ViewDescriptor view2 =
        DistributionViewDescriptor.create(
            VIEW_NAME,
            "This is a different description.",
            MEASUREMENT_DESCRIPTOR,
            DISTRIBUTION_AGGREGATION_DESCRIPTOR,
            Arrays.asList(KEY));
    try {
      thrown.expect(IllegalArgumentException.class);
      thrown.expectMessage("A different view with the same name is already registered");
      statsManager.registerView(view2);
    } finally {
      assertThat(statsManager.getView(VIEW_NAME).getViewDescriptor()).isEqualTo(view1);
    }
  }

  @Test
  public void disallowGettingNonexistentView() {
    thrown.expect(IllegalArgumentException.class);
    statsManager.getView(VIEW_NAME);
  }

  @Test
  public void testRecord() {
    DistributionViewDescriptor viewDescr =
        DistributionViewDescriptor.create(
            VIEW_NAME,
            VIEW_DESCRIPTION,
            MEASUREMENT_DESCRIPTOR,
            DISTRIBUTION_AGGREGATION_DESCRIPTOR,
            Arrays.asList(KEY));
    clock.setTime(Timestamp.create(1, 2));
    statsManager.registerView(viewDescr);
    StatsContextImpl tags = createContext(factory, KEY, VALUE);
    for (double val : Arrays.asList(10.0, 20.0, 30.0, 40.0)) {
      statsManager.record(tags, MeasurementMap.of(MEASUREMENT_DESCRIPTOR, val));
    }
    clock.setTime(Timestamp.create(3, 4));
    DistributionView view = (DistributionView) statsManager.getView(VIEW_NAME);
    assertThat(view.getViewDescriptor()).isEqualTo(viewDescr);
    assertThat(view.getStart()).isEqualTo(Timestamp.create(1, 2));
    assertThat(view.getEnd()).isEqualTo(Timestamp.create(3, 4));
    assertDistributionAggregationsEquivalent(
        TOLERANCE,
        view.getDistributionAggregations(),
        Arrays.asList(
            StatsTestUtil.createDistributionAggregation(
                Arrays.asList(Tag.create(KEY, VALUE)),
                BUCKET_BOUNDARIES,
                Arrays.asList(10.0, 20.0, 30.0, 40.0))));
  }

  @Test
  public void getViewDoesNotClearStats() {
    DistributionViewDescriptor viewDescr =
        DistributionViewDescriptor.create(
            VIEW_NAME,
            VIEW_DESCRIPTION,
            MEASUREMENT_DESCRIPTOR,
            DISTRIBUTION_AGGREGATION_DESCRIPTOR,
            Arrays.asList(KEY));
    clock.setTime(Timestamp.create(10, 0));
    statsManager.registerView(viewDescr);
    StatsContextImpl tags = createContext(factory, KEY, VALUE);
    statsManager.record(
        tags, MeasurementMap.of(MEASUREMENT_DESCRIPTOR, 0.1));
    clock.setTime(Timestamp.create(11, 0));
    DistributionView view1 = (DistributionView) statsManager.getView(VIEW_NAME);
    assertThat(view1.getStart()).isEqualTo(Timestamp.create(10, 0));
    assertThat(view1.getEnd()).isEqualTo(Timestamp.create(11, 0));
    assertDistributionAggregationsEquivalent(
        TOLERANCE,
        view1.getDistributionAggregations(),
        Arrays.asList(
            StatsTestUtil.createDistributionAggregation(
                Arrays.asList(Tag.create(KEY, VALUE)), BUCKET_BOUNDARIES, Arrays.asList(0.1))));
    statsManager.record(
        tags, MeasurementMap.of(MEASUREMENT_DESCRIPTOR, 0.2));
    clock.setTime(Timestamp.create(12, 0));
    DistributionView view2 = (DistributionView) statsManager.getView(VIEW_NAME);

    // The second view should have the same start time as the first view, and it should include both
    // recorded values:
    assertThat(view2.getStart()).isEqualTo(Timestamp.create(10, 0));
    assertThat(view2.getEnd()).isEqualTo(Timestamp.create(12, 0));
    assertDistributionAggregationsEquivalent(
        TOLERANCE,
        view2.getDistributionAggregations(),
        Arrays.asList(
            StatsTestUtil.createDistributionAggregation(
                Arrays.asList(Tag.create(KEY, VALUE)),
                BUCKET_BOUNDARIES,
                Arrays.asList(0.1, 0.2))));
  }

  @Test
  public void testRecordMultipleTagValues() {
    statsManager.registerView(
        DistributionViewDescriptor.create(
            VIEW_NAME,
            VIEW_DESCRIPTION,
            MEASUREMENT_DESCRIPTOR,
            DISTRIBUTION_AGGREGATION_DESCRIPTOR,
            Arrays.asList(KEY)));
    statsManager.record(
        createContext(factory, KEY, VALUE), MeasurementMap.of(MEASUREMENT_DESCRIPTOR, 10.0));
    statsManager.record(
        createContext(factory, KEY, VALUE2), MeasurementMap.of(MEASUREMENT_DESCRIPTOR, 30.0));
    statsManager.record(
        createContext(factory, KEY, VALUE2), MeasurementMap.of(MEASUREMENT_DESCRIPTOR, 50.0));
    DistributionView view = (DistributionView) statsManager.getView(VIEW_NAME);
    assertDistributionAggregationsEquivalent(
        TOLERANCE,
        view.getDistributionAggregations(),
        Arrays.asList(
            StatsTestUtil.createDistributionAggregation(
                Arrays.asList(Tag.create(KEY, VALUE)), BUCKET_BOUNDARIES, Arrays.asList(10.0)),
            StatsTestUtil.createDistributionAggregation(
                Arrays.asList(Tag.create(KEY, VALUE2)),
                BUCKET_BOUNDARIES,
                Arrays.asList(30.0, 50.0))));
  }

  // This test checks that StatsManager.record(...) does not throw an exception when no views are
  // registered.
  @Test
  public void allowRecordingWithoutRegisteringMatchingView() {
    statsManager.record(
        createContext(factory, KEY, VALUE), MeasurementMap.of(MEASUREMENT_DESCRIPTOR, 10));
  }

  @Test
  public void testRecordWithEmptyStatsContext() {
    statsManager.registerView(
        DistributionViewDescriptor.create(
            VIEW_NAME,
            VIEW_DESCRIPTION,
            MEASUREMENT_DESCRIPTOR,
            DISTRIBUTION_AGGREGATION_DESCRIPTOR,
            Arrays.asList(KEY)));
    // DEFAULT doesn't have tags, but the view has tag key "KEY".
    statsManager.record(
        statsManager.getStatsContextFactory().getDefault(),
        MeasurementMap.of(MEASUREMENT_DESCRIPTOR, 10.0));
    DistributionView view = (DistributionView) statsManager.getView(VIEW_NAME);
    assertDistributionAggregationsEquivalent(
        TOLERANCE,
        view.getDistributionAggregations(),
        Arrays.asList(
            StatsTestUtil.createDistributionAggregation(
                // Tag is missing for associated measurementValues, should use default tag value
                // "unknown/not set"
                Arrays.asList(Tag.create(KEY, MutableView.UNKNOWN_TAG_VALUE)),
                BUCKET_BOUNDARIES,
                // Should record stats with default tag value: "KEY" : "unknown/not set".
                Arrays.asList(10.0))));
  }

  @Test
  public void testRecordWithNonExistentMeasurementDescriptor() {
    statsManager.registerView(
        DistributionViewDescriptor.create(
            VIEW_NAME,
            VIEW_DESCRIPTION,
            MeasurementDescriptor.create(MEASUREMENT_NAME, "measurement", MEASUREMENT_UNIT),
            DISTRIBUTION_AGGREGATION_DESCRIPTOR,
            Arrays.asList(KEY)));
    MeasurementDescriptor measure2 =
        MeasurementDescriptor.create(MEASUREMENT_NAME2, "measurement", MEASUREMENT_UNIT);
    statsManager.record(createContext(factory, KEY, VALUE), MeasurementMap.of(measure2, 10.0));
    DistributionView view = (DistributionView) statsManager.getView(VIEW_NAME);
    assertThat(view.getDistributionAggregations()).isEmpty();
  }

  @Test
  public void testRecordWithTagsThatDoNotMatchView() {
    statsManager.registerView(
        DistributionViewDescriptor.create(
            VIEW_NAME,
            VIEW_DESCRIPTION,
            MEASUREMENT_DESCRIPTOR,
            DISTRIBUTION_AGGREGATION_DESCRIPTOR,
            Arrays.asList(KEY)));
    statsManager.record(
        createContext(factory, TagKey.create("wrong key"), VALUE),
        MeasurementMap.of(MEASUREMENT_DESCRIPTOR, 10.0));
    statsManager.record(
        createContext(factory, TagKey.create("another wrong key"), VALUE),
        MeasurementMap.of(MEASUREMENT_DESCRIPTOR, 50.0));
    DistributionView view = (DistributionView) statsManager.getView(VIEW_NAME);
    assertDistributionAggregationsEquivalent(
        TOLERANCE,
        view.getDistributionAggregations(),
        Arrays.asList(
            StatsTestUtil.createDistributionAggregation(
                // Won't record the unregistered tag key, will use default tag instead:
                // "KEY" : "unknown/not set".
                Arrays.asList(Tag.create(KEY, MutableView.UNKNOWN_TAG_VALUE)),
                BUCKET_BOUNDARIES,
                // Should record stats with default tag value: "KEY" : "unknown/not set".
                Arrays.asList(10.0, 50.0))));
  }

  @Test
  public void testViewWithMultipleTagKeys() {
    TagKey key1 = TagKey.create("Key-1");
    TagKey key2 = TagKey.create("Key-2");
    statsManager.registerView(
        DistributionViewDescriptor.create(
            VIEW_NAME,
            VIEW_DESCRIPTION,
            MEASUREMENT_DESCRIPTOR,
            DISTRIBUTION_AGGREGATION_DESCRIPTOR,
            Arrays.asList(key1, key2)));
    statsManager.record(
        createContext(factory, key1, TagValue.create("v1"), key2, TagValue.create("v10")),
        MeasurementMap.of(MEASUREMENT_DESCRIPTOR, 1.1));
    statsManager.record(
        createContext(factory, key1, TagValue.create("v1"), key2, TagValue.create("v20")),
        MeasurementMap.of(MEASUREMENT_DESCRIPTOR, 2.2));
    statsManager.record(
        createContext(factory, key1, TagValue.create("v2"), key2, TagValue.create("v10")),
        MeasurementMap.of(MEASUREMENT_DESCRIPTOR, 3.3));
    statsManager.record(
        createContext(factory, key1, TagValue.create("v1"), key2, TagValue.create("v10")),
        MeasurementMap.of(MEASUREMENT_DESCRIPTOR, 4.4));
    DistributionView view = (DistributionView) statsManager.getView(VIEW_NAME);
    assertDistributionAggregationsEquivalent(
        TOLERANCE,
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
  public void testMultipleViewsSameMeasure() {
    ViewDescriptor viewDescr1 =
        DistributionViewDescriptor.create(
            VIEW_NAME,
            VIEW_DESCRIPTION,
            MEASUREMENT_DESCRIPTOR,
            DISTRIBUTION_AGGREGATION_DESCRIPTOR,
            Arrays.asList(KEY));
    ViewDescriptor viewDescr2 =
        DistributionViewDescriptor.create(
            VIEW_NAME2,
            VIEW_DESCRIPTION,
            MEASUREMENT_DESCRIPTOR,
            DISTRIBUTION_AGGREGATION_DESCRIPTOR,
            Arrays.asList(KEY));
    clock.setTime(Timestamp.create(1, 1));
    statsManager.registerView(viewDescr1);
    clock.setTime(Timestamp.create(2, 2));
    statsManager.registerView(viewDescr2);
    statsManager.record(
        createContext(factory, KEY, VALUE), MeasurementMap.of(MEASUREMENT_DESCRIPTOR, 5.0));
    List<DistributionAggregation> expectedAggs =
        Arrays.asList(
            StatsTestUtil.createDistributionAggregation(
                Arrays.asList(Tag.create(KEY, VALUE)), BUCKET_BOUNDARIES, Arrays.asList(5.0)));
    clock.setTime(Timestamp.create(3, 3));
    DistributionView view1 = (DistributionView) statsManager.getView(VIEW_NAME);
    clock.setTime(Timestamp.create(4, 4));
    DistributionView view2 = (DistributionView) statsManager.getView(VIEW_NAME2);
    assertThat(view1.getStart()).isEqualTo(Timestamp.create(1, 1));
    assertThat(view1.getEnd()).isEqualTo(Timestamp.create(3, 3));
    assertDistributionAggregationsEquivalent(
        TOLERANCE, view1.getDistributionAggregations(), expectedAggs);
    assertThat(view2.getStart()).isEqualTo(Timestamp.create(2, 2));
    assertThat(view2.getEnd()).isEqualTo(Timestamp.create(4, 4));
    assertDistributionAggregationsEquivalent(
        TOLERANCE, view2.getDistributionAggregations(), expectedAggs);
  }

  @Test
  public void testMultipleViewsDifferentMeasures() {
    MeasurementDescriptor measureDescr1 =
        MeasurementDescriptor.create(MEASUREMENT_NAME, MEASUREMENT_DESCRIPTION, MEASUREMENT_UNIT);
    MeasurementDescriptor measureDescr2 =
        MeasurementDescriptor.create(MEASUREMENT_NAME2, MEASUREMENT_DESCRIPTION, MEASUREMENT_UNIT);
    ViewDescriptor viewDescr1 =
        DistributionViewDescriptor.create(
            VIEW_NAME,
            VIEW_DESCRIPTION,
            measureDescr1,
            DISTRIBUTION_AGGREGATION_DESCRIPTOR,
            Arrays.asList(KEY));
    ViewDescriptor viewDescr2 =
        DistributionViewDescriptor.create(
            VIEW_NAME2,
            VIEW_DESCRIPTION,
            measureDescr2,
            DISTRIBUTION_AGGREGATION_DESCRIPTOR,
            Arrays.asList(KEY));
    clock.setTime(Timestamp.create(1, 0));
    statsManager.registerView(viewDescr1);
    clock.setTime(Timestamp.create(2, 0));
    statsManager.registerView(viewDescr2);
    statsManager.record(
        createContext(factory, KEY, VALUE),
        MeasurementMap.of(measureDescr1, 1.1, measureDescr2, 2.2));
    clock.setTime(Timestamp.create(3, 0));
    DistributionView view1 = (DistributionView) statsManager.getView(VIEW_NAME);
    clock.setTime(Timestamp.create(4, 0));
    DistributionView view2 = (DistributionView) statsManager.getView(VIEW_NAME2);
    assertThat(view1.getStart()).isEqualTo(Timestamp.create(1, 0));
    assertThat(view1.getEnd()).isEqualTo(Timestamp.create(3, 0));
    assertDistributionAggregationsEquivalent(
        TOLERANCE,
        view1.getDistributionAggregations(),
        Arrays.asList(
            StatsTestUtil.createDistributionAggregation(
                Arrays.asList(Tag.create(KEY, VALUE)), BUCKET_BOUNDARIES, Arrays.asList(1.1))));
    assertThat(view2.getStart()).isEqualTo(Timestamp.create(2, 0));
    assertThat(view2.getEnd()).isEqualTo(Timestamp.create(4, 0));
    assertDistributionAggregationsEquivalent(
        TOLERANCE,
        view2.getDistributionAggregations(),
        Arrays.asList(
            StatsTestUtil.createDistributionAggregation(
                Arrays.asList(Tag.create(KEY, VALUE)), BUCKET_BOUNDARIES, Arrays.asList(2.2))));
  }
}
