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

package io.opencensus.stats.internal;

import com.google.common.annotations.VisibleForTesting;
import io.opencensus.internal.Provider;
import io.opencensus.stats.StatsManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/** Class for instantiating the {@link StatsManager}. */
public final class StatsInternal {
  private static final Logger logger = Logger.getLogger(StatsInternal.class.getName());

  private static final StatsManager statsManager =
      loadStatsManager(Provider.getCorrectClassLoader(StatsManager.class));

  /** Returns the default {@link StatsManager}. */
  @Nullable
  public static StatsManager getStatsManager() {
    return statsManager;
  }

  // Any provider that may be used for StatsManager can be added here.
  @VisibleForTesting
  @Nullable
  static StatsManager loadStatsManager(ClassLoader classLoader) {
    try {
      // Call Class.forName with literal string name of the class to help shading tools.
      return Provider.createInstance(
          Class.forName("io.opencensus.stats.StatsManagerImpl", true, classLoader),
          StatsManager.class);
    } catch (ClassNotFoundException e) {
      logger.log(
          Level.FINE,
          "Couldn't load full implementation for StatsManager, now trying to load lite "
              + "implementation.",
          e);
    }
    try {
      // Call Class.forName with literal string name of the class to help shading tools.
      return Provider.createInstance(
          Class.forName("io.opencensus.stats.StatsManagerImplLite", true, classLoader),
          StatsManager.class);
    } catch (ClassNotFoundException e) {
      logger.log(
          Level.FINE,
          "Couldn't load lite implementation for StatsManager, now using "
              + "default implementation for StatsManager.",
          e);
    }
    // TODO: Add a no-op implementation.
    return null;
  }

  private StatsInternal() {}
}
