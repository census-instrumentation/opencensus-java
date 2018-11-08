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

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.trace.Sampler;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.propagation.TextFormat;
import io.opencensus.trace.samplers.Samplers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link TraceConfigOptions}. */
@RunWith(JUnit4.class)
public class TraceConfigOptionsTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testDefaultOptions() {
    assertThat(TraceConfigOptions.getOptions()).isNotNull();
    assertThat(TraceConfigOptions.getOptions().getExtractor())
        .isInstanceOf(OcHttpServletExtractor.class);
    assertThat(TraceConfigOptions.getOptions().getPropagator()).isNotNull();
    assertThat(TraceConfigOptions.getOptions().getSampler()).isNotNull();
  }

  @Test
  public void testNonDefaultOptions() {
    TraceConfigOptions options = TraceConfigOptions.getOptions();
    Sampler sampler = Samplers.alwaysSample();
    TextFormat propagator = Tracing.getPropagationComponent().getB3Format();
    options.setSampler(sampler).setPropagator(propagator).setPublicEndpoint(true);
    assertThat(TraceConfigOptions.getOptions().getSampler()).isEqualTo(sampler);
    assertThat(TraceConfigOptions.getOptions().getPropagator()).isEqualTo(propagator);
    assertThat(TraceConfigOptions.getOptions().isPublicEndpoint()).isEqualTo(true);

    options.setPublicEndpoint(false);
    assertThat(TraceConfigOptions.getOptions().isPublicEndpoint()).isEqualTo(false);
  }

  @Test
  public void testNullOptions() {
    TraceConfigOptions options = TraceConfigOptions.getOptions();

    {
      thrown.expect(NullPointerException.class);
      options.setSampler(null);
    }

    {
      thrown.expect(NullPointerException.class);
      options.setPropagator(null);
    }

    {
      thrown.expect(NullPointerException.class);
      options.setPublicEndpoint(null);
    }
  }
}
