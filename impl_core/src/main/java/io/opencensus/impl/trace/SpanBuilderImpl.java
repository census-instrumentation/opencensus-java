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

package io.opencensus.impl.trace;

import static com.google.common.base.Preconditions.checkNotNull;

import io.opencensus.common.Clock;
import io.opencensus.impl.internal.TimestampConverter;
import io.opencensus.impl.trace.internal.RandomHandler;
import io.opencensus.trace.Link;
import io.opencensus.trace.Link.Type;
import io.opencensus.trace.Sampler;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanBuilder;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.config.TraceParams;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;

/** Implementation of the {@link SpanBuilder}. */
final class SpanBuilderImpl extends SpanBuilder {
  private final Options options;

  private final String name;
  private final Span parent;
  private final SpanContext remoteParentSpanContext;
  private Sampler sampler;
  private List<Span> parentLinks = Collections.<Span>emptyList();
  private Boolean recordEvents;

  private SpanImpl startSpanInternal(
      @Nullable SpanContext parent,
      @Nullable Boolean hasRemoteParent,
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
      hasRemoteParent = null;
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
    SpanImpl span =
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
      Link childLink = Link.fromSpanContext(span.getContext(), Type.CHILD_LINKED_SPAN);
      for (Span linkedSpan : parentLinks) {
        linkedSpan.addLink(childLink);
        span.addLink(Link.fromSpanContext(linkedSpan.getContext(), Type.PARENT_LINKED_SPAN));
      }
    }
  }

  static SpanBuilderImpl createWithParent(String spanName, @Nullable Span parent, Options options) {
    return new SpanBuilderImpl(spanName, null, parent, options);
  }

  static SpanBuilderImpl createWithRemoteParent(
      String spanName, @Nullable SpanContext remoteParentSpanContext, Options options) {
    return new SpanBuilderImpl(spanName, remoteParentSpanContext, null, options);
  }

  private SpanBuilderImpl(
      String name,
      @Nullable SpanContext remoteParentSpanContext,
      @Nullable Span parent,
      Options options) {
    this.name = checkNotNull(name, "name");
    this.parent = parent;
    this.remoteParentSpanContext = remoteParentSpanContext;
    this.options = options;
  }

  @Override
  public SpanImpl startSpan() {
    SpanContext parentContext = remoteParentSpanContext;
    Boolean hasRemoteParent = Boolean.TRUE;
    TimestampConverter timestampConverter = null;
    if (remoteParentSpanContext == null) {
      // This is not a child of a remote Span. Get the parent SpanContext from the parent Span if
      // any.
      Span parent = this.parent;
      hasRemoteParent = Boolean.FALSE;
      if (parent != null) {
        parentContext = parent.getContext();
        // Pass the timestamp converter from the parent to ensure that the recorded events are in
        // the right order. Implementation uses System.nanoTime() which is monotonically increasing.
        if (parent instanceof SpanImpl) {
          timestampConverter = ((SpanImpl) parent).getTimestampConverter();
        }
      } else {
        hasRemoteParent = null;
      }
    }
    return startSpanInternal(
        parentContext,
        hasRemoteParent,
        name,
        sampler,
        parentLinks,
        recordEvents,
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

  @Override
  public SpanBuilderImpl setSampler(Sampler sampler) {
    this.sampler = checkNotNull(sampler, "sampler");
    return this;
  }

  @Override
  public SpanBuilderImpl setParentLinks(List<Span> parentLinks) {
    this.parentLinks = checkNotNull(parentLinks, "parentLinks");
    return this;
  }

  @Override
  public SpanBuilderImpl setRecordEvents(boolean recordEvents) {
    this.recordEvents = recordEvents;
    return this;
  }
}
