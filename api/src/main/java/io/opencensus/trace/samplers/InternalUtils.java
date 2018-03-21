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

package io.opencensus.trace.samplers;

import com.google.common.base.Preconditions;
import io.opencensus.trace.Sampler;

/**
 * Internal utilities for samplers.
 *
 * @since 0.13
 */
@io.opencensus.common.Internal
public final class InternalUtils {

  private InternalUtils() {}

  /**
   * Internal accessor to get probability from a {@link ProbabilitySampler}.
   *
   * @param sampler {@code Sampler}.
   * @return the desired probability of sampling if the {@code Sampler} is a {@code
   *     ProbabilitySampler}.
   * @throws IllegalArgumentException if the {@code Sampler} is not a {@code ProbabilitySampler}.
   * @since 0.13
   */
  public static double getProbability(Sampler sampler) {
    Preconditions.checkArgument(
        sampler instanceof ProbabilitySampler, "ProbabilitySampler expected.");
    return ((ProbabilitySampler) sampler).getProbability();
  }
}
