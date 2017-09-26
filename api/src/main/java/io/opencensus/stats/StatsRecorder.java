/*
 * Copyright 2017, OpenCensus Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.stats;

import io.opencensus.tags.TagContext;
import io.opencensus.tags.unsafe.ContextUtils;

/** Provides methods to record stats against tags. */
public abstract class StatsRecorder {
  // TODO(sebright): Should we provide convenience methods for only recording one measure?

  /**
   * Returns an object for recording multiple measures with the tags in the current context.
   *
   * @return an object for recording multiple measures with the tags in the current context.
   */
  public final StatsBuilder builder() {
    // Use the context key directly, to avoid depending on the tags implementation.
    return builder(ContextUtils.TAG_CONTEXT_KEY.get());
  }

  /**
   * Returns an object for recording multiple measures with a set of tags.
   *
   * @param tags the tags associated with the measurements.
   * @return an object for recording multiple measures with a set of tags.
   */
  public abstract StatsBuilder builder(TagContext tags);
}
