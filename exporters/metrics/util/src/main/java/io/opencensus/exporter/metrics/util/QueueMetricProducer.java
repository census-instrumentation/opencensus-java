/*
 * Copyright 2019, OpenCensus Authors
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

package io.opencensus.exporter.metrics.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.auto.value.AutoValue;
import com.google.common.collect.EvictingQueue;
import io.opencensus.common.Scope;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricProducer;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Sampler;
import io.opencensus.trace.Span;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.samplers.Samplers;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.Immutable;

/**
 * Wrapper of {@link MetricProducer} which allows metrics to be pushed and buffered.
 *
 * @since 0.20
 */
public final class QueueMetricProducer extends MetricProducer {

  private static final Object monitor = new Object();
  private static final Tracer tracer = Tracing.getTracer();
  private static final Sampler probabilitySampler = Samplers.probabilitySampler(0.0001);

  private static final String DEFAULT_SPAN_NAME = "PushMetrics";
  private static final int DEFAULT_BUFFER_SIZE = 32;
  private static final String ATTRIBUTE_KEY_DROPPED_METRICS = "DroppedMetrics";

  @GuardedBy("monitor")
  private final EvictingQueue<Metric> bufferedMetrics;

  private final String spanName;

  private QueueMetricProducer(int bufferSize, String spanName) {
    synchronized (monitor) {
      bufferedMetrics = EvictingQueue.<Metric>create(bufferSize);
    }
    this.spanName = spanName;
  }

  /**
   *
   * @param options the options for {@link QueueMetricProducer}.
   * @return a {@code QueueMetricProducer}.
   * @since 0.20
   */
  public static QueueMetricProducer create(Options options) {
    checkNotNull(options, "options");
    checkArgument(options.getBufferSize() > 0, "buffer size should be positive.");
    return new QueueMetricProducer(options.getBufferSize(), options.getSpanName());
  }

  /**
   * Pushes {@link Metric}s to this {@link QueueMetricProducer}.
   *
   * <p>When buffer of this {@link QueueMetricProducer} is full, the oldest {@link Metric}s will be
   * dropped.
   *
   * @param metrics {@code Metrics} to be added to this {@code QueueMetricProducer}.
   * @since 0.20
   */
  public void pushMetrics(Collection<Metric> metrics) {
    synchronized (monitor) {
      Span span =
          tracer
              .spanBuilder(spanName)
              .setRecordEvents(true)
              .setSampler(probabilitySampler)
              .startSpan();
      Scope scope = tracer.withSpan(span);
      try {
        if (metrics.size() > bufferedMetrics.remainingCapacity()) {
          AttributeValue attributeValue =
              AttributeValue.longAttributeValue(
                  metrics.size() - bufferedMetrics.remainingCapacity());
          span.putAttribute(ATTRIBUTE_KEY_DROPPED_METRICS, attributeValue);
        }
        bufferedMetrics.addAll(metrics);
      } finally {
        scope.close();
        span.end();
      }
    }
  }

  @Override
  public Collection<Metric> getMetrics() {
    Queue<Metric> metricsToExport;
    synchronized (monitor) {
      metricsToExport = new LinkedList<>(bufferedMetrics);
      bufferedMetrics.clear();
    }
    return metricsToExport;
  }

  /**
   * Options for {@link QueueMetricProducer}.
   *
   * @since 0.20
   */
  @AutoValue
  @Immutable
  public abstract static class Options {

    Options() {}

    /**
     * Returns the span name for the {@link Span} created when metrics are pushed.
     *
     * @return the span name for the {@link Span} created when metrics are pushed.
     * @since 0.20
     */
    public abstract String getSpanName();

    /**
     * Returns the buffer size for the {@link QueueMetricProducer}.
     *
     * @return the buffer size for the {@code QueueMetricProducer}.
     * @since 0.20
     */
    public abstract int getBufferSize();

    /**
     * Returns a new {@link Builder}.
     *
     * @return a {@code Builder}.
     * @since 0.20
     */
    public static Options.Builder builder() {
      return new AutoValue_QueueMetricProducer_Options.Builder()
          .setSpanName(DEFAULT_SPAN_NAME)
          .setBufferSize(DEFAULT_BUFFER_SIZE);
    }

    /**
     * Builder for {@link Options}.
     *
     * @since 0.20
     */
    @AutoValue.Builder
    public abstract static class Builder {
      /**
       * Sets the span name for the {@link Span} created when metrics are pushed.
       *
       * @param spanName the span name for the {@link Span} created when metrics are pushed.
       * @return this.
       * @since 0.20
       */
      public abstract Builder setSpanName(String spanName);

      /**
       * Sets the buffer size to be used by the {@link QueueMetricProducer}.
       *
       * @param bufferSize the buffer size.
       * @return this.
       * @since 0.20
       */
      public abstract Builder setBufferSize(int bufferSize);

      /**
       * Builds a new {@link Options} with current settings.
       *
       * @return a {@code Options}.
       * @since 0.20
       */
      public abstract Options build();
    }
  }
}
