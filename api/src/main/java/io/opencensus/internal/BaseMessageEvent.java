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

package io.opencensus.internal;

import io.opencensus.trace.MessageEvent;
import io.opencensus.trace.MessageEvent.Type;

/**
 * Superclass for {@link MessageEvent} and {@link io.opencensus.trace.NetworkEvent} to resolve API
 * backward compatibility issue.
 *
 * @since 0.12.0
 */
public abstract class BaseMessageEvent {
  /** Cast a {@code this} as {@code MessageEvent}. */
  @SuppressWarnings("deprecation")
  public MessageEvent asMessageEvent() {
    if (this instanceof MessageEvent) {
      return (MessageEvent) this;
    }
    io.opencensus.trace.NetworkEvent networkEvent = (io.opencensus.trace.NetworkEvent) this;
    Type type =
        (networkEvent.getType() == io.opencensus.trace.NetworkEvent.Type.RECV)
            ? Type.RECEIVED
            : Type.SENT;
    return MessageEvent.builder(type, networkEvent.getMessageId())
        .setKernelTimestamp(networkEvent.getKernelTimestamp())
        .setUncompressedMessageSize(networkEvent.getUncompressedMessageSize())
        .setCompressedMessageSize(networkEvent.getCompressedMessageSize())
        .build();
  }

  /** Cast a {@code this} as {@code NetworkEvent}. */
  @SuppressWarnings("deprecation")
  public io.opencensus.trace.NetworkEvent asNetworkEvent() {
    if (this instanceof io.opencensus.trace.NetworkEvent) {
      return (io.opencensus.trace.NetworkEvent) this;
    }
    MessageEvent messageEvent = (MessageEvent) this;
    io.opencensus.trace.NetworkEvent.Type type =
        (messageEvent.getType() == Type.RECEIVED)
            ? io.opencensus.trace.NetworkEvent.Type.RECV
            : io.opencensus.trace.NetworkEvent.Type.SENT;
    return io.opencensus.trace.NetworkEvent.builder(type, messageEvent.getMessageId())
        .setKernelTimestamp(messageEvent.getKernelTimestamp())
        .setUncompressedMessageSize(messageEvent.getUncompressedMessageSize())
        .setCompressedMessageSize(messageEvent.getCompressedMessageSize())
        .build();
  }
}
