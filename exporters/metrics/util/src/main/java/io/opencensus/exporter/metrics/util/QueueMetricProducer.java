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
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricProducer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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

  private static final int DEFAULT_BUFFER_SIZE = 32;

  @GuardedBy("monitor")
  private final Queue<Metric> bufferedMetrics;

  private QueueMetricProducer(int bufferSize) {
    synchronized (monitor) {
      bufferedMetrics = EvictingQueue.<Metric>create(bufferSize);
    }
  }

  /**
   * Creates a new {@link QueueMetricProducer}.
   *
   * @param options the options for {@link QueueMetricProducer}.
   * @return a {@code QueueMetricProducer}.
   * @since 0.20
   */
  public static QueueMetricProducer create(Options options) {
    checkNotNull(options, "options");
    checkArgument(options.getBufferSize() > 0, "buffer size should be positive.");
    return new QueueMetricProducer(options.getBufferSize());
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
      bufferedMetrics.addAll(metrics);
    }
  }

  @Override
  public Collection<Metric> getMetrics() {
    List<Metric> metricsToExport;
    synchronized (monitor) {
      metricsToExport = new ArrayList<>(bufferedMetrics);
      bufferedMetrics.clear();
    }
    return Collections.unmodifiableList(metricsToExport);
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
      return new AutoValue_QueueMetricProducer_Options.Builder().setBufferSize(DEFAULT_BUFFER_SIZE);
    }

    /**
     * Builder for {@link Options}.
     *
     * @since 0.20
     */
    @AutoValue.Builder
    public abstract static class Builder {

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
