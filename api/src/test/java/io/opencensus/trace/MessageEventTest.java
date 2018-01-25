/*
 * Copyright 2018, OpenCensus Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.trace;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.common.Timestamp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link MessageEvent}. */
@RunWith(JUnit4.class)
public class MessageEventTest {
  @Test(expected = NullPointerException.class)
  public void buildMessageEvent_NullType() {
    MessageEvent.builder(null, 1L).build();
  }

  @Test
  public void buildMessageEvent_WithRequiredFields() {
    MessageEvent messageEvent = MessageEvent.builder(MessageEvent.Type.SENT, 1L).build();
    assertThat(messageEvent.getType()).isEqualTo(MessageEvent.Type.SENT);
    assertThat(messageEvent.getMessageId()).isEqualTo(1L);
    assertThat(messageEvent.getKernelTimestamp()).isNull();
    assertThat(messageEvent.getUncompressedMessageSize()).isEqualTo(0L);
  }

  @Test
  public void buildMessageEvent_WithTimestamp() {
    MessageEvent messageEvent =
        MessageEvent.builder(MessageEvent.Type.SENT, 1L)
            .setKernelTimestamp(Timestamp.fromMillis(123456L))
            .build();
    assertThat(messageEvent.getKernelTimestamp()).isEqualTo(Timestamp.fromMillis(123456L));
    assertThat(messageEvent.getType()).isEqualTo(MessageEvent.Type.SENT);
    assertThat(messageEvent.getMessageId()).isEqualTo(1L);
    assertThat(messageEvent.getUncompressedMessageSize()).isEqualTo(0L);
  }

  @Test
  public void buildMessageEvent_WithUncompressedMessageSize() {
    MessageEvent messageEvent =
        MessageEvent.builder(MessageEvent.Type.SENT, 1L).setUncompressedMessageSize(123L).build();
    assertThat(messageEvent.getKernelTimestamp()).isNull();
    assertThat(messageEvent.getType()).isEqualTo(MessageEvent.Type.SENT);
    assertThat(messageEvent.getMessageId()).isEqualTo(1L);
    assertThat(messageEvent.getUncompressedMessageSize()).isEqualTo(123L);
  }

  @Test
  public void buildMessageEvent_WithCompressedMessageSize() {
    MessageEvent messageEvent =
        MessageEvent.builder(MessageEvent.Type.SENT, 1L).setCompressedMessageSize(123L).build();
    assertThat(messageEvent.getKernelTimestamp()).isNull();
    assertThat(messageEvent.getType()).isEqualTo(MessageEvent.Type.SENT);
    assertThat(messageEvent.getMessageId()).isEqualTo(1L);
    assertThat(messageEvent.getCompressedMessageSize()).isEqualTo(123L);
  }

  @Test
  public void buildMessageEvent_WithAllValues() {
    MessageEvent messageEvent =
        MessageEvent.builder(MessageEvent.Type.RECEIVED, 1L)
            .setKernelTimestamp(Timestamp.fromMillis(123456L))
            .setUncompressedMessageSize(123L)
            .setCompressedMessageSize(63L)
            .build();
    assertThat(messageEvent.getKernelTimestamp()).isEqualTo(Timestamp.fromMillis(123456L));
    assertThat(messageEvent.getType()).isEqualTo(MessageEvent.Type.RECEIVED);
    assertThat(messageEvent.getMessageId()).isEqualTo(1L);
    assertThat(messageEvent.getUncompressedMessageSize()).isEqualTo(123L);
    assertThat(messageEvent.getCompressedMessageSize()).isEqualTo(63L);
  }

  @Test
  public void messageEvent_ToString() {
    MessageEvent messageEvent =
        MessageEvent.builder(MessageEvent.Type.SENT, 1L)
            .setKernelTimestamp(Timestamp.fromMillis(123456L))
            .setUncompressedMessageSize(123L)
            .setCompressedMessageSize(63L)
            .build();
    assertThat(messageEvent.toString()).contains(Timestamp.fromMillis(123456L).toString());
    assertThat(messageEvent.toString()).contains("type=SENT");
    assertThat(messageEvent.toString()).contains("messageId=1");
    assertThat(messageEvent.toString()).contains("compressedMessageSize=63");
    assertThat(messageEvent.toString()).contains("uncompressedMessageSize=123");
  }
}
