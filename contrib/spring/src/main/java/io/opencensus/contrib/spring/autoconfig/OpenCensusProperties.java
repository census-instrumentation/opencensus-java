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

package io.opencensus.contrib.spring.autoconfig;

import static io.opencensus.contrib.spring.autoconfig.OpenCensusProperties.Trace.Propagation.TRACE_PROPAGATION_TRACE_CONTEXT;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Opencensus settings.
 *
 * @since 0.23.0
 */
@ConfigurationProperties("opencensus.spring")
public class OpenCensusProperties {

  private boolean enabled = true;
  private Trace trace = new Trace();

  public boolean isEnabled() {
    return this.enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public Trace getTrace() {
    return this.trace;
  }

  public void setTrace(Trace trace) {
    this.trace = trace;
  }

  /**
   * Trace properties.
   *
   * @since 0.23
   */
  public static final class Trace {

    public enum Propagation {
      /**
       * Specifies Trace Context format for span context propagation.
       *
       * @since 0.23
       */
      TRACE_PROPAGATION_TRACE_CONTEXT,

      /**
       * Specifies B3 format for span context propagation.
       *
       * @since 0.23
       */
      TRACE_PROPAGATION_B3,
    }

    private Propagation propagation = TRACE_PROPAGATION_TRACE_CONTEXT;
    private boolean publicEndpoint = false;

    public Propagation getPropagation() {
      return propagation;
    }

    public void setPropagation(Propagation propagation) {
      this.propagation = propagation;
    }

    public boolean isPublicEndpoint() {
      return publicEndpoint;
    }

    public void setPublicEndpoint(boolean publicEndpoint) {
      this.publicEndpoint = publicEndpoint;
    }
  }
}
