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
import io.opencensus.trace.propagation.TextFormat.Getter;
import io.opencensus.trace.propagation.TextFormat.Setter;
import javax.annotation.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link TextFormat}. */
@RunWith(JUnit4.class)
public class TextFormatTest {
  private static final TextFormat textFormat = TextFormat.getNoopTextFormat();

  @Test(expected = NullPointerException.class)
  public void inject_NullSpanContext() {
    textFormat.inject(
        null,
        new Object(),
        new Setter<Object>() {
          @Override
          public void put(Object carrier, String key, String value) {}
        });
  }

  @Test
  public void inject_NotNullSpanContext_DoesNotFail() {
    textFormat.inject(
        SpanContext.INVALID,
        new Object(),
        new Setter<Object>() {
          @Override
          public void put(Object carrier, String key, String value) {}
        });
  }

  @Test(expected = NullPointerException.class)
  public void fromHeaders_NullGetter() throws SpanContextParseException {
    textFormat.extract(new Object(), null);
  }

  @Test
  public void fromHeaders_NotNullGetter() throws SpanContextParseException {
    assertThat(
            textFormat.extract(
                new Object(),
                new Getter<Object>() {
                  @Nullable
                  @Override
                  public String get(Object carrier, String key) {
                    return null;
                  }
                }))
        .isSameInstanceAs(SpanContext.INVALID);
  }
}
