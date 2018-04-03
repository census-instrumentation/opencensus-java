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

package io.opencensus.internal;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link Utils}. */
@RunWith(JUnit4.class)
public final class UtilsTest {
  private static final String TEST_MESSAGE = "test message";

  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void checkArgument() {
    Utils.checkArgument(true, TEST_MESSAGE);
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage(TEST_MESSAGE);
    Utils.checkArgument(false, TEST_MESSAGE);
  }

  @Test
  public void checkState() {
    Utils.checkNotNull(true, TEST_MESSAGE);
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage(TEST_MESSAGE);
    Utils.checkState(false, TEST_MESSAGE);
  }

  @Test
  public void checkNotNull() {
    Utils.checkNotNull(new Object(), TEST_MESSAGE);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage(TEST_MESSAGE);
    Utils.checkNotNull(null, TEST_MESSAGE);
  }

  @Test
  public void checkIndex_Valid() {
    Utils.checkIndex(1, 2);
  }

  @Test
  public void checkIndex_NegativeSize() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Negative size: -1");
    Utils.checkIndex(0, -1);
  }

  @Test
  public void checkIndex_NegativeIndex() {
    thrown.expect(IndexOutOfBoundsException.class);
    thrown.expectMessage("Index out of bounds: size=10, index=-2");
    Utils.checkIndex(-2, 10);
  }

  @Test
  public void checkIndex_IndexEqualToSize() {
    thrown.expect(IndexOutOfBoundsException.class);
    thrown.expectMessage("Index out of bounds: size=5, index=5");
    Utils.checkIndex(5, 5);
  }

  @Test
  public void checkIndex_IndexGreaterThanSize() {
    thrown.expect(IndexOutOfBoundsException.class);
    thrown.expectMessage("Index out of bounds: size=10, index=11");
    Utils.checkIndex(11, 10);
  }
}
