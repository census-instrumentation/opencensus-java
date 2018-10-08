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

package io.opencensus.contrib.dropwizard;

import static com.google.common.truth.Truth.assertThat;

import com.codahale.metrics.Counter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link DropWizardUtils}. */
@RunWith(JUnit4.class)
public class DropWizardUtilsTest {

  @Test
  public void generateFullMetricName() {
    assertThat(DropWizardUtils.generateFullMetricName("requests", "gauge"))
        .isEqualTo("codahale_requests_gauge");
  }

  @Test
  public void generateFullMetricDescription() {
    assertThat(DropWizardUtils.generateFullMetricDescription("Counter", new Counter()))
        .isEqualTo("Collected from codahale (metric=Counter, type=com.codahale.metrics.Counter)");
  }
}
