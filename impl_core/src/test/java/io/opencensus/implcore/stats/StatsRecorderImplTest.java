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
import com.google.common.collect.Lists;
import io.grpc.Context;
import io.opencensus.implcore.internal.SimpleEventQueue;
import io.opencensus.stats.Aggregation.Sum;
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
import java.util.Iterator;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

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

  private final StatsComponent statsComponent =
      new StatsComponentImplBase(new SimpleEventQueue(), TestClock.create());

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
    Context orig =
        Context.current()
            .withValue(ContextUtils.TAG_CONTEXT_KEY, new SimpleTagContext(Tag.create(KEY, VALUE)))
            .attach();
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

  private static final class SimpleTagContext extends TagContext {
    private final List<Tag> tags;

    SimpleTagContext(Tag... tags) {
      this.tags = Collections.unmodifiableList(Lists.newArrayList(tags));
    }

    @Override
    protected Iterator<Tag> getIterator() {
      return tags.iterator();
    }
  }
}
