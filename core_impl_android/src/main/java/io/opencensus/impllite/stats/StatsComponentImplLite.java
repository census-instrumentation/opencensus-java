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

package io.opencensus.impllite.stats;

import io.opencensus.implcore.common.MillisClock;
import io.opencensus.implcore.internal.SimpleEventQueue;
import io.opencensus.implcore.stats.StatsComponentImplBase;
import io.opencensus.stats.StatsComponent;

/**
 * Android-compatible implementation of {@link StatsComponent}.
 */
public final class StatsComponentImplLite extends StatsComponentImplBase {

  public StatsComponentImplLite() {
    // TODO(sebright): Use a more efficient queue implementation.
    super(new SimpleEventQueue(), MillisClock.getInstance());
  }
}
