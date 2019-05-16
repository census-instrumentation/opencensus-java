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
import static io.opencensus.implcore.stats.MutableViewData.ZERO_TIMESTAMP;
import static io.opencensus.implcore.stats.StatsTestUtil.createEmptyViewData;

import com.google.common.collect.ImmutableMap;
import io.grpc.Context;
import io.opencensus.common.Duration;
import io.opencensus.common.Timestamp;
import io.opencensus.implcore.internal.EventQueue;
import io.opencensus.implcore.internal.SimpleEventQueue;
import io.opencensus.implcore.stats.StatsTestUtil.SimpleTagContext;
import io.opencensus.metrics.data.AttachmentValue;
import io.opencensus.metrics.data.AttachmentValue.AttachmentValueString;
import io.opencensus.metrics.data.Exemplar;
import io.opencensus.stats.Aggregation.Count;
import io.opencensus.stats.Aggregation.Distribution;
import io.opencensus.stats.Aggregation.Sum;
import io.opencensus.stats.AggregationData.CountData;
import io.opencensus.stats.AggregationData.DistributionData;
import io.opencensus.stats.BucketBoundaries;
import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.stats.MeasureMap;
import io.opencensus.stats.StatsCollectionState;
import io.opencensus.stats.StatsComponent;
import io.opencensus.stats.StatsRecorder;
import io.opencensus.stats.View;
import io.opencensus.stats.View.AggregationWindow.Cumulative;
import io.opencensus.stats.ViewData;
import io.opencensus.stats.ViewData.AggregationWindowData.CumulativeData;
import io.opencensus.stats.ViewManager;
import io.opencensus.tags.Tag;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import io.opencensus.tags.unsafe.ContextUtils;
import io.opencensus.testing.common.TestClock;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;

/** Tests for {@link StatsRecorderImpl}. */
@RunWith(JUnit4.class)
public final class StatsRecorderImplTest {
  private static final TagKey KEY = TagKey.create("KEY");
  private static final TagValue VALUE = TagValue.create("VALUE");
  private static final TagValue VALUE_2 = TagValue.create("VALUE_2");
  private static final MeasureDouble MEASURE_DOUBLE =
      MeasureDouble.create("my measurement", "description", "us");
  private static final MeasureDouble MEASURE_DOUBLE_NO_VIEW_1 =
      MeasureDouble.create("my measurement no view 1", "description", "us");
  private static final MeasureDouble MEASURE_DOUBLE_NO_VIEW_2 =
      MeasureDouble.create("my measurement no view 2", "description", "us");
  private static final View.Name VIEW_NAME = View.Name.create("my view");
  private static final BucketBoundaries BUCKET_BOUNDARIES =
      BucketBoundaries.create(Arrays.asList(-10.0, 0.0, 10.0));
  private static final Distribution DISTRIBUTION = Distribution.create(BUCKET_BOUNDARIES);
  private static final Distribution DISTRIBUTION_NO_HISTOGRAM =
      Distribution.create(BucketBoundaries.create(Collections.<Double>emptyList()));
  private static final Timestamp START_TIME = Timestamp.fromMillis(0);
  private static final Duration ONE_SECOND = Duration.fromMillis(1000);
  private static final AttachmentValue ATTACHMENT_VALUE_1 = AttachmentValueString.create("v1");
  private static final AttachmentValue ATTACHMENT_VALUE_2 = AttachmentValueString.create("v2");
  private static final AttachmentValue ATTACHMENT_VALUE_3 = AttachmentValueString.create("v3");

  private final TestClock testClock = TestClock.create();
  private final StatsComponent statsComponent =
      new StatsComponentImplBase(new SimpleEventQueue(), testClock);

  @Mock private final EventQueue mockQueue = Mockito.mock(EventQueue.class);

  private final ViewManager viewManager = statsComponent.getViewManager();
  private final StatsRecorder statsRecorder = statsComponent.getStatsRecorder();

  @Test
  public void record_CurrentContextNotSet() {
    View view =
        View.create(
            VIEW_NAME,
            "description",
            MEASURE_DOUBLE,
            Sum.create(),
            Arrays.asList(KEY),
            Cumulative.create());
    viewManager.registerView(view);
    statsRecorder.newMeasureMap().put(MEASURE_DOUBLE, 1.0).record();
    ViewData viewData = viewManager.getView(VIEW_NAME);

    // record() should have used the default TagContext, so the tag value should be null.
    assertThat(viewData.getAggregationMap().keySet())
        .containsExactly(Arrays.asList((TagValue) null));
  }

  @Test
  public void record_CurrentContextSet() {
    View view =
        View.create(
            VIEW_NAME,
            "description",
            MEASURE_DOUBLE,
            Sum.create(),
            Arrays.asList(KEY),
            Cumulative.create());
    viewManager.registerView(view);
    TagContext tags = new SimpleTagContext(Tag.create(KEY, VALUE));
    Context orig = ContextUtils.withValue(Context.current(), tags).attach();
    try {
      statsRecorder.newMeasureMap().put(MEASURE_DOUBLE, 1.0).record();
    } finally {
      Context.current().detach(orig);
    }
    ViewData viewData = viewManager.getView(VIEW_NAME);

    // record() should have used the given TagContext.
    assertThat(viewData.getAggregationMap().keySet()).containsExactly(Arrays.asList(VALUE));
  }

  @Test
  public void record_UnregisteredMeasure() {
    View view =
        View.create(
            VIEW_NAME,
            "description",
            MEASURE_DOUBLE,
            Sum.create(),
            Arrays.asList(KEY),
            Cumulative.create());
    viewManager.registerView(view);
    statsRecorder
        .newMeasureMap()
        .put(MEASURE_DOUBLE_NO_VIEW_1, 1.0)
        .put(MEASURE_DOUBLE, 2.0)
        .put(MEASURE_DOUBLE_NO_VIEW_2, 3.0)
        .record(new SimpleTagContext(Tag.create(KEY, VALUE)));
    ViewData viewData = viewManager.getView(VIEW_NAME);

    // There should be one entry.
    StatsTestUtil.assertAggregationMapEquals(
        viewData.getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE),
            StatsTestUtil.createAggregationData(Sum.create(), MEASURE_DOUBLE, 2.0)),
        1e-6);
  }

  @Test
  public void record_AllMeasuresUnregistered() {
    StatsComponent fakeStatsComponent = new StatsComponentImplBase(mockQueue, testClock);
    StatsRecorder fakeStatsRecorder = fakeStatsComponent.getStatsRecorder();
    fakeStatsRecorder
        .newMeasureMap()
        .put(MEASURE_DOUBLE_NO_VIEW_1, 1.0)
        .put(MEASURE_DOUBLE, 2.0)
        .put(MEASURE_DOUBLE_NO_VIEW_2, 3.0)
        .record();
    Mockito.verifyZeroInteractions(mockQueue);
  }

  @Test
  public void record_WithAttachments_Distribution() {
    testClock.setTime(START_TIME);
    View view =
        View.create(VIEW_NAME, "description", MEASURE_DOUBLE, DISTRIBUTION, Arrays.asList(KEY));
    viewManager.registerView(view);
    recordWithAttachments();
    ViewData viewData = viewManager.getView(VIEW_NAME);
    assertThat(viewData).isNotNull();
    DistributionData distributionData =
        (DistributionData) viewData.getAggregationMap().get(Collections.singletonList(VALUE));
    List<Exemplar> expected =
        Arrays.asList(
            Exemplar.create(
                1.0, Timestamp.create(2, 0), Collections.singletonMap("k2", ATTACHMENT_VALUE_2)),
            Exemplar.create(
                12.0, Timestamp.create(3, 0), Collections.singletonMap("k1", ATTACHMENT_VALUE_3)));
    assertThat(distributionData.getExemplars()).containsExactlyElementsIn(expected).inOrder();
  }

  @Test
  public void record_WithAttachments_DistributionNoHistogram() {
    testClock.setTime(START_TIME);
    View view =
        View.create(
            VIEW_NAME,
            "description",
            MEASURE_DOUBLE,
            DISTRIBUTION_NO_HISTOGRAM,
            Arrays.asList(KEY));
    viewManager.registerView(view);
    recordWithAttachments();
    ViewData viewData = viewManager.getView(VIEW_NAME);
    assertThat(viewData).isNotNull();
    DistributionData distributionData =
        (DistributionData) viewData.getAggregationMap().get(Collections.singletonList(VALUE));
    // Recording exemplar has no effect if there's no histogram.
    assertThat(distributionData.getExemplars()).isEmpty();
  }

  @Test
  public void record_WithAttachments_Count() {
    testClock.setTime(START_TIME);
    View view =
        View.create(VIEW_NAME, "description", MEASURE_DOUBLE, Count.create(), Arrays.asList(KEY));
    viewManager.registerView(view);
    recordWithAttachments();
    ViewData viewData = viewManager.getView(VIEW_NAME);
    assertThat(viewData).isNotNull();
    CountData countData =
        (CountData) viewData.getAggregationMap().get(Collections.singletonList(VALUE));
    // Recording exemplar does not affect views with an aggregation other than distribution.
    assertThat(countData.getCount()).isEqualTo(2L);
  }

  private void recordWithAttachments() {
    TagContext context = new SimpleTagContext(Tag.create(KEY, VALUE));

    // The test Distribution has bucket boundaries [-10.0, 0.0, 10.0].

    testClock.advanceTime(ONE_SECOND); // 1st second.
    // -1.0 is in the 2nd bucket [-10.0, 0.0).
    statsRecorder
        .newMeasureMap()
        .put(MEASURE_DOUBLE, -1.0)
        .putAttachment("k1", ATTACHMENT_VALUE_1)
        .record(context);

    testClock.advanceTime(ONE_SECOND); // 2nd second.
    // 1.0 is in the 3rd bucket [0.0, 10.0).
    statsRecorder
        .newMeasureMap()
        .put(MEASURE_DOUBLE, 1.0)
        .putAttachment("k2", ATTACHMENT_VALUE_2)
        .record(context);

    testClock.advanceTime(ONE_SECOND); // 3rd second.
    // 12.0 is in the 4th bucket [10.0, +Inf).
    statsRecorder
        .newMeasureMap()
        .put(MEASURE_DOUBLE, 12.0)
        .putAttachment("k1", ATTACHMENT_VALUE_3)
        .record(context);

    testClock.advanceTime(ONE_SECOND); // 4th second.
    // -20.0 is in the 1st bucket [-Inf, -10.0).
    statsRecorder
        .newMeasureMap()
        .put(MEASURE_DOUBLE, -20.0)
        .putAttachment("k3", ATTACHMENT_VALUE_1)
        .record(context);

    testClock.advanceTime(ONE_SECOND); // 5th second.
    // -5.0 is in the 2nd bucket [-10.0, 0), should overwrite the previous exemplar -1.0.
    statsRecorder
        .newMeasureMap()
        .put(MEASURE_DOUBLE, -5.0)
        .putAttachment("k3", ATTACHMENT_VALUE_3)
        .record(context);

    testClock.advanceTime(ONE_SECOND); // 6th second.
    // -3.0 is in the 2nd bucket [-10.0, 0), but this value doesn't come with attachments, so it
    // shouldn't overwrite the previous exemplar (-5.0).
    statsRecorder.newMeasureMap().put(MEASURE_DOUBLE, -3.0).record(context);
  }

  @Test
  public void recordTwice() {
    View view =
        View.create(
            VIEW_NAME,
            "description",
            MEASURE_DOUBLE,
            Sum.create(),
            Arrays.asList(KEY),
            Cumulative.create());
    viewManager.registerView(view);
    MeasureMap statsRecord = statsRecorder.newMeasureMap().put(MEASURE_DOUBLE, 1.0);
    statsRecord.record(new SimpleTagContext(Tag.create(KEY, VALUE)));
    statsRecord.record(new SimpleTagContext(Tag.create(KEY, VALUE_2)));
    ViewData viewData = viewManager.getView(VIEW_NAME);

    // There should be two entries.
    StatsTestUtil.assertAggregationMapEquals(
        viewData.getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE),
            StatsTestUtil.createAggregationData(Sum.create(), MEASURE_DOUBLE, 1.0),
            Arrays.asList(VALUE_2),
            StatsTestUtil.createAggregationData(Sum.create(), MEASURE_DOUBLE, 1.0)),
        1e-6);
  }

  @Test
  public void record_MapDeprecatedRpcConstants() {
    View view =
        View.create(
            VIEW_NAME,
            "description",
            MEASURE_DOUBLE,
            Sum.create(),
            Arrays.asList(RecordUtils.RPC_METHOD));

    viewManager.registerView(view);
    MeasureMap statsRecord = statsRecorder.newMeasureMap().put(MEASURE_DOUBLE, 1.0);
    statsRecord.record(new SimpleTagContext(Tag.create(RecordUtils.GRPC_CLIENT_METHOD, VALUE)));
    ViewData viewData = viewManager.getView(VIEW_NAME);

    // There should be two entries.
    StatsTestUtil.assertAggregationMapEquals(
        viewData.getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE),
            StatsTestUtil.createAggregationData(Sum.create(), MEASURE_DOUBLE, 1.0)),
        1e-6);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void record_StatsDisabled() {
    View view =
        View.create(
            VIEW_NAME,
            "description",
            MEASURE_DOUBLE,
            Sum.create(),
            Arrays.asList(KEY),
            Cumulative.create());

    viewManager.registerView(view);
    statsComponent.setState(StatsCollectionState.DISABLED);
    statsRecorder
        .newMeasureMap()
        .put(MEASURE_DOUBLE, 1.0)
        .record(new SimpleTagContext(Tag.create(KEY, VALUE)));
    assertThat(viewManager.getView(VIEW_NAME)).isEqualTo(createEmptyViewData(view));
  }

  @Test
  @SuppressWarnings("deprecation")
  public void record_StatsReenabled() {
    View view =
        View.create(
            VIEW_NAME,
            "description",
            MEASURE_DOUBLE,
            Sum.create(),
            Arrays.asList(KEY),
            Cumulative.create());
    viewManager.registerView(view);

    statsComponent.setState(StatsCollectionState.DISABLED);
    statsRecorder
        .newMeasureMap()
        .put(MEASURE_DOUBLE, 1.0)
        .record(new SimpleTagContext(Tag.create(KEY, VALUE)));
    assertThat(viewManager.getView(VIEW_NAME)).isEqualTo(createEmptyViewData(view));

    statsComponent.setState(StatsCollectionState.ENABLED);
    assertThat(viewManager.getView(VIEW_NAME).getAggregationMap()).isEmpty();
    assertThat(viewManager.getView(VIEW_NAME).getWindowData())
        .isNotEqualTo(CumulativeData.create(ZERO_TIMESTAMP, ZERO_TIMESTAMP));
    statsRecorder
        .newMeasureMap()
        .put(MEASURE_DOUBLE, 4.0)
        .record(new SimpleTagContext(Tag.create(KEY, VALUE)));
    StatsTestUtil.assertAggregationMapEquals(
        viewManager.getView(VIEW_NAME).getAggregationMap(),
        ImmutableMap.of(
            Arrays.asList(VALUE),
            StatsTestUtil.createAggregationData(Sum.create(), MEASURE_DOUBLE, 4.0)),
        1e-6);
  }
}
