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
import io.opencensus.common.Timestamp;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A class that represents a message event. It requires a {@link Type type} and a message id that
 * serves to uniquely identify each message. It can optionally have information about the kernel
 * time and message size.
 *
 * @since 0.10.0
 */
@Immutable
@AutoValue
public abstract class MessageEvent {
  /** Available types for a {@code MessageEvent}. */
  public enum Type {
    /** When the message was sent. */
    SENT,
    /** When the message was received. */
    RECEIVED,
  }

  /**
   * Returns a new {@link Builder} with default values.
   *
   * @param type designates whether this is a send or receive message.
   * @param messageId serves to uniquely identify each message.
   * @return a new {@code Builder} with default values.
   * @throws NullPointerException if {@code type} is {@code null}.
   */
  public static Builder builder(Type type, long messageId) {
    return new AutoValue_MessageEvent.Builder()
        .setType(checkNotNull(type, "type"))
        .setMessageId(messageId)
        // We need to set a value for the message size because the autovalue requires all
        // primitives to be initialized.
        .setUncompressedMessageSize(0)
        .setCompressedMessageSize(0);
  }

  /**
   * Returns the kernel timestamp associated with the {@code MessageEvent} or {@code null} if not
   * set.
   *
   * @return the kernel timestamp associated with the {@code MessageEvent} or {@code null} if not
   *     set.
   */
  @Nullable
  public abstract Timestamp getKernelTimestamp();

  /**
   * Returns the type of the {@code MessageEvent}.
   *
   * @return the type of the {@code MessageEvent}.
   */
  public abstract Type getType();

  /**
   * Returns the message id argument that serves to uniquely identify each message.
   *
   * @return the message id of the {@code MessageEvent}.
   */
  public abstract long getMessageId();

  /**
   * Returns the uncompressed size in bytes of the {@code MessageEvent}.
   *
   * @return the uncompressed size in bytes of the {@code MessageEvent}.
   */
  public abstract long getUncompressedMessageSize();

  /**
   * Returns the compressed size in bytes of the {@code MessageEvent}.
   *
   * @return the compressed size in bytes of the {@code MessageEvent}.
   */
  public abstract long getCompressedMessageSize();

  /** Builder class for {@link MessageEvent}. */
  @AutoValue.Builder
  public abstract static class Builder {
    // Package protected methods because these values are mandatory and set only in the
    // MessageEvent#builder() function.
    abstract Builder setType(Type type);

    abstract Builder setMessageId(long messageId);

    /**
     * Sets the kernel timestamp.
     *
     * @param kernelTimestamp The kernel timestamp of the event.
     * @return this.
     */
    public abstract Builder setKernelTimestamp(@Nullable Timestamp kernelTimestamp);

    /**
     * Sets the uncompressed message size.
     *
     * @param uncompressedMessageSize represents the uncompressed size in bytes of this message.
     * @return this.
     */
    public abstract Builder setUncompressedMessageSize(long uncompressedMessageSize);

    /**
     * Sets the compressed message size.
     *
     * @param compressedMessageSize represents the compressed size in bytes of this message.
     * @return this.
     */
    public abstract Builder setCompressedMessageSize(long compressedMessageSize);

    /**
     * Builds and returns a {@code MessageEvent} with the desired values.
     *
     * @return a {@code MessageEvent} with the desired values.
     */
    public abstract MessageEvent build();

    Builder() {}
  }

  MessageEvent() {}
}
