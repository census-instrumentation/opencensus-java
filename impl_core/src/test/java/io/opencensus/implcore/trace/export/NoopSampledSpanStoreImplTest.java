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
import io.opencensus.implcore.trace.RecordEventsSpanImpl;
import io.opencensus.implcore.trace.RecordEventsSpanImpl.StartEndHandler;
import io.opencensus.testing.common.TestClock;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.config.TraceParams;
import io.opencensus.trace.export.SampledSpanStore.ErrorFilter;
import io.opencensus.trace.export.SampledSpanStore.LatencyFilter;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link SampledSpanStoreImpl.NoopSampledSpanStoreImpl}. */
@RunWith(JUnit4.class)
public final class NoopSampledSpanStoreImplTest {

  private static final String SPAN_NAME = "MySpanName";
  private static final Collection<String> NAMES_FOR_COLLECTION =
      Collections.<String>singletonList(SPAN_NAME);

  private final Timestamp timestamp = Timestamp.create(1234, 5678);
  private final Random random = new Random(1234);
  private final SpanContext spanContext =
      SpanContext.create(
          TraceId.generateRandomId(random), SpanId.generateRandomId(random), TraceOptions.DEFAULT);
  private final TestClock testClock = TestClock.create(timestamp);
  private final TimestampConverter timestampConverter = TimestampConverter.now(testClock);
  @Mock private StartEndHandler startEndHandler;
  private RecordEventsSpanImpl recordEventsSpanImpl;
  // maxSpansToReturn=0 means all
  private final ErrorFilter errorFilter =
      ErrorFilter.create(SPAN_NAME, null /* canonicalCode */, 0 /* maxSpansToReturn */);
  private final LatencyFilter latencyFilter =
      LatencyFilter.create(
          SPAN_NAME,
          0 /* latencyLowerNs */,
          Long.MAX_VALUE /* latencyUpperNs */,
          0 /* maxSpansToReturn */);
  private final EventQueue eventQueue = new SimpleEventQueue();
  private final SampledSpanStoreImpl sampledSpanStoreImpl =
      ExportComponentImpl.createWithoutInProcessStores(eventQueue).getSampledSpanStore();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  private void getMethodsShouldReturnEmpty() {
    // get methods always return empty collections.
    assertThat(sampledSpanStoreImpl.getSummary().getPerSpanNameSummary()).isEmpty();
    assertThat(sampledSpanStoreImpl.getRegisteredSpanNamesForCollection()).isEmpty();
    assertThat(sampledSpanStoreImpl.getErrorSampledSpans(errorFilter)).isEmpty();
    assertThat(sampledSpanStoreImpl.getLatencySampledSpans(latencyFilter)).isEmpty();
  }

  @Test
  public void noopImplementation() {
    // None of the get methods should yield non-empty result.
    getMethodsShouldReturnEmpty();

    // registerSpanNamesForCollection() should do nothing and do not affect the result.
    sampledSpanStoreImpl.registerSpanNamesForCollection(NAMES_FOR_COLLECTION);
    getMethodsShouldReturnEmpty();

    // considerForSampling() should do nothing and do not affect the result.
    // It should be called after registerSpanNamesForCollection.
    recordEventsSpanImpl =
        RecordEventsSpanImpl.startSpan(
            spanContext,
            SPAN_NAME,
            null,
            null,
            false,
            TraceParams.DEFAULT,
            startEndHandler,
            timestampConverter,
            testClock);
    recordEventsSpanImpl.end();
    sampledSpanStoreImpl.considerForSampling(recordEventsSpanImpl);
    getMethodsShouldReturnEmpty();

    // unregisterSpanNamesForCollection() should do nothing and do not affect the result.
    sampledSpanStoreImpl.unregisterSpanNamesForCollection(NAMES_FOR_COLLECTION);
    getMethodsShouldReturnEmpty();
  }
}
