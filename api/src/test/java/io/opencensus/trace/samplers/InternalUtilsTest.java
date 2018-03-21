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

package io.opencensus.trace.samplers;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.trace.Sampler;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link InternalUtils}. */
@RunWith(JUnit4.class)
public final class InternalUtilsTest {

  private static final double EPSILON = 1e-7;

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void getProbabilityForProbabilitySampler() {
    Sampler probabilitySampler = Samplers.probabilitySampler(0.3);
    assertThat(InternalUtils.getProbability(probabilitySampler)).isWithin(EPSILON).of(0.3);
  }

  @Test
  public void throwIllegalArgumentExceptionForAlwaysSampler() {
    Sampler alwaysSampler = Samplers.alwaysSample();
    thrown.expect(IllegalArgumentException.class);
    InternalUtils.getProbability(alwaysSampler);
  }

  @Test
  public void throwIllegalArgumentExceptionForNeverSampler() {
    Sampler neverSampler = Samplers.neverSample();
    thrown.expect(IllegalArgumentException.class);
    InternalUtils.getProbability(neverSampler);
  }

  @Test
  public void throwIllegalArgumentExceptionForNull() {
    thrown.expect(IllegalArgumentException.class);
    InternalUtils.getProbability(null);
  }
}
