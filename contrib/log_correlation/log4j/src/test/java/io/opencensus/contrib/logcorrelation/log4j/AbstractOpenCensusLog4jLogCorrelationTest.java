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

import io.opencensus.common.Scope;
import io.opencensus.contrib.logcorrelation.log4j.OpenCensusTraceContextDataInjector.SpanSelection;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.EndSpanOptions;
import io.opencensus.trace.Link;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracestate;
import io.opencensus.trace.Tracing;
import java.io.StringWriter;
import java.util.EnumSet;
import java.util.Map;
import java.util.function.Consumer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.StringLayout;
import org.apache.logging.log4j.core.appender.WriterAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 * Superclass for all Log4j log correlation test classes.
 *
 * <p>The tests are split into multiple classes so that each one can be run with a different value
 * for the system property {@link OpenCensusTraceContextDataInjector#SPAN_SELECTION_PROPERTY_NAME}.
 * The property must be set when Log4j initializes a static variable, and running each test class in
 * a separate JVM causes the static variable to be reinitialized.
 */
abstract class AbstractOpenCensusLog4jLogCorrelationTest {
  private static final Tracer tracer = Tracing.getTracer();

  static final String TEST_PATTERN =
      "traceId=%X{openCensusTraceId} spanId=%X{openCensusSpanId} "
          + "sampled=%X{openCensusTraceSampled} %-5level - %msg";

  static final Tracestate EMPTY_TRACESTATE = Tracestate.builder().build();

  private static Logger logger;

  // This method initializes Log4j after setting the SpanSelection, which means that Log4j
  // initializes a static variable with a ContextDataInjector that is constructed with the proper
  // SpanSelection. This method should be called from a @BeforeClass method in each subclass.
  static void initializeLog4j(SpanSelection spanSelection) {
    System.setProperty(
        OpenCensusTraceContextDataInjector.SPAN_SELECTION_PROPERTY_NAME, spanSelection.toString());
    logger = (Logger) LogManager.getLogger(AbstractOpenCensusLog4jLogCorrelationTest.class);
  }

  // Reconfigures Log4j using the given arguments and runs the function with the given SpanContext
  // in scope.
  String logWithSpanAndLog4jConfiguration(
      String log4jPattern, SpanContext spanContext, Consumer<Logger> loggingFunction) {
    StringWriter output = new StringWriter();
    StringLayout layout = PatternLayout.newBuilder().withPattern(log4jPattern).build();
    Appender appender =
        WriterAppender.newBuilder()
            .setTarget(output)
            .setLayout(layout)
            .setName("TestAppender")
            .build();
    ((LoggerContext) LogManager.getContext(false)).updateLoggers();
    appender.start();
    logger.addAppender(appender);
    logger.setLevel(Level.ALL);
    try {
      logWithSpan(spanContext, loggingFunction, logger);
      return output.toString();
    } finally {
      logger.removeAppender(appender);
    }
  }

  private static void logWithSpan(
      SpanContext spanContext, Consumer<Logger> loggingFunction, Logger logger) {
    Scope scope = tracer.withSpan(new TestSpan(spanContext));
    try {
      loggingFunction.accept(logger);
    } finally {
      scope.close();
    }
  }

  private static final class TestSpan extends Span {
    TestSpan(SpanContext context) {
      super(context, EnumSet.of(Options.RECORD_EVENTS));
    }

    @Override
    public void end(EndSpanOptions options) {}

    @Override
    public void addLink(Link link) {}

    @Override
    public void addAnnotation(Annotation annotation) {}

    @Override
    public void addAnnotation(String description, Map<String, AttributeValue> attributes) {}
  }
}
