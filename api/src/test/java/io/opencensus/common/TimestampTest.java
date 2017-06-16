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

/** Unit tests for {@link Timestamp}. */
@RunWith(JUnit4.class)
public class TimestampTest {
  @Test
  public void timestampCreate() {
    assertThat(Timestamp.create(24, 42).getSeconds()).isEqualTo(24);
    assertThat(Timestamp.create(24, 42).getNanos()).isEqualTo(42);
    assertThat(Timestamp.create(-24, 42).getSeconds()).isEqualTo(-24);
    assertThat(Timestamp.create(-24, 42).getNanos()).isEqualTo(42);
    assertThat(Timestamp.create(315576000000L, 999999999).getSeconds()).isEqualTo(315576000000L);
    assertThat(Timestamp.create(315576000000L, 999999999).getNanos()).isEqualTo(999999999);
    assertThat(Timestamp.create(-315576000000L, 999999999).getSeconds()).isEqualTo(-315576000000L);
    assertThat(Timestamp.create(-315576000000L, 999999999).getNanos()).isEqualTo(999999999);
  }

  @Test
  public void timestampCreate_InvalidInput() {
    assertThat(Timestamp.create(-315576000001L, 0)).isEqualTo(Timestamp.create(0, 0));
    assertThat(Timestamp.create(315576000001L, 0)).isEqualTo(Timestamp.create(0, 0));
    assertThat(Timestamp.create(1, 1000000000)).isEqualTo(Timestamp.create(0, 0));
    assertThat(Timestamp.create(1, -1)).isEqualTo(Timestamp.create(0, 0));
    assertThat(Timestamp.create(-1, 1000000000)).isEqualTo(Timestamp.create(0, 0));
    assertThat(Timestamp.create(-1, -1)).isEqualTo(Timestamp.create(0, 0));
  }

  @Test
  public void timestampFromMillis() {
    assertThat(Timestamp.fromMillis(0)).isEqualTo(Timestamp.create(0, 0));
    assertThat(Timestamp.fromMillis(987)).isEqualTo(Timestamp.create(0, 987000000));
    assertThat(Timestamp.fromMillis(3456)).isEqualTo(Timestamp.create(3, 456000000));
  }

  @Test
  public void timestampFromMillis_Negative() {
    assertThat(Timestamp.fromMillis(-1)).isEqualTo(Timestamp.create(-1, 999000000));
    assertThat(Timestamp.fromMillis(-999)).isEqualTo(Timestamp.create(-1, 1000000));
    assertThat(Timestamp.fromMillis(-3456)).isEqualTo(Timestamp.create(-4, 544000000));
  }

  @Test
  public void timestampAddNanos() {
    Timestamp timestamp = Timestamp.create(1234, 223);
    assertThat(timestamp.addNanos(0)).isEqualTo(timestamp);
    assertThat(timestamp.addNanos(999999777)).isEqualTo(Timestamp.create(1235, 0));
    assertThat(timestamp.addNanos(1300200500)).isEqualTo(Timestamp.create(1235, 300200723));
    assertThat(timestamp.addNanos(1999999777)).isEqualTo(Timestamp.create(1236, 0));
    assertThat(timestamp.addNanos(9876543789L)).isEqualTo(Timestamp.create(1243, 876544012));
    assertThat(timestamp.addNanos(Long.MAX_VALUE))
        .isEqualTo(Timestamp.create(1234L + 9223372036L, 223 + 854775807));
  }

  @Test
  public void timestampAddNanos_Negative() {
    Timestamp timestamp = Timestamp.create(1234, 223);
    assertThat(timestamp.addNanos(-223)).isEqualTo(Timestamp.create(1234, 0));
    assertThat(timestamp.addNanos(-1000000223)).isEqualTo(Timestamp.create(1233, 0));
    assertThat(timestamp.addNanos(-1300200500)).isEqualTo(Timestamp.create(1232, 699799723));
    assertThat(timestamp.addNanos(-4123456213L)).isEqualTo(Timestamp.create(1229, 876544010));
    assertThat(timestamp.addNanos(Long.MIN_VALUE))
        .isEqualTo(Timestamp.create(1234L - 9223372036L - 1, 223 + 145224192));
  }

  @Test
  public void testTimestampEqual() {
    // Positive tests.
    assertThat(Timestamp.create(0, 0)).isEqualTo(Timestamp.create(0, 0));
    assertThat(Timestamp.create(24, 42)).isEqualTo(Timestamp.create(24, 42));
    assertThat(Timestamp.create(-24, 42)).isEqualTo(Timestamp.create(-24, 42));
    // Negative tests.
    assertThat(Timestamp.create(25, 42)).isNotEqualTo(Timestamp.create(24, 42));
    assertThat(Timestamp.create(24, 43)).isNotEqualTo(Timestamp.create(24, 42));
    assertThat(Timestamp.create(-25, 42)).isNotEqualTo(Timestamp.create(-24, 42));
    assertThat(Timestamp.create(-24, 43)).isNotEqualTo(Timestamp.create(-24, 42));
  }
}
