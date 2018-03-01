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

package io.opencensus.example.gameoflife;

import static io.opencensus.example.gameoflife.GameOfLifeApplication.CALLER;
import static io.opencensus.example.gameoflife.GameOfLifeApplication.CLIENT_TAG_KEY;
import static io.opencensus.example.gameoflife.GameOfLifeApplication.METHOD;
import static io.opencensus.example.gameoflife.GameOfLifeApplication.ORIGINATOR;
import static io.opencensus.example.gameoflife.GolUtils.getPortOrDefaultFromArgs;

import com.sun.net.httpserver.HttpServer;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.opencensus.common.Duration;
import io.opencensus.contrib.grpc.metrics.RpcViews;
import io.opencensus.contrib.zpages.ZPageHandlers;
import io.opencensus.exporter.stats.prometheus.PrometheusStatsCollector;
import io.opencensus.exporter.stats.stackdriver.StackdriverStatsConfiguration;
import io.opencensus.exporter.stats.stackdriver.StackdriverStatsExporter;
import io.opencensus.exporter.trace.logging.LoggingTraceExporter;
import io.opencensus.exporter.trace.stackdriver.StackdriverTraceConfiguration;
import io.opencensus.exporter.trace.stackdriver.StackdriverTraceExporter;
import io.opencensus.exporter.trace.zipkin.ZipkinTraceExporter;
import io.opencensus.stats.Aggregation.Distribution;
import io.opencensus.stats.BucketBoundaries;
import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.stats.Stats;
import io.opencensus.stats.StatsRecorder;
import io.opencensus.stats.View;
import io.opencensus.stats.View.AggregationWindow.Cumulative;
import io.opencensus.stats.View.Name;
import io.opencensus.stats.ViewManager;
import io.opencensus.trace.Status;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

final class GameOfLifeClient {

  private static final Logger logger = Logger.getLogger(GameOfLifeClient.class.getName());
  private static final StatsRecorder statsRecorder = Stats.getStatsRecorder();
  private static final ViewManager viewManager = Stats.getViewManager();
  private static final Tracer tracer = Tracing.getTracer();

  private static final List<Double> bucketBoundaries = Arrays.asList(0.0, 5.0, 10.0, 15.0, 20.0);
  private static final MeasureDouble CLIENT_MEASURE =
      MeasureDouble.create("gol_client_measure", "Sample measure for game of life client", "1");
  private static final Cumulative CUMULATIVE = Cumulative.create();
  private static final Name CLIENT_VIEW_NAME = Name.create("gol_client_view");
  private static final View CLIENT_VIEW =
      View.create(
          CLIENT_VIEW_NAME,
          "Sample view for game of life client",
          CLIENT_MEASURE,
          Distribution.create(BucketBoundaries.create(bucketBoundaries)),
          Arrays.asList(CLIENT_TAG_KEY, CALLER, METHOD, ORIGINATOR),
          CUMULATIVE);

  // The HttpServer listening socket backlog (maximum number of queued incoming connections).
  private static final int BACKLOG = 5;
  private static final String CLIENTZ_URL = "/clientz";
  private static final int GEN_PER_GOL = 1001;

  private final ManagedChannel channel;
  private final CommandProcessorGrpc.CommandProcessorBlockingStub blockingStub;

  /**
   * Construct client connecting to GameOfLife server at {@code host:port}.
   */
  GameOfLifeClient(String host, int port) {
    this(
        ManagedChannelBuilder.forAddress(host, port)
            // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
            // needing certificates.
            .usePlaintext(true));
  }

  /**
   * Construct client for accessing GameOfLife server using the existing channel.
   */
  GameOfLifeClient(ManagedChannelBuilder<?> channelBuilder) {
    channel = channelBuilder.build();
    blockingStub = CommandProcessorGrpc.newBlockingStub(channel);
    logger.info("Client channel connected.");
  }

  void shutdown() {
    logger.info("Client channel shutting down...");
    channel.shutdownNow();
  }

  String executeCommand(String req) {
    CommandRequest request = CommandRequest.newBuilder().setReq(req).build();
    CommandResponse response;
    try {
      response = blockingStub.execute(request);
      // Record random stats [0, 10) against client tags.
      statsRecorder.newMeasureMap().put(CLIENT_MEASURE, new Random().nextInt(10)).record();
    } catch (StatusRuntimeException e) {
      logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
      tracer.getCurrentSpan().setStatus(
          Status.CanonicalCode.valueOf(e.getStatus().getCode().name()).toStatus());
      return null;
    }
    return response.getRetval();
  }

  public static void main(String[] args) throws Exception {
    int serverPort = getPortOrDefaultFromArgs(args, 0, 3000);
    int clientZPagePort = getPortOrDefaultFromArgs(args, 1, 9001);
    String cloudProjectId = null;
    if (args.length >= 3) {
      cloudProjectId = args[2];
    }

    viewManager.registerView(CLIENT_VIEW);
    RpcViews.registerAllViews();
    HttpServer zpageServer = HttpServer.create(new InetSocketAddress(clientZPagePort), BACKLOG);
    ZPageHandlers.registerAllToHttpServer(zpageServer);
    ClientzHandler clientzHandler = new ClientzHandler("localhost", serverPort, GEN_PER_GOL);
    zpageServer.createContext(CLIENTZ_URL, clientzHandler);
    zpageServer.start();
    logger.fine("Clientz HttpServer started on address " + zpageServer.getAddress().toString());

    if (cloudProjectId != null) {
      StackdriverStatsExporter.createAndRegister(
          StackdriverStatsConfiguration.builder()
              .setProjectId(cloudProjectId)
              .setExportInterval(Duration.create(5, 0))
              .build());
      StackdriverTraceExporter.createAndRegister(
          StackdriverTraceConfiguration.builder().setProjectId(cloudProjectId).build());
    }

    // ZipkinTraceExporter.createAndRegister("http://127.0.0.1:9411/api/v2/spans", "Service");
    LoggingTraceExporter.register();
    PrometheusStatsCollector.createAndRegister();
  }
}
