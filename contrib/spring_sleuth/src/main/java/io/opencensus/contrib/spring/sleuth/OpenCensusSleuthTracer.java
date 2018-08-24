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

package io.opencensus.contrib.spring.sleuth;

import io.opencensus.common.ExperimentalApi;
import java.util.Random;
import java.util.concurrent.Callable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.sleuth.Sampler;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.SpanNamer;
import org.springframework.cloud.sleuth.SpanReporter;
import org.springframework.cloud.sleuth.TraceKeys;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.instrument.async.SpanContinuingTraceCallable;
import org.springframework.cloud.sleuth.instrument.async.SpanContinuingTraceRunnable;
import org.springframework.cloud.sleuth.log.SpanLogger;
import org.springframework.cloud.sleuth.util.ExceptionUtils;
import org.springframework.cloud.sleuth.util.SpanNameUtil;

/*>>>
  import org.checkerframework.checker.nullness.qual.Nullable;
*/

/**
 * Sleuth Tracer that keeps a synchronized OpenCensus Span. This class is based on Sleuth's {@code
 * DefaultTracer}.
 *
 * @since 0.16
 */
@ExperimentalApi
public class OpenCensusSleuthTracer implements Tracer {
  private static final Log log = LogFactory.getLog(OpenCensusSleuthTracer.class);
  private final Sampler defaultSampler;
  private final Random random;
  private final SpanNamer spanNamer;
  private final SpanLogger spanLogger;
  private final SpanReporter spanReporter;
  private final TraceKeys traceKeys;
  private final boolean traceId128;

  /** Basic constructor holding components for implementing Sleuth's {@link Tracer} interface. */
  public OpenCensusSleuthTracer(
      Sampler defaultSampler,
      Random random,
      SpanNamer spanNamer,
      SpanLogger spanLogger,
      SpanReporter spanReporter,
      TraceKeys traceKeys) {
    this(
        defaultSampler,
        random,
        spanNamer,
        spanLogger,
        spanReporter,
        traceKeys,
        /* traceId128= */ false);
  }

  /** Basic constructor holding components for implementing Sleuth's {@link Tracer} interface. */
  public OpenCensusSleuthTracer(
      Sampler defaultSampler,
      Random random,
      SpanNamer spanNamer,
      SpanLogger spanLogger,
      SpanReporter spanReporter,
      TraceKeys traceKeys,
      boolean traceId128) {
    this.defaultSampler = defaultSampler;
    this.random = random;
    this.spanNamer = spanNamer;
    this.spanLogger = spanLogger;
    this.spanReporter = spanReporter;
    this.traceId128 = traceId128;
    this.traceKeys = traceKeys != null ? traceKeys : new TraceKeys();
  }

  @Override
  @javax.annotation.Nullable
  public Span createSpan(String name, /*@Nullable*/ Span parent) {
    if (parent == null) {
      return createSpan(name);
    }
    return continueSpan(createChild(parent, name));
  }

  @Override
  @javax.annotation.Nullable
  public Span createSpan(String name) {
    return this.createSpan(name, this.defaultSampler);
  }

  @Override
  @javax.annotation.Nullable
  public Span createSpan(String name, /*@Nullable*/ Sampler sampler) {
    String shortenedName = SpanNameUtil.shorten(name);
    Span span;
    if (isTracing()) {
      span = createChild(getCurrentSpan(), shortenedName);
    } else {
      long id = createId();
      span =
          Span.builder()
              .name(shortenedName)
              .traceIdHigh(this.traceId128 ? createTraceIdHigh() : 0L)
              .traceId(id)
              .spanId(id)
              .build();
      if (sampler == null) {
        sampler = this.defaultSampler;
      }
      span = sampledSpan(span, sampler);
      this.spanLogger.logStartedSpan(null, span);
    }
    return continueSpan(span);
  }

  @Override
  @javax.annotation.Nullable
  public Span detach(/*@Nullable*/ Span span) {
    if (span == null) {
      return null;
    }
    Span current = OpenCensusSleuthSpanContextHolder.getCurrentSpan();
    if (current == null) {
      if (log.isTraceEnabled()) {
        log.trace(
            "Span in the context is null so something has already detached the span. "
                + "Won't do anything about it");
      }
      return null;
    }
    if (!span.equals(current)) {
      ExceptionUtils.warn(
          "Tried to detach trace span but "
              + "it is not the current span: "
              + span
              + ". You may have forgotten to close or detach "
              + current);
    } else {
      OpenCensusSleuthSpanContextHolder.removeCurrentSpan();
    }
    return span.getSavedSpan();
  }

  @Override
  @javax.annotation.Nullable
  public Span close(/*@Nullable*/ Span span) {
    if (span == null) {
      return null;
    }
    final Span savedSpan = span.getSavedSpan();
    Span current = OpenCensusSleuthSpanContextHolder.getCurrentSpan();
    if (current == null || !span.equals(current)) {
      ExceptionUtils.warn(
          "Tried to close span but it is not the current span: "
              + span
              + ".  You may have forgotten to close or detach "
              + current);
    } else {
      span.stop();
      if (savedSpan != null && span.getParents().contains(savedSpan.getSpanId())) {
        this.spanReporter.report(span);
        this.spanLogger.logStoppedSpan(savedSpan, span);
      } else {
        if (!span.isRemote()) {
          this.spanReporter.report(span);
          this.spanLogger.logStoppedSpan(null, span);
        }
      }
      OpenCensusSleuthSpanContextHolder.close(
          new OpenCensusSleuthSpanContextHolder.SpanFunction() {
            @Override
            public void apply(Span closedSpan) {
              // Note: hasn't this already been done?
              OpenCensusSleuthTracer.this.spanLogger.logStoppedSpan(savedSpan, closedSpan);
            }
          });
    }
    return savedSpan;
  }

  Span createChild(/*@Nullable*/ Span parent, String name) {
    String shortenedName = SpanNameUtil.shorten(name);
    long id = createId();
    if (parent == null) {
      Span span =
          Span.builder()
              .name(shortenedName)
              .traceIdHigh(this.traceId128 ? createTraceIdHigh() : 0L)
              .traceId(id)
              .spanId(id)
              .build();
      span = sampledSpan(span, this.defaultSampler);
      this.spanLogger.logStartedSpan(null, span);
      return span;
    } else {
      if (!isTracing()) {
        OpenCensusSleuthSpanContextHolder.push(parent, /* autoClose= */ true);
      }
      Span span =
          Span.builder()
              .name(shortenedName)
              .traceIdHigh(parent.getTraceIdHigh())
              .traceId(parent.getTraceId())
              .parent(parent.getSpanId())
              .spanId(id)
              .processId(parent.getProcessId())
              .savedSpan(parent)
              .exportable(parent.isExportable())
              .baggage(parent.getBaggage())
              .build();
      this.spanLogger.logStartedSpan(parent, span);
      return span;
    }
  }

  private static Span sampledSpan(Span span, Sampler sampler) {
    if (!sampler.isSampled(span)) {
      // Copy everything, except set exportable to false
      return Span.builder()
          .begin(span.getBegin())
          .traceIdHigh(span.getTraceIdHigh())
          .traceId(span.getTraceId())
          .spanId(span.getSpanId())
          .name(span.getName())
          .exportable(false)
          .build();
    }
    return span;
  }

  // Encodes a timestamp into the upper 32-bits, so that it can be converted to an Amazon trace ID.
  // For example, an Amazon trace ID is composed of the following:
  //  |-- 32 bits for epoch seconds -- | -- 96 bits for random data -- |
  //
  // To support this, Span#getTraceIdHigh() holds the epoch seconds and first 32 random bits: and
  // Span#getTraceId() holds the remaining 64 random bits.
  private long createTraceIdHigh() {
    long epochSeconds = System.currentTimeMillis() / 1000;
    int random = this.random.nextInt();
    return (epochSeconds & 0xffffffffL) << 32 | (random & 0xffffffffL);
  }

  private long createId() {
    return this.random.nextLong();
  }

  @Override
  @javax.annotation.Nullable
  public Span continueSpan(/*@Nullable*/ Span span) {
    if (span != null) {
      this.spanLogger.logContinuedSpan(span);
    } else {
      return null;
    }
    Span newSpan = createContinuedSpan(span, OpenCensusSleuthSpanContextHolder.getCurrentSpan());
    OpenCensusSleuthSpanContextHolder.setCurrentSpan(newSpan);
    return newSpan;
  }

  @SuppressWarnings("deprecation")
  private static Span createContinuedSpan(Span span, /*@Nullable*/ Span saved) {
    if (saved == null && span.getSavedSpan() != null) {
      saved = span.getSavedSpan();
    }
    return new Span(span, saved);
  }

  @Override
  @javax.annotation.Nullable
  public Span getCurrentSpan() {
    return OpenCensusSleuthSpanContextHolder.getCurrentSpan();
  }

  @Override
  public boolean isTracing() {
    return OpenCensusSleuthSpanContextHolder.isTracing();
  }

  @Override
  public void addTag(String key, String value) {
    Span s = getCurrentSpan();
    if (s != null && s.isExportable()) {
      s.tag(key, value);
    }
  }

  /**
   * Wrap the callable in a TraceCallable, if tracing.
   *
   * @return The callable provided, wrapped if tracing, 'callable' if not.
   */
  @Override
  public <V> Callable<V> wrap(Callable<V> callable) {
    if (isTracing()) {
      return new SpanContinuingTraceCallable<V>(this, this.traceKeys, this.spanNamer, callable);
    }
    return callable;
  }

  /**
   * Wrap the runnable in a TraceRunnable, if tracing.
   *
   * @return The runnable provided, wrapped if tracing, 'runnable' if not.
   */
  @Override
  public Runnable wrap(Runnable runnable) {
    if (isTracing()) {
      return new SpanContinuingTraceRunnable(this, this.traceKeys, this.spanNamer, runnable);
    }
    return runnable;
  }
}
