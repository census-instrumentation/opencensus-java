/*
 * Copyright 2017, OpenCensus Authors
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

package io.opencensus.trace.config;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.value.AutoValue;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.Link;
import io.opencensus.trace.MessageEvent;
import io.opencensus.trace.Sampler;
import io.opencensus.trace.Span;
import io.opencensus.trace.samplers.Samplers;
import javax.annotation.concurrent.Immutable;

/**
 * Class that holds global trace parameters.
 *
 * @since 0.5
 */
@AutoValue
@Immutable
public abstract class TraceParams {
  // These values are the default values for all the global parameters.
  private static final double DEFAULT_PROBABILITY = 1e-4;
  private static final Sampler DEFAULT_SAMPLER = Samplers.probabilitySampler(DEFAULT_PROBABILITY);
  private static final int DEFAULT_SPAN_MAX_NUM_ATTRIBUTES = 32;
  private static final int DEFAULT_SPAN_MAX_NUM_ANNOTATIONS = 32;
  private static final int DEFAULT_SPAN_MAX_NUM_MESSAGE_EVENTS = 128;
  private static final int DEFAULT_SPAN_MAX_NUM_LINKS = 128;

  /**
   * Default {@code TraceParams}.
   *
   * @since 0.5
   */
  public static final TraceParams DEFAULT =
      TraceParams.builder()
          .setSampler(DEFAULT_SAMPLER)
          .setMaxNumberOfAttributes(DEFAULT_SPAN_MAX_NUM_ATTRIBUTES)
          .setMaxNumberOfAnnotations(DEFAULT_SPAN_MAX_NUM_ANNOTATIONS)
          .setMaxNumberOfMessageEvents(DEFAULT_SPAN_MAX_NUM_MESSAGE_EVENTS)
          .setMaxNumberOfLinks(DEFAULT_SPAN_MAX_NUM_LINKS)
          .build();

  /**
   * Returns the global default {@code Sampler}. Used if no {@code Sampler} is provided in {@link
   * io.opencensus.trace.SpanBuilder#setSampler(Sampler)}.
   *
   * @return the global default {@code Sampler}.
   * @since 0.5
   */
  public abstract Sampler getSampler();

  /**
   * Returns the global default max number of attributes per {@link Span}.
   *
   * @return the global default max number of attributes per {@link Span}.
   * @since 0.5
   */
  public abstract int getMaxNumberOfAttributes();

  /**
   * Returns the global default max number of {@link Annotation} events per {@link Span}.
   *
   * @return the global default max number of {@code Annotation} events per {@code Span}.
   * @since 0.5
   */
  public abstract int getMaxNumberOfAnnotations();

  /**
   * Returns the global default max number of {@link MessageEvent} events per {@link Span}.
   *
   * @return the global default max number of {@code MessageEvent} events per {@code Span}.
   * @since 0.12
   */
  public abstract int getMaxNumberOfMessageEvents();

  /**
   * Returns the global default max number of {@link io.opencensus.trace.NetworkEvent} events per
   * {@link Span}.
   *
   * @return the global default max number of {@code NetworkEvent} events per {@code Span}.
   * @deprecated Use {@link getMaxNumberOfMessageEvents}.
   * @since 0.5
   */
  @Deprecated
  public int getMaxNumberOfNetworkEvents() {
    return getMaxNumberOfMessageEvents();
  }

  /**
   * Returns the global default max number of {@link Link} entries per {@link Span}.
   *
   * @return the global default max number of {@code Link} entries per {@code Span}.
   * @since 0.5
   */
  public abstract int getMaxNumberOfLinks();

  private static Builder builder() {
    return new AutoValue_TraceParams.Builder();
  }

  /**
   * Returns a {@link Builder} initialized to the same property values as the current instance.
   *
   * @return a {@link Builder} initialized to the same property values as the current instance.
   * @since 0.5
   */
  public abstract Builder toBuilder();

  /**
   * A {@code Builder} class for {@link TraceParams}.
   *
   * @since 0.5
   */
  @AutoValue.Builder
  public abstract static class Builder {

    /**
     * Sets the global default {@code Sampler}. It must be not {@code null} otherwise {@link
     * #build()} will throw an exception.
     *
     * @param sampler the global default {@code Sampler}.
     * @return this.
     * @since 0.5
     */
    public abstract Builder setSampler(Sampler sampler);

    /**
     * Sets the global default max number of attributes per {@link Span}.
     *
     * @param maxNumberOfAttributes the global default max number of attributes per {@link Span}. It
     *     must be positive otherwise {@link #build()} will throw an exception.
     * @return this.
     * @since 0.5
     */
    public abstract Builder setMaxNumberOfAttributes(int maxNumberOfAttributes);

    /**
     * Sets the global default max number of {@link Annotation} events per {@link Span}.
     *
     * @param maxNumberOfAnnotations the global default max number of {@link Annotation} events per
     *     {@link Span}. It must be positive otherwise {@link #build()} will throw an exception.
     * @return this.
     * @since 0.5
     */
    public abstract Builder setMaxNumberOfAnnotations(int maxNumberOfAnnotations);

    /**
     * Sets the global default max number of {@link MessageEvent} events per {@link Span}.
     *
     * @param maxNumberOfMessageEvents the global default max number of {@link MessageEvent} events
     *     per {@link Span}. It must be positive otherwise {@link #build()} will throw an exception.
     * @since 0.12
     * @return this.
     */
    public abstract Builder setMaxNumberOfMessageEvents(int maxNumberOfMessageEvents);

    /**
     * Sets the global default max number of {@link io.opencensus.trace.NetworkEvent} events per
     * {@link Span}.
     *
     * @param maxNumberOfNetworkEvents the global default max number of {@link
     *     io.opencensus.trace.NetworkEvent} events per {@link Span}. It must be positive otherwise
     *     {@link #build()} will throw an exception.
     * @return this.
     * @deprecated Use {@link setMaxNumberOfMessageEvents}.
     * @since 0.5
     */
    @Deprecated
    public Builder setMaxNumberOfNetworkEvents(int maxNumberOfNetworkEvents) {
      return setMaxNumberOfMessageEvents(maxNumberOfNetworkEvents);
    }

    /**
     * Sets the global default max number of {@link Link} entries per {@link Span}.
     *
     * @param maxNumberOfLinks the global default max number of {@link Link} entries per {@link
     *     Span}. It must be positive otherwise {@link #build()} will throw an exception.
     * @return this.
     * @since 0.5
     */
    public abstract Builder setMaxNumberOfLinks(int maxNumberOfLinks);

    abstract TraceParams autoBuild();

    /**
     * Builds and returns a {@code TraceParams} with the desired values.
     *
     * @return a {@code TraceParams} with the desired values.
     * @throws NullPointerException if the sampler is {@code null}.
     * @throws IllegalArgumentException if any of the max numbers are not positive.
     * @since 0.5
     */
    public TraceParams build() {
      TraceParams traceParams = autoBuild();
      checkArgument(traceParams.getMaxNumberOfAttributes() > 0, "maxNumberOfAttributes");
      checkArgument(traceParams.getMaxNumberOfAnnotations() > 0, "maxNumberOfAnnotations");
      checkArgument(traceParams.getMaxNumberOfMessageEvents() > 0, "maxNumberOfMessageEvents");
      checkArgument(traceParams.getMaxNumberOfLinks() > 0, "maxNumberOfLinks");
      return traceParams;
    }
  }
}
