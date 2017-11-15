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
import static com.google.common.base.Preconditions.checkState;

import io.opencensus.stats.StatsCollectionState;
import io.opencensus.stats.StatsComponent;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * The current {@link StatsCollectionState} for a {@link StatsComponent}.
 *
 * <p>This class allows different stats classes to share the state in a thread-safe way.
 */
// TODO(sebright): Remove the locking from this class, since it is used as global state.
@ThreadSafe
public final class CurrentStatsState {

  @GuardedBy("this")
  private StatsCollectionState currentState = StatsCollectionState.ENABLED;

  @GuardedBy("this")
  private boolean isRead;

  public synchronized StatsCollectionState get() {
    isRead = true;
    return getInternal();
  }

  synchronized StatsCollectionState getInternal() {
    return currentState;
  }

  // Sets current state to the given state. Returns true if the current state is changed, false
  // otherwise.
  synchronized boolean set(StatsCollectionState state) {
    checkState(!isRead, "State was already read, cannot set state.");
    if (state == currentState) {
      return false;
    } else {
      currentState = checkNotNull(state, "state");
      return true;
    }
  }
}
