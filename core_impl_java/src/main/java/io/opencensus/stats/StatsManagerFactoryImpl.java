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

import io.opencensus.common.MillisClock;
import io.opencensus.internal.DisruptorEventQueue;

/** Java 7 and 8 implementation of {@link StatsManagerFactory}. */
public final class StatsManagerFactoryImpl extends StatsManagerFactoryImplBase {

  /** Public constructor to be used with reflection loading. */
  public StatsManagerFactoryImpl() {}

  private final StatsManager defaultStatsManager = createStatsManager();

  @Override
  public final StatsManager createTestStatsManager() {
    return createStatsManager();
  }

  @Override
  public StatsManager getDefaultStatsManager() {
    return defaultStatsManager;
  }

  private static StatsManager createStatsManager() {
    return new StatsManagerImpl(DisruptorEventQueue.getInstance(), MillisClock.getInstance());
  }
}
