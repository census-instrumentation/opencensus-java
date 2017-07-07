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

import java.util.List;
import javax.annotation.Nullable;

/** Sampler is used to make decisions on {@link Span} sampling. */
public abstract class Sampler {
  /**
   * Called during {@link Span} creation to make a sampling decision.
   *
   * @param parentContext the parent span's {@link SpanContext}. {@code null} if this is a root
   *     span.
   * @param hasRemoteParent {@code true} if the parentContext is remote. {@code null} if this is a
   *     root span.
   * @param traceId the {@link TraceId} for the new {@code Span}. This will be identical to that in
   *     the parentContext, unless this is a root span.
   * @param spanId the span ID for the new {@code Span}.
   * @param name the name of the new {@code Span}.
   * @param parentLinks the parentLinks associated with the new {@code Span}.
   * @return {@code true} if the {@code Span} is sampled.
   */
  public abstract boolean shouldSample(
      @Nullable SpanContext parentContext,
      @Nullable Boolean hasRemoteParent,
      TraceId traceId,
      SpanId spanId,
      String name,
      List<Span> parentLinks);
}
