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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.instrumentation.common.Timestamp;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A class that enables overriding the default values used when ending a {@link Span}. Allows
 * overriding the {@link Status status} and the {@link Timestamp end time}.
 */
@Immutable
public final class EndSpanOptions {
  private final Status status;
  private final Timestamp endTime;

  /** The default {@code EndSpanOptions}. */
  public static final EndSpanOptions DEFAULT = builder().build();

  private EndSpanOptions(Timestamp endTime, Status status) {
    this.endTime = endTime;
    this.status = status;
  }

  /**
   * Returns a new {@link Builder} with default options.
   *
   * @return a new {@code Builder} with default options.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns the end time or {@code null} if default.
   *
   * @return the end time or {@code null} if default.
   */
  @Nullable
  public Timestamp getEndTime() {
    return endTime;
  }

  /**
   * Returns the status.
   *
   * @return the status.
   */
  public Status getStatus() {
    return status;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }

    if (!(obj instanceof EndSpanOptions)) {
      return false;
    }

    EndSpanOptions that = (EndSpanOptions) obj;
    return Objects.equal(endTime, that.endTime) && Objects.equal(status, that.status);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(endTime, status);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("endTime", endTime)
        .add("status", status)
        .toString();
  }

  /** Builder class for {@link EndSpanOptions}. */
  public static final class Builder {
    private Timestamp endTime;
    private Status status = Status.OK;

    private Builder() {}

    /**
     * Sets the end time for the {@link Span}.
     *
     * <p>If not {@code null}, this will override the {@link Span} end time.
     *
     * @param endTime a timestamp used as the {@code Span} end time; if {@code null}, the default
     *     (system time at which the {@link Span#end} method was called) will be used.
     * @return this.
     */
    public Builder setEndTime(@Nullable Timestamp endTime) {
      this.endTime = endTime;
      return this;
    }

    /**
     * Sets the status for the {@link Span}.
     *
     * <p>If set, this will override the default {@code Span} status. Default is {@link Status#OK}.
     *
     * @param status the status.
     * @return this.
     * @throws NullPointerException if {@code status} is {@code null}.
     */
    public Builder setStatus(Status status) {
      this.status = checkNotNull(status, "status");
      return this;
    }

    /**
     * Builds and returns a {@code EndSpanOptions} with the desired settings.
     *
     * @return a {@code EndSpanOptions} with the desired settings.
     */
    public EndSpanOptions build() {
      return new EndSpanOptions(endTime, status);
    }
  }
}
