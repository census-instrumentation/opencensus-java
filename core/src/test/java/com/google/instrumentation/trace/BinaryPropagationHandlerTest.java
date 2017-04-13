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

package com.google.instrumentation.trace;

import static com.google.common.truth.Truth.assertThat;

import java.text.ParseException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link BinaryPropagationHandler}. */
@RunWith(JUnit4.class)
public class BinaryPropagationHandlerTest {
  private static final BinaryPropagationHandler binaryPropagationHandler =
      BinaryPropagationHandler.getNoopBinaryPropagationHandler();

  @Test(expected = NullPointerException.class)
  public void toBinaryValue_NullSpanContext() {
    binaryPropagationHandler.toBinaryValue(null);
  }

  @Test
  public void toBinaryValue_NotNullSpanContext() {
    assertThat(binaryPropagationHandler.toBinaryValue(SpanContext.INVALID)).isEqualTo(new byte[0]);
  }

  @Test(expected = NullPointerException.class)
  public void fromBinaryValue_NullInput() throws ParseException {
    binaryPropagationHandler.fromBinaryValue(null);
  }

  @Test
  public void fromBinaryValue_NotNullInput() throws ParseException {
    assertThat(binaryPropagationHandler.fromBinaryValue(new byte[0]))
        .isEqualTo(SpanContext.INVALID);
  }
}
