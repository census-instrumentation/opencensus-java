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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/** Sampler that blacklists certain {@link Span}s by name patterns. */
@AutoValue
@Immutable
abstract class BlacklistingSampler extends Sampler {

  BlacklistingSampler() {}

  abstract Sampler getDelegate();

  abstract Set<Pattern> getSpanNamePatternBlacklist();

  /**
   * Returns a new {@link BlacklistingSampler} with span name patterns provided and delegates the
   * sampling decision if the span is not blacklisted.
   *
   * @param delegate The {@link Sampler} to delegate sampling decision if the span is not
   *     blacklisted.
   * @param spanNamePatternBlacklist The set of patterns to match against span names.
   * @return a new {@link BlacklistingSampler}.
   * @throws IllegalArgumentException if {@code delegate} is *null*.
   */
  static BlacklistingSampler create(
      Sampler delegate, @Nullable Collection<String> spanNamePatternBlacklist) {
    Utils.checkArgument(delegate != null, "delegate cannot be null");
    Set<Pattern> compiledPatterns = new HashSet<>();
    if (spanNamePatternBlacklist != null) {
      for (String pattern : spanNamePatternBlacklist) {
        compiledPatterns.add(Pattern.compile(pattern));
      }
    }
    return new AutoValue_BlacklistingSampler(delegate, compiledPatterns);
  }

  @Override
  public boolean shouldSample(
      @Nullable SpanContext parentContext,
      @Nullable Boolean hasRemoteParent,
      TraceId traceId,
      SpanId spanId,
      String name,
      List<Span> parentLinks) {
    for (Pattern pattern : getSpanNamePatternBlacklist()) {
      if (pattern.matcher(name).matches()) {
        return false;
      }
    }
    return getDelegate()
        .shouldSample(parentContext, hasRemoteParent, traceId, spanId, name, parentLinks);
  }

  @Override
  public String getDescription() {
    return String.format("BlacklistingSampler{over %s}", getDelegate().getDescription());
  }
}
