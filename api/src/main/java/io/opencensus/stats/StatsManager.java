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

import io.opencensus.tags.TagContext;

/**
 * Provides facillities to register {@link ViewDescriptor}s for collecting stats and retrieving
 * stats data as a {@link View}.
 */
public abstract class StatsManager {
  /**
   * Pull model for stats. Registers a {@link ViewDescriptor} that will collect data to be accessed
   * via {@link #getView(ViewDescriptor)}.
   */
  //public abstract void registerView(ViewDescriptor viewDescriptor);

  /**
   * Returns the current stats data, {@link View}, associated with the given {@link ViewDescriptor}.
   */
  //public abstract View getView(ViewDescriptor viewDescriptor);

  /**
   * Records the given measurements against the given {@link TagContext}.
   *
   * @param ctxt the {@link TagContext} to record against
   * @param measurements the measurements to record
   * @return ctxt
   */
  public abstract TagContext record(TagContext ctxt, MeasureMap measurements);
}
