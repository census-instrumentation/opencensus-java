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

package io.opencensus.contrib.spring.aop;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.testing.export.TestHandler;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.Status;
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
import org.junit.runners.JUnit4;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * CensusSpringAspectTest verifies the weaving and application of the spring aop annotations.
 *
 * <p>Test Logic:
 *
 * <ol>
 *   <li>Configure a simple bean, Sample, via spring.xml
 *   <li>Include spring annotation support in spring.xml
 *   <li>Use spring to load the Sample bean which will weave the census aspects into the bean.
 *   <li>Use the TestHandler (defined in @Before and @After) to capture generated span.
 *   <li>In each test, we verify the pointcuts are applied correctly by inspecting the span captured
 *       in the TestHandler.
 * </ol>
 */
@RunWith(JUnit4.class)
public class CensusSpringAspectTest {
  ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");

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

  @After
  public void teardown() {
    SpanExporter exporter = Tracing.getExportComponent().getSpanExporter();
    exporter.unregisterHandler("testing");
  }

  @Test
  public void testTracedUsesMethodAsSpanName() throws Exception {
    // When
    Sample sample = (Sample) context.getBean("sample");
    sample.call(100);

    // Then
    List<SpanData> data = handler.waitForExport(1);
    assertThat(data).isNotNull();
    assertThat(data.size()).isEqualTo(1);
    assertThat(data.get(0).getName()).isEqualTo("call");
  }

  @Test
  public void testTracedAcceptsCustomSpanName() throws Exception {
    // When
    Sample sample = (Sample) context.getBean("sample");
    sample.custom(100);

    // Then
    List<SpanData> data = handler.waitForExport(1);
    assertThat(data).isNotNull();
    assertThat(data.size()).isEqualTo(1);
    assertThat(data.get(0).getName()).isEqualTo("blah");
  }

  @Test
  public void testHandlesException() {
    // When
    Sample sample = (Sample) context.getBean("sample");
    try {
      sample.boom();
    } catch (Exception ignored) {
      //  ok
    }

    // Then
    List<SpanData> spanList = handler.waitForExport(1);
    assertThat(spanList).isNotNull();
    assertThat(spanList.size()).isEqualTo(1);

    SpanData spanData = spanList.get(0);
    assertThat(spanData.getName()).isEqualTo("boom");
    assertThat(spanData.getStatus()).isEqualTo(Status.UNKNOWN);

    SpanData.TimedEvents<Annotation> annotations = spanData.getAnnotations();
    assertThat(annotations).isNotNull();

    List<SpanData.TimedEvent<Annotation>> events = annotations.getEvents();
    assertThat(events.size()).isEqualTo(1);
    assertThat(events.get(0).getEvent().getDescription()).isEqualTo("exception");
  }

  @Test
  public void testSqlExecute() throws Exception {
    // When
    String sql = "select 1";
    Sample sample = (Sample) context.getBean("sample");
    sample.execute(sql);

    // Then
    List<SpanData> data = handler.waitForExport(1);
    assertThat(data).isNotNull();
    assertThat(data.size()).isEqualTo(1);
    assertThat(data.get(0).getName()).isEqualTo("execute-4705ea0d"); // sql-{hash of sql statement}

    List<SpanData.TimedEvent<Annotation>> events = data.get(0).getAnnotations().getEvents();
    assertThat(events.size()).isEqualTo(1);
    assertThat(events.get(0).getEvent().getDescription()).isEqualTo(sql);
  }

  @Test
  public void testSqlQuery() throws Exception {
    // When
    String sql = "select 2";
    Sample sample = (Sample) context.getBean("sample");
    sample.executeQuery(sql);

    // Then
    List<SpanData> data = handler.waitForExport(1);
    assertThat(data).isNotNull();
    assertThat(data.size()).isEqualTo(1);
    assertThat(data.get(0).getName()).isEqualTo("executeQuery-4705ea0e");

    SpanData.TimedEvents<Annotation> annotations = data.get(0).getAnnotations();
    List<SpanData.TimedEvent<Annotation>> events = annotations.getEvents();
    assertThat(events.size()).isEqualTo(1);
    assertThat(events.get(0).getEvent().getDescription()).isEqualTo(sql);
  }

  @Test
  public void testSqlUpdate() throws Exception {
    // When
    String sql = "update content set value = 1";
    Sample sample = (Sample) context.getBean("sample");
    sample.executeUpdate(sql);

    // Then
    List<SpanData> data = handler.waitForExport(1);
    assertThat(data).isNotNull();
    assertThat(data.size()).isEqualTo(1);
    assertThat(data.get(0).getName()).isEqualTo("executeUpdate-acaeb423");

    List<SpanData.TimedEvent<Annotation>> events = data.get(0).getAnnotations().getEvents();
    assertThat(events.size()).isEqualTo(1);
    assertThat(events.get(0).getEvent().getDescription()).isEqualTo(sql);
  }
}
