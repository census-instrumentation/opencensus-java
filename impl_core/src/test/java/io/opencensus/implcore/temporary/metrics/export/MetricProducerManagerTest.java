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

package io.opencensus.implcore.temporary.metrics.export;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.implcore.temporary.metrics.MetricProducer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link MetricProducerManager}. */
@RunWith(JUnit4.class)
public class MetricProducerManagerTest {
  private final MetricProducerManager metricProducerManager =
      MetricProducerManager.newNoopMetricProducerManager();
  @Mock private MetricProducer metricProducer;

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void add_DisallowsNull() {
    thrown.expect(NullPointerException.class);
    metricProducerManager.add(null);
  }

  @Test
  public void add() {
    metricProducerManager.add(metricProducer);
    assertThat(metricProducerManager.getAllMetricProducer()).isEmpty();
  }

  @Test
  public void addAndRemove() {
    metricProducerManager.add(metricProducer);
    assertThat(metricProducerManager.getAllMetricProducer()).isEmpty();
    metricProducerManager.remove(metricProducer);
    assertThat(metricProducerManager.getAllMetricProducer()).isEmpty();
  }

  @Test
  public void remove_DisallowsNull() {
    thrown.expect(NullPointerException.class);
    metricProducerManager.remove(null);
  }

  @Test
  public void remove_FromEmpty() {
    metricProducerManager.remove(metricProducer);
    assertThat(metricProducerManager.getAllMetricProducer()).isEmpty();
  }

  @Test
  public void getAllMetricProducer_empty() {
    assertThat(metricProducerManager.getAllMetricProducer()).isEmpty();
  }
}
