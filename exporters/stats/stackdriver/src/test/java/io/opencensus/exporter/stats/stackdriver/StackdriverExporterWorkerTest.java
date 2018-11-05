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

package io.opencensus.exporter.stats.stackdriver;

import static com.google.common.truth.Truth.assertThat;
import static io.opencensus.exporter.stats.stackdriver.StackdriverExporterWorker.CUSTOM_OPENCENSUS_DOMAIN;
import static io.opencensus.exporter.stats.stackdriver.StackdriverExporterWorker.DEFAULT_DISPLAY_NAME_PREFIX;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.api.MetricDescriptor;
import com.google.api.MonitoredResource;
import com.google.api.gax.rpc.UnaryCallable;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.cloud.monitoring.v3.stub.MetricServiceStub;
import com.google.common.collect.ImmutableSet;
import com.google.monitoring.v3.CreateMetricDescriptorRequest;
import com.google.monitoring.v3.CreateTimeSeriesRequest;
import com.google.monitoring.v3.TimeSeries;
import com.google.protobuf.Empty;
import io.opencensus.common.Duration;
import io.opencensus.common.Timestamp;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricDescriptor.Type;
import io.opencensus.metrics.export.MetricProducer;
import io.opencensus.metrics.export.MetricProducerManager;
import io.opencensus.metrics.export.Point;
import io.opencensus.metrics.export.Value;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link StackdriverExporterWorker}. */
@RunWith(JUnit4.class)
public class StackdriverExporterWorkerTest {
  private static final String PROJECT_ID = "projectId";
  private static final Duration ONE_SECOND = Duration.create(1, 0);

  private static final String METRIC_NAME = "my metric";
  private static final String METRIC_DESCRIPTION = "metric description";
  private static final String METRIC_DESCRIPTION_2 = "metric description2";
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
          METRIC_NAME, METRIC_DESCRIPTION_2, METRIC_UNIT, Type.CUMULATIVE_INT64, LABEL_KEY);

  private static final Value VALUE_LONG = Value.longValue(12345678);
  private static final Timestamp TIMESTAMP = Timestamp.fromMillis(3000);
  private static final Timestamp TIMESTAMP_2 = Timestamp.fromMillis(1000);
  private static final Point POINT = Point.create(VALUE_LONG, TIMESTAMP);

  private static final io.opencensus.metrics.export.TimeSeries CUMULATIVE_TIME_SERIES =
      io.opencensus.metrics.export.TimeSeries.createWithOnePoint(LABEL_VALUE, POINT, TIMESTAMP_2);

  private static final Metric METRIC =
      Metric.createWithOneTimeSeries(METRIC_DESCRIPTOR, CUMULATIVE_TIME_SERIES);

  private static final MonitoredResource DEFAULT_RESOURCE =
      MonitoredResource.newBuilder().setType("global").build();

  @Mock private MetricProducerManager metricProducerManager;

  @Mock private MetricProducer metricProducer;

  @Mock private MetricServiceStub mockStub;

  @Mock
  private UnaryCallable<CreateMetricDescriptorRequest, MetricDescriptor>
      mockCreateMetricDescriptorCallable;

  @Mock private UnaryCallable<CreateTimeSeriesRequest, Empty> mockCreateTimeSeriesCallable;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    doReturn(mockCreateMetricDescriptorCallable).when(mockStub).createMetricDescriptorCallable();
    doReturn(mockCreateTimeSeriesCallable).when(mockStub).createTimeSeriesCallable();
    doReturn(null)
        .when(mockCreateMetricDescriptorCallable)
        .call(any(CreateMetricDescriptorRequest.class));
    doReturn(null).when(mockCreateTimeSeriesCallable).call(any(CreateTimeSeriesRequest.class));
    doReturn(ImmutableSet.of(metricProducer)).when(metricProducerManager).getAllMetricProducer();
  }

  @Test
  public void testConstants() {
    assertThat(StackdriverExporterWorker.MAX_BATCH_EXPORT_SIZE).isEqualTo(200);
    assertThat(StackdriverExporterWorker.CUSTOM_METRIC_DOMAIN).isEqualTo("custom.googleapis.com/");
    assertThat(StackdriverExporterWorker.CUSTOM_OPENCENSUS_DOMAIN)
        .isEqualTo("custom.googleapis.com/opencensus/");
    assertThat(StackdriverExporterWorker.DEFAULT_DISPLAY_NAME_PREFIX).isEqualTo("OpenCensus/");
  }

  @Test
  public void export() {
    doReturn(Collections.singletonList(METRIC)).when(metricProducer).getMetrics();
    StackdriverExporterWorker worker =
        new StackdriverExporterWorker(
            PROJECT_ID,
            new FakeMetricServiceClient(mockStub),
            ONE_SECOND,
            metricProducerManager,
            DEFAULT_RESOURCE,
            null);
    worker.export();

    verify(mockStub, times(1)).createMetricDescriptorCallable();
    verify(mockStub, times(1)).createTimeSeriesCallable();

    MetricDescriptor descriptor =
        StackdriverExportUtils.createMetricDescriptor(
            METRIC_DESCRIPTOR, PROJECT_ID, CUSTOM_OPENCENSUS_DOMAIN, DEFAULT_DISPLAY_NAME_PREFIX);
    List<TimeSeries> timeSeries =
        StackdriverExportUtils.createTimeSeriesList(
            METRIC, DEFAULT_RESOURCE, CUSTOM_OPENCENSUS_DOMAIN);
    verify(mockCreateMetricDescriptorCallable, times(1))
        .call(
            eq(
                CreateMetricDescriptorRequest.newBuilder()
                    .setName("projects/" + PROJECT_ID)
                    .setMetricDescriptor(descriptor)
                    .build()));
    verify(mockCreateTimeSeriesCallable, times(1))
        .call(
            eq(
                CreateTimeSeriesRequest.newBuilder()
                    .setName("projects/" + PROJECT_ID)
                    .addAllTimeSeries(timeSeries)
                    .build()));
  }

  @Test
  public void doNotExportForEmptyMetrics() {
    doReturn(Collections.EMPTY_LIST).when(metricProducer).getMetrics();
    StackdriverExporterWorker worker =
        new StackdriverExporterWorker(
            PROJECT_ID,
            new FakeMetricServiceClient(mockStub),
            ONE_SECOND,
            metricProducerManager,
            DEFAULT_RESOURCE,
            null);
    worker.export();
    verify(mockStub, times(0)).createMetricDescriptorCallable();
    verify(mockStub, times(0)).createTimeSeriesCallable();
  }

  @Test
  public void doNotExportIfFailedToRegisterMetric() {
    doThrow(new IllegalArgumentException()).when(mockStub).createMetricDescriptorCallable();
    StackdriverExporterWorker worker =
        new StackdriverExporterWorker(
            PROJECT_ID,
            new FakeMetricServiceClient(mockStub),
            ONE_SECOND,
            metricProducerManager,
            DEFAULT_RESOURCE,
            null);

    assertThat(worker.registerMetricDescriptor(METRIC_DESCRIPTOR)).isFalse();
    worker.export();
    verify(mockStub, times(1)).createMetricDescriptorCallable();
    verify(mockStub, times(0)).createTimeSeriesCallable();
  }

  @Test
  public void skipDifferentMetricWithSameName() throws IOException {
    StackdriverExporterWorker worker =
        new StackdriverExporterWorker(
            PROJECT_ID,
            new FakeMetricServiceClient(mockStub),
            ONE_SECOND,
            metricProducerManager,
            DEFAULT_RESOURCE,
            null);
    assertThat(worker.registerMetricDescriptor(METRIC_DESCRIPTOR)).isTrue();
    verify(mockStub, times(1)).createMetricDescriptorCallable();

    assertThat(worker.registerMetricDescriptor(METRIC_DESCRIPTOR_2)).isFalse();
    verify(mockStub, times(1)).createMetricDescriptorCallable();
  }

  @Test
  public void skipSameMetricWithSameName() throws IOException {
    StackdriverExporterWorker worker =
        new StackdriverExporterWorker(
            PROJECT_ID,
            new FakeMetricServiceClient(mockStub),
            ONE_SECOND,
            metricProducerManager,
            DEFAULT_RESOURCE,
            null);
    assertThat(worker.registerMetricDescriptor(METRIC_DESCRIPTOR)).isTrue();
    verify(mockStub, times(1)).createMetricDescriptorCallable();

    assertThat(worker.registerMetricDescriptor(METRIC_DESCRIPTOR)).isTrue();
    verify(mockStub, times(1)).createMetricDescriptorCallable();
  }

  @Test
  public void doNotCreateMetricDescriptorForRegisteredeMetric() {
    StackdriverExporterWorker worker =
        new StackdriverExporterWorker(
            PROJECT_ID,
            new FakeMetricServiceClient(mockStub),
            ONE_SECOND,
            metricProducerManager,
            DEFAULT_RESOURCE,
            null);
    assertThat(worker.registerMetricDescriptor(METRIC_DESCRIPTOR)).isTrue();
    verify(mockStub, times(1)).createMetricDescriptorCallable();

    assertThat(worker.registerMetricDescriptor(METRIC_DESCRIPTOR)).isTrue();
    verify(mockStub, times(1)).createMetricDescriptorCallable();
  }

  @Test
  public void getDomain() {
    assertThat(StackdriverExporterWorker.getDomain(null))
        .isEqualTo("custom.googleapis.com/opencensus/");
    assertThat(StackdriverExporterWorker.getDomain(""))
        .isEqualTo("custom.googleapis.com/opencensus/");
    assertThat(StackdriverExporterWorker.getDomain("custom.googleapis.com/myorg/"))
        .isEqualTo("custom.googleapis.com/myorg/");
    assertThat(StackdriverExporterWorker.getDomain("external.googleapis.com/prometheus/"))
        .isEqualTo("external.googleapis.com/prometheus/");
    assertThat(StackdriverExporterWorker.getDomain("myorg")).isEqualTo("myorg/");
  }

  @Test
  public void getDisplayNamePrefix() {
    assertThat(StackdriverExporterWorker.getDisplayNamePrefix(null)).isEqualTo("OpenCensus/");
    assertThat(StackdriverExporterWorker.getDisplayNamePrefix("")).isEqualTo("");
    assertThat(StackdriverExporterWorker.getDisplayNamePrefix("custom.googleapis.com/myorg/"))
        .isEqualTo("custom.googleapis.com/myorg/");
    assertThat(
            StackdriverExporterWorker.getDisplayNamePrefix("external.googleapis.com/prometheus/"))
        .isEqualTo("external.googleapis.com/prometheus/");
    assertThat(StackdriverExporterWorker.getDisplayNamePrefix("myorg")).isEqualTo("myorg/");
  }

  /*
   * MetricServiceClient.createMetricDescriptor() and MetricServiceClient.createTimeSeries() are
   * final methods and cannot be mocked. We have to use a mock MetricServiceStub in order to verify
   * the output.
   */
  private static final class FakeMetricServiceClient extends MetricServiceClient {

    protected FakeMetricServiceClient(MetricServiceStub stub) {
      super(stub);
    }
  }
}
