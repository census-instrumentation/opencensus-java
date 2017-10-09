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

package io.opencensus.stats;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.common.Duration;
import io.opencensus.common.Timestamp;
import io.opencensus.stats.Aggregation.Sum;
import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.stats.View.AggregationWindow.Cumulative;
import io.opencensus.stats.View.AggregationWindow.Interval;
import io.opencensus.stats.View.Name;
import io.opencensus.stats.ViewData.AggregationWindowData.CumulativeData;
import io.opencensus.stats.ViewData.AggregationWindowData.IntervalData;
import io.opencensus.tags.TagKey.TagKeyString;
import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link NoopStats#newNoopViewManager}. */
@RunWith(JUnit4.class)
public final class NoopViewManagerTest {
  private static final MeasureDouble MEASURE =
      Measure.MeasureDouble.create("my measure", "description", "s");
  private static final TagKeyString KEY = TagKeyString.create("KEY");
  private static final Name VIEW_NAME = Name.create("my view");
  private static final String VIEW_DESCRIPTION = "view description";
  private static final Sum AGGREGATION = Sum.create();
  private static final Cumulative CUMULATIVE = Cumulative.create();
  private static final Duration TEN_SECONDS = Duration.create(10, 0);
  private static final Interval INTERVAL = Interval.create(TEN_SECONDS);

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void noopViewManager_RegisterView_DisallowRegisteringDifferentViewWithSameName() {
    final View view1 =
        View.create(
            VIEW_NAME, "description 1", MEASURE, AGGREGATION, Arrays.asList(KEY), CUMULATIVE);
    final View view2 =
        View.create(
            VIEW_NAME, "description 2", MEASURE, AGGREGATION, Arrays.asList(KEY), CUMULATIVE);
    ViewManager viewManager = NoopStats.newNoopViewManager();
    viewManager.registerView(view1);

    try {
      thrown.expect(IllegalArgumentException.class);
      thrown.expectMessage("A different view with the same name already exists.");
      viewManager.registerView(view2);
    } finally {
      assertThat(viewManager.getView(VIEW_NAME).getView()).isEqualTo(view1);
    }
  }

  @Test
  public void noopViewManager_RegisterView_AllowRegisteringSameViewTwice() {
    View view =
        View.create(
            VIEW_NAME, VIEW_DESCRIPTION, MEASURE, AGGREGATION, Arrays.asList(KEY), CUMULATIVE);
    ViewManager viewManager = NoopStats.newNoopViewManager();
    viewManager.registerView(view);
    viewManager.registerView(view);
  }

  @Test
  public void noopViewManager_RegisterView_DisallowNull() {
    ViewManager viewManager = NoopStats.newNoopViewManager();
    thrown.expect(NullPointerException.class);
    viewManager.registerView(null);
  }

  @Test
  public void noopViewManager_GetView_DisallowGettingNonExistentView() {
    ViewManager viewManager = NoopStats.newNoopViewManager();

    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("View is not registered.");
    viewManager.getView(VIEW_NAME);
  }

  @Test
  public void noopViewManager_GetView_Cumulative() {
    View view =
        View.create(
            VIEW_NAME, VIEW_DESCRIPTION, MEASURE, AGGREGATION, Arrays.asList(KEY), CUMULATIVE);
    ViewManager viewManager = NoopStats.newNoopViewManager();
    viewManager.registerView(view);

    ViewData viewData = viewManager.getView(VIEW_NAME);
    assertThat(viewData.getView()).isEqualTo(view);
    assertThat(viewData.getAggregationMap()).isEmpty();
    assertThat(viewData.getWindowData())
        .isEqualTo(CumulativeData.create(Timestamp.create(0, 0), Timestamp.create(0, 0)));
  }

  @Test
  public void noopViewManager_GetView_Interval() {
    View view =
        View.create(
            VIEW_NAME, VIEW_DESCRIPTION, MEASURE, AGGREGATION, Arrays.asList(KEY), INTERVAL);
    ViewManager viewManager = NoopStats.newNoopViewManager();
    viewManager.registerView(view);

    ViewData viewData = viewManager.getView(VIEW_NAME);
    assertThat(viewData.getView()).isEqualTo(view);
    assertThat(viewData.getAggregationMap()).isEmpty();
    assertThat(viewData.getWindowData()).isEqualTo(IntervalData.create(Timestamp.create(0, 0)));
  }

  @Test
  public void noopViewManager_GetView_DisallowNull() {
    ViewManager viewManager = NoopStats.newNoopViewManager();
    thrown.expect(NullPointerException.class);
    viewManager.getView(null);
  }
}
