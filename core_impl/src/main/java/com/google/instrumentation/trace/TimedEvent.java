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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.instrumentation.common.Timestamp;
import javax.annotation.concurrent.Immutable;

/** A timed event representation. It can be a timed {@link Annotation} or {@link NetworkEvent}. */
@Immutable
public final class TimedEvent<T> {
  private final Timestamp timestamp;
  private final T event;

  TimedEvent(Timestamp timestamp, T event) {
    this.timestamp = timestamp;
    this.event = event;
  }

  /**
   * Returns the {@code Timestamp} of this event.
   *
   * @return the {@code Timestamp} of this event.
   */
  public Timestamp getTimestamp() {
    return timestamp;
  }

  /**
   * Returns the event.
   *
   * @return the event.
   */
  public T getEvent() {
    return event;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }

    if (!(obj instanceof TimedEvent<?>)) {
      return false;
    }

    TimedEvent<?> that = (TimedEvent<?>) obj;
    return Objects.equal(timestamp, that.timestamp) && Objects.equal(event, that.event);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(timestamp, event);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("timestamp", timestamp)
        .add("event", event)
        .toString();
  }
}
