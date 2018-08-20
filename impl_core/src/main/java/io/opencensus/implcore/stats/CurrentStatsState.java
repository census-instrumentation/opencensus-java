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
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.concurrent.ThreadSafe;

/**
 * The current {@link StatsCollectionState} for a {@link StatsComponent}.
 *
 * <p>This class allows different stats classes to share the state in a thread-safe way.
 */
@ThreadSafe
final class CurrentStatsState {
  private static final StatsCollectionState DEFAULT_STATE = StatsCollectionState.ENABLED;
  private final AtomicReference<StatsCollectionState> currentState =
      new AtomicReference<StatsCollectionState>(DEFAULT_STATE);

  StatsCollectionState get() {
    return currentState.get();
  }

  // Sets current state to the given state. Returns true if the current state is changed, false
  // otherwise.
  boolean set(StatsCollectionState newState) {
    return newState != currentState.getAndSet(checkNotNull(newState, "newState"));
  }
}
