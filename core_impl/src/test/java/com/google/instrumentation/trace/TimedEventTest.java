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
import com.google.instrumentation.common.Timestamp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link TimedEvent}. */
@RunWith(JUnit4.class)
public class TimedEventTest {
  private static final Timestamp timestamp = Timestamp.create(123, 456);
  private static final Annotation annotation = Annotation.fromDescription("MyTextAnnotation");
  private static final NetworkEvent networkEvent =
      NetworkEvent.builder(NetworkEvent.Type.RECV, 1).build();

  @Test
  public void timedEvent_WithAnnotation() {
    TimedEvent<Annotation> timedEvent = new TimedEvent<Annotation>(timestamp, annotation);
    assertThat(timedEvent.getTimestamp()).isEqualTo(timestamp);
    assertThat(timedEvent.getEvent()).isEqualTo(annotation);
    assertThat(timedEvent.toString()).contains(timestamp.toString());
    assertThat(timedEvent.toString()).contains(annotation.toString());
  }

  @Test
  public void timedEvent_WithNetworkEvent() {
    TimedEvent<NetworkEvent> timedEvent = new TimedEvent<NetworkEvent>(timestamp, networkEvent);
    assertThat(timedEvent.getTimestamp()).isEqualTo(timestamp);
    assertThat(timedEvent.getEvent()).isEqualTo(networkEvent);
    assertThat(timedEvent.toString()).contains(timestamp.toString());
    assertThat(timedEvent.toString()).contains(networkEvent.toString());
  }

  @Test
  public void link_EqualsAndHashCode() {
    EqualsTester tester = new EqualsTester();
    tester
        .addEqualityGroup(
            new TimedEvent<Annotation>(timestamp, annotation),
            new TimedEvent<Annotation>(timestamp, annotation))
        .addEqualityGroup(
            new TimedEvent<NetworkEvent>(timestamp, networkEvent),
            new TimedEvent<NetworkEvent>(timestamp, networkEvent));
    tester.testEquals();
  }
}
