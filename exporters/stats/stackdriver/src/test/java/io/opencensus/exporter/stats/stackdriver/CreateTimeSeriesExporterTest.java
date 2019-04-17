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

import static io.opencensus.exporter.stats.stackdriver.StackdriverExportUtils.DEFAULT_CONSTANT_LABELS;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.api.MetricDescriptor;
import com.google.api.MonitoredResource;
import com.google.api.gax.rpc.UnaryCallable;
import com.google.cloud.monitoring.v3.stub.MetricServiceStub;
import com.google.monitoring.v3.CreateMetricDescriptorRequest;
import com.google.monitoring.v3.CreateTimeSeriesRequest;
import com.google.monitoring.v3.TimeSeries;
import com.google.protobuf.Empty;
import io.opencensus.common.Timestamp;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricDescriptor.Type;
import io.opencensus.metrics.export.Point;
import io.opencensus.metrics.export.Value;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link CreateTimeSeriesExporter}. */
@RunWith(JUnit4.class)
public class CreateTimeSeriesExporterTest {
  private static final String PROJECT_ID = "projectId";
  private static final String METRIC_NAME = "my metric";
  private static final String METRIC_DESCRIPTION = "metric description";
  private static final String METRIC_UNIT = "us";
  private static final List<LabelKey> LABEL_KEY =
      Collections.singletonList(LabelKey.create("KEY", "key description"));
  private static final List<LabelValue> LABEL_VALUE =
      Collections.singletonList(LabelValue.create("VALUE"));

  private static final io.opencensus.metrics.export.MetricDescriptor METRIC_DESCRIPTOR =
      io.opencensus.metrics.export.MetricDescriptor.create(
          METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, Type.CUMULATIVE_INT64, LABEL_KEY);

  private static final Value VALUE_LONG = Value.longValue(12345678);
  private static final Point POINT = Point.create(VALUE_LONG, Timestamp.fromMillis(3000));

  private static final io.opencensus.metrics.export.TimeSeries CUMULATIVE_TIME_SERIES =
      io.opencensus.metrics.export.TimeSeries.createWithOnePoint(
          LABEL_VALUE, POINT, Timestamp.fromMillis(1000));

  private static final Metric METRIC =
      Metric.createWithOneTimeSeries(METRIC_DESCRIPTOR, CUMULATIVE_TIME_SERIES);

  private static final MonitoredResource DEFAULT_RESOURCE =
      MonitoredResource.newBuilder().setType("global").build();

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
  }

  @Test
  public void export() {
    CreateTimeSeriesExporter exporter =
        new CreateTimeSeriesExporter(
            PROJECT_ID,
            new FakeMetricServiceClient(mockStub),
            DEFAULT_RESOURCE,
            null,
            DEFAULT_CONSTANT_LABELS);
    exporter.export(Collections.singletonList(METRIC));
    verify(mockStub, times(1)).createTimeSeriesCallable();

    List<TimeSeries> timeSeries =
        StackdriverExportUtils.createTimeSeriesList(
            METRIC,
            DEFAULT_RESOURCE,
            StackdriverExportUtils.CUSTOM_OPENCENSUS_DOMAIN,
            PROJECT_ID,
            DEFAULT_CONSTANT_LABELS);

    verify(mockCreateTimeSeriesCallable, times(1))
        .call(
            eq(
                CreateTimeSeriesRequest.newBuilder()
                    .setName("projects/" + PROJECT_ID)
                    .addAllTimeSeries(timeSeries)
                    .build()));
  }

  @Test
  public void splitInMultipleBatches() {
    CreateTimeSeriesExporter exporter =
        new CreateTimeSeriesExporter(
            PROJECT_ID,
            new FakeMetricServiceClient(mockStub),
            DEFAULT_RESOURCE,
            null,
            DEFAULT_CONSTANT_LABELS);
    final int numExportedTimeSeries = 4 * StackdriverExportUtils.MAX_BATCH_EXPORT_SIZE;
    ArrayList<Metric> exportedMetrics = new ArrayList<>(numExportedTimeSeries);
    for (int i = 0; i < numExportedTimeSeries; i++) {
      exportedMetrics.add(METRIC);
    }
    exporter.export(exportedMetrics);
    verify(mockStub, times(4)).createTimeSeriesCallable();
  }

  @Test
  public void doNotExportForEmptyMetrics() {
    CreateTimeSeriesExporter exporter =
        new CreateTimeSeriesExporter(
            PROJECT_ID,
            new FakeMetricServiceClient(mockStub),
            DEFAULT_RESOURCE,
            null,
            DEFAULT_CONSTANT_LABELS);
    exporter.export(Collections.<Metric>emptyList());
    verify(mockStub, times(0)).createTimeSeriesCallable();
  }
}
