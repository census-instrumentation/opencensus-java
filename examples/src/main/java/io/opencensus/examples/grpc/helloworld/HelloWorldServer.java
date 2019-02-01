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

package io.opencensus.examples.grpc.helloworld;

import static io.opencensus.examples.grpc.helloworld.HelloWorldUtils.getPortOrDefaultFromArgs;
import static io.opencensus.examples.grpc.helloworld.HelloWorldUtils.getStringOrDefaultFromArgs;

import com.google.common.collect.ImmutableMap;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import io.opencensus.common.Duration;
import io.opencensus.common.Scope;
import io.opencensus.contrib.grpc.metrics.RpcViews;
import io.opencensus.contrib.zpages.ZPageHandlers;
import io.opencensus.exporter.stats.prometheus.PrometheusStatsCollector;
import io.opencensus.exporter.stats.stackdriver.StackdriverStatsConfiguration;
import io.opencensus.exporter.stats.stackdriver.StackdriverStatsExporter;
import io.opencensus.exporter.trace.logging.LoggingTraceExporter;
import io.opencensus.exporter.trace.stackdriver.StackdriverTraceConfiguration;
import io.opencensus.exporter.trace.stackdriver.StackdriverTraceExporter;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanBuilder;
import io.opencensus.trace.Status;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.samplers.Samplers;
import io.prometheus.client.exporter.HTTPServer;
import java.io.IOException;
import java.util.logging.Logger;

/** Server that manages startup/shutdown of a {@code Greeter} server. */
public class HelloWorldServer {
  private static final Logger logger = Logger.getLogger(HelloWorldServer.class.getName());

  private static final Tracer tracer = Tracing.getTracer();

  private final int serverPort;
  private Server server;

  private HelloWorldServer(int serverPort) {
    this.serverPort = serverPort;
  }

  // A helper function that performs some work in its own Span.
  private static void performWork(Span parent) {
    SpanBuilder spanBuilder =
        tracer
            .spanBuilderWithExplicitParent("internal_work", parent)
            .setRecordEvents(true)
            .setSampler(Samplers.alwaysSample());
    try (Scope scope = spanBuilder.startScopedSpan()) {
      Span span = tracer.getCurrentSpan();
      span.putAttribute("my_attribute", AttributeValue.stringAttributeValue("blue"));
      span.addAnnotation("Performing work.");
      sleepFor(20); // Working hard here.
      span.addAnnotation("Done work.");
    }
  }

  private static void sleepFor(int milliseconds) {
    try {
      Thread.sleep(milliseconds);
    } catch (InterruptedException e) {
      Span span = tracer.getCurrentSpan();
      span.addAnnotation("Exception thrown when performing work " + e.getMessage());
      span.setStatus(Status.UNKNOWN);
    }
  }

  private void start() throws IOException {
    server = ServerBuilder.forPort(serverPort).addService(new GreeterImpl()).build().start();
    logger.info("Server started, listening on " + serverPort);
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              @Override
              public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                HelloWorldServer.this.stop();
                System.err.println("*** server shut down");
              }
            });
  }

  private void stop() {
    if (server != null) {
      server.shutdown();
    }
  }

  /** Await termination on the main thread since the grpc library uses daemon threads. */
  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  /** Main launches the server from the command line. */
  public static void main(String[] args) throws IOException, InterruptedException {
    // Add final keyword to pass checkStyle.
    final int serverPort = getPortOrDefaultFromArgs(args, 0, 50051);
    final String cloudProjectId = getStringOrDefaultFromArgs(args, 1, null);
    final int zPagePort = getPortOrDefaultFromArgs(args, 2, 3000);
    final int prometheusPort = getPortOrDefaultFromArgs(args, 3, 9090);

    // Registers all RPC views. For demonstration all views are registered. You may want to
    // start with registering basic views and register other views as needed for your application.
    RpcViews.registerAllViews();

    // Registers logging trace exporter.
    LoggingTraceExporter.register();

    // Starts a HTTP server and registers all Zpages to it.
    ZPageHandlers.startHttpServerAndRegisterAll(zPagePort);
    logger.info("ZPages server starts at localhost:" + zPagePort);

    // Registers Stackdriver exporters.
    if (cloudProjectId != null) {
      StackdriverTraceExporter.createAndRegister(
          StackdriverTraceConfiguration.builder().setProjectId(cloudProjectId).build());
      StackdriverStatsExporter.createAndRegister(
          StackdriverStatsConfiguration.builder()
              .setProjectId(cloudProjectId)
              .setExportInterval(Duration.create(60, 0))
              .build());
    }

    // Register Prometheus exporters and export metrics to a Prometheus HTTPServer.
    PrometheusStatsCollector.createAndRegister();
    HTTPServer prometheusServer = new HTTPServer(prometheusPort, true);

    // Start the RPC server. You shouldn't see any output from gRPC before this.
    logger.info("gRPC starting.");
    final HelloWorldServer server = new HelloWorldServer(serverPort);
    server.start();
    server.blockUntilShutdown();
  }

  static class GreeterImpl extends GreeterGrpc.GreeterImplBase {

    @Override
    public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
      Span span = tracer.getCurrentSpan();
      span.putAttribute("my_attribute", AttributeValue.stringAttributeValue("red"));
      span.addAnnotation(
          "Constructing greeting.",
          ImmutableMap.of(
              "name", AttributeValue.stringAttributeValue(req.getName()),
              "name length", AttributeValue.longAttributeValue(req.getName().length())));
      sleepFor(10);
      performWork(span);
      span.addAnnotation("Sleeping.");
      sleepFor(30);
      HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + req.getName()).build();
      responseObserver.onNext(reply);
      responseObserver.onCompleted();
      logger.info("SayHello RPC handled.");
    }
  }
}
