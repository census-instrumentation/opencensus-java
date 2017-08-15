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

package io.opencensus.impl.internal;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

import io.opencensus.common.Clock;
import io.opencensus.common.Timestamp;
import io.opencensus.impl.internal.TimestampConverter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link TimestampConverter}. */
@RunWith(JUnit4.class)
public class TimestampConverterTest {
  private final Timestamp timestamp = Timestamp.create(1234, 5678);
  @Mock private Clock mockClock;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void convertNanoTime() {
    when(mockClock.now()).thenReturn(timestamp);
    when(mockClock.nowNanos()).thenReturn(1234L);
    TimestampConverter timeConverter = TimestampConverter.now(mockClock);
    assertThat(timeConverter.convertNanoTime(6234)).isEqualTo(Timestamp.create(1234, 10678));
    assertThat(timeConverter.convertNanoTime(1000)).isEqualTo(Timestamp.create(1234, 5444));
    assertThat(timeConverter.convertNanoTime(999995556)).isEqualTo(Timestamp.create(1235, 0));
  }
}
