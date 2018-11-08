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

package io.opencensus.contrib.http.servlet;

import static com.google.common.base.Preconditions.checkNotNull;

import io.opencensus.common.ExperimentalApi;
import io.opencensus.trace.Sampler;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.propagation.TextFormat;
import io.opencensus.trace.samplers.Samplers;

/**
 * This class provide configuring options for Tracing.
 *
 * @since 0.18
 */
@ExperimentalApi
public class TraceConfigOptions {
  private TextFormat propagator = Tracing.getPropagationComponent().getB3Format();
  private static final TraceConfigOptions options = new TraceConfigOptions();
  private Sampler sampler = Samplers.probabilitySampler(0.0001);
  private Boolean publicEndpoint = true;

  private static final OcHttpServletExtractor extractor = new OcHttpServletExtractor();

  private TraceConfigOptions() {}

  /**
   * Returns an instance of {@link TraceConfigOptions}.
   *
   * @return {@link TraceConfigOptions}
   */
  public static TraceConfigOptions getOptions() {

    return options;
  }

  /**
   * Sets the {@code TraceConfigOptions#publicEndpoint} property. Set to true if tracing a public
   * endpoint. Otherwise, set to false.
   *
   * @param publicEndpoint {@code TraceConfigOptions#publicEndpoint}
   * @return {@link TraceConfigOptions}
   */
  public TraceConfigOptions setPublicEndpoint(Boolean publicEndpoint) {
    checkNotNull(publicEndpoint, "publicEndpoint");
    this.publicEndpoint = publicEndpoint;
    return this;
  }

  /**
   * Returns the value of {@code TraceConfigOptions#publicEndpoint}.
   *
   * @return true if the endpoint is pubic.
   * @since 0.18
   */
  Boolean isPublicEndpoint() {
    return publicEndpoint;
  }

  /**
   * This method sets propagator {@link TextFormat} to use to extract {@link
   * io.opencensus.trace.SpanContext} from http request.
   *
   * @param propagator {@link TextFormat}
   * @return itself {@link TraceConfigOptions}
   * @since 0.18
   */
  public TraceConfigOptions setPropagator(TextFormat propagator) {
    checkNotNull(propagator, "propagator");
    this.propagator = propagator;
    return this;
  }

  /**
   * This method sets {@link Sampler} used for sampling http request for tracing.
   *
   * @param sampler {@link Sampler}
   * @return itself {@link TraceConfigOptions}
   * @since 0.18
   */
  public TraceConfigOptions setSampler(Sampler sampler) {
    checkNotNull(sampler, "sampler");
    this.sampler = sampler;
    return this;
  }

  TextFormat getPropagator() {
    return propagator;
  }

  Sampler getSampler() {
    return sampler;
  }

  OcHttpServletExtractor getExtractor() {
    return extractor;
  }
}
