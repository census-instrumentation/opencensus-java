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

/** Unit tests for {@link TimestampConverter}. */
@RunWith(JUnit4.class)
public class TimestampConverterTest {
  @Test
  public void convertNanoTime() {
    Timestamp timestamp = Timestamp.create(1234, 5678);
    TimestampConverter timeConverter = new TimestampConverter(timestamp, 2345);
    assertThat(timeConverter.convertNanoTime(1234)).isEqualTo(timestamp.addNanos(-1111));
    assertThat(timeConverter.convertNanoTime(3456)).isEqualTo(timestamp.addNanos(1111));
  }
}
