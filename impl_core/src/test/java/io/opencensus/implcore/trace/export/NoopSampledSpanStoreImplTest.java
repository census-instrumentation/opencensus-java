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
import io.opencensus.trace.export.SampledSpanStore.ErrorFilter;
import io.opencensus.trace.export.SampledSpanStore.LatencyFilter;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link NoopSampledSpanStoreImpl}. */
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
  private final EnumSet<Options> recordSpanOptions = EnumSet.of(Options.RECORD_EVENTS);
  @Mock private StartEndHandler startEndHandler;
  private SpanImpl spanImpl;
  private ErrorFilter errorFilter = ErrorFilter.create(SPAN_NAME, null, 0);
  private LatencyFilter latencyFilter = LatencyFilter.create(SPAN_NAME, 0, 0, 0);
  private final SampledSpanStoreImpl sampledSpanStoreImpl =
      ExportComponentImpl.createWithoutInProcessStores(null).getSampledSpanStore();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    SpanImpl span =
        SpanImpl.startSpan(
            spanContext,
            recordSpanOptions,
            SPAN_NAME,
            null,
            false,
            TraceParams.DEFAULT,
            startEndHandler,
            timestampConverter,
            testClock);
  }

  private void getMethodsReturnsEmpty() {
    // get methods always return empty collections.
    assertThat(sampledSpanStoreImpl.getSummary().getPerSpanNameSummary()).isEmpty();
    assertThat(sampledSpanStoreImpl.getRegisteredSpanNamesForCollection()).isEmpty();
    assertThat(sampledSpanStoreImpl.getErrorSampledSpans(errorFilter)).isEmpty();
    assertThat(sampledSpanStoreImpl.getLatencySampledSpans(latencyFilter)).isEmpty();
  }

  @Test
  public void noopImplementation() {
    getMethodsReturnsEmpty();
    // considerForSampling() does not affect the result.
    sampledSpanStoreImpl.considerForSampling(spanImpl);
    getMethodsReturnsEmpty();
    // registerSpanNamesForCollection() does not affect the result.
    sampledSpanStoreImpl.registerSpanNamesForCollection(NAMES_FOR_COLLECTION);
    getMethodsReturnsEmpty();
    // unregisterSpanNamesForCollection() does not affect the result.
    sampledSpanStoreImpl.unregisterSpanNamesForCollection(NAMES_FOR_COLLECTION);
    getMethodsReturnsEmpty();
  }
}
