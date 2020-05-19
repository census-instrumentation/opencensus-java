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

package io.opencensus.contrib.http;

import io.opencensus.common.ExperimentalApi;
import io.opencensus.trace.Sampler;
import javax.annotation.Nullable;

/**
 * A class that represents options applied to spans created by {@link
 * HttpServerHandler#handleStart}.
 *
 * @since 0.27
 */
@ExperimentalApi
public class StartOptions {

  @Nullable final Sampler sampler;

  protected StartOptions(@Nullable Sampler sampler) {
    this.sampler = sampler;
  }

  /**
   * Returns a {@link StartOptions.Builder} to construct a new {@link StartOptions} instance.
   *
   * @return a new {@link StartOptions.Builder}.
   * @since 0.27
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * A class that is used to construct {@link StartOptions} instances.
   *
   * @since 0.27
   */
  public static class Builder {
    @Nullable private Sampler sampler;

    /**
     * Sets the {@link Sampler} to use for the created span. If not set, the implementation will
     * provide a default.
     *
     * @param sampler the {@code Sampler} to use when determining sampling for a {@code Span}.
     * @return this.
     * @since 0.27
     */
    public Builder setSampler(Sampler sampler) {
      this.sampler = sampler;
      return this;
    }

    /**
     * Cosntructs a new {@link StartOptions} isntance.
     *
     * @return the newly created {@code Span}.
     * @since 0.27
     */
    public StartOptions build() {
      return new StartOptions(sampler);
    }
  }
}
