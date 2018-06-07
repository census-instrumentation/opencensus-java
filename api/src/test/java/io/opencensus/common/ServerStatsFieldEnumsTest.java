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

import io.opencensus.common.ServerStatsFieldEnums.Id;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link ServerStatsFieldEnums}. */
@RunWith(JUnit4.class)
public class ServerStatsFieldEnumsTest {

  @Test
  public void enumIdValueOfTest() {
    assertThat(Id.valueOf(0)).isEqualTo(Id.SERVER_STATS_LB_LATENCY_ID);
    assertThat(Id.valueOf(1)).isEqualTo(Id.SERVER_STATS_SERVICE_LATENCY_ID);
    assertThat(Id.valueOf(2)).isEqualTo(Id.SERVER_STATS_TRACE_OPTION_ID);
  }

  @Test
  public void enumIdInvalidValueOfTest() {
    assertThat(Id.valueOf(-1)).isNull();
    assertThat(Id.valueOf(Id.values().length)).isNull();
    assertThat(Id.valueOf(Id.values().length + 1)).isNull();
  }
}
