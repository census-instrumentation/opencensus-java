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

package io.opencensus.common;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link ServerStats}. */
@RunWith(JUnit4.class)
public class ServerStatsTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void timestampCreate() {
    ServerStats serverStats = null;

    serverStats = ServerStats.create(31, 22, (byte) 0);
    assertThat(serverStats.lbLatencyNs()).isEqualTo(31);
    assertThat(serverStats.serviceLatencyNs()).isEqualTo(22);
    assertThat(serverStats.traceOption()).isEqualTo((byte) 0);

    serverStats = ServerStats.create(1000011L, 900022L, (byte) 1);
    assertThat(serverStats.lbLatencyNs()).isEqualTo(1000011L);
    assertThat(serverStats.serviceLatencyNs()).isEqualTo(900022L);
    assertThat(serverStats.traceOption()).isEqualTo((byte) 1);

    serverStats = ServerStats.create(0, 22, (byte) 0);
    assertThat(serverStats.lbLatencyNs()).isEqualTo(0);
    assertThat(serverStats.serviceLatencyNs()).isEqualTo(22);
    assertThat(serverStats.traceOption()).isEqualTo((byte) 0);

    serverStats = ServerStats.create(1010, 0, (byte) 0);
    assertThat(serverStats.lbLatencyNs()).isEqualTo(1010);
    assertThat(serverStats.serviceLatencyNs()).isEqualTo(0);
    assertThat(serverStats.traceOption()).isEqualTo((byte) 0);
  }

  @Test
  public void create_LbLatencyNegative() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("'lbLatencyNs' is less than zero");
    ServerStats.create(-1L, 100, (byte) 0);
  }

  @Test
  public void create_ServerLatencyNegative() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("'serviceLatencyNs' is less than zero");
    ServerStats.create(100L, -1L, (byte) 0);
  }

  @Test
  public void create_LbLatencyAndServerLatencyNegative() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("'lbLatencyNs' is less than zero");
    ServerStats.create(-100L, -1L, (byte) 0);
  }
}
