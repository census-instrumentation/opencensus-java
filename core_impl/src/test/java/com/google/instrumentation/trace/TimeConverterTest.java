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

/** Unit tests for {@link TimeConverter}. */
@RunWith(JUnit4.class)
public class TimeConverterTest {
  @Test
  public void convertTime_WithTimestamp() {
    TimeConverter timeConverter = TimeConverter.now();
    Timestamp timestamp = Timestamp.create(1234, 0);
    Time time = Time.withTimestamp(timestamp);
    assertThat(timeConverter.convert(time)).isEqualTo(timestamp);
  }

  @Test
  public void convertTime_WithNanoTime() {
    Timestamp timestamp = Timestamp.create(1234, 0);
    TimeConverter timeConverter = new TimeConverter(timestamp, 1000);
    assertThat(timeConverter.convert(Time.withNanoTime(1234))).isEqualTo(timestamp.addNanos(-234));
    assertThat(timeConverter.convert(Time.withNanoTime(678))).isEqualTo(timestamp.addNanos(322));
  }
}
