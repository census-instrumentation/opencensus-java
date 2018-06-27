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

import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.tags.Tag;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@link NoopStats}. Tests for {@link NoopStats#newNoopViewManager} are in {@link
 * NoopViewManagerTest}
 */
@RunWith(JUnit4.class)
public final class NoopStatsTest {
  private static final Tag TAG = Tag.create(TagKey.create("key"), TagValue.create("value"));
  private static final MeasureDouble MEASURE =
      Measure.MeasureDouble.create("my measure", "description", "s");
  private static final Map<String, String> ATTACHMENTS = Collections.singletonMap("key", "value");

  private final TagContext tagContext =
      new TagContext() {

        @Override
        protected Iterator<Tag> getIterator() {
          return Collections.<Tag>singleton(TAG).iterator();
        }
      };

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void noopStatsComponent() {
    assertThat(NoopStats.newNoopStatsComponent().getStatsRecorder())
        .isSameAs(NoopStats.getNoopStatsRecorder());
    assertThat(NoopStats.newNoopStatsComponent().getViewManager())
        .isInstanceOf(NoopStats.newNoopViewManager().getClass());
  }

  @Test
  public void noopStatsComponent_GetState() {
    assertThat(NoopStats.newNoopStatsComponent().getState())
        .isEqualTo(StatsCollectionState.DISABLED);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void noopStatsComponent_SetState_IgnoresInput() {
    StatsComponent noopStatsComponent = NoopStats.newNoopStatsComponent();
    noopStatsComponent.setState(StatsCollectionState.ENABLED);
    assertThat(noopStatsComponent.getState()).isEqualTo(StatsCollectionState.DISABLED);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void noopStatsComponent_SetState_DisallowsNull() {
    StatsComponent noopStatsComponent = NoopStats.newNoopStatsComponent();
    thrown.expect(NullPointerException.class);
    noopStatsComponent.setState(null);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void noopStatsComponent_DisallowsSetStateAfterGetState() {
    StatsComponent noopStatsComponent = NoopStats.newNoopStatsComponent();
    noopStatsComponent.setState(StatsCollectionState.DISABLED);
    noopStatsComponent.getState();
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("State was already read, cannot set state.");
    noopStatsComponent.setState(StatsCollectionState.ENABLED);
  }

  @Test
  public void noopStatsRecorder_Record_DisallowNullTagContext() {
    MeasureMap measureMap = NoopStats.getNoopStatsRecorder().newMeasureMap();
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("tags");
    measureMap.record((TagContext) null);
  }

  @Test
  public void noopStatsRecorder_Record_DisallowNullAttachments() {
    MeasureMap measureMap = NoopStats.getNoopStatsRecorder().newMeasureMap();
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("attachments");
    measureMap.record((Map<String, String>) null);
  }

  @Test
  public void noopStatsRecorder_RecordWithAttachments_DisallowNullTagContext() {
    MeasureMap measureMap = NoopStats.getNoopStatsRecorder().newMeasureMap();
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("tags");
    measureMap.record(null, ATTACHMENTS);
  }

  @Test
  public void noopStatsRecorder_RecordWithAttachments_DisallowNullAttachments() {
    MeasureMap measureMap = NoopStats.getNoopStatsRecorder().newMeasureMap();
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("attachments");
    measureMap.record(tagContext, null);
  }

  // The NoopStatsRecorder should do nothing, so the tests below just checks that record doesn't
  // throw an exception.
  @Test
  public void noopStatsRecorder_Record() {
    NoopStats.getNoopStatsRecorder().newMeasureMap().put(MEASURE, 5).record(tagContext);
  }

  @Test
  public void noopStatsRecorder_RecordWithCurrentContext() {
    NoopStats.getNoopStatsRecorder().newMeasureMap().put(MEASURE, 6).record();
  }

  @Test
  public void noopStatsRecorder_RecordWithAttachments() {
    NoopStats.getNoopStatsRecorder()
        .newMeasureMap()
        .put(MEASURE, 5)
        .record(tagContext, ATTACHMENTS);
  }

  @Test
  public void noopStatsRecorder_RecordWithCurrentContextAndAttachments() {
    NoopStats.getNoopStatsRecorder().newMeasureMap().put(MEASURE, 6).record(ATTACHMENTS);
  }
}
