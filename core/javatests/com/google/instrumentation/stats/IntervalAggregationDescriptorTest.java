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

package com.google.instrumentation.stats;

import static com.google.common.truth.Truth.assertThat;

import com.google.instrumentation.common.Duration;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link AggregationDescriptor}
 */
@RunWith(JUnit4.class)
public final class IntervalAggregationDescriptorTest {
  @Test
  public void testIntervalAggregationDescriptor() {
    Duration[] intervals =
        new Duration[] { Duration.fromMillis(1), Duration.fromMillis(22), Duration.fromMillis(333)};
    IntervalAggregationDescriptor iDescriptor =
         IntervalAggregationDescriptor.create(Arrays.asList(intervals));
    assertThat(iDescriptor.getIntervalSizes()).isNotNull();
    assertThat(iDescriptor.getIntervalSizes().size()).isEqualTo(intervals.length);
    for (int i = 0; i < intervals.length; i++) {
      assertThat(iDescriptor.getIntervalSizes().get(i)).isEqualTo(intervals[i]);
    }
  }
}
