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

import io.opencensus.trace.Span;
import io.opencensus.trace.Span.Options;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.base.SpanId;
import io.opencensus.trace.base.StartSpanOptions;
import io.opencensus.trace.base.TraceId;
import io.opencensus.trace.base.TraceOptions;
import io.opencensus.trace.internal.SpanFactory;
import java.util.EnumSet;
import java.util.Random;
import javax.annotation.Nullable;

/**
 * A {@link Tracer} that allows users to test instrumented code.
 */
public final class FakeTracer extends Tracer {

  /** Constructs a new {@code FakeTracer}. */
  public FakeTracer() {
    super(new FakeSpanFactory());
  }

  private static class FakeSpanFactory extends SpanFactory {
    Random random = new Random();

    @Override
    public FakeSpan startSpan(@Nullable Span parent, String name, StartSpanOptions options) {
      SpanContext parentSpanContext =
          parent != null && parent.getContext().isValid() ? parent.getContext() : null;
      SpanContext spanContext = generateSpanContext(parentSpanContext, options, name, false);
      return new FakeSpan(
          parentSpanContext, spanContext, generateSpanOptions(spanContext, options), name, options);
    }

    @Override
    public FakeSpan startSpanWithRemoteParent(
        @Nullable SpanContext remoteParent, String name, StartSpanOptions options) {
      SpanContext parentSpanContext =
          remoteParent != null && remoteParent.isValid() ? remoteParent : null;
      SpanContext spanContext = generateSpanContext(parentSpanContext, options, name, true);
      return new FakeSpan(
          parentSpanContext, spanContext, generateSpanOptions(spanContext, options), name, options);
    }

    private SpanContext generateSpanContext(
        @Nullable SpanContext parentSpanContext,
        StartSpanOptions startSpanOptions,
        String name,
        boolean hasRemoteParent) {
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
      if (startSpanOptions.getSampler() != null
          && startSpanOptions
              .getSampler()
              .shouldSample(
                  parentSpanContext,
                  hasRemoteParent,
                  traceId,
                  spanId,
                  name,
                  startSpanOptions.getParentLinks())) {
        traceOptionsBuilder.setIsSampled();
      }
      return SpanContext.create(traceId, spanId, traceOptionsBuilder.build());
    }

    private static EnumSet<Options> generateSpanOptions(
        SpanContext spanContext, StartSpanOptions startSpanOptions) {
      return (spanContext.getTraceOptions().isSampled()
              || Boolean.TRUE.equals(startSpanOptions.getRecordEvents()))
          ? EnumSet.of(Options.RECORD_EVENTS)
          : EnumSet.noneOf(Options.class);
    }
  }
}
