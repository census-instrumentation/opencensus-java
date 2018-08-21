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

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.implcore.internal.CurrentState.State;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link CurrentState}. */
@RunWith(JUnit4.class)
public final class CurrentStateTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void defaultState() {
    assertThat(new CurrentState(State.ENABLED).get()).isEqualTo(State.ENABLED);
  }

  @Test
  public void setState() {
    CurrentState currentState = new CurrentState(State.ENABLED);
    assertThat(currentState.set(State.DISABLED)).isTrue();
    assertThat(currentState.getInternal()).isEqualTo(State.DISABLED);
    assertThat(currentState.set(State.ENABLED)).isTrue();
    assertThat(currentState.getInternal()).isEqualTo(State.ENABLED);
    assertThat(currentState.set(State.ENABLED)).isFalse();
  }

  @Test
  public void preventSettingStateAfterReadingState() {
    CurrentState currentState = new CurrentState(State.ENABLED);
    currentState.get();
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("State was already read, cannot set state.");
    currentState.set(State.DISABLED);
  }
}
