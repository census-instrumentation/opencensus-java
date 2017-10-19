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
import io.opencensus.tags.Tag.TagString;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagKey.TagKeyString;
import io.opencensus.tags.TagValue.TagValueString;
import java.util.Collections;
import java.util.Iterator;
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
  private static final TagString TAG =
      TagString.create(TagKeyString.create("key"), TagValueString.create("value"));
  private static final MeasureDouble MEASURE =
      Measure.MeasureDouble.create("my measure", "description", "s");

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
  public void noopStatsComponent_SetState_IgnoresInput() {
    StatsComponent noopStatsComponent = NoopStats.newNoopStatsComponent();
    noopStatsComponent.setState(StatsCollectionState.ENABLED);
    assertThat(noopStatsComponent.getState()).isEqualTo(StatsCollectionState.DISABLED);
  }

  @Test
  public void noopStatsComponent_SetState_DisallowsNull() {
    StatsComponent noopStatsComponent = NoopStats.newNoopStatsComponent();
    thrown.expect(NullPointerException.class);
    noopStatsComponent.setState(null);
  }

  // The NoopStatsRecorder should do nothing, so this test just checks that record doesn't throw an
  // exception.
  @Test
  public void noopStatsRecorder_Record() {
    NoopStats.getNoopStatsRecorder()
        .record(tagContext, MeasureMap.builder().put(MEASURE, 5).build());
  }

  // The NoopStatsRecorder should do nothing, so this test just checks that record doesn't throw an
  // exception.
  @Test
  public void noopStatsRecorder_RecordWithCurrentContext() {
    NoopStats.getNoopStatsRecorder().record(MeasureMap.builder().put(MEASURE, 6).build());
  }

  @Test
  public void noopStatsRecorder_Record_DisallowNullTagContext() {
    MeasureMap measures = MeasureMap.builder().put(MEASURE, 7).build();
    thrown.expect(NullPointerException.class);
    NoopStats.getNoopStatsRecorder().record(null, measures);
  }

  @Test
  public void noopStatsRecorder_Record_DisallowNullMeasureMap() {
    thrown.expect(NullPointerException.class);
    NoopStats.getNoopStatsRecorder().record(tagContext, null);
  }

  @Test
  public void noopStatsRecorder_RecordWithCurrentContext_DisallowNullMeasureMap() {
    thrown.expect(NullPointerException.class);
    NoopStats.getNoopStatsRecorder().record(null);
  }
}
