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

package io.opencensus.trace.base;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.auto.value.AutoValue;
import io.opencensus.trace.Span;
import javax.annotation.concurrent.Immutable;

/**
 * A class that enables overriding the default values used when ending a {@link Span}. Allows
 * overriding the {@link Status status}.
 */
@Immutable
@AutoValue
public abstract class EndSpanOptions {
  /** The default {@code EndSpanOptions}. */
  public static final EndSpanOptions DEFAULT = builder().build();

  /**
   * Returns a new {@link Builder} with default options.
   *
   * @return a new {@code Builder} with default options.
   */
  public static Builder builder() {
    return new AutoValue_EndSpanOptions.Builder().setStatus(Status.OK);
  }

  /**
   * Returns the status.
   *
   * @return the status.
   */
  public abstract Status getStatus();

  /** Builder class for {@link EndSpanOptions}. */
  @AutoValue.Builder
  public abstract static class Builder {
    /**
     * Sets the status for the {@link Span}.
     *
     * <p>If set, this will override the default {@code Span} status. Default is {@link Status#OK}.
     *
     * @param status the status.
     * @return this.
     */
    public abstract Builder setStatus(Status status);

    abstract EndSpanOptions autoBuild(); // not public

    /**
     * Builds and returns a {@code EndSpanOptions} with the desired settings.
     *
     * @return a {@code EndSpanOptions} with the desired settings.
     * @throws NullPointerException if {@code status} is {@code null}.
     */
    public EndSpanOptions build() {
      EndSpanOptions endSpanOptions = autoBuild();
      checkNotNull(endSpanOptions.getStatus(), "status");
      return endSpanOptions;
    }

    Builder() {}
  }

  EndSpanOptions() {}
}
