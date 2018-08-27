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

package io.opencensus.contrib.logcorrelation.log4j;

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.common.Function;
import io.opencensus.contrib.logcorrelation.log4j.OpenCensusTraceContextDataInjector.SpanSelection;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import org.apache.logging.log4j.core.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Log4j log correlation with {@link
 * OpenCensusTraceContextDataInjector#SPAN_SELECTION_PROPERTY_NAME} set to {@link
 * SpanSelection#NO_SPANS}.
 */
@RunWith(JUnit4.class)
public final class OpenCensusLog4jLogCorrelationNoSpansTest
    extends AbstractOpenCensusLog4jLogCorrelationTest {

  @BeforeClass
  public static void setUp() {
    initializeLog4j(SpanSelection.NO_SPANS);
  }

  @Test
  public void doNotAddSampledSpanToLogEntryWithNoSpans() {
    String log =
        logWithSpanAndLog4jConfiguration(
            TEST_PATTERN,
            SpanContext.create(
                TraceId.fromLowerBase16("03d2ada98f6eb8330605a45a88c7e67d"),
                SpanId.fromLowerBase16("ce5b1cf09fe58bcb"),
                TraceOptions.builder().setIsSampled(true).build(),
                EMPTY_TRACESTATE),
            new Function<Logger, Void>() {
              @Override
              public Void apply(Logger logger) {
                logger.trace("message #1");
                return null;
              }
            });
    assertThat(log).isEqualTo("traceId= spanId= sampled= TRACE - message #1");
  }

  @Test
  public void doNotAddNonSampledSpanToLogEntryWithNoSpans() {
    String log =
        logWithSpanAndLog4jConfiguration(
            TEST_PATTERN,
            SpanContext.create(
                TraceId.fromLowerBase16("09664283d189791de5218ffe3be88d54"),
                SpanId.fromLowerBase16("a7203a50089a4029"),
                TraceOptions.builder().setIsSampled(false).build(),
                EMPTY_TRACESTATE),
            new Function<Logger, Void>() {
              @Override
              public Void apply(Logger logger) {
                logger.warn("message #2");
                return null;
              }
            });
    assertThat(log).isEqualTo("traceId= spanId= sampled= WARN  - message #2");
  }
}
