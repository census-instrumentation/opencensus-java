/*
 * Copyright 2018, OpenCensus Authors
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

package io.opencensus.contrib.spring.sleuth.v1x;

import io.grpc.Context;
import io.opencensus.common.ExperimentalApi;
import io.opencensus.trace.unsafe.ContextUtils;
import org.apache.commons.logging.Log;
import org.springframework.cloud.sleuth.Span;
import org.springframework.core.NamedThreadLocal;

/*>>>
  import org.checkerframework.checker.nullness.qual.Nullable;
*/

/** Inspired by the Sleuth's {@code SpanContextHolder}. */
@ExperimentalApi
final class OpenCensusSleuthSpanContextHolder {
  private static final Log log =
      org.apache.commons.logging.LogFactory.getLog(OpenCensusSleuthSpanContextHolder.class);
  private static final ThreadLocal</*@Nullable*/ SpanContext> CURRENT_SPAN =
      new NamedThreadLocal</*@Nullable*/ SpanContext>("Trace Context");

  // Get the current span out of the thread context.
  @javax.annotation.Nullable
  static Span getCurrentSpan() {
    SpanContext currentSpanContext = CURRENT_SPAN.get();
    return currentSpanContext != null ? currentSpanContext.span : null;
  }

  // Set the current span in the thread context
  static void setCurrentSpan(Span span) {
    if (log.isTraceEnabled()) {
      log.trace("Setting current span " + span);
    }
    push(span, /* autoClose= */ false);
  }

  // Remove all thread context relating to spans (useful for testing).
  // See close() for a better alternative in instrumetation
  static void removeCurrentSpan() {
    removeCurrentSpanInternal(null);
  }

  @SuppressWarnings("CheckReturnValue")
  @javax.annotation.Nullable
  private static SpanContext removeCurrentSpanInternal(
      @javax.annotation.Nullable SpanContext toRestore) {
    if (toRestore != null) {
      setSpanContextInternal(toRestore);
    } else {
      CURRENT_SPAN.remove();
      // This is a big hack and can cause other data in the io.grpc.Context to be lost. But
      // Spring 1.5 does not use io.grpc.Context and because the framework does not accept any
      // gRPC context, the context will always be ROOT anyway.
      Context.ROOT.attach();
    }
    return toRestore;
  }

  // Check if there is already a span in the current thread.
  static boolean isTracing() {
    return CURRENT_SPAN.get() != null;
  }

  // Close the current span and all parents that can be auto closed. On every iteration a function
  // will be applied on the closed Span.
  static void close(SpanFunction spanFunction) {
    SpanContext current = CURRENT_SPAN.get();
    while (current != null) {
      spanFunction.apply(current.span);
      current = removeCurrentSpanInternal(current.parent);
      if (current == null || !current.autoClose) {
        return;
      }
    }
  }

  // Close the current span and all parents that can be auto closed.
  static void close() {
    close(NO_OP_FUNCTION);
  }

  /**
   * Push a span into the thread context, with the option to have it auto close if any child spans
   * are themselves closed. Use autoClose=true if you start a new span with a parent that wasn't
   * already in thread context.
   */
  static void push(Span span, boolean autoClose) {
    if (isCurrent(span)) {
      return;
    }
    setSpanContextInternal(new SpanContext(span, autoClose));
  }

  interface SpanFunction {
    void apply(Span span);
  }

  private static final SpanFunction NO_OP_FUNCTION =
      new SpanFunction() {
        @Override
        public void apply(Span span) {}
      };

  @SuppressWarnings("CheckReturnValue")
  private static void setSpanContextInternal(SpanContext spanContext) {
    CURRENT_SPAN.set(spanContext);
    spanContext.ocCurrentContext.attach();
  }

  private static boolean isCurrent(Span span) {
    if (span == null) {
      return false;
    }
    SpanContext currentSpanContext = CURRENT_SPAN.get();
    return currentSpanContext != null && span.equals(currentSpanContext.span);
  }

  private static class SpanContext {
    final Span span;
    final boolean autoClose;
    @javax.annotation.Nullable final SpanContext parent;
    final OpenCensusSleuthSpan ocSpan;
    final Context ocCurrentContext;

    private SpanContext(Span span, boolean autoClose) {
      this.span = span;
      this.autoClose = autoClose;
      this.parent = CURRENT_SPAN.get();
      this.ocSpan = new OpenCensusSleuthSpan(span);
      this.ocCurrentContext =
          Context.current().withValue(ContextUtils.CONTEXT_SPAN_KEY, this.ocSpan);
    }
  }

  private OpenCensusSleuthSpanContextHolder() {}
}
