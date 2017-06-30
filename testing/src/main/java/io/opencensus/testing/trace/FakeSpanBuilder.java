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

package io.opencensus.testing.trace;

import static com.google.common.base.Preconditions.checkNotNull;

import io.opencensus.trace.Span;
import io.opencensus.trace.Span.Options;
import io.opencensus.trace.SpanBuilder;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.base.Sampler;
import io.opencensus.trace.base.SpanId;
import io.opencensus.trace.base.TraceId;
import io.opencensus.trace.base.TraceOptions;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;

/** A {@link SpanBuilder} that allows users to test instrumented code. */
public class FakeSpanBuilder extends SpanBuilder {
  private final String name;
  private final Span parentSpan;
  private final SpanContext remoteParentSpanContext;
  private final SpanContext parentSpanContext;
  private final boolean hasRemoteParent;
  private Sampler sampler;
  private List<Span> parentLinks = Collections.<Span>emptyList();
  private Boolean recordEvents;
  private final Random random = new Random();

  /**
   * Returns a new {@code SpanBuilder}.
   *
   * @param parentSpan the parent {@code Span} or {@code null} if root.
   * @param name the name of the {@code Span}.
   * @return a new {@code SpanBuilder}.
   */
  public static FakeSpanBuilder createWithParent(@Nullable Span parentSpan, String name) {
    parentSpan = parentSpan != null && parentSpan.getContext().isValid() ? parentSpan : null;
    SpanContext parentSpanContext = parentSpan != null ? parentSpan.getContext() : null;
    return new FakeSpanBuilder(name, parentSpan, null, parentSpanContext, false);
  }

  /**
   * Returns a new {@code SpanBuilder}.
   *
   * @param remoteParentSpanContext the remote parent {@code SpanContext} or {@code null} if root.
   * @param name the name of the {@code Span}.
   * @return a new {@code SpanBuilder}.
   */
  public static FakeSpanBuilder createWithRemoteParent(
      @Nullable SpanContext remoteParentSpanContext, String name) {
    remoteParentSpanContext =
        remoteParentSpanContext != null && remoteParentSpanContext.isValid()
            ? remoteParentSpanContext
            : null;
    return new FakeSpanBuilder(
        name,
        null,
        remoteParentSpanContext,
        remoteParentSpanContext,
        remoteParentSpanContext != null);
  }

  @Override
  public FakeSpanBuilder setSampler(Sampler sampler) {
    this.sampler = checkNotNull(sampler, "sampler");
    return this;
  }

  @Override
  public FakeSpanBuilder setParentLinks(List<Span> parentLinks) {
    this.parentLinks = checkNotNull(parentLinks, "parentLinks");
    return this;
  }

  @Override
  public FakeSpanBuilder setRecordEvents(boolean recordEvents) {
    this.recordEvents = recordEvents;
    return this;
  }

  @Override
  public FakeSpan startSpan() {
    SpanContext spanContext = generateSpanContext(parentSpanContext, name, hasRemoteParent);
    return new FakeSpan(
        spanContext,
        generateSpanOptions(spanContext),
        name,
        parentSpan,
        remoteParentSpanContext,
        parentLinks);
  }

  private SpanContext generateSpanContext(
      @Nullable SpanContext parentSpanContext, String name, boolean hasRemoteParent) {
    TraceId traceId;
    SpanId spanId = SpanId.generateRandomId(random);
    TraceOptions.Builder traceOptionsBuilder;
    if (parentSpanContext == null || !parentSpanContext.isValid()) {
      // New root span.
      traceId = TraceId.generateRandomId(random);
      traceOptionsBuilder = TraceOptions.builder();
      // This is a root span so no remote or local parent.
      hasRemoteParent = false;
    } else {
      // New child span.
      traceId = parentSpanContext.getTraceId();
      traceOptionsBuilder = TraceOptions.builder(parentSpanContext.getTraceOptions());
    }
    if (sampler != null
        && sampler.shouldSample(
            parentSpanContext, hasRemoteParent, traceId, spanId, name, parentLinks)) {
      traceOptionsBuilder.setIsSampled();
    }
    return SpanContext.create(traceId, spanId, traceOptionsBuilder.build());
  }

  private EnumSet<Options> generateSpanOptions(SpanContext spanContext) {
    return (spanContext.getTraceOptions().isSampled() || Boolean.TRUE.equals(recordEvents))
        ? EnumSet.of(Options.RECORD_EVENTS)
        : EnumSet.noneOf(Options.class);
  }

  private FakeSpanBuilder(
      String name,
      @Nullable Span parentSpan,
      @Nullable SpanContext remoteParentSpanContext,
      @Nullable SpanContext parentSpanContext,
      boolean hasRemoteParent) {
    this.name = checkNotNull(name, "name");
    this.parentSpan = parentSpan;
    this.remoteParentSpanContext = remoteParentSpanContext;
    this.parentSpanContext = parentSpanContext;
    this.hasRemoteParent = hasRemoteParent;
  }
}
