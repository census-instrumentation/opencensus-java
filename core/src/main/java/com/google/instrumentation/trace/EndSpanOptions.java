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
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A class that enables overriding the default values used when ending a {@link Span}. Allows
 * overriding the {@link Status status}.
 */
@Immutable
public final class EndSpanOptions {
  private final Status status;

  /** The default {@code EndSpanOptions}. */
  public static final EndSpanOptions DEFAULT = builder().build();

  private EndSpanOptions(Status status) {
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
    return Objects.equal(status, that.status);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(status);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("status", status)
        .toString();
  }

  /** Builder class for {@link EndSpanOptions}. */
  public static final class Builder {
    private Status status = Status.OK;

    private Builder() {}

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
      return new EndSpanOptions(status);
    }
  }
}
