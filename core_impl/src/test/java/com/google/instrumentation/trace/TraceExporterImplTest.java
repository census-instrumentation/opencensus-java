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

import com.google.instrumentation.common.MillisClock;
import com.google.instrumentation.common.SimpleEventQueue;
import com.google.instrumentation.trace.Span.Options;
import com.google.instrumentation.trace.TraceConfig.TraceParams;
import com.google.instrumentation.trace.TraceExporter.ServiceHandler;
import java.util.ArrayList;
import java.util.Collection;
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
  private final SpanContext sampledSpanContext =
      SpanContext.create(
          TraceId.generateRandomId(random),
          SpanId.generateRandomId(random),
          TraceOptions.builder().setIsSampled().build());
  private final SpanContext notSampledSpanContext =
      SpanContext.create(
          TraceId.generateRandomId(random), SpanId.generateRandomId(random), TraceOptions.DEFAULT);
  private final TraceExporterImpl traceExporter =
      TraceExporterImpl.create(4, 1000, new SimpleEventQueue());
  private EnumSet<Options> recordSpanOptions = EnumSet.of(Options.RECORD_EVENTS);
  private final FakeServiceHandler serviceHandler = new FakeServiceHandler();
  @Mock private ServiceHandler mockServiceHandler;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    traceExporter.registerServiceHandler("test.service", serviceHandler);
  }

  private final SpanImpl createSampledEndedSpan(String spanName) {
    SpanImpl span =
        SpanImpl.startSpan(
            sampledSpanContext,
            recordSpanOptions,
            spanName,
            null,
            false,
            TraceParams.DEFAULT,
            traceExporter,
            null,
            MillisClock.getInstance());
    span.end();
    return span;
  }

  private final SpanImpl createNotSampledEndedSpan(String spanName) {
    SpanImpl span =
        SpanImpl.startSpan(
            notSampledSpanContext,
            recordSpanOptions,
            spanName,
            null,
            false,
            TraceParams.DEFAULT,
            traceExporter,
            null,
            MillisClock.getInstance());
    span.end();
    return span;
  }

  @Test
  public void exportDifferentSampledSpans() {
    SpanImpl span1 = createSampledEndedSpan(SPAN_NAME_1);
    SpanImpl span2 = createSampledEndedSpan(SPAN_NAME_2);
    List<SpanData> exported = serviceHandler.waitForExport(2);
    assertThat(exported.size()).isEqualTo(2);
    assertThat(exported.get(0)).isEqualTo(span1.toSpanData());
    assertThat(exported.get(1)).isEqualTo(span2.toSpanData());
  }

  @Test
  public void exportMoreSpansThanTheBufferSize() {
    SpanImpl span1 = createSampledEndedSpan(SPAN_NAME_1);
    SpanImpl span2 = createSampledEndedSpan(SPAN_NAME_1);
    SpanImpl span3 = createSampledEndedSpan(SPAN_NAME_1);
    SpanImpl span4 = createSampledEndedSpan(SPAN_NAME_1);
    SpanImpl span5 = createSampledEndedSpan(SPAN_NAME_1);
    SpanImpl span6 = createSampledEndedSpan(SPAN_NAME_1);
    List<SpanData> exported = serviceHandler.waitForExport(6);
    assertThat(exported.size()).isEqualTo(6);
    assertThat(exported.get(0)).isEqualTo(span1.toSpanData());
    assertThat(exported.get(1)).isEqualTo(span2.toSpanData());
    assertThat(exported.get(2)).isEqualTo(span3.toSpanData());
    assertThat(exported.get(3)).isEqualTo(span4.toSpanData());
    assertThat(exported.get(4)).isEqualTo(span5.toSpanData());
    assertThat(exported.get(5)).isEqualTo(span6.toSpanData());
  }

  @Test
  public void interruptWorkerThreadStops() throws InterruptedException {
    Thread serviceExporterThread = traceExporter.getServiceExporterThread();
    serviceExporterThread.interrupt();
    // Test that the worker thread will stop.
    serviceExporterThread.join();
  }

  @Test
  public void serviceHandlerThrowsException() {
    doThrow(new IllegalArgumentException("No export for you."))
        .when(mockServiceHandler)
        .export(anyListOf(SpanData.class));
    traceExporter.registerServiceHandler("mock.service", mockServiceHandler);
    SpanImpl span1 = createSampledEndedSpan(SPAN_NAME_1);
    List<SpanData> exported = serviceHandler.waitForExport(1);
    assertThat(exported.size()).isEqualTo(1);
    assertThat(exported.get(0)).isEqualTo(span1.toSpanData());
    // Continue to export after the exception was received.
    SpanImpl span2 = createSampledEndedSpan(SPAN_NAME_1);
    exported = serviceHandler.waitForExport(1);
    assertThat(exported.size()).isEqualTo(1);
    assertThat(exported.get(0)).isEqualTo(span2.toSpanData());
  }

  @Test
  public void exportSpansToMultipleServices() {
    FakeServiceHandler serviceHandler2 = new FakeServiceHandler();
    traceExporter.registerServiceHandler("test.service2", serviceHandler2);
    SpanImpl span1 = createSampledEndedSpan(SPAN_NAME_1);
    SpanImpl span2 = createSampledEndedSpan(SPAN_NAME_2);
    List<SpanData> exported1 = serviceHandler.waitForExport(2);
    List<SpanData> exported2 = serviceHandler2.waitForExport(2);
    assertThat(exported1.size()).isEqualTo(2);
    assertThat(exported2.size()).isEqualTo(2);
    assertThat(exported1.get(0)).isEqualTo(span1.toSpanData());
    assertThat(exported2.get(0)).isEqualTo(span1.toSpanData());
    assertThat(exported1.get(1)).isEqualTo(span2.toSpanData());
    assertThat(exported2.get(1)).isEqualTo(span2.toSpanData());
  }

  @Test
  public void exportNotSampledSpans() {
    SpanImpl span1 = createNotSampledEndedSpan(SPAN_NAME_1);
    SpanImpl span2 = createSampledEndedSpan(SPAN_NAME_2);
    // Spans are recorded and exported in the same order as they are ended, we test that a non
    // sampled span is not exported by creating and ending a sampled span after a non sampled span
    // and checking that the first exported span is the sampled span (the non sampled did not get
    // exported).
    List<SpanData> exported = serviceHandler.waitForExport(1);
    assertThat(exported.size()).isEqualTo(1);
    assertThat(exported.get(0)).isNotEqualTo(span1.toSpanData());
    assertThat(exported.get(0)).isEqualTo(span2.toSpanData());
  }

  /** Fake {@link ServiceHandler} for testing only. */
  private static final class FakeServiceHandler extends ServiceHandler {
    private final Object monitor = new Object();

    @GuardedBy("monitor")
    private final List<SpanData> spanDataList = new LinkedList<SpanData>();

    @Override
    public void export(Collection<SpanData> spanDataList) {
      synchronized (monitor) {
        this.spanDataList.addAll(spanDataList);
        monitor.notifyAll();
      }
    }

    /**
     * Waits until we received numberOfSpans spans to export. Returns the list of exported {@link
     * SpanData} objects, otherwise {@code null} if the current thread is interrupted.
     *
     * @param numberOfSpans the number of minimum spans to be collected.
     * @return the list of exported {@link SpanData} objects, otherwise {@code null} if the current
     *     thread is interrupted.
     */
    private List<SpanData> waitForExport(int numberOfSpans) {
      List<SpanData> ret;
      synchronized (monitor) {
        while (spanDataList.size() < numberOfSpans) {
          try {
            monitor.wait();
          } catch (InterruptedException e) {
            // Preserve the interruption status as per guidance.
            Thread.currentThread().interrupt();
            return null;
          }
        }
        ret = new ArrayList<SpanData>(spanDataList);
        spanDataList.clear();
      }
      return ret;
    }
  }
}
