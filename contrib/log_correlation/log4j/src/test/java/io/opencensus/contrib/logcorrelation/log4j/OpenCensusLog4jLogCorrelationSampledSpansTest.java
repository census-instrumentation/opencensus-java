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
 * SpanSelection#SAMPLED_SPANS}.
 */
@RunWith(JUnit4.class)
public final class OpenCensusLog4jLogCorrelationSampledSpansTest
    extends AbstractOpenCensusLog4jLogCorrelationTest {

  @BeforeClass
  public static void setUp() {
    initializeLog4j(SpanSelection.SAMPLED_SPANS);
  }

  @Test
  public void addSampledSpanToLogEntryWithSampledSpans() {
    String log =
        logWithSpanAndLog4jConfiguration(
            TEST_PATTERN,
            SpanContext.create(
                TraceId.fromLowerBase16("0af7a7bef890695f1c5e85a8e7290164"),
                SpanId.fromLowerBase16("d3f07c467ec2fbb2"),
                TraceOptions.builder().setIsSampled(true).build(),
                EMPTY_TRACESTATE),
            new Function<Logger, Void>() {
              @Override
              public Void apply(Logger logger) {
                logger.error("message #1");
                return null;
              }
            });
    assertThat(log)
        .isEqualTo(
            "traceId=0af7a7bef890695f1c5e85a8e7290164 spanId=d3f07c467ec2fbb2 sampled=true ERROR "
                + "- message #1");
  }

  @Test
  public void doNotAddNonSampledSpanToLogEntryWithSampledSpans() {
    String log =
        logWithSpanAndLog4jConfiguration(
            TEST_PATTERN,
            SpanContext.create(
                TraceId.fromLowerBase16("9e09b559ebb8f7f7ed7451aff68cf441"),
                SpanId.fromLowerBase16("0fc9ef54c50a1816"),
                TraceOptions.builder().setIsSampled(false).build(),
                EMPTY_TRACESTATE),
            new Function<Logger, Void>() {
              @Override
              public Void apply(Logger logger) {
                logger.debug("message #2");
                return null;
              }
            });
    assertThat(log).isEqualTo("traceId= spanId= sampled= DEBUG - message #2");
  }
}
