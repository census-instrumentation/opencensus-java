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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link BaseMessageEventUtil}. */
@RunWith(JUnit4.class)
public class BaseMessageEventUtilTest {
  private static final long SENT_EVENT_ID = 12345L;
  private static final long RECV_EVENT_ID = 67890L;
  private static final long UNCOMPRESSED_SIZE = 100;
  private static final long COMPRESSED_SIZE = 99;

  private static final MessageEvent SENT_MESSAGE_EVENT =
      MessageEvent.builder(MessageEvent.Type.SENT, SENT_EVENT_ID)
          .setUncompressedMessageSize(UNCOMPRESSED_SIZE)
          .setCompressedMessageSize(COMPRESSED_SIZE)
          .build();
  private static final MessageEvent RECV_MESSAGE_EVENT =
      MessageEvent.builder(MessageEvent.Type.RECEIVED, RECV_EVENT_ID)
          .setUncompressedMessageSize(UNCOMPRESSED_SIZE)
          .setCompressedMessageSize(COMPRESSED_SIZE)
          .build();
  private static final NetworkEvent SENT_NETWORK_EVENT =
      NetworkEvent.builder(NetworkEvent.Type.SENT, SENT_EVENT_ID)
          .setUncompressedMessageSize(UNCOMPRESSED_SIZE)
          .setCompressedMessageSize(COMPRESSED_SIZE)
          .build();
  private static final NetworkEvent RECV_NETWORK_EVENT =
      NetworkEvent.builder(NetworkEvent.Type.RECV, RECV_EVENT_ID)
          .setUncompressedMessageSize(UNCOMPRESSED_SIZE)
          .setCompressedMessageSize(COMPRESSED_SIZE)
          .build();

  @Test
  public void networkEventToMessageEvent() {
    assertThat(BaseMessageEventUtil.asMessageEvent(SENT_NETWORK_EVENT))
        .isEqualTo(SENT_MESSAGE_EVENT);
    assertThat(BaseMessageEventUtil.asMessageEvent(RECV_NETWORK_EVENT))
        .isEqualTo(RECV_MESSAGE_EVENT);
  }

  @Test
  public void messageEventToNetworkEvent() {
    assertThat(BaseMessageEventUtil.asNetworkEvent(SENT_MESSAGE_EVENT))
        .isEqualTo(SENT_NETWORK_EVENT);
    assertThat(BaseMessageEventUtil.asNetworkEvent(RECV_MESSAGE_EVENT))
        .isEqualTo(RECV_NETWORK_EVENT);
  }
}
