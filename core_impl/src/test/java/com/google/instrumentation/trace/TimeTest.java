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

import com.google.instrumentation.common.Timestamp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Time}. */
@RunWith(JUnit4.class)
public class TimeTest {
  @Test
  public void withTimestamp() {
    Timestamp timestamp = Timestamp.create(1234, 5678);
    Time time = Time.withTimestamp(timestamp);
    assertThat(time.hasTimestamp()).isTrue();
    assertThat(time.getTimestamp()).isEqualTo(timestamp);
    assertThat(time.getNanoTime()).isEqualTo(0);
  }

  @Test
  public void withNanoTime() {
    long nanoTime = 1234;
    Time time = Time.withNanoTime(nanoTime);
    assertThat(time.hasTimestamp()).isFalse();
    assertThat(time.getTimestamp()).isNull();
    assertThat(time.getNanoTime()).isEqualTo(nanoTime);
  }

  @Test
  public void traceId_ToString() {
    Timestamp timestamp = Timestamp.create(1234, 0);
    assertThat(Time.withTimestamp(timestamp).toString()).contains(timestamp.toString());
    long nanoTime = 1234;
    assertThat(Time.withNanoTime(nanoTime).toString()).contains("" + nanoTime);
  }

}
