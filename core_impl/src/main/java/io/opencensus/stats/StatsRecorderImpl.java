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

import static com.google.common.base.Preconditions.checkNotNull;

/** Implementation of {@link StatsRecorder}. */
final class StatsRecorderImpl extends StatsRecorder {
  private final StatsManager statsManager;

  StatsRecorderImpl(StatsManager statsManager) {
    checkNotNull(statsManager, "statsManager");
    this.statsManager = statsManager;
  }

  @Override
  void record(StatsContext tags, MeasureMap measurementValues) {
    // TODO(sebright): Replace StatsContext with TagContext, and then this cast won't be necessary.
    statsManager.record((StatsContextImpl) tags, measurementValues);
  }
}
