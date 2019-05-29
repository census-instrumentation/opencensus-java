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
import static org.mockito.Mockito.doThrow;

import io.opencensus.common.Duration;
import io.opencensus.implcore.common.MillisClock;
import io.opencensus.implcore.internal.SimpleEventQueue;
import io.opencensus.implcore.trace.RecordEventsSpanImpl;
import io.opencensus.implcore.trace.RecordEventsSpanImpl.StartEndHandler;
import io.opencensus.implcore.trace.StartEndHandlerImpl;
import io.opencensus.testing.export.TestHandler;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracestate;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.export.SpanExporter.Handler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import javax.annotation.concurrent.GuardedBy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentMatchers;
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
          TraceOptions.builder().setIsSampled(true).build(),
          Tracestate.builder().build());
  private final SpanContext notSampledSpanContext =
      SpanContext.create(
          TraceId.generateRandomId(random),
          SpanId.generateRandomId(random),
          TraceOptions.DEFAULT,
          Tracestate.builder().build());
  private final InProcessRunningSpanStore runningSpanStore = new InProcessRunningSpanStore();
  private final SampledSpanStoreImpl sampledSpanStore =
      SampledSpanStoreImpl.getNoopSampledSpanStoreImpl();
  private final TestHandler serviceHandler = new TestHandler();
  @Mock private Handler mockServiceHandler;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  private RecordEventsSpanImpl createSampledEndedSpan(
      StartEndHandler startEndHandler, String spanName) {
    RecordEventsSpanImpl span =
        RecordEventsSpanImpl.startSpan(
            sampledSpanContext,
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

  private RecordEventsSpanImpl createNotSampledEndedSpan(
      StartEndHandler startEndHandler, String spanName) {
    RecordEventsSpanImpl span =
        RecordEventsSpanImpl.startSpan(
            notSampledSpanContext,
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
        new StartEndHandlerImpl(
            spanExporter, runningSpanStore, sampledSpanStore, new SimpleEventQueue());

    spanExporter.registerHandler("test.service", serviceHandler);

    RecordEventsSpanImpl span1 = createSampledEndedSpan(startEndHandler, SPAN_NAME_1);
    RecordEventsSpanImpl span2 = createSampledEndedSpan(startEndHandler, SPAN_NAME_2);
    List<SpanData> exported = serviceHandler.waitForExport(2);
    assertThat(exported).containsExactly(span1.toSpanData(), span2.toSpanData());
  }

  @Test
  public void exportMoreSpansThanTheBufferSize() {
    SpanExporterImpl spanExporter = SpanExporterImpl.create(4, Duration.create(1, 0));
    StartEndHandler startEndHandler =
        new StartEndHandlerImpl(
            spanExporter, runningSpanStore, sampledSpanStore, new SimpleEventQueue());

    spanExporter.registerHandler("test.service", serviceHandler);

    RecordEventsSpanImpl span1 = createSampledEndedSpan(startEndHandler, SPAN_NAME_1);
    RecordEventsSpanImpl span2 = createSampledEndedSpan(startEndHandler, SPAN_NAME_1);
    RecordEventsSpanImpl span3 = createSampledEndedSpan(startEndHandler, SPAN_NAME_1);
    RecordEventsSpanImpl span4 = createSampledEndedSpan(startEndHandler, SPAN_NAME_1);
    RecordEventsSpanImpl span5 = createSampledEndedSpan(startEndHandler, SPAN_NAME_1);
    RecordEventsSpanImpl span6 = createSampledEndedSpan(startEndHandler, SPAN_NAME_1);
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

  private static class BlockingExporter extends Handler {
    final Object monitor = new Object();

    @GuardedBy("monitor")
    Boolean condition = Boolean.FALSE;

    @Override
    public void export(Collection<SpanData> spanDataList) {
      synchronized (monitor) {
        while (!condition) {
          try {
            monitor.wait();
          } catch (InterruptedException e) {
            // Do nothing
          }
        }
      }
    }

    private void unblock() {
      synchronized (monitor) {
        condition = Boolean.TRUE;
        monitor.notifyAll();
      }
    }
  }

  @Test
  public void exportMoreSpansThanTheMaximumLimit() {
    final int bufferSize = 4;
    final int maxReferencedSpans = bufferSize * 4;
    SpanExporterImpl spanExporter = SpanExporterImpl.create(bufferSize, Duration.create(1, 0));
    StartEndHandler startEndHandler =
        new StartEndHandlerImpl(
            spanExporter, runningSpanStore, sampledSpanStore, new SimpleEventQueue());
    BlockingExporter blockingExporter = new BlockingExporter();

    spanExporter.registerHandler("test.service", serviceHandler);
    spanExporter.registerHandler("test.blocking", blockingExporter);

    List<SpanData> spansToExport = new ArrayList<>(maxReferencedSpans);
    for (int i = 0; i < maxReferencedSpans; i++) {
      spansToExport.add(createSampledEndedSpan(startEndHandler, "span_1_" + i).toSpanData());
    }

    assertThat(spanExporter.getReferencedSpans()).isEqualTo(maxReferencedSpans);

    // Now we should start dropping.
    for (int i = 0; i < 7; i++) {
      createSampledEndedSpan(startEndHandler, "span_2_" + i);
      assertThat(spanExporter.getDroppedSpans()).isEqualTo(i + 1);
    }

    assertThat(spanExporter.getReferencedSpans()).isEqualTo(maxReferencedSpans);

    // Release the blocking exporter
    blockingExporter.unblock();

    List<SpanData> exported = serviceHandler.waitForExport(maxReferencedSpans);
    assertThat(exported).isNotNull();
    assertThat(exported).containsExactlyElementsIn(spansToExport);
    exported.clear();
    spansToExport.clear();

    // We cannot compare with maxReferencedSpans here because the worker thread may get
    // unscheduled immediately after exporting, but before updating the pushed spans, if that is
    // the case at most bufferSize spans will miss.
    assertThat(spanExporter.getPushedSpans()).isAtLeast((long) maxReferencedSpans - bufferSize);

    for (int i = 0; i < 7; i++) {
      spansToExport.add(createSampledEndedSpan(startEndHandler, "span_3_" + i).toSpanData());
      // No more dropped spans.
      assertThat(spanExporter.getDroppedSpans()).isEqualTo(7);
    }

    exported = serviceHandler.waitForExport(7);
    assertThat(exported).isNotNull();
    assertThat(exported).containsExactlyElementsIn(spansToExport);
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
        .export(ArgumentMatchers.<SpanData>anyList());

    SpanExporterImpl spanExporter = SpanExporterImpl.create(4, Duration.create(1, 0));
    StartEndHandler startEndHandler =
        new StartEndHandlerImpl(
            spanExporter, runningSpanStore, sampledSpanStore, new SimpleEventQueue());

    spanExporter.registerHandler("test.service", serviceHandler);

    spanExporter.registerHandler("mock.service", mockServiceHandler);
    RecordEventsSpanImpl span1 = createSampledEndedSpan(startEndHandler, SPAN_NAME_1);
    List<SpanData> exported = serviceHandler.waitForExport(1);
    assertThat(exported).containsExactly(span1.toSpanData());
    // Continue to export after the exception was received.
    RecordEventsSpanImpl span2 = createSampledEndedSpan(startEndHandler, SPAN_NAME_1);
    exported = serviceHandler.waitForExport(1);
    assertThat(exported).containsExactly(span2.toSpanData());
  }

  @Test
  public void exportSpansToMultipleServices() {
    SpanExporterImpl spanExporter = SpanExporterImpl.create(4, Duration.create(1, 0));
    StartEndHandler startEndHandler =
        new StartEndHandlerImpl(
            spanExporter, runningSpanStore, sampledSpanStore, new SimpleEventQueue());

    spanExporter.registerHandler("test.service", serviceHandler);

    TestHandler serviceHandler2 = new TestHandler();
    spanExporter.registerHandler("test.service2", serviceHandler2);
    RecordEventsSpanImpl span1 = createSampledEndedSpan(startEndHandler, SPAN_NAME_1);
    RecordEventsSpanImpl span2 = createSampledEndedSpan(startEndHandler, SPAN_NAME_2);
    List<SpanData> exported1 = serviceHandler.waitForExport(2);
    List<SpanData> exported2 = serviceHandler2.waitForExport(2);
    assertThat(exported1).containsExactly(span1.toSpanData(), span2.toSpanData());
    assertThat(exported2).containsExactly(span1.toSpanData(), span2.toSpanData());
  }

  @Test
  public void exportNotSampledSpans() {
    SpanExporterImpl spanExporter = SpanExporterImpl.create(4, Duration.create(1, 0));
    StartEndHandler startEndHandler =
        new StartEndHandlerImpl(
            spanExporter, runningSpanStore, sampledSpanStore, new SimpleEventQueue());

    spanExporter.registerHandler("test.service", serviceHandler);

    RecordEventsSpanImpl span1 = createNotSampledEndedSpan(startEndHandler, SPAN_NAME_1);
    RecordEventsSpanImpl span2 = createSampledEndedSpan(startEndHandler, SPAN_NAME_2);
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
        new StartEndHandlerImpl(
            spanExporter, runningSpanStore, sampledSpanStore, new SimpleEventQueue());

    spanExporter.registerHandler("test.service", serviceHandler);

    RecordEventsSpanImpl span2 = createSampledEndedSpan(startEndHandler, SPAN_NAME_2);

    // Force a flush, without this, the #waitForExport() call below would block indefinitely.
    spanExporter.flush();

    List<SpanData> exported = serviceHandler.waitForExport(1);

    assertThat(exported).containsExactly(span2.toSpanData());
  }
}
