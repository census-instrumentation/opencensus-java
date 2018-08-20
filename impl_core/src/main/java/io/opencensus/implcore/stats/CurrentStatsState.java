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

  private enum StatsCollectionStateInternal {
    // Enabled and not read.
    ENABLED_NOT_READ(StatsCollectionState.ENABLED, false),

    // Enabled and read.
    ENABLED_READ(StatsCollectionState.ENABLED, true),

    // Disable and not read.
    DISABLED_NOT_READ(StatsCollectionState.DISABLED, false),

    // Disable and read.
    DISABLED_READ(StatsCollectionState.DISABLED, true);

    private final StatsCollectionState state;
    private final boolean isRead;

    StatsCollectionStateInternal(StatsCollectionState state, boolean isRead) {
      this.state = state;
      this.isRead = isRead;
    }
  }

  private final AtomicReference<StatsCollectionStateInternal> currentInternalState =
      new AtomicReference<StatsCollectionStateInternal>(
          DEFAULT_STATE == StatsCollectionState.ENABLED
              ? StatsCollectionStateInternal.ENABLED_NOT_READ
              : StatsCollectionStateInternal.DISABLED_NOT_READ);

  StatsCollectionState get() {
    StatsCollectionStateInternal internalState = currentInternalState.get();
    while (!internalState.isRead) {
      // Slow path, the state is first time read. Change the state only if no other changes
      // happened between the moment initialState is read and this moment. This ensures that this
      // method only changes the isRead part of the internal state.
      currentInternalState.compareAndSet(
          internalState,
          internalState.state == StatsCollectionState.ENABLED
              ? StatsCollectionStateInternal.ENABLED_READ
              : StatsCollectionStateInternal.DISABLED_READ);
      internalState = currentInternalState.get();
    }
    return internalState.state;
  }

  StatsCollectionState getInternal() {
    return currentInternalState.get().state;
  }

  // Sets current state to the given state. Returns true if the current state is changed, false
  // otherwise.
  boolean set(StatsCollectionState state) {
    while (true) {
      StatsCollectionStateInternal internalState = currentInternalState.get();
      checkState(!internalState.isRead, "State was already read, cannot set state.");
      if (checkNotNull(state, "state") == internalState.state) {
        return false;
      } else {
        if (!currentInternalState.compareAndSet(
            internalState,
            state == StatsCollectionState.ENABLED
                ? StatsCollectionStateInternal.ENABLED_NOT_READ
                : StatsCollectionStateInternal.DISABLED_NOT_READ)) {
          // The state was changed between the moment the internalState was read and this point.
          // Some conditions may be not correct, reset at the beginning and recheck all conditions.
          continue;
        }
        return true;
      }
    }
  }
}
