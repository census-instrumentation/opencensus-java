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

package io.opencensus.exporter.stats.signalfx;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.signalfx.metrics.errorhandler.OnSendErrorHandler;
import com.signalfx.metrics.flush.AggregateMetricSender;
import com.signalfx.metrics.protobuf.SignalFxProtocolBuffers.DataPoint;
import com.signalfx.metrics.protobuf.SignalFxProtocolBuffers.Datum;
import com.signalfx.metrics.protobuf.SignalFxProtocolBuffers.Dimension;
import com.signalfx.metrics.protobuf.SignalFxProtocolBuffers.MetricType;
import io.opencensus.common.Duration;
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.AggregationData;
import io.opencensus.stats.AggregationData.MeanData;
import io.opencensus.stats.View;
import io.opencensus.stats.View.AggregationWindow;
import io.opencensus.stats.View.Name;
import io.opencensus.stats.ViewData;
import io.opencensus.stats.ViewManager;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class SignalFxStatsExporterWorkerThreadTest {

  private static final String TEST_TOKEN = "token";
  private static final Duration ONE_SECOND = Duration.create(1, 0);

  @Mock private AggregateMetricSender.Session session;

  @Mock private ViewManager viewManager;

  @Mock private SignalFxMetricsSenderFactory factory;

  private URI endpoint;

  @Before
  public void setUp() throws Exception {
    endpoint = new URI("http://example.com");

    Mockito.when(
            factory.create(
                Mockito.any(URI.class), Mockito.anyString(), Mockito.any(OnSendErrorHandler.class)))
        .thenAnswer(
            new Answer<AggregateMetricSender>() {
              @Override
              public AggregateMetricSender answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                AggregateMetricSender sender =
                    SignalFxMetricsSenderFactory.DEFAULT.create(
                        (URI) args[0], (String) args[1], (OnSendErrorHandler) args[2]);
                AggregateMetricSender spy = Mockito.spy(sender);
                Mockito.doReturn(session).when(spy).createSession();
                return spy;
              }
            });
  }

  @Test
  public void createThread() {
    SignalFxStatsExporterWorkerThread thread =
        new SignalFxStatsExporterWorkerThread(
            factory, endpoint, TEST_TOKEN, ONE_SECOND, viewManager);
    assertTrue(thread.isDaemon());
    assertThat(thread.getName(), startsWith("SignalFx"));
  }

  @Test
  public void senderThreadInterruptStopsLoop() throws InterruptedException {
    Mockito.when(session.setDatapoint(Mockito.any(DataPoint.class))).thenReturn(session);
    Mockito.when(viewManager.getAllExportedViews()).thenReturn(ImmutableSet.<View>of());

    SignalFxStatsExporterWorkerThread thread =
        new SignalFxStatsExporterWorkerThread(
            factory, endpoint, TEST_TOKEN, ONE_SECOND, viewManager);
    thread.start();
    thread.interrupt();
    thread.join(5000, 0);
    assertFalse("Worker thread should have stopped", thread.isAlive());
  }

  @Test
  public void setsDatapointsFromViewOnSession() throws IOException {
    View view = Mockito.mock(View.class);
    Name viewName = Name.create("test");
    Mockito.when(view.getName()).thenReturn(viewName);
    Mockito.when(view.getAggregation()).thenReturn(Aggregation.Mean.create());
    Mockito.when(view.getWindow()).thenReturn(AggregationWindow.Cumulative.create());
    Mockito.when(view.getColumns()).thenReturn(ImmutableList.of(TagKey.create("animal")));

    ViewData viewData = Mockito.mock(ViewData.class);
    Mockito.when(viewData.getView()).thenReturn(view);
    Mockito.when(viewData.getAggregationMap())
        .thenReturn(
            ImmutableMap.<List<TagValue>, AggregationData>of(
                ImmutableList.of(TagValue.create("cat")), MeanData.create(3.15d, 1)));

    Mockito.when(viewManager.getAllExportedViews()).thenReturn(ImmutableSet.of(view));
    Mockito.when(viewManager.getView(Mockito.eq(viewName))).thenReturn(viewData);

    SignalFxStatsExporterWorkerThread thread =
        new SignalFxStatsExporterWorkerThread(
            factory, endpoint, TEST_TOKEN, ONE_SECOND, viewManager);
    thread.export();

    DataPoint datapoint =
        DataPoint.newBuilder()
            .setMetric("test")
            .setMetricType(MetricType.GAUGE)
            .addDimensions(Dimension.newBuilder().setKey("animal").setValue("cat").build())
            .setValue(Datum.newBuilder().setDoubleValue(3.15d).build())
            .build();
    Mockito.verify(session).setDatapoint(Mockito.eq(datapoint));
    Mockito.verify(session).close();
  }
}
