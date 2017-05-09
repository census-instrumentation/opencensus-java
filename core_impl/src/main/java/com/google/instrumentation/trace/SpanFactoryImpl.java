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

import com.google.instrumentation.common.Clock;
import com.google.instrumentation.trace.Link.Type;
import com.google.instrumentation.trace.Span.Options;
import com.google.instrumentation.trace.TraceConfig.TraceParams;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;

/** Implementation of the {@link SpanFactory}. */
final class SpanFactoryImpl extends SpanFactory {
  private final RandomHandler randomHandler;
  private final SpanImpl.StartEndHandler startEndHandler;
  private final Clock clock;
  private final TraceConfig traceConfig;

  @Override
  protected Span startSpan(@Nullable Span parent, String name, StartSpanOptions options) {
    SpanContext parentContext = null;
    TimestampConverter timestampConverter = null;
    if (parent != null) {
      parentContext = parent.getContext();
      // Pass the timestamp converter from the parent to ensure that the recorded events are in
      // the right order. Implementation uses System.nanoTime() which is monotonically increasing.
      if (parent instanceof SpanImpl) {
        timestampConverter = ((SpanImpl) parent).getTimestampConverter();
      }
    }
    return startSpanInternal(parentContext, false, name, options, timestampConverter);
  }

  @Override
  protected Span startSpanWithRemoteParent(
      @Nullable SpanContext remoteParent, String name, StartSpanOptions options) {
    return startSpanInternal(remoteParent, true, name, options, null);
  }

  Span startSpanInternal(
      @Nullable SpanContext parent,
      boolean hasRemoteParent,
      String name,
      StartSpanOptions startSpanOptions,
      @Nullable TimestampConverter timestampConverter) {
    TraceParams activeTraceParams = traceConfig.getActiveTraceParams();
    Random random = randomHandler.current();
    TraceId traceId;
    SpanId spanId = SpanId.generateRandomId(random);
    SpanId parentSpanId = null;
    TraceOptions.Builder traceOptionsBuilder;
    if (parent == null || !parent.isValid()) {
      // New root span.
      traceId = TraceId.generateRandomId(random);
      traceOptionsBuilder = TraceOptions.builder();
      // This is a root span so no remote or local parent.
      hasRemoteParent = false;
    } else {
      // New child span.
      traceId = parent.getTraceId();
      parentSpanId = parent.getSpanId();
      traceOptionsBuilder = TraceOptions.builder(parent.getTraceOptions());
    }
    Sampler sampler =
        startSpanOptions.getSampler() != null
            ? startSpanOptions.getSampler()
            : activeTraceParams.getSampler();
    if (sampler.shouldSample(
        parent, hasRemoteParent, traceId, spanId, name, startSpanOptions.getParentLinks())) {
      traceOptionsBuilder.setIsSampled();
    }
    TraceOptions traceOptions = traceOptionsBuilder.build();
    EnumSet<Span.Options> spanOptions = EnumSet.noneOf(Options.class);
    if (traceOptions.isSampled()
        || (startSpanOptions.getRecordEvents() != null && startSpanOptions.getRecordEvents())) {
      spanOptions.add(Options.RECORD_EVENTS);
    }
    Span span =
        SpanImpl.startSpan(
            SpanContext.create(traceId, spanId, traceOptions),
            spanOptions,
            name,
            parentSpanId,
            hasRemoteParent,
            startEndHandler,
            timestampConverter,
            clock);
    linkSpans(span, startSpanOptions.getParentLinks());
    return span;
  }

  private static void linkSpans(Span span, List<Span> parentLinks) {
    if (!parentLinks.isEmpty()) {
      Link childLink = Link.fromSpanContext(span.getContext(), Type.CHILD);
      for (Span linkedSpan : parentLinks) {
        linkedSpan.addLink(childLink);
        span.addLink(Link.fromSpanContext(linkedSpan.getContext(), Type.PARENT));
      }
    }
  }

  SpanFactoryImpl(
      RandomHandler randomHandler,
      SpanImpl.StartEndHandler startEndHandler,
      Clock clock,
      TraceConfig traceConfig) {
    this.randomHandler = checkNotNull(randomHandler, "randomHandler");
    this.startEndHandler = checkNotNull(startEndHandler, "startEndHandler");
    this.clock = checkNotNull(clock, "clock");
    this.traceConfig = checkNotNull(traceConfig, "traceConfig");
  }
}
