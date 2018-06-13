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

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
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
import java.io.IOException;
import java.util.Random;
import org.junit.AfterClass;
import org.junit.AssumptionViolatedException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

public class JaegerExporterHandlerIntegrationTest {
  private static final String JAEGER_IMAGE = "jaegertracing/all-in-one:1.3";
  private static final int JAEGER_HTTP_PORT = 16686;
  private static final int JAEGER_HTTP_PORT_THRIFT = 14268;
  private static final String SERVICE_NAME = "test";
  private static final String SPAN_NAME = "my.org/ProcessVideo";
  private static final String START_PROCESSING_VIDEO = "Start processing video.";
  private static final String FINISHED_PROCESSING_VIDEO = "Finished processing video.";

  private static final Logger logger =
      LoggerFactory.getLogger(JaegerExporterHandlerIntegrationTest.class);

  private final HttpRequestFactory httpRequestFactory =
      new NetHttpTransport().createRequestFactory();

  private static GenericContainer<?> container;

  /** Starts a docker container optionally. For example, skips if Docker is unavailable. */
  @SuppressWarnings("rawtypes")
  @BeforeClass
  public static void startContainer() {
    try {
      container =
          new GenericContainer(JAEGER_IMAGE)
              .withExposedPorts(JAEGER_HTTP_PORT, JAEGER_HTTP_PORT_THRIFT)
              .waitingFor(new HttpWaitStrategy());
      container.start();
    } catch (RuntimeException e) {
      throw new AssumptionViolatedException("could not start docker container", e);
    }
  }

  @AfterClass
  public static void stopContainer() {
    if (container != null) {
      container.stop();
    }
  }

  @Before
  public void before() {
    JaegerTraceExporter.createAndRegister(thriftTracesEndpoint(), SERVICE_NAME);
  }

  @Test
  public void exportToJaeger() throws InterruptedException, IOException {
    Tracer tracer = Tracing.getTracer();
    final long startTimeInMillis = currentTimeMillis();

    SpanBuilder spanBuilder =
        tracer.spanBuilder(SPAN_NAME).setRecordEvents(true).setSampler(Samplers.alwaysSample());
    int spanDurationInMillis = new Random().nextInt(10) + 1;

    Scope scopedSpan = spanBuilder.startScopedSpan();
    try {
      tracer.getCurrentSpan().addAnnotation(START_PROCESSING_VIDEO);
      Thread.sleep(spanDurationInMillis); // Fake work.
      tracer.getCurrentSpan().putAttribute("foo", AttributeValue.stringAttributeValue("bar"));
      tracer.getCurrentSpan().addAnnotation(FINISHED_PROCESSING_VIDEO);
    } catch (Exception e) {
      tracer.getCurrentSpan().addAnnotation("Exception thrown when processing video.");
      tracer.getCurrentSpan().setStatus(Status.UNKNOWN);
      logger.error(e.getMessage());
    } finally {
      scopedSpan.close();
    }

    logger.info("Wait longer than the reporting duration...");
    // Wait for a duration longer than reporting duration (5s) to ensure spans are exported.
    long timeWaitingForSpansToBeExportedInMillis = 5100L;
    Thread.sleep(timeWaitingForSpansToBeExportedInMillis);
    JaegerTraceExporter.unregister();
    final long endTimeInMillis = currentTimeMillis();

    // Get traces recorded by Jaeger:
    HttpRequest request =
        httpRequestFactory.buildGetRequest(new GenericUrl(tracesForServiceEndpoint(SERVICE_NAME)));
    HttpResponse response = request.execute();
    String body = response.parseAsString();
    assertWithMessage("Response was: " + body).that(response.getStatusCode()).isEqualTo(200);

    JsonObject result = new JsonParser().parse(body).getAsJsonObject();
    // Pretty-print for debugging purposes:
    logger.debug(new GsonBuilder().setPrettyPrinting().create().toJson(result));

    assertThat(result).isNotNull();
    assertThat(result.get("total").getAsInt()).isEqualTo(0);
    assertThat(result.get("limit").getAsInt()).isEqualTo(0);
    assertThat(result.get("offset").getAsInt()).isEqualTo(0);
    assertThat(result.get("errors").getAsJsonNull()).isEqualTo(JsonNull.INSTANCE);
    JsonArray data = result.get("data").getAsJsonArray();
    assertThat(data).isNotNull();
    assertThat(data.size()).isEqualTo(1);
    JsonObject trace = data.get(0).getAsJsonObject();
    assertThat(trace).isNotNull();
    assertThat(trace.get("traceID").getAsString()).matches("[a-z0-9]{1,32}");

    JsonArray spans = trace.get("spans").getAsJsonArray();
    assertThat(spans).isNotNull();
    assertThat(spans.size()).isEqualTo(1);

    JsonObject span = spans.get(0).getAsJsonObject();
    assertThat(span).isNotNull();
    assertThat(span.get("traceID").getAsString()).matches("[a-z0-9]{1,32}");
    assertThat(span.get("spanID").getAsString()).matches("[a-z0-9]{1,16}");
    assertThat(span.get("flags").getAsInt()).isEqualTo(1);
    assertThat(span.get("operationName").getAsString()).isEqualTo(SPAN_NAME);
    assertThat(span.get("references").getAsJsonArray()).isEmpty();
    assertThat(span.get("startTime").getAsLong())
        .isAtLeast(MILLISECONDS.toMicros(startTimeInMillis));
    assertThat(span.get("startTime").getAsLong()).isAtMost(MILLISECONDS.toMicros(endTimeInMillis));
    assertThat(span.get("duration").getAsLong())
        .isAtLeast(MILLISECONDS.toMicros(spanDurationInMillis));
    assertThat(span.get("duration").getAsLong())
        .isAtMost(
            MILLISECONDS.toMicros(spanDurationInMillis + timeWaitingForSpansToBeExportedInMillis));

    JsonArray tags = span.get("tags").getAsJsonArray();
    assertThat(tags.size()).isEqualTo(1);
    JsonObject tag = tags.get(0).getAsJsonObject();
    assertThat(tag.get("key").getAsString()).isEqualTo("foo");
    assertThat(tag.get("type").getAsString()).isEqualTo("string");
    assertThat(tag.get("value").getAsString()).isEqualTo("bar");

    JsonArray logs = span.get("logs").getAsJsonArray();
    assertThat(logs.size()).isEqualTo(2);

    JsonObject log1 = logs.get(0).getAsJsonObject();
    long ts1 = log1.get("timestamp").getAsLong();
    assertThat(ts1).isAtLeast(MILLISECONDS.toMicros(startTimeInMillis));
    assertThat(ts1).isAtMost(MILLISECONDS.toMicros(endTimeInMillis));
    JsonArray fields1 = log1.get("fields").getAsJsonArray();
    assertThat(fields1.size()).isEqualTo(1);
    JsonObject field1 = fields1.get(0).getAsJsonObject();
    assertThat(field1.get("key").getAsString()).isEqualTo("description");
    assertThat(field1.get("type").getAsString()).isEqualTo("string");
    assertThat(field1.get("value").getAsString()).isEqualTo(START_PROCESSING_VIDEO);

    JsonObject log2 = logs.get(1).getAsJsonObject();
    long ts2 = log2.get("timestamp").getAsLong();
    assertThat(ts2).isAtLeast(MILLISECONDS.toMicros(startTimeInMillis));
    assertThat(ts2).isAtMost(MILLISECONDS.toMicros(endTimeInMillis));
    assertThat(ts2).isAtLeast(ts1);
    JsonArray fields2 = log2.get("fields").getAsJsonArray();
    assertThat(fields2.size()).isEqualTo(1);
    JsonObject field2 = fields2.get(0).getAsJsonObject();
    assertThat(field2.get("key").getAsString()).isEqualTo("description");
    assertThat(field2.get("type").getAsString()).isEqualTo("string");
    assertThat(field2.get("value").getAsString()).isEqualTo(FINISHED_PROCESSING_VIDEO);

    assertThat(span.get("processID").getAsString()).isEqualTo("p1");
    assertThat(span.get("warnings").getAsJsonNull()).isEqualTo(JsonNull.INSTANCE);

    JsonObject processes = trace.get("processes").getAsJsonObject();
    assertThat(processes.size()).isEqualTo(1);
    JsonObject p1 = processes.get("p1").getAsJsonObject();
    assertThat(p1.get("serviceName").getAsString()).isEqualTo(SERVICE_NAME);
    assertThat(p1.get("tags").getAsJsonArray().size()).isEqualTo(0);
    assertThat(trace.get("warnings").getAsJsonNull()).isEqualTo(JsonNull.INSTANCE);
  }

  private static String thriftTracesEndpoint() {
    return format(
        "http://%s:%s/api/traces",
        container.getContainerIpAddress(), container.getMappedPort(JAEGER_HTTP_PORT_THRIFT));
  }

  private static String tracesForServiceEndpoint(String service) {
    return format(
        "http://%s:%s/api/traces?service=%s",
        container.getContainerIpAddress(), container.getMappedPort(JAEGER_HTTP_PORT), service);
  }
}
