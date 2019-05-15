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

import io.opencensus.testing.export.TestHandler;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.export.SpanExporter;
import io.opencensus.trace.samplers.Samplers;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RunWith(SpringRunner.class)
@SpringBootTest(
    classes = HttpServletFilterIntegrationTests.Config.class,
    properties = "opencensus.spring.enabled=true")
@ContextConfiguration(
    locations = {"file:src/test/resources/beans/HttpServletFilterIntegrationTest-context.xml"})
public class HttpServletFilterIntegrationTests extends AbstractMvcIntegrationTest {

  private TestHandler handler;

  @Autowired HttpServletFilter httpServletFilter;

  @Before
  @Override
  public void setup() {
    super.setup();
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

  @After
  public void teardown() {
    SpanExporter exporter = Tracing.getExportComponent().getSpanExporter();
    exporter.unregisterHandler("testing");
  }

  @Override
  protected void configureMockMvcBuilder(DefaultMockMvcBuilder mockMvcBuilder) {
    mockMvcBuilder.addFilters(this.httpServletFilter);
  }

  @Test(timeout = 10000)
  public void shouldCreateServerTrace() throws Exception {
    sendRequest();

    List<SpanData> data = handler.waitForExport(1);
    assertThat(data).isNotNull();
    assertThat(data.size()).isEqualTo(1);
    assertThat(data.get(0).getName()).isEqualTo("/foo");
  }

  private MvcResult sendRequest() throws Exception {
    MvcResult result =
        this.mockMvc
            .perform(MockMvcRequestBuilders.get("/foo").accept(MediaType.TEXT_PLAIN))
            .andReturn();
    return result;
  }

  @Configuration
  protected static class Config {

    @RestController
    public static class TestController {

      @RequestMapping("/foo")
      public String ping() {
        return "fooResult";
      }
    }
  }
}
