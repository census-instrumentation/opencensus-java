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

/** Tests for {@link TimeUtils}. */
@RunWith(JUnit4.class)
public final class TimeUtilsTest {

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void compareLongs() {
    assertThat(TimeUtils.compareLongs(-1L, 1L)).isLessThan(0);
    assertThat(TimeUtils.compareLongs(10L, 10L)).isEqualTo(0);
    assertThat(TimeUtils.compareLongs(1L, 0L)).isGreaterThan(0);
  }

  @Test
  public void checkedAdd_TooLow() {
    thrown.expect(ArithmeticException.class);
    thrown.expectMessage("Long sum overflow: x=-9223372036854775807, y=-2");
    TimeUtils.checkedAdd(Long.MIN_VALUE + 1, -2);
  }

  @Test
  public void checkedAdd_TooHigh() {
    thrown.expect(ArithmeticException.class);
    thrown.expectMessage("Long sum overflow: x=9223372036854775806, y=2");
    TimeUtils.checkedAdd(Long.MAX_VALUE - 1, 2);
  }

  @Test
  public void checkedAdd_Valid() {
    assertThat(TimeUtils.checkedAdd(1, 2)).isEqualTo(3);
    assertThat(TimeUtils.checkedAdd(Integer.MAX_VALUE, Integer.MAX_VALUE))
        .isEqualTo(2L * Integer.MAX_VALUE);
  }
}
