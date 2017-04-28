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

import com.google.auto.value.AutoValue;
import com.google.instrumentation.common.Timestamp;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A class that represents a network event. It requires a {@link Type type} and a message id that
 * serves to uniquely identify each network message. It can optionally can have information about
 * the kernel time and message size.
 */
@Immutable
@AutoValue
public abstract class NetworkEvent {
  /** Available types for a {@code NetworkEvent}. */
  public enum Type {
    /** When the message was sent. */
    SENT,
    /** When the message was received. */
    RECV,
  }

  /**
   * Returns a new {@link Builder} with default values.
   *
   * @param type designates whether this is a network send or receive message.
   * @param messageId serves to uniquely identify each network message.
   * @return a new {@code Builder} with default values.
   * @throws NullPointerException if {@code type} is {@code null}.
   */
  public static Builder builder(Type type, long messageId) {
    return new AutoValue_NetworkEvent.Builder()
        .setType(checkNotNull(type, "type"))
        .setMessageId(messageId)
        // We need to set a value for the message size because the autovalue requires all
        // primitives to be initialized.
        // TODO(bdrutu): Consider to change the API to require message size.
        .setMessageSize(0);
  }

  /**
   * Returns the kernel timestamp associated with the {@code NetworkEvent} or {@code null} if not
   * set.
   *
   * @return the kernel timestamp associated with the {@code NetworkEvent} or {@code null} if not
   *     set.
   */
  @Nullable
  public abstract Timestamp getKernelTimestamp();

  /**
   * Returns the type of the {@code NetworkEvent}.
   *
   * @return the type of the {@code NetworkEvent}.
   */
  public abstract Type getType();

  /**
   * Returns the message id argument that serves to uniquely identify each network message.
   *
   * @return The message id of the {@code NetworkEvent}.
   */
  public abstract long getMessageId();

  /**
   * Returns The message size in bytes of the {@code NetworkEvent}.
   *
   * @return The message size in bytes of the {@code NetworkEvent}.
   */
  public abstract long getMessageSize();

  /** Builder class for {@link NetworkEvent}. */
  @AutoValue.Builder
  public abstract static class Builder {
    // Package protected methods because these values are mandatory and set only in the
    // NetworkEvent#builder() function.
    abstract Builder setType(Type type);

    abstract Builder setMessageId(long messageId);

    /**
     * Sets the kernel timestamp.
     *
     * @param kernelTimestamp The kernel timestamp of the event.
     * @return this.
     */
    public abstract Builder setKernelTimestamp(Timestamp kernelTimestamp);

    /**
     * Sets the message size.
     *
     * @param messageSize represents the size in bytes of this network message.
     * @return this.
     */
    public abstract Builder setMessageSize(long messageSize);

    /**
     * Builds and returns a {@code NetworkEvent} with the desired values.
     *
     * @return a {@code NetworkEvent} with the desired values.
     */
    public abstract NetworkEvent build();

    Builder() {}
  }

  NetworkEvent() {}
}
