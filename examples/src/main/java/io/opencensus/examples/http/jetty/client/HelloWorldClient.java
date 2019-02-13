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

package io.opencensus.examples.http.jetty.client;

import io.opencensus.contrib.http.jetty.client.OcJettyHttpClient;
import io.opencensus.contrib.http.util.HttpViews;
import io.opencensus.exporter.stats.prometheus.PrometheusStatsCollector;
import io.opencensus.exporter.trace.jaeger.JaegerTraceExporter;
import io.opencensus.exporter.trace.logging.LoggingTraceExporter;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.config.TraceConfig;
import io.opencensus.trace.samplers.Samplers;
import io.prometheus.client.exporter.HTTPServer;
import java.io.IOException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.jetty.client.HttpClientTransport;
import org.eclipse.jetty.client.HttpRequest;
import org.eclipse.jetty.client.http.HttpClientTransportOverHTTP;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/** Sample application that shows how to instrument jetty client. */
public class HelloWorldClient {
  private static final Logger logger = Logger.getLogger(HelloWorldClient.class.getName());

  private static void initTracing() {
    TraceConfig traceConfig = Tracing.getTraceConfig();
    Logger.getRootLogger().setLevel(Level.INFO);
    traceConfig.updateActiveTraceParams(
        traceConfig.getActiveTraceParams().toBuilder().setSampler(Samplers.alwaysSample()).build());

    LoggingTraceExporter.register();
    // Register Jaeger Tracing. Refer to https://www.jaegertracing.io/docs/1.8/getting-started/ to
    // run Jaeger
    JaegerTraceExporter.createAndRegister("http://localhost:14268/api/traces", "helloworldclient");
  }

  private static void initStatsExporter() throws IOException {
    HttpViews.registerAllClientViews();

    // Register Prometheus exporters and export metrics to a Prometheus HTTPServer.
    // Refer to https://prometheus.io/ to run Prometheus Server.
    PrometheusStatsCollector.createAndRegister();
    HTTPServer prometheusServer = new HTTPServer(9091, true);
  }

  /**
   * HelloWorldClient sends http request periodically to {@link HelloWorldServer}. These requests
   * are instrumented using opencensus jetty client library to enable tracing and monitoring stats.
   */
  public static void main(String[] args) throws Exception {
    BasicConfigurator.configure();

    initTracing();
    initStatsExporter();

    HttpClientTransport transport = new HttpClientTransportOverHTTP();
    SslContextFactory sslCf = new SslContextFactory();
    OcJettyHttpClient httpClient = new OcJettyHttpClient(transport, sslCf, null, null);

    httpClient.start();

    do {
      HttpRequest request =
          (HttpRequest) httpClient.newRequest("http://localhost:8080/").method(HttpMethod.GET);
      HttpRequest asyncRequest =
          (HttpRequest) httpClient.newRequest("http://localhost:8080/async").method(HttpMethod.GET);
      HttpRequest postRequest =
          (HttpRequest) httpClient.newRequest("http://localhost:8080/").method(HttpMethod.POST);
      postRequest.content(new StringContentProvider("{\"hello\": \"world\"}"), "application/json");

      if (request == null) {
        logger.info("Request is null");
        break;
      }

      request.send();
      asyncRequest.send();
      postRequest.send();
      try {
        Thread.sleep(15000);
      } catch (Exception e) {
        logger.error("Error while sleeping");
      }
    } while (true);
  }
}
