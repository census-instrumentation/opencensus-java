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

package io.opencensus.exporter.stats.stackdriver;

import static com.google.common.truth.Truth.assertThat;
import static io.opencensus.exporter.stats.stackdriver.StackdriverExportUtils.CUSTOM_OPENCENSUS_DOMAIN;
import static io.opencensus.exporter.stats.stackdriver.StackdriverExportUtils.DEFAULT_DISPLAY_NAME_PREFIX;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.api.MetricDescriptor;
import com.google.api.gax.rpc.UnaryCallable;
import com.google.cloud.monitoring.v3.stub.MetricServiceStub;
import com.google.monitoring.v3.CreateMetricDescriptorRequest;
import io.opencensus.common.Timestamp;
import io.opencensus.exporter.metrics.util.MetricExporter;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricDescriptor.Type;
import io.opencensus.metrics.export.Point;
import io.opencensus.metrics.export.Value;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link CreateMetricDescriptorExporter}. */
@RunWith(JUnit4.class)
public class CreateMetricDescriptorExporterTest {
  private static final String PROJECT_ID = "projectId";

  private static final String METRIC_NAME = CUSTOM_OPENCENSUS_DOMAIN + "my_metric";
  private static final String METRIC_NAME_2 = CUSTOM_OPENCENSUS_DOMAIN + "my_metric_2";
  private static final String METRIC_NAME_3 = "bigquery.googleapis.com/query/count";
  private static final String METRIC_DESCRIPTION = "metric_description";
  private static final String METRIC_DESCRIPTION_2 = "metric_description2";
  private static final String METRIC_UNIT = "us";
  private static final List<LabelKey> LABEL_KEY =
      Collections.singletonList(LabelKey.create("KEY", "key description"));
  private static final List<LabelValue> LABEL_VALUE =
      Collections.singletonList(LabelValue.create("VALUE"));

  private static final io.opencensus.metrics.export.MetricDescriptor METRIC_DESCRIPTOR =
      io.opencensus.metrics.export.MetricDescriptor.create(
          METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, Type.CUMULATIVE_INT64, LABEL_KEY);

  private static final io.opencensus.metrics.export.MetricDescriptor METRIC_DESCRIPTOR_2 =
      io.opencensus.metrics.export.MetricDescriptor.create(
          METRIC_NAME_2, METRIC_DESCRIPTION_2, METRIC_UNIT, Type.CUMULATIVE_INT64, LABEL_KEY);

  // Same name as METRIC_DESCRIPTOR but different descriptor.
  private static final io.opencensus.metrics.export.MetricDescriptor METRIC_DESCRIPTOR_3 =
      io.opencensus.metrics.export.MetricDescriptor.create(
          METRIC_NAME, METRIC_DESCRIPTION_2, METRIC_UNIT, Type.CUMULATIVE_INT64, LABEL_KEY);

  // Stackdriver built-in metric.
  private static final io.opencensus.metrics.export.MetricDescriptor METRIC_DESCRIPTOR_4 =
      io.opencensus.metrics.export.MetricDescriptor.create(
          METRIC_NAME_3, METRIC_DESCRIPTION, METRIC_UNIT, Type.CUMULATIVE_INT64, LABEL_KEY);

  private static final Value VALUE_LONG = Value.longValue(12345678);
  private static final Timestamp TIMESTAMP = Timestamp.fromMillis(3000);
  private static final Timestamp TIMESTAMP_2 = Timestamp.fromMillis(1000);
  private static final Point POINT = Point.create(VALUE_LONG, TIMESTAMP);

  private static final io.opencensus.metrics.export.TimeSeries CUMULATIVE_TIME_SERIES =
      io.opencensus.metrics.export.TimeSeries.createWithOnePoint(LABEL_VALUE, POINT, TIMESTAMP_2);

  private static final Metric METRIC =
      Metric.createWithOneTimeSeries(METRIC_DESCRIPTOR, CUMULATIVE_TIME_SERIES);
  private static final Metric METRIC_2 =
      Metric.createWithOneTimeSeries(METRIC_DESCRIPTOR_2, CUMULATIVE_TIME_SERIES);
  private static final Metric METRIC_3 =
      Metric.createWithOneTimeSeries(METRIC_DESCRIPTOR_3, CUMULATIVE_TIME_SERIES);
  private static final Metric METRIC_4 =
      Metric.createWithOneTimeSeries(METRIC_DESCRIPTOR_4, CUMULATIVE_TIME_SERIES);

  @Mock private MetricServiceStub mockStub;

  @Mock
  private UnaryCallable<CreateMetricDescriptorRequest, MetricDescriptor>
      mockCreateMetricDescriptorCallable;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    doReturn(mockCreateMetricDescriptorCallable).when(mockStub).createMetricDescriptorCallable();
    doReturn(null)
        .when(mockCreateMetricDescriptorCallable)
        .call(any(CreateMetricDescriptorRequest.class));
  }

  @Test
  public void export() {
    FakeMetricExporter fakeMetricExporter = new FakeMetricExporter();
    CreateMetricDescriptorExporter exporter =
        new CreateMetricDescriptorExporter(
            PROJECT_ID, new FakeMetricServiceClient(mockStub), null, fakeMetricExporter);
    exporter.export(Arrays.asList(METRIC, METRIC_2));

    verify(mockStub, times(2)).createMetricDescriptorCallable();

    MetricDescriptor descriptor =
        StackdriverExportUtils.createMetricDescriptor(
            METRIC_DESCRIPTOR, PROJECT_ID, CUSTOM_OPENCENSUS_DOMAIN, DEFAULT_DISPLAY_NAME_PREFIX);
    verify(mockCreateMetricDescriptorCallable, times(1))
        .call(
            eq(
                CreateMetricDescriptorRequest.newBuilder()
                    .setName("projects/" + PROJECT_ID)
                    .setMetricDescriptor(descriptor)
                    .build()));

    MetricDescriptor descriptor2 =
        StackdriverExportUtils.createMetricDescriptor(
            METRIC_DESCRIPTOR_2, PROJECT_ID, CUSTOM_OPENCENSUS_DOMAIN, DEFAULT_DISPLAY_NAME_PREFIX);
    verify(mockCreateMetricDescriptorCallable, times(1))
        .call(
            eq(
                CreateMetricDescriptorRequest.newBuilder()
                    .setName("projects/" + PROJECT_ID)
                    .setMetricDescriptor(descriptor2)
                    .build()));
    assertThat(fakeMetricExporter.getLastExported()).containsExactly(METRIC, METRIC_2);
  }

  @Test
  public void doNotExportForEmptyMetrics() {
    FakeMetricExporter fakeMetricExporter = new FakeMetricExporter();
    CreateMetricDescriptorExporter exporter =
        new CreateMetricDescriptorExporter(
            PROJECT_ID, new FakeMetricServiceClient(mockStub), null, fakeMetricExporter);
    exporter.export(Collections.<Metric>emptyList());
    verify(mockStub, times(0)).createMetricDescriptorCallable();
    assertThat(fakeMetricExporter.getLastExported()).isEmpty();
  }

  @Test
  public void doNotExportIfFailedToRegisterMetric() {
    FakeMetricExporter fakeMetricExporter = new FakeMetricExporter();
    doThrow(new IllegalArgumentException()).when(mockStub).createMetricDescriptorCallable();
    CreateMetricDescriptorExporter exporter =
        new CreateMetricDescriptorExporter(
            PROJECT_ID, new FakeMetricServiceClient(mockStub), null, fakeMetricExporter);

    exporter.export(Collections.singletonList(METRIC));
    verify(mockStub, times(1)).createMetricDescriptorCallable();
    assertThat(fakeMetricExporter.getLastExported()).isEmpty();
  }

  @Test
  public void skipDifferentMetricsWithSameName() {
    FakeMetricExporter fakeMetricExporter = new FakeMetricExporter();
    CreateMetricDescriptorExporter exporter =
        new CreateMetricDescriptorExporter(
            PROJECT_ID, new FakeMetricServiceClient(mockStub), null, fakeMetricExporter);
    exporter.export(Collections.singletonList(METRIC));
    verify(mockStub, times(1)).createMetricDescriptorCallable();
    assertThat(fakeMetricExporter.getLastExported()).containsExactly(METRIC);

    exporter.export(Collections.singletonList(METRIC_3));
    verify(mockStub, times(1)).createMetricDescriptorCallable();
    assertThat(fakeMetricExporter.getLastExported()).isEmpty();
  }

  @Test
  public void doNotCreateMetricDescriptorForRegisteredMetric() {
    FakeMetricExporter fakeMetricExporter = new FakeMetricExporter();
    CreateMetricDescriptorExporter exporter =
        new CreateMetricDescriptorExporter(
            PROJECT_ID, new FakeMetricServiceClient(mockStub), null, fakeMetricExporter);
    exporter.export(Collections.singletonList(METRIC));
    verify(mockStub, times(1)).createMetricDescriptorCallable();
    assertThat(fakeMetricExporter.getLastExported()).containsExactly(METRIC);

    exporter.export(Collections.singletonList(METRIC));
    verify(mockStub, times(1)).createMetricDescriptorCallable();
    assertThat(fakeMetricExporter.getLastExported()).containsExactly(METRIC);
  }

  @Test
  public void doNotCreateMetricDescriptorForBuiltInMetric() {
    FakeMetricExporter fakeMetricExporter = new FakeMetricExporter();
    CreateMetricDescriptorExporter exporter =
        new CreateMetricDescriptorExporter(
            PROJECT_ID, new FakeMetricServiceClient(mockStub), null, fakeMetricExporter);
    exporter.export(Collections.singletonList(METRIC_4));

    // Should not create MetricDescriptor for built-in metrics, but TimeSeries should be uploaded.
    verify(mockStub, times(0)).createMetricDescriptorCallable();
    assertThat(fakeMetricExporter.getLastExported()).containsExactly(METRIC_4);
  }

  private static final class FakeMetricExporter extends MetricExporter {
    @Nullable private List<Metric> lastExported = null;

    @Override
    public void export(Collection<Metric> metrics) {
      lastExported = new ArrayList<>(metrics);
    }

    @Nullable
    List<Metric> getLastExported() {
      return lastExported;
    }
  }
}
