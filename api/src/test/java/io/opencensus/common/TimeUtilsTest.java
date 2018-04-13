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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link TimeUtils}. */
@RunWith(JUnit4.class)
public class TimeUtilsTest {

  @Test
  public void toMillis() {
    assertThat(TimeUtils.toMillis(Duration.create(10, 0))).isEqualTo(10000L);
    assertThat(TimeUtils.toMillis(Duration.create(10, 1000))).isEqualTo(10000L);
    assertThat(TimeUtils.toMillis(Duration.create(0, (int) 1e6))).isEqualTo(1L);
    assertThat(TimeUtils.toMillis(Duration.create(0, 0))).isEqualTo(0L);
    assertThat(TimeUtils.toMillis(Duration.create(-10, 0))).isEqualTo(-10000L);
    assertThat(TimeUtils.toMillis(Duration.create(-10, -1000))).isEqualTo(-10000L);
    assertThat(TimeUtils.toMillis(Duration.create(0, -(int) 1e6))).isEqualTo(-1L);
  }
}
