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

import org.hamcrest.CoreMatchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link DropWizardUtils}. */
@RunWith(JUnit4.class)
public class DropWizardUtilsTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void generateFullMetricName() {
    assertThat(DropWizardUtils.generateFullMetricName("requests", "count"))
        .isEqualTo("requests_count");

    assertThat(DropWizardUtils.generateFullMetricName("requests", "")).isEqualTo("requests");
  }

  @Test
  public void generateFullMetricDescription() {
    assertThat(DropWizardUtils.generateFullMetricDescription("Counter", "count"))
        .isEqualTo("DropWizard Metric=Counter Data=count");

    assertThat(DropWizardUtils.generateFullMetricDescription("Counter", null))
        .isEqualTo("DropWizard Metric=Counter");
  }

  @Test
  public void preventNullMetricName() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage(CoreMatchers.equalTo("name"));
    DropWizardUtils.generateFullMetricName(null, "count");
  }

  @Test
  public void preventNullMetricDescription() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage(CoreMatchers.equalTo("name"));
    DropWizardUtils.generateFullMetricDescription(null, "count");
  }
}
