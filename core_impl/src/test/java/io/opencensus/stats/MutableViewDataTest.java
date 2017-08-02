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
import io.opencensus.internal.SimpleEventQueue;
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
import io.opencensus.stats.MutableAggregation.MutableCount;
import io.opencensus.stats.MutableAggregation.MutableHistogram;
import io.opencensus.stats.MutableAggregation.MutableMean;
import io.opencensus.stats.MutableAggregation.MutableRange;
import io.opencensus.stats.MutableAggregation.MutableStdDev;
import io.opencensus.stats.MutableAggregation.MutableSum;
import io.opencensus.stats.View.Window;
import io.opencensus.stats.View.Window.Cumulative;
import io.opencensus.stats.View.Window.Interval;
import io.opencensus.testing.common.TestClock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link MutableViewData}. */
@RunWith(JUnit4.class)
public class MutableViewDataTest {

  private final TestClock clock = TestClock.create();
  private final StatsContextImpl context = new StatsContextImpl(
      new StatsRecorderImpl(new StatsManager(new SimpleEventQueue(), clock)),
      Collections.<TagKey, TagValue>emptyMap());

  private static final Cumulative CUMULATIVE = Cumulative.create();
  private static final int MILLIS_PER_SECOND = 1000;
  private static final Duration TEN_SECONDS = Duration.create(10, 0);
  private static final Window INTERVAL = Interval.create(TEN_SECONDS);
  private static final double EPSILON = 1e-7;
  private static final Measure MEASURE = Measure.MeasureDouble.create(
      "measure", "description", "1");
  private static final View.Name VIEW_NAME = View.Name.create("view");
  private static final String VIEW_DESC = "description";
  private static final List<TagKey> TAG_KEYS = Arrays.asList(TagKey.create("key1"));
  private static final List<Aggregation> AGGREGATIONS = Arrays.asList(
      Sum.create(),
      Count.create(),
      Histogram.create(BucketBoundaries.create(Arrays.asList(-10.0, 0.0, 10.0))),
      Range.create(),
      Mean.create(),
      StdDev.create());

  private static final TagKey ORIGINATOR = TagKey.create("originator");
  private static final TagKey CALLER = TagKey.create("caller");
  private static final TagKey METHOD = TagKey.create("method");
  private static final TagValue CALLER_V = TagValue.create("some caller");
  private static final TagValue METHOD_V = TagValue.create("some method");

  @Test
  public void testConstants() {
    assertThat(MutableViewData.UNKNOWN_TAG_VALUE.asString()).isEqualTo("unknown/not set");
  }

  @Test
  public void testGetTagValues() {
    List<TagKey> columns = Arrays.asList(CALLER, METHOD, ORIGINATOR);
    Map<TagKey, TagValue> tags = ImmutableMap.of(CALLER, CALLER_V, METHOD, METHOD_V);

    assertThat(MutableViewData.getTagValues(tags, columns))
        .containsExactly(CALLER_V, METHOD_V, MutableViewData.UNKNOWN_TAG_VALUE)
        .inOrder();
  }

  @Test
  public void testCreateMutableAggregation() {
    BucketBoundaries bucketBoundaries = BucketBoundaries.create(Arrays.asList(-1.0, 0.0, 1.0));

    assertThat(
        ((MutableSum) MutableViewData.createMutableAggregation(Sum.create())).getSum())
        .isWithin(EPSILON).of(0.0);
    assertThat(
        ((MutableCount) MutableViewData.createMutableAggregation(Count.create())).getCount())
        .isEqualTo(0L);
    assertThat(
        ((MutableHistogram) MutableViewData.createMutableAggregation(
            Histogram.create(bucketBoundaries))).getBucketCounts())
        .isEqualTo(new long[]{0, 0, 0, 0});
    assertThat(
        ((MutableRange) MutableViewData.createMutableAggregation(Range.create())).getMin())
        .isPositiveInfinity();
    assertThat(
        ((MutableRange) MutableViewData.createMutableAggregation(Range.create())).getMax())
        .isNegativeInfinity();
    assertThat(
        ((MutableMean) MutableViewData.createMutableAggregation(Mean.create())).getMean())
        .isWithin(EPSILON).of(0D);
    assertThat(
        ((MutableStdDev) MutableViewData.createMutableAggregation(StdDev.create())).getStdDev())
        .isWithin(EPSILON).of(0D);
  }

  @Test
  public void testCreateAggregationData() {
    BucketBoundaries bucketBoundaries = BucketBoundaries.create(Arrays.asList(-1.0, 0.0, 1.0));
    List<MutableAggregation> mutableAggregations = Arrays.asList(
        MutableSum.create(),
        MutableCount.create(),
        MutableHistogram.create(bucketBoundaries),
        MutableRange.create(),
        MutableMean.create(),
        MutableStdDev.create());
    List<AggregationData> aggregates = new ArrayList<AggregationData>();
    for (MutableAggregation mutableAggregation : mutableAggregations) {
      aggregates.add(MutableViewData.createAggregationData(mutableAggregation));
    }
    assertThat(aggregates).containsExactly(
        SumData.create(0),
        CountData.create(0),
        HistogramData.create(0, 0, 0, 0),
        RangeData.create(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY),
        MeanData.create(0),
        StdDevData.create(0))
        .inOrder();
  }

  @Test
  public void testRecordCumulative() {
    View view = View.create(VIEW_NAME, VIEW_DESC, MEASURE, AGGREGATIONS, TAG_KEYS, CUMULATIVE);
    MutableViewData mView = MutableViewData.create(view, Timestamp.create(5, 0));

    double[] values = {-1.0, 1.0, -5.0, 20.0, 5.0};

    for (double value : values) {
      mView.record(context, value, clock);
    }

    StatsTestUtil.assertAggregationMapEquals(
        mView.toViewData(clock).getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(MutableViewData.UNKNOWN_TAG_VALUE),
            StatsTestUtil.createAggregationData(AGGREGATIONS, values)),
        EPSILON);
  }

  @Test
  public void testRecordInterval() {
    // The interval is 10 seconds, i.e. values should expire after 10 seconds.
    View view = View.create(VIEW_NAME, VIEW_DESC, MEASURE, AGGREGATIONS, TAG_KEYS, INTERVAL);
    MutableViewData mView = MutableViewData.create(view, Timestamp.fromMillis(100));

    double[] values = {20.0, -1.0, 1.0, -5.0, 5.0};
    for (int i = 1; i <= 5; i++) {
      // Add each value in sequence, at 1st second, 2nd second, 3rd second, etc.
      clock.setTime(Timestamp.fromMillis(i * MILLIS_PER_SECOND));
      mView.record(context, values[i - 1], clock);
    }

    clock.setTime(Timestamp.fromMillis(8 * MILLIS_PER_SECOND));
    // 8s, no values should have expired
    StatsTestUtil.assertAggregationMapEquals(
        mView.toViewData(clock).getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(MutableViewData.UNKNOWN_TAG_VALUE),
            StatsTestUtil.createAggregationData(AGGREGATIONS, values)),
        EPSILON);

    clock.setTime(Timestamp.fromMillis((long) (11.1 * MILLIS_PER_SECOND)));
    // 11.1s, the first value should have expired
    StatsTestUtil.assertAggregationMapEquals(
        mView.toViewData(clock).getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(MutableViewData.UNKNOWN_TAG_VALUE),
            StatsTestUtil.createAggregationData(AGGREGATIONS, -1.0, 1.0, -5.0, 5.0)),
        EPSILON);

    clock.setTime(Timestamp.fromMillis((long) (13.1 * MILLIS_PER_SECOND)));
    // 13.1s, the 1st, 2nd and 3rd value should have expired
    StatsTestUtil.assertAggregationMapEquals(
        mView.toViewData(clock).getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(MutableViewData.UNKNOWN_TAG_VALUE),
            StatsTestUtil.createAggregationData(AGGREGATIONS, -5.0, 5.0)),
        EPSILON);

    clock.setTime(Timestamp.fromMillis((long) 13.2 * MILLIS_PER_SECOND));
    // 13.2s, add a new value 9.0
    mView.record(context, 9.0, clock);

    clock.setTime(Timestamp.fromMillis((long) (13.5 * MILLIS_PER_SECOND)));
    // 13.5s, after adding value 9.0
    StatsTestUtil.assertAggregationMapEquals(
        mView.toViewData(clock).getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(MutableViewData.UNKNOWN_TAG_VALUE),
            StatsTestUtil.createAggregationData(AGGREGATIONS, -5.0, 5.0, 9.0)),
        EPSILON);

    clock.setTime(Timestamp.fromMillis((long) (60.0 * MILLIS_PER_SECOND)));
    // 60s, all values should have expired
    StatsTestUtil.assertAggregationMapEquals(
        mView.toViewData(clock).getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(MutableViewData.UNKNOWN_TAG_VALUE),
            StatsTestUtil.createAggregationData(AGGREGATIONS)),
        EPSILON);
  }
}
