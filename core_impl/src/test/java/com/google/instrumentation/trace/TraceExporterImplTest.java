/*
 * Copyright 2017, Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.instrumentation.trace;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doThrow;

import com.google.instrumentation.internal.MillisClock;
import com.google.instrumentation.trace.Span.Options;
import com.google.instrumentation.trace.SpanImpl.StartEndHandler;
import com.google.instrumentation.trace.TraceExporter.ServiceHandler;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import javax.annotation.concurrent.GuardedBy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link TraceExporterImpl}. */
@RunWith(JUnit4.class)
public class TraceExporterImplTest {
  private static final String SPAN_NAME_1 = "MySpanName/1";
  private static final String SPAN_NAME_2 = "MySpanName/2";
  private final Random random = new Random(1234);
  private final SpanContext spanContext =
      SpanContext.create(
          TraceId.generateRandomId(random),
          SpanId.generateRandomId(random),
          TraceOptions.builder().setIsSampled().build());
  private final TraceExporterImpl traceExporter = TraceExporterImpl.create(4, 1000);
  private EnumSet<Options> recordSpanOptions = EnumSet.of(Options.RECORD_EVENTS);
  private final FakeServiceHandler serviceHandler = new FakeServiceHandler();
  @Mock private StartEndHandler startEndHandler;
  @Mock private ServiceHandler mockServiceHandler;

  private static final class FakeServiceHandler extends ServiceHandler {
    private final Object monitor = new Object();

    @GuardedBy("monitor")
    List<SpanData> spanDataList = new LinkedList<SpanData>();

    @Override
    public void export(List<SpanData> spanDataList) {
      synchronized (monitor) {
        this.spanDataList.addAll(spanDataList);
        monitor.notifyAll();
      }
    }

    // Waits until we received numberOfSpans spans to export;
    private List<SpanData> waitForExport(int numberOfSpans) {
      List<SpanData> ret;
      synchronized (monitor) {
        while (spanDataList.size() != numberOfSpans) {
          try {
            monitor.wait();
          } catch (InterruptedException e) {
            // Preserve the interruption status as per guidance.
            Thread.currentThread().interrupt();
          }
        }
        ret = new ArrayList<SpanData>(spanDataList);
        spanDataList.clear();
      }
      return ret;
    }
  }

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    traceExporter.registerServiceHandler("test.service", serviceHandler);
  }

  private final SpanImpl generateSpan(String spanName) {
    SpanImpl span =
        SpanImpl.startSpan(
            spanContext,
            recordSpanOptions,
            spanName,
            null,
            startEndHandler,
            null,
            MillisClock.getInstance());
    span.end();
    return span;
  }

  @Test
  public void exportDifferentSampledSpans() {
    traceExporter.addSpan(generateSpan(SPAN_NAME_1));
    traceExporter.addSpan(generateSpan(SPAN_NAME_2));
    List<SpanData> exported = serviceHandler.waitForExport(2);
    assertThat(exported.get(0).getDisplayName()).isEqualTo(SPAN_NAME_1);
    assertThat(exported.get(1).getDisplayName()).isEqualTo(SPAN_NAME_2);
  }

  @Test
  public void exportMoreSpansThanTheBufferSize() {
    traceExporter.addSpan(generateSpan(SPAN_NAME_1));
    traceExporter.addSpan(generateSpan(SPAN_NAME_1));
    traceExporter.addSpan(generateSpan(SPAN_NAME_1));
    traceExporter.addSpan(generateSpan(SPAN_NAME_1));
    traceExporter.addSpan(generateSpan(SPAN_NAME_1));
    traceExporter.addSpan(generateSpan(SPAN_NAME_1));
    List<SpanData> exported = serviceHandler.waitForExport(6);
    assertThat(exported.get(0).getDisplayName()).isEqualTo(SPAN_NAME_1);
    assertThat(exported.get(1).getDisplayName()).isEqualTo(SPAN_NAME_1);
    assertThat(exported.get(2).getDisplayName()).isEqualTo(SPAN_NAME_1);
    assertThat(exported.get(3).getDisplayName()).isEqualTo(SPAN_NAME_1);
    assertThat(exported.get(4).getDisplayName()).isEqualTo(SPAN_NAME_1);
    assertThat(exported.get(5).getDisplayName()).isEqualTo(SPAN_NAME_1);
  }

  @Test
  public void interruptWorkerThreadStops() throws InterruptedException {
    Thread workerThread = traceExporter.getWorkerThread();
    workerThread.interrupt();
    // Test that the worker thread will stop.
    workerThread.join();
  }

  @Test
  public void serviceHandlerThrowsException() {
    doThrow(new IllegalArgumentException("No export for you."))
        .when(mockServiceHandler)
        .export(anyListOf(SpanData.class));
    traceExporter.registerServiceHandler("mock.service", mockServiceHandler);
    traceExporter.addSpan(generateSpan(SPAN_NAME_1));
    List<SpanData> exported = serviceHandler.waitForExport(1);
    assertThat(exported.get(0).getDisplayName()).isEqualTo(SPAN_NAME_1);
    // Continue to export after the exception was received.
    traceExporter.addSpan(generateSpan(SPAN_NAME_1));
    exported = serviceHandler.waitForExport(1);
    assertThat(exported.get(0).getDisplayName()).isEqualTo(SPAN_NAME_1);
  }
}
