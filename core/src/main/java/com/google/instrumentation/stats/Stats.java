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

package com.google.instrumentation.stats;

import com.google.common.annotations.VisibleForTesting;
import io.opencensus.internal.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/** {@link Stats}. */
public final class Stats {
  private static final Logger logger = Logger.getLogger(Stats.class.getName());

  private static final StatsManager statsManager =
      loadStatsManager(Provider.getCorrectClassLoader(StatsManager.class));

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

  // Any provider that may be used for StatsManager can be added here.
  @VisibleForTesting
  @Nullable
  static StatsManager loadStatsManager(ClassLoader classLoader) {
    try {
      // Call Class.forName with literal string name of the class to help shading tools.
      return Provider.createInstance(
          Class.forName("com.google.instrumentation.stats.StatsManagerImpl", true, classLoader),
          StatsManager.class);
    } catch (ClassNotFoundException e) {
      logger.log(Level.FINE, "Try to load lite implementation.", e);
    }
    try {
      // Call Class.forName with literal string name of the class to help shading tools.
      return Provider.createInstance(
          Class.forName("com.google.instrumentation.stats.StatsManagerImplLite", true, classLoader),
          StatsManager.class);
    } catch (ClassNotFoundException e) {
      logger.log(Level.FINE, "Using default implementation for StatsManager.", e);
    }
    // TODO: Add a no-op implementation.
    return null;
  }

  private Stats() {}
}
