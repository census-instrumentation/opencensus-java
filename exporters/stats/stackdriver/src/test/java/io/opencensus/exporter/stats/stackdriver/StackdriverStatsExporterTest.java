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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.api.MetricDescriptor;
import com.google.api.gax.rpc.UnaryCallable;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.monitoring.v3.MetricServiceClient;
import com.google.cloud.monitoring.v3.stub.MetricServiceStub;
import com.google.common.collect.ImmutableMap;
import com.google.monitoring.v3.CreateMetricDescriptorRequest;
import com.google.monitoring.v3.CreateTimeSeriesRequest;
import com.google.monitoring.v3.TimeSeries;
import com.google.protobuf.Empty;
import io.opencensus.common.Duration;
import io.opencensus.common.Timestamp;
import io.opencensus.stats.Aggregation.Sum;
import io.opencensus.stats.AggregationData.SumDataLong;
import io.opencensus.stats.Measure.MeasureLong;
import io.opencensus.stats.View;
import io.opencensus.stats.View.AggregationWindow.Cumulative;
import io.opencensus.stats.View.Name;
import io.opencensus.stats.ViewData;
import io.opencensus.stats.ViewData.AggregationWindowData.CumulativeData;
import io.opencensus.stats.ViewManager;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link StackdriverStatsExporter}. */
@RunWith(JUnit4.class)
public class StackdriverStatsExporterTest {

  private static final String PROJECT_ID = "projectId";
  private static final Duration ONE_SECOND = Duration.create(1, 0);
  private static final TagKey KEY = TagKey.create("KEY");
  private static final TagValue VALUE = TagValue.create("VALUE");
  private static final String MEASURE_NAME = "my measurement";
  private static final String MEASURE_UNIT = "us";
  private static final String MEASURE_DESCRIPTION = "measure description";
  private static final MeasureLong MEASURE =
      MeasureLong.create(MEASURE_NAME, MEASURE_DESCRIPTION, MEASURE_UNIT);
  private static final Name VIEW_NAME = Name.create("my view");
  private static final String VIEW_DESCRIPTION = "view description";
  private static final Cumulative CUMULATIVE = Cumulative.create();
  private static final Sum SUM = Sum.create();

  @Mock private ViewManager mockViewManager;

  @Mock private MetricServiceStub mockStub;

  @Mock
  private UnaryCallable<CreateMetricDescriptorRequest, MetricDescriptor>
      mockCreateMetricDescriptorCallable;

  @Mock private UnaryCallable<CreateTimeSeriesRequest, Empty> mockCreateTimeSeriesCallable;

  @Rule public final ExpectedException thrown = ExpectedException.none();

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
  public void createWithNullCredentials() throws IOException {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("credentials");
    StackdriverStatsExporter.createWithCredentialsAndProjectId(null, PROJECT_ID, ONE_SECOND);
  }

  @Test
  public void createWithNullProjectId() throws IOException {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("projectId");
    StackdriverStatsExporter.createWithCredentialsAndProjectId(
        GoogleCredentials.newBuilder().build(), null, ONE_SECOND);
  }

  @Test
  public void createWithNullDuration() throws IOException {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("exportInterval");
    StackdriverStatsExporter.createWithCredentialsAndProjectId(
        GoogleCredentials.newBuilder().build(), PROJECT_ID, null);
  }

  @Test
  public void createWithNegativeDuration() throws IOException {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Duration must be positive");
    StackdriverStatsExporter.createWithCredentialsAndProjectId(
        GoogleCredentials.newBuilder().build(), PROJECT_ID, Duration.create(-1, 0));
  }

  @Test
  public void createHandlerTwice() throws IOException {
    StackdriverStatsExporter.createWithCredentialsAndProjectId(
        GoogleCredentials.newBuilder().build(), PROJECT_ID, ONE_SECOND);
    try {
      thrown.expect(IllegalStateException.class);
      thrown.expectMessage("Stackdriver stats exporter is already created.");
      StackdriverStatsExporter.createWithCredentialsAndProjectId(
          GoogleCredentials.newBuilder().build(), PROJECT_ID, ONE_SECOND);
    } finally {
      StackdriverStatsExporter.unsafeSetExporter(null);
    }
  }

  @Test
  public void registerViewAndExport() throws IOException {
    View view =
        View.create(VIEW_NAME, VIEW_DESCRIPTION, MEASURE, SUM, Arrays.asList(KEY), CUMULATIVE);
    ViewData viewData =
        ViewData.create(
            view,
            ImmutableMap.of(Arrays.asList(VALUE), SumDataLong.create(1)),
            CumulativeData.create(Timestamp.fromMillis(100), Timestamp.fromMillis(200)));
    doNothing().when(mockViewManager).registerView(view);
    doReturn(viewData).when(mockViewManager).getView(VIEW_NAME);

    try {
      StackdriverStatsExporter.unsafeSetExporter(
          new StackdriverStatsExporter(
              PROJECT_ID, new FakeMetricServiceClient(mockStub), ONE_SECOND, mockViewManager));
      StackdriverStatsExporter.registerView(view);

      verify(mockStub, times(1)).createMetricDescriptorCallable();
      // This verification is slower on some machine. Relax the timeout be greater than the export
      // interval to make this more steady.
      verify(mockStub, timeout(2000).times(1)).createTimeSeriesCallable();

      MetricDescriptor descriptor = StackdriverExportUtils.createMetricDescriptor(view, PROJECT_ID);
      List<TimeSeries> timeSeries =
          StackdriverExportUtils.createTimeSeriesList(viewData, PROJECT_ID);
      verify(mockCreateMetricDescriptorCallable, times(1))
          .call(
              eq(
                  CreateMetricDescriptorRequest.newBuilder()
                      .setMetricDescriptor(descriptor)
                      .build()));
      verify(mockCreateTimeSeriesCallable, times(1))
          .call(eq(CreateTimeSeriesRequest.newBuilder().addAllTimeSeries(timeSeries).build()));
    } finally {
      StackdriverStatsExporter.unsafeSetExporter(null);
    }
  }

  @Test
  public void preventRegisterViewBeforeCreateExporter() throws IOException {
    View view1 =
        View.create(VIEW_NAME, VIEW_DESCRIPTION, MEASURE, SUM, Arrays.asList(KEY), CUMULATIVE);
    thrown.expect(IllegalStateException.class);
    thrown.expectMessage("Stackdriver stats exporter has not been created.");
    StackdriverStatsExporter.registerView(view1);
  }

  @Test
  public void preventRegisteringDifferentViewWithSameName() throws IOException {
    doNothing().when(mockViewManager).registerView(any(View.class));
    StackdriverStatsExporter.unsafeSetExporter(
        new StackdriverStatsExporter(
            PROJECT_ID, new FakeMetricServiceClient(mockStub), ONE_SECOND, mockViewManager));
    View view1 =
        View.create(VIEW_NAME, VIEW_DESCRIPTION, MEASURE, SUM, Arrays.asList(KEY), CUMULATIVE);
    StackdriverStatsExporter.registerView(view1);
    View view2 =
        View.create(
            VIEW_NAME,
            "This is a different description.",
            MEASURE,
            SUM,
            Arrays.asList(KEY),
            CUMULATIVE);
    try {
      thrown.expect(IllegalArgumentException.class);
      thrown.expectMessage("A different view with the same name is already registered: ");
      StackdriverStatsExporter.registerView(view2);
    } finally {
      StackdriverStatsExporter.unsafeSetExporter(null);
    }
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
