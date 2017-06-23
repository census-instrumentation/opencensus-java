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

import io.opencensus.internal.Provider;
import io.opencensus.tags.TagContext;
import javax.annotation.Nullable;

/**
 * {@link Stats}.
 */
public final class StatsExplicit {
  private static final StatsManager statsManager = Provider.newInstance(
      "io.opencensus.stats.StatsManagerImpl", null);

  /**
   * Records the given measurements against the given {@link StatsContext}.
   *
   * @param ctxt the {@link StatsContext} to record the measurements against
   * @param measurements the measurements to record
   * @return ctxt
   */
  public static TagContext record(TagContext ctxt, MeasureMap mMap) {
    if (statsManager != null) {
      statsManager.record(ctxt, mMap);
    } else {
      System.err.println("record()");
    }
    return ctxt;
  }

  // VisibleForTesting
  StatsExplicit() {
    throw new AssertionError();
  }
}
