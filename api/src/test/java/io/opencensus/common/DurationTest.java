/*
 * Copyright 2017, Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
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

/** Unit tests for {@link Duration}. */
@RunWith(JUnit4.class)
public class DurationTest {
  @Test
  public void testDurationCreate() {
    assertThat(Duration.create(24, 42).getSeconds()).isEqualTo(24);
    assertThat(Duration.create(24, 42).getNanos()).isEqualTo(42);
    assertThat(Duration.create(-24, -42).getSeconds()).isEqualTo(-24);
    assertThat(Duration.create(-24, -42).getNanos()).isEqualTo(-42);
    assertThat(Duration.create(315576000000L, 999999999).getSeconds()).isEqualTo(315576000000L);
    assertThat(Duration.create(315576000000L, 999999999).getNanos()).isEqualTo(999999999);
    assertThat(Duration.create(-315576000000L, -999999999).getSeconds()).isEqualTo(-315576000000L);
    assertThat(Duration.create(-315576000000L, -999999999).getNanos()).isEqualTo(-999999999);
  }

  @Test
  public void testDurationCreateInvalidInput() {
    assertThat(Duration.create(-315576000001L, 0)).isEqualTo(Duration.create(0, 0));
    assertThat(Duration.create(315576000001L, 0)).isEqualTo(Duration.create(0, 0));
    assertThat(Duration.create(0, 1000000000)).isEqualTo(Duration.create(0, 0));
    assertThat(Duration.create(0, -1000000000)).isEqualTo(Duration.create(0, 0));
    assertThat(Duration.create(-1, 1)).isEqualTo(Duration.create(0, 0));
    assertThat(Duration.create(1, -1)).isEqualTo(Duration.create(0, 0));
  }

  @Test
  public void testDurationFromMillis() {
    assertThat(Duration.fromMillis(0)).isEqualTo(Duration.create(0, 0));
    assertThat(Duration.fromMillis(987)).isEqualTo(Duration.create(0, 987000000));
    assertThat(Duration.fromMillis(3456)).isEqualTo(Duration.create(3, 456000000));
  }

  @Test
  public void testDurationFromMillisNegative() {
    assertThat(Duration.fromMillis(-1)).isEqualTo(Duration.create(0, -1000000));
    assertThat(Duration.fromMillis(-999)).isEqualTo(Duration.create(0, -999000000));
    assertThat(Duration.fromMillis(-1000)).isEqualTo(Duration.create(-1, 0));
    assertThat(Duration.fromMillis(-3456)).isEqualTo(Duration.create(-3, -456000000));
  }

  @Test
  public void testDurationEqual() {
    // Positive tests.
    assertThat(Duration.create(0, 0)).isEqualTo(Duration.create(0, 0));
    assertThat(Duration.create(24, 42)).isEqualTo(Duration.create(24, 42));
    assertThat(Duration.create(-24, -42)).isEqualTo(Duration.create(-24, -42));
    // Negative tests.
    assertThat(Duration.create(25, 42)).isNotEqualTo(Duration.create(24, 42));
    assertThat(Duration.create(24, 43)).isNotEqualTo(Duration.create(24, 42));
    assertThat(Duration.create(-25, -42)).isNotEqualTo(Duration.create(-24, -42));
    assertThat(Duration.create(-24, -43)).isNotEqualTo(Duration.create(-24, -42));
  }
}
