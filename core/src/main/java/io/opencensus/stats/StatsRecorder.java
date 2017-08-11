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

import com.google.common.base.Preconditions;
import io.opencensus.tags.TagContext;
import javax.annotation.concurrent.Immutable;

/** Provides methods to record stats against tags. */
public abstract class StatsRecorder {
  private static final StatsRecorder NOOP_STATS_RECORDER = new NoopStatsRecorder();

  //  // TODO(sebright): Add a "record" method for implicit propagation:
  //
  //  public void record(MeasurementMap measurementValues) {
  //    record(getCurrentTagContext(), measurementValues);
  //  }

  /**
   * Records a set of measurements with a set of tags.
   *
   * @param tags the tags associated with the measurements.
   * @param measurementValues the measurements to record.
   */
  protected abstract void record(TagContext tags, MeasureMap measurementValues);

  /**
   * Returns a {@code StatsRecorder} that does not record any data.
   *
   * @return a {@code StatsRecorder} that does not record any data.
   */
  static StatsRecorder getNoopStatsRecorder() {
    return NOOP_STATS_RECORDER;
  }

  @Immutable
  private static final class NoopStatsRecorder extends StatsRecorder {

    @Override
    protected void record(TagContext tags, MeasureMap measurementValues) {
      Preconditions.checkNotNull(tags);
      Preconditions.checkNotNull(measurementValues);
    }
  }
}
