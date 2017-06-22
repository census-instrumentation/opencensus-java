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

package io.opencensus.testing.stats;

import io.opencensus.common.Clock;
import io.opencensus.stats.StatsManager;
import io.opencensus.stats.internal.StatsInternal;
import javax.annotation.Nullable;

/**
 * Stats testing utilities.
 */
public final class StatsTester {
  private StatsTester() {}

  /**
   * Creates a new {@link StatsManager} for testing.
   */
  @Nullable
  public static StatsManager createTestStatsManager() {
    return StatsInternal.getStatsManagerFactory().createTestStatsManager();
  }

  /**
   * Creates a new {@link StatsManager} for testing.
   *
   * @param clock The clock that the {@code StatsManager} should use.
   */
  @Nullable
  public static StatsManager createTestStatsManager(Clock clock) {
    return StatsInternal.getStatsManagerFactory().createTestStatsManager(clock);
  }
}
