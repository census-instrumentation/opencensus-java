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

package io.opencensus.exporter.metrics.ocagent;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.netty.NettyServerBuilder;
import io.opencensus.common.Duration;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.LongGauge;
import io.opencensus.metrics.LongGauge.LongPoint;
import io.opencensus.metrics.MetricRegistry;
import io.opencensus.metrics.Metrics;
import io.opencensus.proto.agent.common.v1.Node;
import io.opencensus.proto.agent.metrics.v1.ExportMetricsServiceRequest;
import io.opencensus.proto.metrics.v1.Metric;
import io.opencensus.stats.Aggregation;
import io.opencensus.stats.Aggregation.Distribution;
import io.opencensus.stats.BucketBoundaries;
import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.stats.Measure.MeasureLong;
import io.opencensus.stats.Stats;
import io.opencensus.stats.StatsRecorder;
import io.opencensus.stats.View;
import io.opencensus.stats.View.Name;
import io.opencensus.stats.ViewManager;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagKey;
import io.opencensus.tags.TagValue;
import io.opencensus.tags.Tagger;
import io.opencensus.tags.Tags;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** End-to-end integration test for {@link OcAgentMetricsExporter}. */
@RunWith(JUnit4.class)
public class OcAgentMetricsExporterIntegrationTest {

  private Server agent;
  private FakeOcAgentMetricsServiceGrpcImpl fakeOcAgentMetricsServiceGrpc;

  private static final String SERVICE_NAME = "integration-test";
  private static final Duration RETRY_INTERVAL = Duration.create(2, 0);
  private static final Duration EXPORT_INTERVAL = Duration.create(2, 0);

  // The latency in milliseconds
  private static final MeasureDouble M_LATENCY_MS =
      MeasureDouble.create("repl/latency", "The latency in milliseconds per REPL loop", "ms");

  // Counts the number of lines read.
  private static final MeasureLong M_LINES_IN =
      MeasureLong.create("repl/lines_in", "The number of lines read in", "1");

  // Counts the number of non EOF(end-of-file) errors.
  private static final MeasureLong M_ERRORS =
      MeasureLong.create("repl/errors", "The number of errors encountered", "1");

  // Counts/groups the lengths of lines read in.
  private static final MeasureLong M_LINE_LENGTHS =
      MeasureLong.create("repl/line_lengths", "The distribution of line lengths", "By");

  // The tag "method"
  private static final TagKey KEY_METHOD = TagKey.create("method");

  // Defining the distribution aggregations
  private static final Aggregation LATENCY_DISTRIBUTION =
      Distribution.create(
          BucketBoundaries.create(
              Arrays.asList(
                  // [>=0ms, >=25ms, >=50ms, >=75ms, >=100ms, >=200ms, >=400ms, >=600ms, >=800ms,
                  // >=1s, >=2s, >=4s, >=6s]
                  0.0,
                  25.0,
                  50.0,
                  75.0,
                  100.0,
                  200.0,
                  400.0,
                  600.0,
                  800.0,
                  1000.0,
                  2000.0,
                  4000.0,
                  6000.0)));

  private static final Aggregation LENGTH_DISTRIBUTION =
      Distribution.create(
          BucketBoundaries.create(
              Arrays.asList(
                  // [>=0B, >=5B, >=10B, >=20B, >=40B, >=60B, >=80B, >=100B, >=200B, >=400B,
                  // >=600B,
                  // >=800B, >=1000B]
                  0.0,
                  5.0,
                  10.0,
                  20.0,
                  40.0,
                  60.0,
                  80.0,
                  100.0,
                  200.0,
                  400.0,
                  600.0,
                  800.0,
                  1000.0)));

  // Define the count aggregation
  private static final Aggregation COUNT = Aggregation.Count.create();

  // Empty column
  private static final List<TagKey> NO_KEYS = Collections.emptyList();

  // Define the views
  private static final List<View> VIEWS =
      Arrays.asList(
          View.create(
              Name.create("ocjavametrics/latency"),
              "The distribution of latencies",
              M_LATENCY_MS,
              LATENCY_DISTRIBUTION,
              Collections.singletonList(KEY_METHOD)),
          View.create(
              Name.create("ocjavametrics/lines_in"),
              "The number of lines read in from standard input",
              M_LINES_IN,
              COUNT,
              NO_KEYS),
          View.create(
              Name.create("ocjavametrics/errors"),
              "The number of errors encountered",
              M_ERRORS,
              COUNT,
              Collections.singletonList(KEY_METHOD)),
          View.create(
              Name.create("ocjavametrics/line_lengths"),
              "The distribution of line lengths",
              M_LINE_LENGTHS,
              LENGTH_DISTRIBUTION,
              NO_KEYS));

  private static final Random random = new Random();
  private static final Tagger tagger = Tags.getTagger();
  private static final StatsRecorder statsRecorder = Stats.getStatsRecorder();
  private static final ViewManager viewManager = Stats.getViewManager();
  private static final MetricRegistry metricRegistry = Metrics.getMetricRegistry();

  @Before
  public void setUp() {
    fakeOcAgentMetricsServiceGrpc = new FakeOcAgentMetricsServiceGrpcImpl();
    agent = getServer(OcAgentMetricsExporter.DEFAULT_END_POINT, fakeOcAgentMetricsServiceGrpc);
  }

  @After
  public void tearDown() {
    agent.shutdown();
  }

  @Test
  public void testExportMetrics() throws InterruptedException, IOException {
    // Mock a real-life scenario in production, where Agent is not enabled at first, then enabled
    // after an outage. Users should be able to see metrics shortly after Agent is up.

    registerAllViews();
    LongGauge gauge = registerGauge();

    // Register the OcAgent Exporter first.
    // Agent is not yet up and running so Exporter will just retry connection.
    OcAgentMetricsExporter.createAndRegister(
        OcAgentMetricsExporterConfiguration.builder()
            .setServiceName(SERVICE_NAME)
            .setUseInsecure(true)
            .setRetryInterval(RETRY_INTERVAL)
            .setExportInterval(EXPORT_INTERVAL)
            .build());

    doWork(
        5, gauge.getOrCreateTimeSeries(Collections.singletonList(LabelValue.create("First work"))));

    // Wait 3s so that all metrics get exported.
    Thread.sleep(3000);

    // No interaction with Agent so far.
    assertThat(fakeOcAgentMetricsServiceGrpc.getExportMetricsServiceRequests()).isEmpty();

    // Imagine that an outage happened, now start Agent. Exporter should be able to connect to Agent
    // after the next retry interval.
    agent.start();

    // Wait 3s for Exporter to start another attempt to connect to Agent.
    Thread.sleep(3000);

    doWork(
        8,
        gauge.getOrCreateTimeSeries(Collections.singletonList(LabelValue.create("Second work"))));

    // Wait 3s so that all metrics get exported.
    Thread.sleep(3000);

    List<ExportMetricsServiceRequest> exportRequests =
        fakeOcAgentMetricsServiceGrpc.getExportMetricsServiceRequests();
    assertThat(exportRequests.size()).isAtLeast(2);

    ExportMetricsServiceRequest firstRequest = exportRequests.get(0);
    Node expectedNode = OcAgentNodeUtils.getNodeInfo(SERVICE_NAME);
    Node actualNode = firstRequest.getNode();
    assertThat(actualNode.getIdentifier().getHostName())
        .isEqualTo(expectedNode.getIdentifier().getHostName());
    assertThat(actualNode.getIdentifier().getPid())
        .isEqualTo(expectedNode.getIdentifier().getPid());
    assertThat(actualNode.getLibraryInfo()).isEqualTo(expectedNode.getLibraryInfo());
    assertThat(actualNode.getServiceInfo()).isEqualTo(expectedNode.getServiceInfo());

    List<Metric> metricProtos = new ArrayList<>();
    for (int i = 1; i < exportRequests.size(); i++) {
      metricProtos.addAll(exportRequests.get(i).getMetricsList());
    }

    // There should be at least one metric exported for each view and gauge (4 + 1).
    assertThat(metricProtos.size()).isAtLeast(5);

    Set<String> expectedMetrics = new HashSet<>();
    expectedMetrics.add("jobs");
    for (View view : VIEWS) {
      expectedMetrics.add(view.getName().asString());
    }
    Set<String> actualMetrics = new HashSet<>();
    for (Metric metricProto : metricProtos) {
      if (metricProto.hasMetricDescriptor()) {
        actualMetrics.add(metricProto.getMetricDescriptor().getName());
      } else {
        actualMetrics.add(metricProto.getName());
      }
    }
    assertThat(actualMetrics).containsExactlyElementsIn(expectedMetrics);
  }

  private static void registerAllViews() {
    for (View view : VIEWS) {
      viewManager.registerView(view);
    }
  }

  private static LongGauge registerGauge() {
    return metricRegistry.addLongGauge(
        "jobs", "Pending jobs", "1", Collections.singletonList(LabelKey.create("Name", "desc")));
  }

  private static void doWork(int jobs, LongPoint point) {
    for (int i = 0; i < jobs; i++) {
      point.set(jobs - i);
      String line = generateRandom(random.nextInt(128));
      processLine(line);
      recordStat(M_LINES_IN, 1L);
      recordStat(M_LINE_LENGTHS, (long) line.length());
    }
  }

  private static String generateRandom(int size) {
    byte[] array = new byte[size];
    random.nextBytes(array);
    return new String(array, Charset.forName("UTF-8"));
  }

  private static String processLine(String line) {
    long startTimeNs = System.nanoTime();

    try {
      Thread.sleep(10L);
      return line.toUpperCase(Locale.US);
    } catch (Exception e) {
      recordTaggedStat(KEY_METHOD, "processLine", M_ERRORS, 1L);
      return "";
    } finally {
      long totalTimeNs = System.nanoTime() - startTimeNs;
      double timespentMs = totalTimeNs / 1e6;
      recordTaggedStat(KEY_METHOD, "processLine", M_LATENCY_MS, timespentMs);
    }
  }

  private static void recordStat(MeasureLong ml, Long n) {
    TagContext empty = tagger.emptyBuilder().build();
    statsRecorder.newMeasureMap().put(ml, n).record(empty);
  }

  private static void recordTaggedStat(TagKey key, String value, MeasureLong ml, long n) {
    TagContext context = tagger.emptyBuilder().put(key, TagValue.create(value)).build();
    statsRecorder.newMeasureMap().put(ml, n).record(context);
  }

  private static void recordTaggedStat(TagKey key, String value, MeasureDouble md, double d) {
    TagContext context = tagger.emptyBuilder().put(key, TagValue.create(value)).build();
    statsRecorder.newMeasureMap().put(md, d).record(context);
  }

  private static Server getServer(String endPoint, BindableService service) {
    ServerBuilder<?> builder = NettyServerBuilder.forAddress(parseEndpoint(endPoint));
    Executor executor = MoreExecutors.directExecutor();
    builder.executor(executor);
    return builder.addService(service).build();
  }

  private static InetSocketAddress parseEndpoint(String endPoint) {
    try {
      int colonIndex = endPoint.indexOf(":");
      String host = endPoint.substring(0, colonIndex);
      int port = Integer.parseInt(endPoint.substring(colonIndex + 1));
      return new InetSocketAddress(host, port);
    } catch (RuntimeException e) {
      return new InetSocketAddress("localhost", 55678);
    }
  }
}
