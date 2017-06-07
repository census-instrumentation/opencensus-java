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

package io.opencensus.trace;

import io.opencensus.trace.base.SpanId;
import io.opencensus.trace.base.TraceId;
import java.util.List;
import javax.annotation.Nullable;

/** Sampler is used to make decisions on {@link Span} sampling. */
public abstract class Sampler {
  /**
   * Called during {@link Span} creation to make a sampling decision.
   *
   * @param parentContext The parent {@code Span} {@link SpanContext}. May be {@code null} if this
   *     is a root span.
   * @param remoteParent true if the parentContext is remote.
   * @param traceId The {@link TraceId} for the new {@code Span}. This will be identical to that in
   *     the parentContext, unless this is a root span.
   * @param spanId The span ID for the new {@code Span}.
   * @param name The name of the new {@code Span}.
   * @param parentLinks The parentLinks associated with the new {@code Span}.
   * @return {@code true} if the {@code Span} is sampled.
   */
  protected abstract boolean shouldSample(
      @Nullable SpanContext parentContext,
      boolean remoteParent,
      TraceId traceId,
      SpanId spanId,
      String name,
      List<Span> parentLinks);
}
