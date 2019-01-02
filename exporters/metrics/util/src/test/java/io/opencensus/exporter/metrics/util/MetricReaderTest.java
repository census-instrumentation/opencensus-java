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

package io.opencensus.exporter.metrics.util;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opencensus.exporter.metrics.util.MetricReader.Options;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricProducer;
import io.opencensus.metrics.export.MetricProducerManager;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link MetricReader}. */
@RunWith(JUnit4.class)
public class MetricReaderTest {
  @Mock private MetricProducerManager metricProducerManager;
  @Mock private MetricProducer metricProducer;
  @Mock private MetricExporter metricExporter;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testConstants() {
    assertThat(MetricReader.DEFAULT_SPAN_NAME).isEqualTo("ExportMetrics");
  }

  @Test
  public void readAndExport() {
    Set<MetricProducer> metricProducerSet = new HashSet<>();
    metricProducerSet.add(metricProducer);
    when(metricProducer.getMetrics()).thenReturn(Collections.<Metric>emptyList());
    when(metricProducerManager.getAllMetricProducer()).thenReturn(metricProducerSet);
    MetricReader metricReader =
        MetricReader.create(
            Options.builder().setMetricProducerManager(metricProducerManager).build());
    metricReader.readAndExport(metricExporter);
    verify(metricExporter).export(eq(Collections.<Metric>emptyList()));
  }
}
