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

package io.opencensus.contrib.http;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link HttpStatsCtx}. */
@RunWith(JUnit4.class)
public class HttpStatsCtxTest {
  private final HttpStatsCtx statsCtx = new HttpStatsCtx();

  @Test
  public void testInitValues() {
    assertThat(statsCtx.requestStartTime).isEqualTo(HttpStatsCtx.INVALID_STARTTIME);
    assertThat(statsCtx.requestMessageSize.longValue()).isEqualTo(0L);
    assertThat(statsCtx.responseMessageSize.longValue()).isEqualTo(0L);
  }

  @Test
  public void testAddToRequestSize() {
    assertThat(statsCtx.addAndGetRequestMessageSize(10L)).isEqualTo(10L);
    assertThat(statsCtx.addAndGetRequestMessageSize(40000000000L)).isEqualTo(40000000010L);
    assertThat(statsCtx.getRequestMessageSize()).isEqualTo(40000000010L);
  }

  @Test
  public void testAddToResponseSize() {
    assertThat(statsCtx.addAndGetResponseMessageSize(10L)).isEqualTo(10L);
    assertThat(statsCtx.addAndGetResponseMessageSize(40000000000L)).isEqualTo(40000000010L);
    assertThat(statsCtx.getResponseMessageSize()).isEqualTo(40000000010L);
  }
}
