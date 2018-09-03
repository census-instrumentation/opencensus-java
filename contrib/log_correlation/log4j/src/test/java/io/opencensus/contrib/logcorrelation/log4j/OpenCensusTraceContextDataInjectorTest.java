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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.opencensus.contrib.logcorrelation.log4j.OpenCensusTraceContextDataInjector.SpanSelection;
import java.util.Collections;
import java.util.Map;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.SortedArrayStringMap;
import org.apache.logging.log4j.util.StringMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link OpenCensusTraceContextDataInjector}. */
@RunWith(JUnit4.class)
public final class OpenCensusTraceContextDataInjectorTest {

  @Test
  @SuppressWarnings("TruthConstantAsserts")
  public void spanSelectionPropertyName() {
    assertThat(OpenCensusTraceContextDataInjector.SPAN_SELECTION_PROPERTY_NAME)
        .isEqualTo(OpenCensusTraceContextDataInjector.class.getName() + ".spanSelection");
  }

  @Test
  public void traceIdKey() {
    assertThat(OpenCensusTraceContextDataInjector.TRACE_ID_CONTEXT_KEY)
        .isEqualTo("openCensusTraceId");
  }

  @Test
  public void spanIdKey() {
    assertThat(OpenCensusTraceContextDataInjector.SPAN_ID_CONTEXT_KEY)
        .isEqualTo("openCensusSpanId");
  }

  @Test
  public void traceSampledKey() {
    assertThat(OpenCensusTraceContextDataInjector.TRACE_SAMPLED_CONTEXT_KEY)
        .isEqualTo("openCensusTraceSampled");
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
            toMap(
                new OpenCensusTraceContextDataInjector()
                    .injectContextData(
                        Lists.newArrayList(
                            Property.createProperty("property1", "value1"),
                            Property.createProperty("property2", "value2")),
                        new SortedArrayStringMap())))
        .containsExactly(
            "property1",
            "value1",
            "property2",
            "value2",
            "openCensusTraceId",
            "00000000000000000000000000000000",
            "openCensusSpanId",
            "0000000000000000",
            "openCensusTraceSampled",
            "false");
  }

  @Test
  public void handleEmptyConfigurationProperties() {
    assertContainsOnlyDefaultTracingEntries(
        new OpenCensusTraceContextDataInjector()
            .injectContextData(Collections.<Property>emptyList(), new SortedArrayStringMap()));
  }

  @Test
  public void handleNullConfigurationProperties() {
    assertContainsOnlyDefaultTracingEntries(
        new OpenCensusTraceContextDataInjector()
            .injectContextData(null, new SortedArrayStringMap()));
  }

  private static void assertContainsOnlyDefaultTracingEntries(StringMap stringMap) {
    assertThat(toMap(stringMap))
        .containsExactly(
            "openCensusTraceId",
            "00000000000000000000000000000000",
            "openCensusSpanId",
            "0000000000000000",
            "openCensusTraceSampled",
            "false");
  }

  private static Map<String, String> toMap(StringMap stringMap) {
    final ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
    stringMap.forEach(
        new BiConsumer<String, String>() {
          @Override
          public void accept(String key, String value) {
            builder.put(key, value);
          }
        });
    return builder.build();
  }
}
