/*
 * Copyright 2017, OpenCensus Authors
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

package io.opencensus.trace.propagation;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.trace.SpanContext;
import io.opencensus.trace.propagation.B3Format.HeaderName;
import java.text.ParseException;
import java.util.Collections;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link B3Format}. */
@RunWith(JUnit4.class)
public class B3FormatTest {
  private static final B3Format b3Format = B3Format.getNoopB3Format();

  @Test(expected = NullPointerException.class)
  public void toHeaders_NullSpanContext() {
    b3Format.toHeaders(null);
  }

  @Test
  public void toHeaders_NotNullSpanContext() {
    assertThat(b3Format.toHeaders(SpanContext.INVALID)).isEqualTo(Collections.emptyMap());
  }

  @Test(expected = NullPointerException.class)
  public void fromHeaders_NullInput() throws ParseException {
    b3Format.fromHeaders(null);
  }

  @Test
  public void fromHeaders_NotNullInput() throws ParseException {
    assertThat(b3Format.fromHeaders(Collections.<HeaderName, String>emptyMap()))
        .isEqualTo(SpanContext.INVALID);
  }
}
