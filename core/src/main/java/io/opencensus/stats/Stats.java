/*
 * Copyright 2016, Google Inc.
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

import io.opencensus.stats.internal.StatsInternal;
import javax.annotation.Nullable;

/** {@link Stats}. */
public final class Stats {
  @Nullable
  private static final StatsManagerFactory statsManagerFactory =
      StatsInternal.getStatsManagerFactory();

  @Nullable
  private static final StatsManager statsManager =
      statsManagerFactory == null ? null : statsManagerFactory.getDefaultStatsManager();

  /** Returns the default {@link StatsContextFactory}. */
  @Nullable
  public static StatsContextFactory getStatsContextFactory() {
    return statsManager == null ? null : statsManager.getStatsContextFactory();
  }

  /** Returns the default {@link StatsManager}. */
  @Nullable
  public static StatsManager getStatsManager() {
    return statsManager;
  }

  private Stats() {}
}
