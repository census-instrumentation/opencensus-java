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

package io.opencensus.implcore.trace.export;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.common.Timestamp;
import io.opencensus.implcore.internal.EventQueue;
import io.opencensus.implcore.internal.SimpleEventQueue;
import io.opencensus.implcore.internal.TimestampConverter;
import io.opencensus.implcore.trace.SpanImpl;
import io.opencensus.implcore.trace.SpanImpl.StartEndHandler;
import io.opencensus.testing.common.TestClock;
import io.opencensus.trace.Span.Options;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.export.RunningSpanStore.Filter;
import java.util.EnumSet;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link RunningSpanStoreImpl.NoopRunningSpanStoreImpl}. */
@RunWith(JUnit4.class)
public class NoopRunningSpanStoreImplTest {

  private static final String SPAN_NAME = "MySpanName";

  private final Timestamp timestamp = Timestamp.create(1234, 5678);
  private final Random random = new Random(1234);
  private final SpanContext spanContext =
      SpanContext.create(
          TraceId.generateRandomId(random), SpanId.generateRandomId(random), TraceOptions.DEFAULT);
  private final TestClock testClock = TestClock.create(timestamp);
  private final TimestampConverter timestampConverter = TimestampConverter.now(testClock);
  private final EnumSet<Options> recordSpanOptions = EnumSet.of(Options.RECORD_EVENTS);
  @Mock private StartEndHandler startEndHandler;
  private SpanImpl spanImpl;
  // maxSpansToReturn=0 means all
  private final Filter filter = Filter.create(SPAN_NAME, 0 /* maxSpansToReturn */);
  private final EventQueue eventQueue = new SimpleEventQueue();
  private final RunningSpanStoreImpl runningSpanStoreImpl =
      ExportComponentImpl.createWithoutInProcessStores(eventQueue).getRunningSpanStore();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    spanImpl =
        SpanImpl.startSpan(
            spanContext,
            recordSpanOptions,
            SPAN_NAME,
            null,
            null,
            false,
            TraceParams.DEFAULT,
            startEndHandler,
            timestampConverter,
            testClock);
  }

  private void getMethodsShouldReturnEmpty() {
    // get methods should always return empty collections.
    assertThat(runningSpanStoreImpl.getSummary().getPerSpanNameSummary()).isEmpty();
    assertThat(runningSpanStoreImpl.getRunningSpans(filter)).isEmpty();
  }

  @Test
  public void noopImplementation() {
    getMethodsShouldReturnEmpty();
    // onStart() does not affect the result.
    runningSpanStoreImpl.onStart(spanImpl);
    getMethodsShouldReturnEmpty();
    // onEnd() does not affect the result.
    runningSpanStoreImpl.onEnd(spanImpl);
    getMethodsShouldReturnEmpty();
  }
}
