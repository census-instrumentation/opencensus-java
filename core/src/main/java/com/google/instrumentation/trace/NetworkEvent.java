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

package com.google.instrumentation.trace;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.instrumentation.common.Timestamp;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A class that represents a network event. It requires a {@link Type type} and a message id that
 * serves to uniquely identify each network message. It can optionally can have information about
 * the kernel time and message size.
 */
@Immutable
public final class NetworkEvent {
  /**
   * Available types for a {@code NetworkEvent}.
   */
  public enum Type {
    /** When the message was sent. */
    SENT,
    /** When the message was received. */
    RECV,
  }

  // Can be null if not available.
  private final Timestamp kernelTimestamp;
  private final Type type;
  private final long messageId;
  private final long messageSize;

  private NetworkEvent(
      @Nullable Timestamp kernelTimestamp, Type type, long messageId, long messageSize) {
    this.kernelTimestamp = kernelTimestamp;
    this.type = type;
    this.messageId = messageId;
    this.messageSize = messageSize;
  }

  /**
   * Returns a new {@link Builder} with default values.
   *
   * @param type designates whether this is a network send or receive message.
   * @param messageId serves to uniquely identify each network message.
   * @return a new {@code Builder} with default values.
   * @throws NullPointerException if type is null.
   */
  public static Builder builder(Type type, long messageId) {
    return new Builder(type, messageId);
  }

  /**
   * Returns the kernel timestamp associated with the {@code NetworkEvent} or {@code null} if not
   * set.
   *
   * @return the kernel timestamp associated with the {@code NetworkEvent} or {@code null} if not
   *     set.
   */
  @Nullable
  public Timestamp getKernelTimestamp() {
    return kernelTimestamp;
  }

  /**
   * Returns the type of the {@code NetworkEvent}.
   *
   * @return the type of the {@code NetworkEvent}.
   */
  public Type getType() {
    return type;
  }

  /**
   * Returns the message id argument that serves to uniquely identify each network message.
   *
   * @return The message id of the {@code NetworkEvent}.
   */
  public long getMessageId() {
    return messageId;
  }

  /**
   * Returns The message size in bytes of the {@code NetworkEvent}.
   *
   * @return The message size in bytes of the {@code NetworkEvent}.
   */
  public long getMessageSize() {
    return messageSize;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("kernelTimestamp", kernelTimestamp)
        .add("type", type)
        .add("messageId", messageId)
        .add("messageSize", messageSize)
        .toString();
  }

  /**
   * Builder class for {@link NetworkEvent}.
   */
  public static final class Builder {
    // Required fields.
    private final Type type;
    private final long messageId;
    // Optional fields.
    private Timestamp kernelTimestamp;
    private long messageSize;

    // Contructs a new {@link Builder} with default values.
    private Builder(Type type, long messageId) {
      this.type = checkNotNull(type, "type");
      this.messageId = messageId;
    }

    /**
     * Sets the kernel timestamp.
     *
     * @param kernelTimestamp The kernel timestamp of the event.
     * @return this.
     */
    public Builder setKernelTimestamp(@Nullable Timestamp kernelTimestamp) {
      this.kernelTimestamp = kernelTimestamp;
      return this;
    }

    /**
     * Sets the message size.
     *
     * @param messageSize represents the size in bytes of this network message.
     * @return this.
     */
    public Builder setMessageSize(long messageSize) {
      this.messageSize = messageSize;
      return this;
    }

    /**
     * Builds and returns a {@code NetworkEvent} with the desired values.
     *
     * @return a {@code NetworkEvent} with the desired values.
     */
    public NetworkEvent build() {
      return new NetworkEvent(kernelTimestamp, type, messageId, messageSize);
    }
  }
}
