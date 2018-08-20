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

import io.opencensus.implcore.internal.SimpleEventQueue;
import io.opencensus.stats.StatsCollectionState;
import io.opencensus.stats.StatsComponent;
import io.opencensus.testing.common.TestClock;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link StatsComponentImplBase}. */
@RunWith(JUnit4.class)
public final class StatsComponentImplBaseTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  private final StatsComponent statsComponent =
      new StatsComponentImplBase(new SimpleEventQueue(), TestClock.create());

  @Test
  public void defaultState() {
    assertThat(statsComponent.getState()).isEqualTo(StatsCollectionState.ENABLED);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void setState_Disabled() {
    statsComponent.setState(StatsCollectionState.DISABLED);
    assertThat(statsComponent.getState()).isEqualTo(StatsCollectionState.DISABLED);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void setState_ReEnabled() {
    statsComponent.setState(StatsCollectionState.DISABLED);
    assertThat(statsComponent.getState()).isEqualTo(StatsCollectionState.DISABLED);
    statsComponent.setState(StatsCollectionState.ENABLED);
    assertThat(statsComponent.getState()).isEqualTo(StatsCollectionState.ENABLED);
  }

  @Test
  @SuppressWarnings("deprecation")
  public void setState_DisallowsNull() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("newState");
    statsComponent.setState(null);
  }
}
