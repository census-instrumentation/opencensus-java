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

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Superclass for {@link MessageEvent} and {@link NetworkEvent} to resolve API backward
 * compatibility issue.
 *
 * @since 0.12
 */
// public for io.opencensus.trace.export.SpanData.
@SuppressWarnings("deprecation")
public abstract class BaseMessageEvent {
  /**
   * Cast or convert a {@link BaseMessageEvent} to {@link MessageEvent}.
   *
   * <p>Warning: if the input is a {@code NetworkEvent} and contains {@code kernelTimestamp}
   * information, this information will be dropped.
   *
   * @param event the {@code BaseMessageEvent} that is being cast or converted.
   * @return a {@code MessageEvent} representation of the input.
   * @since 0.12
   */
  public static MessageEvent asMessageEvent(BaseMessageEvent event) {
    checkNotNull(event);
    if (event instanceof MessageEvent) {
      return (MessageEvent) event;
    }
    assert event instanceof NetworkEvent;
    NetworkEvent networkEvent = (NetworkEvent) event;
    MessageEvent.Type type =
        (networkEvent.getType() == NetworkEvent.Type.RECV)
            ? MessageEvent.Type.RECEIVED
            : MessageEvent.Type.SENT;
    return MessageEvent.builder(type, networkEvent.getMessageId())
        .setUncompressedMessageSize(networkEvent.getUncompressedMessageSize())
        .setCompressedMessageSize(networkEvent.getCompressedMessageSize())
        .build();
  }

  /**
   * Cast or convert a {@link BaseMessageEvent} to {@link NetworkEvent}.
   *
   * @param event the {@code BaseMessageEvent} that is being cast or converted.
   * @return a {@code NetworkEvent} representation of the input.
   * @since 0.12
   */
  public static NetworkEvent asNetworkEvent(BaseMessageEvent event) {
    checkNotNull(event);
    if (event instanceof NetworkEvent) {
      return (NetworkEvent) event;
    }
    assert event instanceof MessageEvent;
    MessageEvent messageEvent = (MessageEvent) event;
    NetworkEvent.Type type =
        (messageEvent.getType() == MessageEvent.Type.RECEIVED)
            ? NetworkEvent.Type.RECV
            : NetworkEvent.Type.SENT;
    return NetworkEvent.builder(type, messageEvent.getMessageId())
        .setUncompressedMessageSize(messageEvent.getUncompressedMessageSize())
        .setCompressedMessageSize(messageEvent.getCompressedMessageSize())
        .build();
  }

  BaseMessageEvent() {}
}
