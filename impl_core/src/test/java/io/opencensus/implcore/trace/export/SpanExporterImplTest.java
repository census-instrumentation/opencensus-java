/*
 * Copyright 2017, OpenCensus Authors
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

package io.opencensus.implcore.trace.export;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doThrow;

import io.opencensus.common.Duration;
import io.opencensus.implcore.common.MillisClock;
import io.opencensus.implcore.internal.SimpleEventQueue;
import io.opencensus.implcore.trace.SpanImpl;
import io.opencensus.implcore.trace.SpanImpl.StartEndHandler;
import io.opencensus.implcore.trace.StartEndHandlerImpl;
import io.opencensus.testing.export.TestHandler;
import io.opencensus.trace.Span.Options;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.export.SpanExporter.Handler;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link SpanExporterImpl}. */
@RunWith(JUnit4.class)
public class SpanExporterImplTest {
  private static final String SPAN_NAME_1 = "MySpanName/1";
  private static final String SPAN_NAME_2 = "MySpanName/2";
  private final Random random = new Random(1234);
  private final SpanContext sampledSpanContext =
      SpanContext.create(
          TraceId.generateRandomId(random),
          SpanId.generateRandomId(random),
          TraceOptions.builder().setIsSampled(true).build());
  private final SpanContext notSampledSpanContext =
      SpanContext.create(
          TraceId.generateRandomId(random), SpanId.generateRandomId(random), TraceOptions.DEFAULT);
  private final RunningSpanStoreImpl runningSpanStore = new InProcessRunningSpanStoreImpl();
  private EnumSet<Options> recordSpanOptions = EnumSet.of(Options.RECORD_EVENTS);
  private final TestHandler serviceHandler = new TestHandler();
  @Mock private Handler mockServiceHandler;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  private SpanImpl createSampledEndedSpan(StartEndHandler startEndHandler, String spanName) {
    SpanImpl span =
        SpanImpl.startSpan(
            sampledSpanContext,
            recordSpanOptions,
            spanName,
            null,
            null,
            false,
            TraceParams.DEFAULT,
            startEndHandler,
            null,
            MillisClock.getInstance());
    span.end();
    return span;
  }

  private SpanImpl createNotSampledEndedSpan(StartEndHandler startEndHandler, String spanName) {
    SpanImpl span =
        SpanImpl.startSpan(
            notSampledSpanContext,
            recordSpanOptions,
            spanName,
            null,
            null,
            false,
            TraceParams.DEFAULT,
            startEndHandler,
            null,
            MillisClock.getInstance());
    span.end();
    return span;
  }

  @Test
  public void exportDifferentSampledSpans() {
    SpanExporterImpl spanExporter = SpanExporterImpl.create(4, Duration.create(1, 0));
    StartEndHandler startEndHandler =
        new StartEndHandlerImpl(spanExporter, runningSpanStore, null, new SimpleEventQueue());

    spanExporter.registerHandler("test.service", serviceHandler);

    SpanImpl span1 = createSampledEndedSpan(startEndHandler, SPAN_NAME_1);
    SpanImpl span2 = createSampledEndedSpan(startEndHandler, SPAN_NAME_2);
    List<SpanData> exported = serviceHandler.waitForExport(2);
    assertThat(exported).containsExactly(span1.toSpanData(), span2.toSpanData());
  }

  @Test
  public void exportMoreSpansThanTheBufferSize() {
    SpanExporterImpl spanExporter = SpanExporterImpl.create(4, Duration.create(1, 0));
    StartEndHandler startEndHandler =
        new StartEndHandlerImpl(spanExporter, runningSpanStore, null, new SimpleEventQueue());

    spanExporter.registerHandler("test.service", serviceHandler);

    SpanImpl span1 = createSampledEndedSpan(startEndHandler, SPAN_NAME_1);
    SpanImpl span2 = createSampledEndedSpan(startEndHandler, SPAN_NAME_1);
    SpanImpl span3 = createSampledEndedSpan(startEndHandler, SPAN_NAME_1);
    SpanImpl span4 = createSampledEndedSpan(startEndHandler, SPAN_NAME_1);
    SpanImpl span5 = createSampledEndedSpan(startEndHandler, SPAN_NAME_1);
    SpanImpl span6 = createSampledEndedSpan(startEndHandler, SPAN_NAME_1);
    List<SpanData> exported = serviceHandler.waitForExport(6);
    assertThat(exported)
        .containsExactly(
            span1.toSpanData(),
            span2.toSpanData(),
            span3.toSpanData(),
            span4.toSpanData(),
            span5.toSpanData(),
            span6.toSpanData());
  }

  @Test
  public void interruptWorkerThreadStops() throws InterruptedException {
    SpanExporterImpl spanExporter = SpanExporterImpl.create(4, Duration.create(1, 0));

    spanExporter.registerHandler("test.service", serviceHandler);

    Thread serviceExporterThread = spanExporter.getServiceExporterThread();
    serviceExporterThread.interrupt();
    // Test that the worker thread will stop.
    serviceExporterThread.join();
  }

  @Test
  public void serviceHandlerThrowsException() {
    doThrow(new IllegalArgumentException("No export for you."))
        .when(mockServiceHandler)
        .export(anyListOf(SpanData.class));

    SpanExporterImpl spanExporter = SpanExporterImpl.create(4, Duration.create(1, 0));
    StartEndHandler startEndHandler =
        new StartEndHandlerImpl(spanExporter, runningSpanStore, null, new SimpleEventQueue());

    spanExporter.registerHandler("test.service", serviceHandler);

    spanExporter.registerHandler("mock.service", mockServiceHandler);
    SpanImpl span1 = createSampledEndedSpan(startEndHandler, SPAN_NAME_1);
    List<SpanData> exported = serviceHandler.waitForExport(1);
    assertThat(exported).containsExactly(span1.toSpanData());
    // Continue to export after the exception was received.
    SpanImpl span2 = createSampledEndedSpan(startEndHandler, SPAN_NAME_1);
    exported = serviceHandler.waitForExport(1);
    assertThat(exported).containsExactly(span2.toSpanData());
  }

  @Test
  public void exportSpansToMultipleServices() {
    SpanExporterImpl spanExporter = SpanExporterImpl.create(4, Duration.create(1, 0));
    StartEndHandler startEndHandler =
        new StartEndHandlerImpl(spanExporter, runningSpanStore, null, new SimpleEventQueue());

    spanExporter.registerHandler("test.service", serviceHandler);

    TestHandler serviceHandler2 = new TestHandler();
    spanExporter.registerHandler("test.service2", serviceHandler2);
    SpanImpl span1 = createSampledEndedSpan(startEndHandler, SPAN_NAME_1);
    SpanImpl span2 = createSampledEndedSpan(startEndHandler, SPAN_NAME_2);
    List<SpanData> exported1 = serviceHandler.waitForExport(2);
    List<SpanData> exported2 = serviceHandler2.waitForExport(2);
    assertThat(exported1).containsExactly(span1.toSpanData(), span2.toSpanData());
    assertThat(exported2).containsExactly(span1.toSpanData(), span2.toSpanData());
  }

  @Test
  public void exportNotSampledSpans() {
    SpanExporterImpl spanExporter = SpanExporterImpl.create(4, Duration.create(1, 0));
    StartEndHandler startEndHandler =
        new StartEndHandlerImpl(spanExporter, runningSpanStore, null, new SimpleEventQueue());

    spanExporter.registerHandler("test.service", serviceHandler);

    SpanImpl span1 = createNotSampledEndedSpan(startEndHandler, SPAN_NAME_1);
    SpanImpl span2 = createSampledEndedSpan(startEndHandler, SPAN_NAME_2);
    // Spans are recorded and exported in the same order as they are ended, we test that a non
    // sampled span is not exported by creating and ending a sampled span after a non sampled span
    // and checking that the first exported span is the sampled span (the non sampled did not get
    // exported).
    List<SpanData> exported = serviceHandler.waitForExport(1);
    // Need to check this because otherwise the variable span1 is unused, other option is to not
    // have a span1 variable.
    assertThat(exported).doesNotContain(span1.toSpanData());
    assertThat(exported).containsExactly(span2.toSpanData());
  }

  @Test(timeout = 10000L)
  public void exportNotSampledSpansFlushed() {
    // Set the export delay to zero, for no timeout, in order to confirm the #flush() below works
    SpanExporterImpl spanExporter = SpanExporterImpl.create(4, Duration.create(0, 0));
    StartEndHandler startEndHandler =
        new StartEndHandlerImpl(spanExporter, runningSpanStore, null, new SimpleEventQueue());

    spanExporter.registerHandler("test.service", serviceHandler);

    SpanImpl span2 = createSampledEndedSpan(startEndHandler, SPAN_NAME_2);

    // Force a flush, without this, the #waitForExport() call below would block indefinitely.
    spanExporter.flush();

    List<SpanData> exported = serviceHandler.waitForExport(1);

    assertThat(exported).containsExactly(span2.toSpanData());
  }
}
