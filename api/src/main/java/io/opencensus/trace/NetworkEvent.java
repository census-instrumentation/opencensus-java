/*
 * Copyright 2016-17, OpenCensus Authors
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

import com.google.auto.value.AutoValue;
import io.opencensus.common.Internal;
import io.opencensus.common.Timestamp;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A class that represents a network event. It requires a {@link Type type} and a message id that
 * serves to uniquely identify each network message. It can optionally can have information about
 * the kernel time and message size.
 *
 * @since 0.5
 */
// TODO(@Hailong): add `Deprecated` and `deprecated` annotation once we've removed most of the
// imports.
@Immutable
@AutoValue
// Suppress Checker Framework warning about missing @Nullable in generated equals method.
@AutoValue.CopyAnnotations
@SuppressWarnings("nullness")
public abstract class NetworkEvent {
  /**
   * Available types for a {@code NetworkEvent}.
   *
   * @since 0.5
   */
  public enum Type {
    /**
     * When the message was sent.
     *
     * @since 0.5
     */
    SENT,
    /**
     * When the message was received.
     *
     * @since 0.5
     */
    RECV,
  }

  /**
   * Returns a new {@link Builder} with default values.
   *
   * @param type designates whether this is a network send or receive message.
   * @param messageId serves to uniquely identify each network message.
   * @return a new {@code Builder} with default values.
   * @throws NullPointerException if {@code type} is {@code null}.
   * @deprecated Use {@link MessageEvent#builder}.
   * @since 0.5
   */
  @Deprecated
  public static Builder builder(Type type, long messageId) {
    return new NetworkEvent.Builder()
        .setType(checkNotNull(type, "type"))
        .setMessageId(messageId)
        // We need to set a value for the message size because the autovalue requires all
        // primitives to be initialized.
        .setUncompressedMessageSize(0)
        .setCompressedMessageSize(0);
  }

  /**
   * Internal utility method to convert a {@link MessageEvent} to {@code NetworkEvent}.
   *
   * <p>This method is used when users invoke deprecated API {@code SpanData#getNetworkEvents} to
   * get all collected events, which is why it is made public. However, it is not intended for
   * external use.
   *
   * @return a new {@code NetworkEvent}.
   */
  @Internal
  public static NetworkEvent fromMessageEvent(MessageEvent messageEvent) {
    return new AutoValue_NetworkEvent(messageEvent);
  }

  /**
   * Returns the underlying {@link MessageEvent}.
   *
   * <p>This method is used when users add event by deprecated API {@code Span#addNetworkEvents} and
   * is thus package protected.
   *
   * @return the underlying @{code MessageEvent}.
   */
  abstract MessageEvent getMessageEvent();

  /**
   * Returns the kernel timestamp associated with the {@code NetworkEvent} or {@code null} if not
   * set.
   *
   * @return the kernel timestamp associated with the {@code NetworkEvent} or {@code null} if not
   *     set.
   * @since 0.5
   */
  @Nullable
  public Timestamp getKernelTimestamp() {
    return getMessageEvent().getKernelTimestamp();
  }

  /**
   * Returns the type of the {@code NetworkEvent}.
   *
   * @return the type of the {@code NetworkEvent}.
   * @since 0.5
   */
  public Type getType() {
    return getMessageEvent().getType() == MessageEvent.Type.SENT ? Type.SENT : Type.RECV;
  }

  /**
   * Returns the message id argument that serves to uniquely identify each network message.
   *
   * @return the message id of the {@code NetworkEvent}.
   * @since 0.5
   */
  public long getMessageId() {
    return getMessageEvent().getMessageId();
  }

  /**
   * Returns the uncompressed size in bytes of the {@code NetworkEvent}.
   *
   * @return the uncompressed size in bytes of the {@code NetworkEvent}.
   * @since 0.6
   */
  public long getUncompressedMessageSize() {
    return getMessageEvent().getUncompressedMessageSize();
  }

  /**
   * Returns the compressed size in bytes of the {@code NetworkEvent}.
   *
   * @return the compressed size in bytes of the {@code NetworkEvent}.
   * @since 0.6
   */
  public long getCompressedMessageSize() {
    return getMessageEvent().getCompressedMessageSize();
  }

  /**
   * @deprecated Use {@link #getUncompressedMessageSize}.
   * @return the uncompressed size in bytes of the {@code NetworkEvent}.
   * @since 0.5
   */
  @Deprecated
  public long getMessageSize() {
    return getUncompressedMessageSize();
  }

  /**
   * Builder class for {@link NetworkEvent}.
   *
   * @since 0.5
   */
  @AutoValue.Builder
  public abstract static class Builder {
    // Package protected methods because these values are mandatory and set only in the
    // NetworkEvent#builder() function.
    Builder setType(Type type) {
      messageEventBuilder.setType(
          type == Type.SENT ? MessageEvent.Type.SENT : MessageEvent.Type.RECEIVED);
      return this;
    }

    Builder setMessageId(long messageId) {
      messageEventBuilder.setMessageId(messageId);
      return this;
    }

    /**
     * Sets the kernel timestamp.
     *
     * @param kernelTimestamp The kernel timestamp of the event.
     * @return this.
     * @since 0.5
     */
    public Builder setKernelTimestamp(@Nullable Timestamp kernelTimestamp) {
      messageEventBuilder.setKernelTimestamp(kernelTimestamp);
      return this;
    }

    /**
     * @deprecated Use {@link #setUncompressedMessageSize}.
     * @param messageSize represents the uncompressed size in bytes of this message.
     * @return this.
     * @since 0.5
     */
    @Deprecated
    public Builder setMessageSize(long messageSize) {
      return setUncompressedMessageSize(messageSize);
    }

    /**
     * Sets the uncompressed message size.
     *
     * @param uncompressedMessageSize represents the uncompressed size in bytes of this message.
     * @return this.
     * @since 0.6
     */
    public Builder setUncompressedMessageSize(long uncompressedMessageSize) {
      messageEventBuilder.setUncompressedMessageSize(uncompressedMessageSize);
      return this;
    }

    /**
     * Sets the compressed message size.
     *
     * @param compressedMessageSize represents the compressed size in bytes of this message.
     * @return this.
     * @since 0.6
     */
    public Builder setCompressedMessageSize(long compressedMessageSize) {
      messageEventBuilder.setCompressedMessageSize(compressedMessageSize);
      return this;
    }

    /**
     * Builds and returns a {@code NetworkEvent} with the desired values.
     *
     * @return a {@code NetworkEvent} with the desired values.
     * @since 0.5
     */
    public NetworkEvent build() {
      return new AutoValue_NetworkEvent(messageEventBuilder.build());
    }

    Builder() {
      messageEventBuilder = new AutoValue_MessageEvent.Builder();
    }
  }

  NetworkEvent() {}
}
