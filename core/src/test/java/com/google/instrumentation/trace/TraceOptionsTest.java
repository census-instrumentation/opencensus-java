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

/** Unit tests for {@link TraceOptions}. */
@RunWith(JUnit4.class)
public class TraceOptionsTest {
  private static final byte[] firstBytes = {(byte) 0xff};
  private static final byte[] secondBytes = {1};
  private static final byte[] thirdBytes = {6};

  @Test
  public void getOptions() {
    assertThat(TraceOptions.DEFAULT.getOptions()).isEqualTo(0);
    assertThat(TraceOptions.builder().setIsSampled().build().getOptions()).isEqualTo(1);
    assertThat(TraceOptions.fromBytes(firstBytes).getOptions()).isEqualTo(-1);
    assertThat(TraceOptions.fromBytes(secondBytes).getOptions()).isEqualTo(1);
    assertThat(TraceOptions.fromBytes(thirdBytes).getOptions()).isEqualTo(6);
  }

  @Test
  public void isSampled() {
    assertThat(TraceOptions.DEFAULT.isSampled()).isFalse();
    assertThat(TraceOptions.builder().setIsSampled().build().isSampled()).isTrue();
  }

  @Test
  public void toFromBytes() {
    assertThat(TraceOptions.fromBytes(firstBytes).getBytes()).isEqualTo(firstBytes);
    assertThat(TraceOptions.fromBytes(secondBytes).getBytes()).isEqualTo(secondBytes);
    assertThat(TraceOptions.fromBytes(thirdBytes).getBytes()).isEqualTo(thirdBytes);
  }

  @Test
  public void builder_FromOptions() {
    assertThat(
            TraceOptions.builder(TraceOptions.fromBytes(thirdBytes))
                .setIsSampled()
                .build()
                .getOptions())
        .isEqualTo(6 | 1);
  }

  @Test
  public void traceOptions_EqualsAndHashCode() {
    EqualsTester tester = new EqualsTester();
    tester.addEqualityGroup(TraceOptions.DEFAULT);
    tester.addEqualityGroup(
        TraceOptions.fromBytes(secondBytes), TraceOptions.builder().setIsSampled().build());
    tester.addEqualityGroup(TraceOptions.fromBytes(firstBytes));
    tester.testEquals();
  }

  @Test
  public void traceOptions_ToString() {
    assertThat(TraceOptions.DEFAULT.toString()).contains("sampled=false");
    assertThat(TraceOptions.builder().setIsSampled().build().toString()).contains("sampled=true");
  }
}
