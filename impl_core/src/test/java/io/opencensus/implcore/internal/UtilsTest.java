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

package io.opencensus.implcore.internal;

import java.util.Arrays;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Utils}. */
@RunWith(JUnit4.class)
public class UtilsTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void checkListElementNull() {
    List<Double> list = Arrays.asList(0.0, 1.0, 2.0, null);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("null");
    Utils.checkListElementNotNull(list, null);
  }

  @Test
  public void checkListElementNull_WithMessage() {
    List<Double> list = Arrays.asList(0.0, 1.0, 2.0, null);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("list should not be null.");
    Utils.checkListElementNotNull(list, "list should not be null.");
  }
}
