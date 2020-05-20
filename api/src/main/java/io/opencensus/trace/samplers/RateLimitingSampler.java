/*
 * Copyright 2017, OpenCensus Authors
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

import com.google.auto.value.AutoValue;
import io.opencensus.internal.Utils;
import io.opencensus.trace.Sampler;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Sampler that makes a sampling decision based on specified sample QPS (samples per second).
 *
 * <p>We use the elapsed time Z since the coin flip to weight our current coin flip. We choose our
 * probability function P(Z) such that we get the desired sample QPS.
 *
 * <p>Let X be the desired QPS. Let Z be the elapsed time since the last sampling decision in
 * seconds.
 *
 * <p>P(Z) = min(Z * X, 1)
 */
@AutoValue
@Immutable
abstract class RateLimitingSampler extends Sampler {

  static final double NANOS_PER_SECOND = 1.E+9;
  private final AtomicLong timer = new AtomicLong(0);

  RateLimitingSampler() {}

  abstract double getSamplesPerSecond();

  /**
   * Returns a new {@link RateLimitingSampler} with desired sample QPS.
   *
   * @param samplesPerSecond The desired number of samples per second.
   * @return a new {@link RateLimitingSampler}.
   * @throws IllegalArgumentException if {@code samplesPerSecond} is less than 0.
   */
  static RateLimitingSampler create(double samplesPerSecond) {
    Utils.checkArgument(samplesPerSecond >= 0.0, "samplesPerSecond must be non-negative");
    return new AutoValue_RateLimitingSampler(samplesPerSecond);
  }

  long getCurrentNanos() {
    return System.nanoTime();
  }

  @Override
  public boolean shouldSample(
      @Nullable SpanContext parentContext,
      @Nullable Boolean hasRemoteParent,
      TraceId traceId,
      SpanId spanId,
      String name,
      List<Span> parentLinks) {
    // If the parent is present keep the sampling decision.
    if (parentContext != null && parentContext.isValid()) {
      return parentContext.getTraceOptions().isSampled();
    }
    // If any parent link is present keep the sampling decision.
    if (parentLinks != null && !parentLinks.isEmpty()) {
      boolean oneValidLinkFound = false;
      for (Span parentLink : parentLinks) {
        if (parentLink.getContext().isValid()) {
          oneValidLinkFound = true;
          if (parentLink.getContext().getTraceOptions().isSampled()) {
            return true;
          }
        }
      }
      if (oneValidLinkFound) {
        return false;
      }
    }
    long currentNanos = getCurrentNanos();
    long prevNanos = timer.get();
    long nanosPassed = currentNanos - prevNanos;
    double probability;
    // To prevent issues with double accuracy - set 0 explicitly. Same done in
    // Samplers.probabilitySampler.
    if (nanosPassed <= 0) {
      probability = 0;
    } else {
      probability = Math.min(nanosPassed / NANOS_PER_SECOND * getSamplesPerSecond(), 1);
    }
    // Non-blocking way to thread-safely update the timer.
    // If the value has been updated in the meanwhile - means another span is being coin-flipped
    // right now so we can safely ignore this one
    // as most likely it wouldn't have been sampled anyway (close to 0 probability considering the
    // current logic)
    boolean timerUpdated = timer.compareAndSet(prevNanos, currentNanos);
    return SamplingProbabilityUtils.shouldSampleTrace(probability, traceId) && timerUpdated;
  }

  @Override
  public String getDescription() {
    return String.format("RateLimitingSampler{%.6f}", getSamplesPerSecond());
  }
}
