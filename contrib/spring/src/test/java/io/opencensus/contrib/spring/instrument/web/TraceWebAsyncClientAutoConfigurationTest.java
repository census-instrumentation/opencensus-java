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

package io.opencensus.contrib.spring.instrument.web;

import static com.google.common.truth.Truth.assertThat;
import static io.opencensus.trace.Span.Kind.CLIENT;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import io.opencensus.common.Scope;
import io.opencensus.contrib.http.util.HttpTraceAttributeConstants;
import io.opencensus.testing.export.TestHandler;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Span;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.export.SpanExporter;
import io.opencensus.trace.samplers.Samplers;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(
    classes = {TraceWebAsyncClientAutoConfigurationTest.TestConfiguration.class},
    properties = "opencensus.spring.enabled=true",
    webEnvironment = RANDOM_PORT)
@SuppressWarnings("deprecation")
public class TraceWebAsyncClientAutoConfigurationTest {
  @Autowired org.springframework.web.client.AsyncRestTemplate asyncRestTemplate;

  @Autowired Environment environment;

  Tracer tracer;

  private TestHandler handler;

  @Before
  public void setup() {
    handler = new TestHandler();

    SpanExporter exporter = Tracing.getExportComponent().getSpanExporter();
    exporter.registerHandler("testing", handler);

    TraceParams params =
        Tracing.getTraceConfig()
            .getActiveTraceParams()
            .toBuilder()
            .setSampler(Samplers.alwaysSample())
            .build();
    Tracing.getTraceConfig().updateActiveTraceParams(params);
  }

  @Test(timeout = 10000)
  @Order(1)
  public void should_close_span_upon_success_callback()
      throws ExecutionException, InterruptedException {
    tracer = Tracing.getTracer();
    Span initialSpan = this.tracer.spanBuilder("initial").startSpan();

    try (Scope ws = this.tracer.withSpan(initialSpan)) {
      ListenableFuture<ResponseEntity<String>> future =
          this.asyncRestTemplate.getForEntity(
              "http://localhost:" + port() + "/async", String.class);
      String result = future.get().getBody();

      assertThat(result).isEqualTo("async");
    } catch (Exception e) {
      System.out.println(e);
    } finally {
      initialSpan.end();
    }

    // 3 spans are initial, client, server.
    List<SpanData> spans = handler.waitForExport(3);
    SpanData clientSpan = null;
    for (SpanData span : spans) {
      if (span.getKind() == CLIENT) {
        clientSpan = span;
        assertThat(clientSpan.getName()).isEqualTo("/async");
        assertThat(clientSpan.getStatus().isOk()).isTrue();
        assertThat(
                clientSpan
                    .getAttributes()
                    .getAttributeMap()
                    .get(HttpTraceAttributeConstants.HTTP_METHOD))
            .isEqualTo(AttributeValue.stringAttributeValue("GET"));
        assertThat(
                clientSpan
                    .getAttributes()
                    .getAttributeMap()
                    .get(HttpTraceAttributeConstants.HTTP_HOST))
            .isEqualTo(AttributeValue.stringAttributeValue("localhost"));
        assertThat(
                clientSpan
                    .getAttributes()
                    .getAttributeMap()
                    .get(HttpTraceAttributeConstants.HTTP_PATH))
            .isEqualTo(AttributeValue.stringAttributeValue("/async"));
        assertThat(clientSpan.getKind()).isEqualTo(CLIENT);
        break;
      }
    }
    assertThat(clientSpan).isNotNull();
  }

  @Test(timeout = 10000)
  @Order(2)
  public void should_close_span_upon_failure_callback() {
    boolean exceptionOccured = false;
    final ListenableFuture<ResponseEntity<String>> future;
    try {
      future =
          this.asyncRestTemplate.getForEntity("http://localhost:" + port() + "/fail", String.class);
      new Thread(
              new Runnable() {
                @Override
                public void run() {
                  try {
                    Thread.sleep(100);
                  } catch (Exception e) {
                    System.out.println("exception " + e);
                  }
                  future.cancel(true);
                }
              })
          .start();
      future.get(500, TimeUnit.MILLISECONDS);
    } catch (CancellationException e) {
      assertThat(e).isInstanceOf(CancellationException.class);
      exceptionOccured = true;
    } catch (Exception e) {
      Assert.fail("unexpected exception:" + e);
    }
    assertThat(exceptionOccured).isTrue();

    List<SpanData> spans = handler.waitForExport(1);
    System.out.println("Spans " + spans.toString());
    SpanData span = spans.get(0);
    assertThat(span.getName()).isEqualTo("/fail");
    assertThat(span.getStatus().isOk()).isFalse();
    assertThat(span.getAttributes().getAttributeMap().get(HttpTraceAttributeConstants.HTTP_METHOD))
        .isEqualTo(AttributeValue.stringAttributeValue("GET"));
    assertThat(span.getAttributes().getAttributeMap().get(HttpTraceAttributeConstants.HTTP_HOST))
        .isEqualTo(AttributeValue.stringAttributeValue("localhost"));
    assertThat(span.getAttributes().getAttributeMap().get(HttpTraceAttributeConstants.HTTP_PATH))
        .isEqualTo(AttributeValue.stringAttributeValue("/fail"));
    assertThat(
            span.getAttributes()
                .getAttributeMap()
                .get(HttpTraceAttributeConstants.HTTP_STATUS_CODE))
        .isEqualTo(AttributeValue.longAttributeValue(0));
    assertThat(span.getKind()).isEqualTo(CLIENT);
  }

  int port() {
    Integer port = this.environment.getProperty("local.server.port", Integer.class);
    if (port != null) {
      return port;
    }
    return 0;
  }

  @EnableAutoConfiguration
  @Configuration
  public static class TestConfiguration {

    @Bean
    org.springframework.web.client.AsyncRestTemplate restTemplate() {
      return new org.springframework.web.client.AsyncRestTemplate();
    }
  }

  @RestController
  public static class MyController {

    @RequestMapping("/async")
    String foo() {
      try {
        Thread.sleep(100);
      } catch (Exception e) {
        System.out.println(e);
      }
      return "async";
    }

    @RequestMapping("/fail")
    String fail() throws Exception {
      Thread.sleep(1000);
      throw new RuntimeException("fail");
    }
  }
}
