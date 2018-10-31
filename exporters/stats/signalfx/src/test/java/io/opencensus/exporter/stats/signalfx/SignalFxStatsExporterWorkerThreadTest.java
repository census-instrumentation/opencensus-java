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
import static org.mockito.Mockito.doReturn;

import com.google.common.collect.ImmutableSet;
import com.signalfx.metrics.errorhandler.OnSendErrorHandler;
import com.signalfx.metrics.flush.AggregateMetricSender;
import com.signalfx.metrics.protobuf.SignalFxProtocolBuffers.DataPoint;
import com.signalfx.metrics.protobuf.SignalFxProtocolBuffers.Datum;
import com.signalfx.metrics.protobuf.SignalFxProtocolBuffers.Dimension;
import com.signalfx.metrics.protobuf.SignalFxProtocolBuffers.MetricType;
import io.opencensus.common.Duration;
import io.opencensus.common.Timestamp;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricDescriptor;
import io.opencensus.metrics.export.MetricDescriptor.Type;
import io.opencensus.metrics.export.MetricProducer;
import io.opencensus.metrics.export.MetricProducerManager;
import io.opencensus.metrics.export.Point;
import io.opencensus.metrics.export.TimeSeries;
import io.opencensus.metrics.export.Value;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

/** Unit tests for {@link SignalFxStatsExporterWorkerThread}. */
@RunWith(MockitoJUnitRunner.class)
public class SignalFxStatsExporterWorkerThreadTest {

  private static final String TEST_TOKEN = "token";
  private static final Duration ONE_SECOND = Duration.create(1, 0);
  private static final String METRIC_NAME = "metric-name";
  private static final String METRIC_DESCRIPTION = "description";
  private static final String METRIC_UNIT = "1";
  private static final List<LabelKey> LABEL_KEY =
      Collections.singletonList(LabelKey.create("key1", "description"));
  private static final List<LabelValue> LABEL_VALUE =
      Collections.singletonList(LabelValue.create("value1"));
  private static final MetricDescriptor METRIC_DESCRIPTOR =
      MetricDescriptor.create(
          METRIC_NAME, METRIC_DESCRIPTION, METRIC_UNIT, Type.GAUGE_DOUBLE, LABEL_KEY);
  private static final Value VALUE_LONG = Value.doubleValue(3.15d);
  private static final Timestamp TIMESTAMP = Timestamp.fromMillis(3000);
  private static final Point POINT = Point.create(VALUE_LONG, TIMESTAMP);
  private static final TimeSeries TIME_SERIES =
      TimeSeries.createWithOnePoint(LABEL_VALUE, POINT, null);
  private static final Metric METRIC =
      Metric.createWithOneTimeSeries(METRIC_DESCRIPTOR, TIME_SERIES);

  @Mock private AggregateMetricSender.Session session;

  @Mock private MetricProducerManager metricProducerManager;

  @Mock private MetricProducer metricProducer;

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
                doReturn(session).when(spy).createSession();
                return spy;
              }
            });
    doReturn(ImmutableSet.of(metricProducer)).when(metricProducerManager).getAllMetricProducer();
    doReturn(ImmutableSet.<Metric>of()).when(metricProducer).getMetrics();
  }

  @Test
  public void createThread() {
    SignalFxStatsExporterWorkerThread thread =
        new SignalFxStatsExporterWorkerThread(
            factory, endpoint, TEST_TOKEN, ONE_SECOND, metricProducerManager);
    assertTrue(thread.isDaemon());
    assertThat(thread.getName(), startsWith("SignalFx"));
  }

  @Test
  public void senderThreadInterruptStopsLoop() throws InterruptedException {
    Mockito.when(session.setDatapoint(Mockito.any(DataPoint.class))).thenReturn(session);
    doReturn(ImmutableSet.<Metric>of()).when(metricProducer).getMetrics();

    SignalFxStatsExporterWorkerThread thread =
        new SignalFxStatsExporterWorkerThread(
            factory, endpoint, TEST_TOKEN, ONE_SECOND, metricProducerManager);
    thread.start();
    thread.interrupt();
    thread.join(5000, 0);
    assertFalse("Worker thread should have stopped", thread.isAlive());
  }

  @Test
  public void setsDatapointsFromViewOnSession() throws IOException {
    doReturn(Collections.singletonList(METRIC)).when(metricProducer).getMetrics();

    SignalFxStatsExporterWorkerThread thread =
        new SignalFxStatsExporterWorkerThread(
            factory, endpoint, TEST_TOKEN, ONE_SECOND, metricProducerManager);
    thread.export();

    DataPoint datapoint =
        DataPoint.newBuilder()
            .setMetric("metric-name")
            .setMetricType(MetricType.GAUGE)
            .addDimensions(Dimension.newBuilder().setKey("key1").setValue("value1").build())
            .setValue(Datum.newBuilder().setDoubleValue(3.15d).build())
            .build();
    Mockito.verify(session).setDatapoint(Mockito.eq(datapoint));
    Mockito.verify(session).close();
  }
}
