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

import io.opencensus.common.Clock;

/**
 * Object that determines which implementation of the {@link StatsManager} to use.
 */
public abstract class StatsManagerFactory {

  /**
   * Returns the default {@link StatsManager}.
   */
  public abstract StatsManager getDefaultStatsManager();

  /**
   * Creates a new {@link StatsManager} for testing.
   */
  public abstract StatsManager createTestStatsManager();

  /**
   * Creates a new {@link StatsManager} for testing.
   *
   * @param clock The clock that the {@code StatsManager} should use.
   */
  public abstract StatsManager createTestStatsManager(Clock clock);
}
