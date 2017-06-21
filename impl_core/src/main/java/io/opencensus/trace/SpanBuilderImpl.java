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

import static com.google.common.base.Preconditions.checkNotNull;

import io.opencensus.common.Clock;
import io.opencensus.internal.TimestampConverter;
import io.opencensus.trace.base.Link;
import io.opencensus.trace.base.Link.Type;
import io.opencensus.trace.base.Sampler;
import io.opencensus.trace.base.SpanId;
import io.opencensus.trace.base.TraceId;
import io.opencensus.trace.base.TraceOptions;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.internal.RandomHandler;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;

/** Implementation of the {@link SpanBuilder}. */
final class SpanBuilderImpl extends SpanBuilder {
  private final Options options;

  Span startSpanInternal(
      @Nullable SpanContext parent,
      boolean hasRemoteParent,
      String name,
      Sampler sampler,
      List<Span> parentLinks,
      Boolean recordEvents,
      @Nullable TimestampConverter timestampConverter) {
    TraceParams activeTraceParams = options.traceConfig.getActiveTraceParams();
    Random random = options.randomHandler.current();
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
    if (sampler == null) {
      sampler = activeTraceParams.getSampler();
    }
    if (sampler.shouldSample(parent, hasRemoteParent, traceId, spanId, name, parentLinks)) {
      traceOptionsBuilder.setIsSampled();
    }
    TraceOptions traceOptions = traceOptionsBuilder.build();
    EnumSet<Span.Options> spanOptions = EnumSet.noneOf(Span.Options.class);
    if (traceOptions.isSampled() || Boolean.TRUE.equals(recordEvents)) {
      spanOptions.add(Span.Options.RECORD_EVENTS);
    }
    Span span =
        SpanImpl.startSpan(
            SpanContext.create(traceId, spanId, traceOptions),
            spanOptions,
            name,
            parentSpanId,
            hasRemoteParent,
            activeTraceParams,
            options.startEndHandler,
            timestampConverter,
            options.clock);
    linkSpans(span, parentLinks);
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

  static SpanBuilder createBuilder(@Nullable Span parentSpan, String name, Options options) {
    return new SpanBuilderImpl(parentSpan, null, name, options);
  }

  static SpanBuilder createBuilderWithRemoteParent(
      SpanContext remoteParentSpanContext, String name, Options options) {
    return new SpanBuilderImpl(null, checkNotNull(remoteParentSpanContext,
        "remoteParentSpanContext"), name, options);
  }

  private SpanBuilderImpl(
      @Nullable Span parentSpan,
      @Nullable SpanContext remoteParentSpanContext,
      String name,
      Options options) {
    super(parentSpan, remoteParentSpanContext, name);
    this.options = options;
  }

  @Override
  public Span startSpan() {
    SpanContext parentContext = getRemoteParentSpanContext();
    boolean hasRemoteParent = parentContext != null;
    TimestampConverter timestampConverter = null;
    if (!hasRemoteParent) {
      // This is not a child of a remote Span. Get the parent SpanContext from the parent Span if
      // any.
      Span parent = getParentSpan();
      if (parent != null) {
        parentContext = parent.getContext();
        // Pass the timestamp converter from the parent to ensure that the recorded events are in
        // the right order. Implementation uses System.nanoTime() which is monotonically increasing.
        if (parent instanceof SpanImpl) {
          timestampConverter = ((SpanImpl) parent).getTimestampConverter();
        }
      }
    }
    return startSpanInternal(
        parentContext,
        hasRemoteParent,
        getName(),
        getSampler(),
        getParentLinks(),
        getRecordEvents(),
        timestampConverter);
  }

  static final class Options {
    private final RandomHandler randomHandler;
    private final SpanImpl.StartEndHandler startEndHandler;
    private final Clock clock;
    private final TraceConfig traceConfig;

    Options(
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
}
