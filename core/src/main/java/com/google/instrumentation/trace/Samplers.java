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

import java.util.List;
import javax.annotation.Nullable;

/**
 * Static class to access a set of pre-defined {@link Sampler Samplers}.
 */
public final class Samplers {
  private static final Sampler ALWAYS_SAMPLE = new AlwaysSampleSampler();
  private static final Sampler NEVER_SAMPLE = new NeverSampleSampler();

  // No instance of this class.
  private Samplers() {}

  /**
   * Returns a {@link Sampler} that always makes a "yes" decision on {@link Span} sampling.
   *
   * @return a {@code Sampler} that always makes a "yes" decision on {@code Span} sampling.
   */
  public static Sampler alwaysSample() {
    return ALWAYS_SAMPLE;
  }

  /**
   * Returns a {@link Sampler} that always makes a "no" decision on {@link Span} sampling.
   *
   * @return a {@code Sampler} that always makes a "no" decision on {@code Span} sampling.
   */
  public static Sampler neverSample() {
    return NEVER_SAMPLE;
  }

  private static final class AlwaysSampleSampler extends Sampler {
    private AlwaysSampleSampler() {}

    // Returns always makes a "yes" decision on {@link Span} sampling.
    @Override
    protected boolean shouldSample(
        @Nullable SpanContext parentContext,
        boolean remoteParent,
        TraceId traceId,
        long spanId,
        String name,
        List<Span> parentLinks) {
      return true;
    }

    @Override
    public String toString() {
      return "AlwaysSampleSampler";
    }
  }

  private static final class NeverSampleSampler extends Sampler {
    private NeverSampleSampler() {}

    // Returns always makes a "no" decision on {@link Span} sampling.
    @Override
    protected boolean shouldSample(
        @Nullable SpanContext parentContext,
        boolean remoteParent,
        TraceId traceId,
        long spanId,
        String name,
        List<Span> parentLinks) {
      return false;
    }

    @Override
    public String toString() {
      return "NeverSampleSampler";
    }
  }
}
