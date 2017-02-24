/*
 * Copyright 2016, Google Inc.
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

/** Unit tests for {@link TraceId}. */
@RunWith(JUnit4.class)
public class TraceIdTest {
  private static final TraceId first = new TraceId(123, 456);
  private static final TraceId second = new TraceId(0, 789);
  private static final TraceId third = new TraceId(987, 0);

  @Test
  public void invalidTraceId() {
    assertThat(TraceId.getInvalid().getTraceIdLo()).isEqualTo(0);
    assertThat(TraceId.getInvalid().getTraceIdHi()).isEqualTo(0);
  }

  @Test
  public void isValid() {
    assertThat(TraceId.getInvalid().isValid()).isFalse();
    assertThat(second.isValid()).isTrue();
    assertThat(third.isValid()).isTrue();
    assertThat(first.isValid()).isTrue();
  }

  @Test
  public void getTraceIdLo() {
    assertThat(first.getTraceIdLo()).isEqualTo(456);
    assertThat(second.getTraceIdLo()).isEqualTo(789);
    assertThat(third.getTraceIdLo()).isEqualTo(0);
  }

  @Test
  public void getTraceIdHi() {
    assertThat(first.getTraceIdHi()).isEqualTo(123);
    assertThat(second.getTraceIdHi()).isEqualTo(0);
    assertThat(third.getTraceIdHi()).isEqualTo(987);
  }

  @Test
  public void traceId_EqualsAndHashCode() {
    EqualsTester tester = new EqualsTester();
    tester.addEqualityGroup(TraceId.getInvalid(), TraceId.getInvalid());
    tester.addEqualityGroup(first, new TraceId(123, 456));
    tester.addEqualityGroup(second, new TraceId(0, 789));
    tester.addEqualityGroup(third, new TraceId(987, 0));
    tester.testEquals();
  }

  @Test
  public void traceId_ToString() {
    assertThat(TraceId.getInvalid().toString()).contains("00000000000000000000000000000000");
    assertThat(new TraceId(0, 0xFEDCBA9876543210L).toString())
        .contains("0000000000000000fedcba9876543210");
    assertThat(new TraceId(0x0123456789ABCDEFL, 0).toString())
        .contains("0123456789abcdef0000000000000000");
    assertThat(new TraceId(0x0123456789ABCDEFL, 0xFEDCBA9876543210L).toString())
        .contains("0123456789abcdeffedcba9876543210");
    assertThat(new TraceId(-1, -2).toString()).contains("fffffffffffffffffffffffffffffffe");
  }
}
