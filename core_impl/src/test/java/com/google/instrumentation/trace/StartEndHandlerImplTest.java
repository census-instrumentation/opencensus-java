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

import com.google.instrumentation.common.SimpleEventQueue;
import com.google.instrumentation.internal.MillisClock;
import com.google.instrumentation.trace.Span.Options;
import com.google.instrumentation.trace.testing.FakeServiceHandler;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link StartEndHandlerImpl}. */
@RunWith(JUnit4.class)
public class StartEndHandlerImplTest {
  private static final String SPAN_NAME_1 = "MySpanName/1";
  private static final String SPAN_NAME_2 = "MySpanName/2";
  private final Random random = new Random(1234);
  private final SpanContext spanContext =
      SpanContext.create(
          TraceId.generateRandomId(random),
          SpanId.generateRandomId(random),
          TraceOptions.builder().setIsSampled().build());
  private final FakeServiceHandler serviceHandler = new FakeServiceHandler();
  private final TraceExporterImpl traceExporter = TraceExporterImpl.create(4, 1000);
  private final StartEndHandlerImpl startEndHandler =
      new StartEndHandlerImpl(new SimpleEventQueue(), traceExporter);

  @Before
  public void setUp() {
    traceExporter.registerServiceHandler("test.service", serviceHandler);
  }

  private final SpanImpl createEndedSpan(String spanName) {
    SpanImpl span =
        SpanImpl.startSpan(
            spanContext,
            EnumSet.of(Options.RECORD_EVENTS),
            spanName,
            null,
            false,
            startEndHandler,
            null,
            MillisClock.getInstance());
    span.end();
    return span;
  }

  @Test
  public void exportSpans() {
    SpanImpl span1 = createEndedSpan(SPAN_NAME_1);
    SpanImpl span2 = createEndedSpan(SPAN_NAME_2);
    List<SpanData> exported = serviceHandler.waitForExport(2);
    assertThat(exported.size()).isEqualTo(2);
    assertThat(exported.get(0)).isEqualTo(span1.toSpanData());
    assertThat(exported.get(1)).isEqualTo(span2.toSpanData());
  }
}
