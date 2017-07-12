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

package io.opencensus.stats;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Range}. */
@RunWith(JUnit4.class)
public class RangeTest {

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private static final double TOLERANCE = 1e-6;

  @Test
  public void testCreateAndGet() {
    Range range = Range.create(1.0, 9.0);
    assertThat(range.getMax()).isWithin(TOLERANCE).of(9.0);
    assertThat(range.getMin()).isWithin(TOLERANCE).of(1.0);
  }

  @Test
  public void testEquals() {
    assertThat(Range.create(-1.0, 10.0)).isEqualTo(Range.create(-1.0, 10.0));
  }

  @Test
  public void testMinIsGreaterThanMax() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("max should be greater or equal to min.");
    Range.create(10.0, 0.0);
  }
}
