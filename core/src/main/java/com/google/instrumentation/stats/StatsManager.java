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

/**
 * Provides facillities to register {@link ViewDescriptor}s for collecting stats and retrieving
 * stats data as a {@link View}.
 */
public abstract class StatsManager {
  /**
   * Pull model for stats. Registers a {@link ViewDescriptor} that will collect data to be accessed
   * via {@link #getView(ViewDescriptor)}.
   */
  public abstract void registerView(ViewDescriptor viewDescriptor);

  /**
   * Returns the current stats data, {@link View}, associated with the given {@link ViewDescriptor}.
   */
  public abstract View getView(ViewDescriptor viewDescriptor);

  /**
   * Returns the default {@link StatsContextFactory}.
   */
  abstract StatsContextFactory getStatsContextFactory();
}
