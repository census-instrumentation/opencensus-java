/*
 * Copyright 2018, OpenCensus Authors
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

package io.opencensus.implcore.internal;

import static com.google.common.base.Preconditions.checkState;

import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.concurrent.ThreadSafe;

/** The current state base implementation for stats and tags. */
@ThreadSafe
public final class CurrentState {

  /** Current state for stats or tags. */
  public enum State {
    /** State that fully enables stats collection or tag propagation. */
    ENABLED,

    /** State that disables stats collection or tag propagation. */
    DISABLED
  }

  private enum InternalState {
    // Enabled and not read.
    ENABLED_NOT_READ(State.ENABLED, false),

    // Enabled and read.
    ENABLED_READ(State.ENABLED, true),

    // Disable and not read.
    DISABLED_NOT_READ(State.DISABLED, false),

    // Disable and read.
    DISABLED_READ(State.DISABLED, true);

    private final State state;
    private final boolean isRead;

    InternalState(State state, boolean isRead) {
      this.state = state;
      this.isRead = isRead;
    }
  }

  private final AtomicReference<InternalState> currentInternalState;

  /**
   * Constructs a new {@code CurrentState}.
   *
   * @param defaultState the default initial state.
   */
  public CurrentState(State defaultState) {
    this.currentInternalState =
        new AtomicReference<InternalState>(
            defaultState == State.ENABLED
                ? InternalState.ENABLED_NOT_READ
                : InternalState.DISABLED_NOT_READ);
  }

  /**
   * Returns the current state and updates the status as being read.
   *
   * @return the current state and updates the status as being read.
   */
  public State get() {
    InternalState internalState = currentInternalState.get();
    while (!internalState.isRead) {
      // Slow path, the state is first time read. Change the state only if no other changes
      // happened between the moment initialState is read and this moment. This ensures that this
      // method only changes the isRead part of the internal state.
      currentInternalState.compareAndSet(
          internalState,
          internalState.state == State.ENABLED
              ? InternalState.ENABLED_READ
              : InternalState.DISABLED_READ);
      internalState = currentInternalState.get();
    }
    return internalState.state;
  }

  /**
   * Returns the current state without updating the status as being read.
   *
   * @return the current state without updating the status as being read.
   */
  public State getInternal() {
    return currentInternalState.get().state;
  }

  /**
   * Sets current state to the given state. Returns true if the current state is changed, false
   * otherwise.
   *
   * @param state the state to be set.
   * @return true if the current state is changed, false otherwise.
   */
  public boolean set(State state) {
    while (true) {
      InternalState internalState = currentInternalState.get();
      checkState(!internalState.isRead, "State was already read, cannot set state.");
      if (state == internalState.state) {
        return false;
      } else {
        if (!currentInternalState.compareAndSet(
            internalState,
            state == State.ENABLED
                ? InternalState.ENABLED_NOT_READ
                : InternalState.DISABLED_NOT_READ)) {
          // The state was changed between the moment the internalState was read and this point.
          // Some conditions may be not correct, reset at the beginning and recheck all conditions.
          continue;
        }
        return true;
      }
    }
  }
}
