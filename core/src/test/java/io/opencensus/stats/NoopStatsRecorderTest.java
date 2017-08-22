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

import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.tags.Tag;
import io.opencensus.tags.Tag.TagString;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagKey.TagKeyString;
import io.opencensus.tags.TagValueString;
import java.util.Collections;
import java.util.Iterator;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link StatsRecorder#getNoopStatsRecorder}. */
@RunWith(JUnit4.class)
public final class NoopStatsRecorderTest {
  private static final TagString TAG =
      TagString.create(TagKeyString.create("key"), TagValueString.create("value"));
  private static final MeasureDouble MEASURE =
      Measure.MeasureDouble.create("my measure", "description", "s");

  private final TagContext tagContext =
      new TagContext() {

        @Override
        public Iterator<Tag> unsafeGetIterator() {
          return Collections.<Tag>singleton(TAG).iterator();
        }
      };

  @Rule public final ExpectedException thrown = ExpectedException.none();

  // The NoopStatsRecorder should do nothing, so this test just checks that record doesn't throw an
  // exception.
  @Test
  public void record() {
    StatsRecorder.getNoopStatsRecorder()
        .record(tagContext, MeasureMap.builder().set(MEASURE, 5).build());
  }

  // The NoopStatsRecorder should do nothing, so this test just checks that record doesn't throw an
  // exception.
  @Test
  public void recordWithCurrentContext() {
    StatsRecorder.getNoopStatsRecorder().record(MeasureMap.builder().set(MEASURE, 6).build());
  }

  @Test
  public void record_DisallowNullTagContext() {
    MeasureMap measures = MeasureMap.builder().set(MEASURE, 7).build();
    thrown.expect(NullPointerException.class);
    StatsRecorder.getNoopStatsRecorder().record(null, measures);
  }

  @Test
  public void record_DisallowNullMeasureMap() {
    thrown.expect(NullPointerException.class);
    StatsRecorder.getNoopStatsRecorder().record(tagContext, null);
  }

  @Test
  public void recordWithCurrentContext_DisallowNullMeasureMap() {
    thrown.expect(NullPointerException.class);
    StatsRecorder.getNoopStatsRecorder().record(null);
  }
}
