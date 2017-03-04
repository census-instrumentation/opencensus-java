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

import com.google.common.testing.EqualsTester;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link SpanId}. */
@RunWith(JUnit4.class)
public class SpanIdTest {
  private static final SpanId first = new SpanId(123);

  @Test
  public void invalidSpanId() {
    assertThat(SpanId.INVALID.getSpanId()).isEqualTo(0);
  }

  @Test
  public void isValid() {
    assertThat(SpanId.INVALID.isValid()).isFalse();
    assertThat(first.isValid()).isTrue();
  }

  @Test
  public void getSpanId() {
    assertThat(first.getSpanId()).isEqualTo(123);
  }

  @Test
  public void traceId_EqualsAndHashCode() {
    EqualsTester tester = new EqualsTester();
    tester.addEqualityGroup(SpanId.INVALID, SpanId.INVALID);
    tester.addEqualityGroup(first, new SpanId(123));
    tester.testEquals();
  }

  @Test
  public void traceId_ToString() {
    assertThat(SpanId.INVALID.toString()).contains("0000000000000000");
    assertThat(new SpanId(0xFEDCBA9876543210L).toString())
        .contains("fedcba9876543210");
    assertThat(new SpanId(-1).toString()).contains("ffffffffffffffff");
  }
}
