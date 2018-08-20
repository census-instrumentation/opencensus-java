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

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.stats.StatsCollectionState;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link CurrentStatsState}. */
@RunWith(JUnit4.class)
public final class CurrentStatsStateTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void defaultState() {
    assertThat(new CurrentStatsState().get()).isEqualTo(StatsCollectionState.ENABLED);
  }

  @Test
  public void setState() {
    CurrentStatsState state = new CurrentStatsState();
    assertThat(state.set(StatsCollectionState.DISABLED)).isTrue();
    assertThat(state.get()).isEqualTo(StatsCollectionState.DISABLED);
    assertThat(state.set(StatsCollectionState.ENABLED)).isTrue();
    assertThat(state.get()).isEqualTo(StatsCollectionState.ENABLED);
    assertThat(state.set(StatsCollectionState.ENABLED)).isFalse();
  }

  @Test
  public void preventNull() {
    CurrentStatsState state = new CurrentStatsState();
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("newState");
    state.set(null);
  }
}
