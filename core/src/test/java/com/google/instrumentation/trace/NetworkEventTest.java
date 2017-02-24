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

/** Unit tests for {@link NetworkEvent}. */
@RunWith(JUnit4.class)
public class NetworkEventTest {
  @Test(expected = NullPointerException.class)
  public void buildNetworkEvent_NullType() {
    NetworkEvent.builder(null, 1L).build();
  }

  @Test
  public void buildNetworkEvent_WithRequiredFields() {
    NetworkEvent networkEvent = NetworkEvent.builder(NetworkEvent.Type.SENT, 1L).build();
    assertThat(networkEvent.getType()).isEqualTo(NetworkEvent.Type.SENT);
    assertThat(networkEvent.getMessageId()).isEqualTo(1L);
    assertThat(networkEvent.getKernelTimestamp()).isNull();
    assertThat(networkEvent.getMessageSize()).isEqualTo(0L);
  }

  @Test
  public void buildNetworkEvent_WithTimestamp() {
    NetworkEvent networkEvent =
        NetworkEvent.builder(NetworkEvent.Type.SENT, 1L)
            .setKernelTimestamp(Timestamp.fromMillis(123456L))
            .build();
    assertThat(networkEvent.getKernelTimestamp()).isEqualTo(Timestamp.fromMillis(123456L));
    assertThat(networkEvent.getType()).isEqualTo(NetworkEvent.Type.SENT);
    assertThat(networkEvent.getMessageId()).isEqualTo(1L);
    assertThat(networkEvent.getMessageSize()).isEqualTo(0L);
  }

  @Test
  public void buildNetworkEvent_WithMessageSize() {
    NetworkEvent networkEvent =
        NetworkEvent.builder(NetworkEvent.Type.SENT, 1L).setMessageSize(123L).build();
    assertThat(networkEvent.getKernelTimestamp()).isNull();
    assertThat(networkEvent.getType()).isEqualTo(NetworkEvent.Type.SENT);
    assertThat(networkEvent.getMessageId()).isEqualTo(1L);
    assertThat(networkEvent.getMessageSize()).isEqualTo(123L);
  }

  @Test
  public void buildNetworkEvent_WithAllValues() {
    NetworkEvent networkEvent =
        NetworkEvent.builder(NetworkEvent.Type.RECV, 1L)
            .setKernelTimestamp(Timestamp.fromMillis(123456L))
            .setMessageSize(123L)
            .build();
    assertThat(networkEvent.getKernelTimestamp()).isEqualTo(Timestamp.fromMillis(123456L));
    assertThat(networkEvent.getType()).isEqualTo(NetworkEvent.Type.RECV);
    assertThat(networkEvent.getMessageId()).isEqualTo(1L);
    assertThat(networkEvent.getMessageSize()).isEqualTo(123L);
  }

  @Test
  public void networkEvent_ToString() {
    NetworkEvent networkEvent =
        NetworkEvent.builder(NetworkEvent.Type.SENT, 1L)
            .setKernelTimestamp(Timestamp.fromMillis(123456L))
            .setMessageSize(123L)
            .build();
    assertThat(networkEvent.toString()).contains(Timestamp.fromMillis(123456L).toString());
    assertThat(networkEvent.toString()).contains("type=SENT");
    assertThat(networkEvent.toString()).contains("messageId=1");
    assertThat(networkEvent.toString()).contains("messageSize=123");
  }
}
