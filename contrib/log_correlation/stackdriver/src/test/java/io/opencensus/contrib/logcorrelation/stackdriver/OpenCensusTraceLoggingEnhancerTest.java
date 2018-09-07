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

package io.opencensus.contrib.logcorrelation.stackdriver;

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.logging.LogEntry;
import com.google.cloud.logging.LoggingEnhancer;
import com.google.common.base.Charsets;
import com.google.common.io.CharSource;
import io.opencensus.common.Scope;
import io.opencensus.contrib.logcorrelation.stackdriver.OpenCensusTraceLoggingEnhancer.SpanSelection;
import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.BlankSpan;
import io.opencensus.trace.EndSpanOptions;
import io.opencensus.trace.Link;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracestate;
import io.opencensus.trace.Tracing;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.Map;
import java.util.logging.LogManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Test for {@link OpenCensusTraceLoggingEnhancer}. */
// TODO(sebright): Find a way to test that OpenCensusTraceLoggingEnhancer is called from Stackdriver
// logging. See
// https://github.com/GoogleCloudPlatform/google-cloud-java/blob/master/TESTING.md#testing-code-that-uses-logging.
@RunWith(JUnit4.class)
public class OpenCensusTraceLoggingEnhancerTest {
  private static final String GOOGLE_CLOUD_PROJECT = "GOOGLE_CLOUD_PROJECT";
  private static final Tracestate EMPTY_TRACESTATE = Tracestate.builder().build();

  private static final Tracer tracer = Tracing.getTracer();

  @Test
  public void enhanceLogEntry_DoNotAddSampledSpanToLogEntryWithNoSpans() {
    LogEntry logEntry =
        getEnhancedLogEntry(
            new OpenCensusTraceLoggingEnhancer("my-test-project-1", SpanSelection.NO_SPANS),
            new TestSpan(
                SpanContext.create(
                    TraceId.fromLowerBase16("3da31be987098abb08c71c7700d2680e"),
                    SpanId.fromLowerBase16("51b109f15e0d3881"),
                    TraceOptions.builder().setIsSampled(true).build(),
                    EMPTY_TRACESTATE)));
    assertContainsNoTracingData(logEntry);
  }

  @Test
  public void enhanceLogEntry_AddSampledSpanToLogEntryWithSampledSpans() {
    LogEntry logEntry =
        getEnhancedLogEntry(
            new OpenCensusTraceLoggingEnhancer("my-test-project-2", SpanSelection.SAMPLED_SPANS),
            new TestSpan(
                SpanContext.create(
                    TraceId.fromLowerBase16("4c9874d0b41224cce77ff74ee10f5ee6"),
                    SpanId.fromLowerBase16("592ae363e92cb3dd"),
                    TraceOptions.builder().setIsSampled(true).build(),
                    EMPTY_TRACESTATE)));
    assertThat(logEntry.getLabels()).containsEntry("opencensusTraceSampled", "true");
    assertThat(logEntry.getTrace())
        .isEqualTo("projects/my-test-project-2/traces/4c9874d0b41224cce77ff74ee10f5ee6");
    assertThat(logEntry.getSpanId()).isEqualTo("592ae363e92cb3dd");
  }

  @Test
  public void enhanceLogEntry_AddSampledSpanToLogEntryWithAllSpans() {
    LogEntry logEntry =
        getEnhancedLogEntry(
            new OpenCensusTraceLoggingEnhancer("my-test-project-3", SpanSelection.ALL_SPANS),
            new TestSpan(
                SpanContext.create(
                    TraceId.fromLowerBase16("4c6af40c499951eb7de2777ba1e4fefa"),
                    SpanId.fromLowerBase16("de52e84d13dd232d"),
                    TraceOptions.builder().setIsSampled(true).build(),
                    EMPTY_TRACESTATE)));
    assertThat(logEntry.getLabels()).containsEntry("opencensusTraceSampled", "true");
    assertThat(logEntry.getTrace())
        .isEqualTo("projects/my-test-project-3/traces/4c6af40c499951eb7de2777ba1e4fefa");
    assertThat(logEntry.getSpanId()).isEqualTo("de52e84d13dd232d");
  }

  @Test
  public void enhanceLogEntry_DoNotAddNonSampledSpanToLogEntryWithNoSpans() {
    LogEntry logEntry =
        getEnhancedLogEntry(
            new OpenCensusTraceLoggingEnhancer("my-test-project-4", SpanSelection.NO_SPANS),
            new TestSpan(
                SpanContext.create(
                    TraceId.fromLowerBase16("88ab22b18b97369df065ca830e41cf6a"),
                    SpanId.fromLowerBase16("8987d372039021fd"),
                    TraceOptions.builder().setIsSampled(false).build(),
                    EMPTY_TRACESTATE)));
    assertContainsNoTracingData(logEntry);
  }

  @Test
  public void enhanceLogEntry_DoNotAddNonSampledSpanToLogEntryWithSampledSpans() {
    LogEntry logEntry =
        getEnhancedLogEntry(
            new OpenCensusTraceLoggingEnhancer("my-test-project-5", SpanSelection.SAMPLED_SPANS),
            new TestSpan(
                SpanContext.create(
                    TraceId.fromLowerBase16("7f4703d9bb02f4f2e67fb840103cdd34"),
                    SpanId.fromLowerBase16("2d7d95a555557434"),
                    TraceOptions.builder().setIsSampled(false).build(),
                    EMPTY_TRACESTATE)));
    assertContainsNoTracingData(logEntry);
  }

  @Test
  public void enhanceLogEntry_AddNonSampledSpanToLogEntryWithAllSpans() {
    LogEntry logEntry =
        getEnhancedLogEntry(
            new OpenCensusTraceLoggingEnhancer("my-test-project-6", SpanSelection.ALL_SPANS),
            new TestSpan(
                SpanContext.create(
                    TraceId.fromLowerBase16("72c905c76f99e99974afd84dc053a480"),
                    SpanId.fromLowerBase16("731e102335b7a5a0"),
                    TraceOptions.builder().setIsSampled(false).build(),
                    EMPTY_TRACESTATE)));
    assertThat(logEntry.getLabels()).containsEntry("opencensusTraceSampled", "false");
    assertThat(logEntry.getTrace())
        .isEqualTo("projects/my-test-project-6/traces/72c905c76f99e99974afd84dc053a480");
    assertThat(logEntry.getSpanId()).isEqualTo("731e102335b7a5a0");
  }

  @Test
  public void enhanceLogEntry_AddBlankSpanToLogEntryWithAllSpans() {
    LogEntry logEntry =
        getEnhancedLogEntry(
            new OpenCensusTraceLoggingEnhancer("my-test-project-7", SpanSelection.ALL_SPANS),
            BlankSpan.INSTANCE);
    assertThat(logEntry.getLabels().get("opencensusTraceSampled")).isEqualTo("false");
    assertThat(logEntry.getTrace())
        .isEqualTo("projects/my-test-project-7/traces/00000000000000000000000000000000");
    assertThat(logEntry.getSpanId()).isEqualTo("0000000000000000");
  }

  @Test
  public void enhanceLogEntry_ConvertNullProjectIdToEmptyString() {
    LogEntry logEntry =
        getEnhancedLogEntry(
            new OpenCensusTraceLoggingEnhancer(null, SpanSelection.ALL_SPANS),
            new TestSpan(
                SpanContext.create(
                    TraceId.fromLowerBase16("bfb4248a24325a905873a1d43001d9a0"),
                    SpanId.fromLowerBase16("6f23f9afd448e272"),
                    TraceOptions.builder().setIsSampled(true).build(),
                    EMPTY_TRACESTATE)));
    assertThat(logEntry.getTrace()).isEqualTo("projects//traces/bfb4248a24325a905873a1d43001d9a0");
  }

  private static LogEntry getEnhancedLogEntry(LoggingEnhancer loggingEnhancer, Span span) {
    Scope scope = tracer.withSpan(span);
    try {
      LogEntry.Builder builder = LogEntry.newBuilder(null);
      loggingEnhancer.enhanceLogEntry(builder);
      return builder.build();
    } finally {
      scope.close();
    }
  }

  private static void assertContainsNoTracingData(LogEntry logEntry) {
    assertThat(logEntry.getLabels()).doesNotContainKey("opencensusTraceSampled");
    assertThat(logEntry.getTrace()).isNull();
    assertThat(logEntry.getSpanId()).isNull();
  }

  @Test
  public void spanSelectionDefaultIsAllSpans() {
    assertThat(new OpenCensusTraceLoggingEnhancer().getSpanSelection())
        .isEqualTo(SpanSelection.ALL_SPANS);
  }

  @Test
  @SuppressWarnings("TruthConstantAsserts")
  public void projectIdPropertyName() {
    assertThat(OpenCensusTraceLoggingEnhancer.PROJECT_ID_PROPERTY_NAME)
        .isEqualTo(OpenCensusTraceLoggingEnhancer.class.getName() + ".projectId");
  }

  @Test
  @SuppressWarnings("TruthConstantAsserts")
  public void spanSelectionPropertyName() {
    assertThat(OpenCensusTraceLoggingEnhancer.SPAN_SELECTION_PROPERTY_NAME)
        .isEqualTo(OpenCensusTraceLoggingEnhancer.class.getName() + ".spanSelection");
  }

  @Test
  public void setProjectIdWithGoogleCloudJava() {
    try {
      System.setProperty(GOOGLE_CLOUD_PROJECT, "my-project-id");
      assertThat(new OpenCensusTraceLoggingEnhancer().getProjectId()).isEqualTo("my-project-id");
    } finally {
      System.clearProperty(GOOGLE_CLOUD_PROJECT);
    }
  }

  @Test
  public void overrideProjectIdWithSystemProperty() {
    try {
      System.setProperty(
          OpenCensusTraceLoggingEnhancer.PROJECT_ID_PROPERTY_NAME, "project ID override");
      try {
        System.setProperty(GOOGLE_CLOUD_PROJECT, "GOOGLE_CLOUD_PROJECT project ID");
        assertThat(new OpenCensusTraceLoggingEnhancer().getProjectId())
            .isEqualTo("project ID override");
      } finally {
        System.clearProperty(GOOGLE_CLOUD_PROJECT);
      }
    } finally {
      System.clearProperty(OpenCensusTraceLoggingEnhancer.PROJECT_ID_PROPERTY_NAME);
    }
  }

  @Test
  public void setSpanSelectionWithSystemProperty() {
    try {
      System.setProperty(OpenCensusTraceLoggingEnhancer.SPAN_SELECTION_PROPERTY_NAME, "NO_SPANS");
      assertThat(new OpenCensusTraceLoggingEnhancer().getSpanSelection())
          .isEqualTo(SpanSelection.NO_SPANS);
    } finally {
      System.clearProperty(OpenCensusTraceLoggingEnhancer.SPAN_SELECTION_PROPERTY_NAME);
    }
  }

  @Test
  public void overrideProjectIdWithLoggingProperty() throws IOException {
    try {
      LogManager.getLogManager()
          .readConfiguration(
              stringToInputStream(
                  OpenCensusTraceLoggingEnhancer.PROJECT_ID_PROPERTY_NAME + "=PROJECT_OVERRIDE"));
      try {
        System.setProperty(GOOGLE_CLOUD_PROJECT, "GOOGLE_CLOUD_PROJECT project ID");
        assertThat(new OpenCensusTraceLoggingEnhancer().getProjectId())
            .isEqualTo("PROJECT_OVERRIDE");
      } finally {
        System.clearProperty(GOOGLE_CLOUD_PROJECT);
      }
    } finally {
      LogManager.getLogManager().reset();
    }
  }

  @Test
  public void setSpanSelectionWithLoggingProperty() throws IOException {
    try {
      LogManager.getLogManager()
          .readConfiguration(
              stringToInputStream(
                  OpenCensusTraceLoggingEnhancer.SPAN_SELECTION_PROPERTY_NAME + "=SAMPLED_SPANS"));
      assertThat(new OpenCensusTraceLoggingEnhancer().getSpanSelection())
          .isEqualTo(SpanSelection.SAMPLED_SPANS);
    } finally {
      LogManager.getLogManager().reset();
    }
  }

  @Test
  public void loggingPropertyTakesPrecedenceOverSystemProperty() throws IOException {
    try {
      LogManager.getLogManager()
          .readConfiguration(
              stringToInputStream(
                  OpenCensusTraceLoggingEnhancer.SPAN_SELECTION_PROPERTY_NAME + "=NO_SPANS"));
      try {
        System.setProperty(
            OpenCensusTraceLoggingEnhancer.SPAN_SELECTION_PROPERTY_NAME, "SAMPLED_SPANS");
        assertThat(new OpenCensusTraceLoggingEnhancer().getSpanSelection())
            .isEqualTo(SpanSelection.NO_SPANS);
      } finally {
        System.clearProperty(OpenCensusTraceLoggingEnhancer.SPAN_SELECTION_PROPERTY_NAME);
      }
    } finally {
      LogManager.getLogManager().reset();
    }
  }

  private static InputStream stringToInputStream(String contents) throws IOException {
    return CharSource.wrap(contents).asByteSource(Charsets.UTF_8).openBufferedStream();
  }

  @Test
  public void useDefaultValueForInvalidSpanSelection() {
    try {
      System.setProperty(
          OpenCensusTraceLoggingEnhancer.SPAN_SELECTION_PROPERTY_NAME, "INVALID_SPAN_SELECTION");
      assertThat(new OpenCensusTraceLoggingEnhancer().getSpanSelection())
          .isEqualTo(SpanSelection.ALL_SPANS);
    } finally {
      System.clearProperty(OpenCensusTraceLoggingEnhancer.SPAN_SELECTION_PROPERTY_NAME);
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
