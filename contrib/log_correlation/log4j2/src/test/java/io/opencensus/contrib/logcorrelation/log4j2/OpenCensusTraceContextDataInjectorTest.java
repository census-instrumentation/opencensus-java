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

package io.opencensus.contrib.logcorrelation.log4j2;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.Lists;
import io.opencensus.common.Scope;
import io.opencensus.contrib.logcorrelation.log4j2.OpenCensusTraceContextDataInjector.SpanSelection;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracestate;
import io.opencensus.trace.Tracing;
import java.util.Collections;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.util.SortedArrayStringMap;
import org.apache.logging.log4j.util.StringMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link OpenCensusTraceContextDataInjector}. */
@RunWith(JUnit4.class)
public final class OpenCensusTraceContextDataInjectorTest {
  static final Tracestate EMPTY_TRACESTATE = Tracestate.builder().build();

  private final Tracer tracer = Tracing.getTracer();

  @Test
  @SuppressWarnings("TruthConstantAsserts")
  public void spanSelectionPropertyName() {
    assertThat(OpenCensusTraceContextDataInjector.SPAN_SELECTION_PROPERTY_NAME)
        .isEqualTo(OpenCensusTraceContextDataInjector.class.getName() + ".spanSelection");
  }

  @Test
  public void traceIdKey() {
    assertThat(OpenCensusTraceContextDataInjector.TRACE_ID_CONTEXT_KEY)
        .isEqualTo("opencensusTraceId");
  }

  @Test
  public void spanIdKey() {
    assertThat(OpenCensusTraceContextDataInjector.SPAN_ID_CONTEXT_KEY)
        .isEqualTo("opencensusSpanId");
  }

  @Test
  public void traceSampledKey() {
    assertThat(OpenCensusTraceContextDataInjector.TRACE_SAMPLED_CONTEXT_KEY)
        .isEqualTo("opencensusTraceSampled");
  }

  @Test
  public void spanSelectionDefaultIsAllSpans() {
    assertThat(new OpenCensusTraceContextDataInjector().getSpanSelection())
        .isEqualTo(SpanSelection.ALL_SPANS);
  }

  @Test
  public void setSpanSelectionWithSystemProperty() {
    try {
      System.setProperty(
          OpenCensusTraceContextDataInjector.SPAN_SELECTION_PROPERTY_NAME, "NO_SPANS");
      assertThat(new OpenCensusTraceContextDataInjector().getSpanSelection())
          .isEqualTo(SpanSelection.NO_SPANS);
    } finally {
      System.clearProperty(OpenCensusTraceContextDataInjector.SPAN_SELECTION_PROPERTY_NAME);
    }
  }

  @Test
  public void useDefaultValueForInvalidSpanSelection() {
    try {
      System.setProperty(
          OpenCensusTraceContextDataInjector.SPAN_SELECTION_PROPERTY_NAME,
          "INVALID_SPAN_SELECTION");
      assertThat(new OpenCensusTraceContextDataInjector().getSpanSelection())
          .isEqualTo(SpanSelection.ALL_SPANS);
    } finally {
      System.clearProperty(OpenCensusTraceContextDataInjector.SPAN_SELECTION_PROPERTY_NAME);
    }
  }

  @Test
  public void insertConfigurationProperties() {
    assertThat(
            new OpenCensusTraceContextDataInjector(SpanSelection.ALL_SPANS)
                .injectContextData(
                    Lists.newArrayList(
                        Property.createProperty("property1", "value1"),
                        Property.createProperty("property2", "value2")),
                    new SortedArrayStringMap())
                .toMap())
        .containsExactly(
            "property1",
            "value1",
            "property2",
            "value2",
            "opencensusTraceId",
            "00000000000000000000000000000000",
            "opencensusSpanId",
            "0000000000000000",
            "opencensusTraceSampled",
            "false");
  }

  @Test
  public void handleEmptyConfigurationProperties() {
    assertContainsOnlyDefaultTracingEntries(
        new OpenCensusTraceContextDataInjector(SpanSelection.ALL_SPANS)
            .injectContextData(Collections.<Property>emptyList(), new SortedArrayStringMap()));
  }

  @Test
  public void handleNullConfigurationProperties() {
    assertContainsOnlyDefaultTracingEntries(
        new OpenCensusTraceContextDataInjector(SpanSelection.ALL_SPANS)
            .injectContextData(null, new SortedArrayStringMap()));
  }

  private static void assertContainsOnlyDefaultTracingEntries(StringMap stringMap) {
    assertThat(stringMap.toMap())
        .containsExactly(
            "opencensusTraceId",
            "00000000000000000000000000000000",
            "opencensusSpanId",
            "0000000000000000",
            "opencensusTraceSampled",
            "false");
  }

  @Test
  public void rawContextDataWithTracingData() {
    OpenCensusTraceContextDataInjector plugin =
        new OpenCensusTraceContextDataInjector(SpanSelection.ALL_SPANS);
    SpanContext spanContext =
        SpanContext.create(
            TraceId.fromLowerBase16("e17944156660f55b8cae5ce3f45d4a40"),
            SpanId.fromLowerBase16("fc3d2ba0d283b66a"),
            TraceOptions.builder().setIsSampled(true).build(),
            EMPTY_TRACESTATE);
    Scope scope = tracer.withSpan(new TestSpan(spanContext));
    try {
      String key = "myTestKey";
      ThreadContext.put(key, "myTestValue");
      try {
        assertThat(plugin.rawContextData().toMap())
            .containsExactly(
                "myTestKey",
                "myTestValue",
                "opencensusTraceId",
                "e17944156660f55b8cae5ce3f45d4a40",
                "opencensusSpanId",
                "fc3d2ba0d283b66a",
                "opencensusTraceSampled",
                "true");
      } finally {
        ThreadContext.remove(key);
      }
    } finally {
      scope.close();
    }
  }

  @Test
  public void rawContextDataWithoutTracingData() {
    OpenCensusTraceContextDataInjector plugin =
        new OpenCensusTraceContextDataInjector(SpanSelection.NO_SPANS);
    SpanContext spanContext =
        SpanContext.create(
            TraceId.fromLowerBase16("ea236000f6d387fe7c06c5a6d6458b53"),
            SpanId.fromLowerBase16("f3b39dbbadb73074"),
            TraceOptions.builder().setIsSampled(true).build(),
            EMPTY_TRACESTATE);
    Scope scope = tracer.withSpan(new TestSpan(spanContext));
    try {
      String key = "myTestKey";
      ThreadContext.put(key, "myTestValue");
      try {
        assertThat(plugin.rawContextData().toMap()).containsExactly("myTestKey", "myTestValue");
      } finally {
        ThreadContext.remove(key);
      }
    } finally {
      scope.close();
    }
  }
}
