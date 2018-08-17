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

import io.opencensus.contrib.logcorrelation.log4j.OpenCensusTraceContextDataInjector.SpanSelection;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import org.apache.logging.log4j.ThreadContext;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for Log4j log correlation with {@link
 * OpenCensusTraceContextDataInjector#SPAN_SELECTION_PROPERTY_NAME} set to {@link
 * SpanSelection#ALL_SPANS}.
 */
// TODO(sebright): Add a test with non-empty configuration properties.
@RunWith(JUnit4.class)
public final class OpenCensusLog4jLogCorrelationTestAllSpans
    extends AbstractOpenCensusLog4jLogCorrelationTest {

  @BeforeClass
  public static void setUp() {
    initializeLog4j(SpanSelection.ALL_SPANS);
  }

  @Test
  public void addSampledSpanToLogEntryWithAllSpans() {
    String log =
        logWithSpanAndLog4jConfiguration(
            TEST_PATTERN,
            SpanContext.create(
                TraceId.fromLowerBase16("b9718fe3d82d36fce0e6a1ada1c21db0"),
                SpanId.fromLowerBase16("75159dde8c503fee"),
                TraceOptions.builder().setIsSampled(true).build(),
                EMPTY_TRACESTATE),
            logger -> {
              logger.warn("message #1");
            });
    assertThat(log)
        .isEqualTo(
            "traceId=b9718fe3d82d36fce0e6a1ada1c21db0 spanId=75159dde8c503fee "
                + "sampled=true WARN  - message #1");
  }

  @Test
  public void addNonSampledSpanToLogEntryWithAllSpans() {
    String log =
        logWithSpanAndLog4jConfiguration(
            TEST_PATTERN,
            SpanContext.create(
                TraceId.fromLowerBase16("cd7061dfa9d312cdcc42edab3feab51b"),
                SpanId.fromLowerBase16("117d42d4c7acd066"),
                TraceOptions.builder().setIsSampled(false).build(),
                EMPTY_TRACESTATE),
            logger -> {
              logger.info("message #2");
            });
    assertThat(log)
        .isEqualTo(
            "traceId=cd7061dfa9d312cdcc42edab3feab51b spanId=117d42d4c7acd066 sampled=false INFO  "
                + "- message #2");
  }

  @Test
  public void addBlankSpanToLogEntryWithAllSpans() {
    String log =
        logWithSpanAndLog4jConfiguration(
            TEST_PATTERN,
            SpanContext.INVALID,
            logger -> {
              logger.fatal("message #3");
            });
    assertThat(log)
        .isEqualTo(
            "traceId=00000000000000000000000000000000 spanId=0000000000000000 sampled=false FATAL "
                + "- message #3");
  }

  @Test
  public void preserveOtherKeyValuePairs() {
    String log =
        logWithSpanAndLog4jConfiguration(
            "%X{myTestKey} %-5level - %msg",
            SpanContext.create(
                TraceId.fromLowerBase16("c95329bb6b7de41afbc51a231c128f97"),
                SpanId.fromLowerBase16("bf22ea74d38eddad"),
                TraceOptions.builder().setIsSampled(true).build(),
                EMPTY_TRACESTATE),
            logger -> {
              String key = "myTestKey";
              ThreadContext.put(key, "myTestValue");
              try {
                logger.error("message #4");
              } finally {
                ThreadContext.remove(key);
              }
            });
    assertThat(log).isEqualTo("myTestValue ERROR - message #4");
  }
}
