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
import io.opencensus.common.Scope;
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
import io.opencensus.trace.Tracing;
import java.util.EnumSet;
import java.util.Map;
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

  private static Tracer tracer = Tracing.getTracer();

  @Test
  public void enhanceLogEntry_Sampled() {
    String projectId = "my-test-project-1";
    String traceId = "4c9874d0b41224cce77ff74ee10f5ee6";
    String spanId = "592ae363e92cb3dd";
    boolean isSampled = true;
    testLoggingEnhancer(projectId, traceId, spanId, isSampled);
  }

  @Test
  public void enhanceLogEntry_NotSampled() {
    String projectId = "my-test-project-2";
    String traceId = "7f4703d9bb02f4f2e67fb840103cdd34";
    String spanId = "2d7d95a555557434";
    boolean isSampled = false;
    testLoggingEnhancer(projectId, traceId, spanId, isSampled);
  }

  private static void testLoggingEnhancer(
      String projectId, String traceId, String spanId, boolean isSampled) {
    System.setProperty(GOOGLE_CLOUD_PROJECT, projectId);
    try {
      Scope scope =
          tracer.withSpan(
              new TestSpan(
                  SpanContext.create(
                      TraceId.fromLowerBase16(traceId),
                      SpanId.fromLowerBase16(spanId),
                      TraceOptions.builder().setIsSampled(isSampled).build())));
      try {
        LogEntry.Builder builder = LogEntry.newBuilder(null);
        new OpenCensusTraceLoggingEnhancer().enhanceLogEntry(builder);
        LogEntry logEntry = builder.build();
        assertThat(logEntry.getLabels().get("span_id")).isEqualTo(spanId);
        assertThat(logEntry.getLabels().get("sampled")).isEqualTo(isSampled ? "true" : "false");
        assertThat(logEntry.getTrace()).isEqualTo("projects/" + projectId + "/traces/" + traceId);
      } finally {
        scope.close();
      }
    } finally {
      System.clearProperty(GOOGLE_CLOUD_PROJECT);
    }
  }

  // TODO(sebright): Should the OpenCensusTraceLoggingEnhancer avoid adding tracing data when the
  // span is blank?
  @Test
  public void enhanceLogEntry_BlankSpan() {
    System.setProperty(GOOGLE_CLOUD_PROJECT, "my-test-project-3");
    try {
      Scope scope = tracer.withSpan(BlankSpan.INSTANCE);
      try {
        LogEntry.Builder builder = LogEntry.newBuilder(null);
        new OpenCensusTraceLoggingEnhancer().enhanceLogEntry(builder);
        LogEntry logEntry = builder.build();
        assertThat(logEntry.getLabels().get("span_id")).isEqualTo("0000000000000000");
        assertThat(logEntry.getLabels().get("sampled")).isEqualTo("false");
        assertThat(logEntry.getTrace())
            .isEqualTo("projects/my-test-project-3/traces/00000000000000000000000000000000");
      } finally {
        scope.close();
      }
    } finally {
      System.clearProperty(GOOGLE_CLOUD_PROJECT);
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
