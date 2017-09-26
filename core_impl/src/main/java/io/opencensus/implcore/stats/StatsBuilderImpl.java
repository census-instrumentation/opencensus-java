/*
 * Copyright 2016-17, OpenCensus Authors
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

import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.stats.Measure.MeasureLong;
import io.opencensus.stats.StatsBuilder;
import io.opencensus.tags.TagContext;

/** Implementation of {@link StatsBuilder}. */
final class StatsBuilderImpl extends StatsBuilder {
  private final StatsManager statsManager;
  private final TagContext tags;
  private final MeasureMap.Builder builder = MeasureMap.builder();

  static StatsBuilderImpl create(StatsManager statsManager, TagContext tags) {
    return new StatsBuilderImpl(statsManager, tags);
  }

  private StatsBuilderImpl(StatsManager statsManager, TagContext tags) {
    this.statsManager = statsManager;
    this.tags = tags;
  }

  @Override
  public StatsBuilderImpl put(MeasureDouble measure, double value) {
    builder.put(measure, value);
    return this;
  }

  @Override
  public StatsBuilderImpl put(MeasureLong measure, long value) {
    builder.put(measure, value);
    return this;
  }

  @Override
  public void record() {
    statsManager.record(tags, builder.build());
  }
}
