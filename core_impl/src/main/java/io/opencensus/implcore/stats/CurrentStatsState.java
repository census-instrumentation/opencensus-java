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

package io.opencensus.implcore.stats;

import static com.google.common.base.Preconditions.checkNotNull;

import io.opencensus.stats.StatsCollectionState;
import io.opencensus.stats.StatsComponent;
import javax.annotation.concurrent.ThreadSafe;

/**
 * The current {@link StatsCollectionState} for a {@link StatsComponent}.
 *
 * <p>This class allows different stats classes to share the state in a thread-safe way.
 */
@ThreadSafe
public final class CurrentStatsState {
  private volatile StatsCollectionState currentState = StatsCollectionState.ENABLED;

  public StatsCollectionState get() {
    return currentState;
  }

  void set(StatsCollectionState state) {
    currentState = checkNotNull(state, "state");
  }
}
