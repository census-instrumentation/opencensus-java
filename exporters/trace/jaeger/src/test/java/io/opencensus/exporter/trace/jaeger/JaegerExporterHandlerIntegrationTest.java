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

package io.opencensus.exporter.trace.jaeger;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.common.base.Splitter;
import com.google.common.collect.TreeTraverser;
import com.google.common.io.Files;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.opencensus.common.Scope;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.SpanBuilder;
import io.opencensus.trace.Status;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.samplers.Samplers;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;

public class JaegerExporterHandlerIntegrationTest {
  private static final String JAEGER_HOST = "127.0.0.1";
  private static final String SERVICE_NAME = "test";
  private static final String SPAN_NAME = "my.org/ProcessVideo";
  private static final String START_PROCESSING_VIDEO = "Start processing video.";
  private static final String FINISHED_PROCESSING_VIDEO = "Finished processing video.";

  private static final Logger logger =
      Logger.getLogger(JaegerExporterHandlerIntegrationTest.class.getName());
  private static final Tracer tracer = Tracing.getTracer();

  @Test(timeout = 30000)
  public void exportToJaeger() throws InterruptedException, IOException {
    assumeThat("docker is installed and executable", dockerIsInstalledAndExecutable(), is(true));

    final Process jaeger =
        Runtime.getRuntime()
            .exec(
                "docker run --rm "
                    + "-e COLLECTOR_ZIPKIN_HTTP_PORT=9411 -p5775:5775/udp -p6831:6831/udp "
                    + "-p6832:6832/udp -p5778:5778 -p16686:16686 -p14268:14268 -p9411:9411 "
                    + "jaegertracing/all-in-one:latest");
    final long timeWaitingForJaegerToStart = 1000L;
    Thread.sleep(timeWaitingForJaegerToStart);
    final long startTime = currentTimeMillis();

    try {
      SpanBuilder spanBuilder =
          tracer.spanBuilder(SPAN_NAME).setRecordEvents(true).setSampler(Samplers.alwaysSample());
      JaegerTraceExporter.createAndRegister(
          format("http://%s:14268/api/traces", JAEGER_HOST), SERVICE_NAME);

      final Scope scopedSpan = spanBuilder.startScopedSpan();
      try {
        tracer.getCurrentSpan().addAnnotation(START_PROCESSING_VIDEO);
        // Sleep for [0,10] milliseconds to fake work.
        Thread.sleep(new Random().nextInt(10) + 1);
        tracer.getCurrentSpan().putAttribute("foo", AttributeValue.stringAttributeValue("bar"));
        tracer.getCurrentSpan().addAnnotation(FINISHED_PROCESSING_VIDEO);
      } catch (Exception e) {
        tracer.getCurrentSpan().addAnnotation("Exception thrown when processing video.");
        tracer.getCurrentSpan().setStatus(Status.UNKNOWN);
        logger.severe(e.getMessage());
      } finally {
        scopedSpan.close();
      }

      logger.info("Wait longer than the reporting duration...");
      // Wait for a duration longer than reporting duration (5s) to ensure spans are exported.
      final long timeWaitingForSpansToBeExported = 5100L;
      Thread.sleep(timeWaitingForSpansToBeExported);
      JaegerTraceExporter.unregister();
      final long endTime = currentTimeMillis();

      // Get traces recorded by Jaeger:
      final HttpRequest request =
          new NetHttpTransport()
              .createRequestFactory()
              .buildGetRequest(
                  new GenericUrl(
                      format(
                          "http://%s:16686/api/traces?end=%d&limit=20&lookback=1m&maxDuration&minDuration&service=%s",
                          JAEGER_HOST, MILLISECONDS.toMicros(currentTimeMillis()), SERVICE_NAME)));
      final HttpResponse response = request.execute();
      final String body = response.parseAsString();
      assertThat("Response was: " + body, response.getStatusCode(), is(200));

      final JsonObject result = new JsonParser().parse(body).getAsJsonObject();
      // Pretty-print for debugging purposes:
      logger.log(Level.FINE, new GsonBuilder().setPrettyPrinting().create().toJson(result));

      assertThat(result, is(not(nullValue())));
      assertThat(result.get("total").getAsInt(), is(0));
      assertThat(result.get("limit").getAsInt(), is(0));
      assertThat(result.get("offset").getAsInt(), is(0));
      assertThat(result.get("errors").getAsJsonNull(), is(JsonNull.INSTANCE));
      final JsonArray data = result.get("data").getAsJsonArray();
      assertThat(data, is(not(nullValue())));
      assertThat(data.size(), is(1));
      final JsonObject trace = data.get(0).getAsJsonObject();
      assertThat(trace, is(not(nullValue())));
      assertThat(trace.get("traceID").getAsString(), matchesPattern("[a-z0-9]{1,32}"));

      final JsonArray spans = trace.get("spans").getAsJsonArray();
      assertThat(spans, is(not(nullValue())));
      assertThat(spans.size(), is(1));

      final JsonObject span = spans.get(0).getAsJsonObject();
      assertThat(span, is(not(nullValue())));
      assertThat(span.get("traceID").getAsString(), matchesPattern("[a-z0-9]{1,32}"));
      assertThat(span.get("spanID").getAsString(), matchesPattern("[a-z0-9]{1,16}"));
      assertThat(span.get("flags").getAsInt(), is(1));
      assertThat(span.get("operationName").getAsString(), is(SPAN_NAME));
      assertThat(span.get("references").getAsJsonArray().size(), is(0));
      assertThat(
          span.get("startTime").getAsLong(),
          is(greaterThanOrEqualTo(MILLISECONDS.toMicros(startTime))));
      assertThat(
          span.get("startTime").getAsLong(), is(lessThanOrEqualTo(MILLISECONDS.toMicros(endTime))));
      assertThat(
          span.get("duration").getAsLong(),
          is(greaterThanOrEqualTo(timeWaitingForJaegerToStart + timeWaitingForSpansToBeExported)));

      final JsonArray tags = span.get("tags").getAsJsonArray();
      assertThat(tags.size(), is(1));
      final JsonObject tag = tags.get(0).getAsJsonObject();
      assertThat(tag.get("key").getAsString(), is("foo"));
      assertThat(tag.get("type").getAsString(), is("string"));
      assertThat(tag.get("value").getAsString(), is("bar"));

      final JsonArray logs = span.get("logs").getAsJsonArray();
      assertThat(logs.size(), is(2));

      final JsonObject log1 = logs.get(0).getAsJsonObject();
      final long ts1 = log1.get("timestamp").getAsLong();
      assertThat(ts1, is(greaterThanOrEqualTo(MILLISECONDS.toMicros(startTime))));
      assertThat(ts1, is(lessThanOrEqualTo(MILLISECONDS.toMicros(endTime))));
      final JsonArray fields1 = log1.get("fields").getAsJsonArray();
      assertThat(fields1.size(), is(1));
      final JsonObject field1 = fields1.get(0).getAsJsonObject();
      assertThat(field1.get("key").getAsString(), is("description"));
      assertThat(field1.get("type").getAsString(), is("string"));
      assertThat(field1.get("value").getAsString(), is(START_PROCESSING_VIDEO));

      final JsonObject log2 = logs.get(1).getAsJsonObject();
      final long ts2 = log2.get("timestamp").getAsLong();
      assertThat(ts2, is(greaterThanOrEqualTo(MILLISECONDS.toMicros(startTime))));
      assertThat(ts2, is(lessThanOrEqualTo(MILLISECONDS.toMicros(endTime))));
      assertThat(ts2, is(greaterThanOrEqualTo(ts1)));
      final JsonArray fields2 = log2.get("fields").getAsJsonArray();
      assertThat(fields2.size(), is(1));
      final JsonObject field2 = fields2.get(0).getAsJsonObject();
      assertThat(field2.get("key").getAsString(), is("description"));
      assertThat(field2.get("type").getAsString(), is("string"));
      assertThat(field2.get("value").getAsString(), is(FINISHED_PROCESSING_VIDEO));

      assertThat(span.get("processID").getAsString(), is("p1"));
      assertThat(span.get("warnings").getAsJsonNull(), is(JsonNull.INSTANCE));

      final JsonObject processes = trace.get("processes").getAsJsonObject();
      assertThat(processes.size(), is(1));
      final JsonObject p1 = processes.get("p1").getAsJsonObject();
      assertThat(p1.get("serviceName").getAsString(), is(SERVICE_NAME));
      assertThat(p1.get("tags").getAsJsonArray().size(), is(0));
      assertThat(trace.get("warnings").getAsJsonNull(), is(JsonNull.INSTANCE));
    } finally {
      jaeger.destroy();
    }
  }

  private static boolean dockerIsInstalledAndExecutable() {
    final TreeTraverser<File> traverser = Files.fileTreeTraverser();
    for (final String pathPart : Splitter.on(File.pathSeparator).split(System.getenv("PATH"))) {
      final File file = new File(pathPart);
      if (isDocker(file) && file.canExecute()) {
        return true;
      } else if (file.isDirectory()) {
        for (final File child : traverser.children(file)) {
          if (isDocker(child) && child.canExecute()) {
            return true;
          }
        }
      }
    }
    return false;
  }

  private static boolean isDocker(final File file) {
    return file.isFile() && file.getName().toLowerCase().equals("docker");
  }
}
