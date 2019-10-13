/*
 * Copyright 2019, OpenCensus Authors
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

package io.opencensus.implcore.trace.propagation;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracestate;
import io.opencensus.trace.propagation.SpanContextParseException;
import io.opencensus.trace.propagation.TextFormat.Getter;
import io.opencensus.trace.propagation.TextFormat.Setter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link B3SingleFormat}. */
@RunWith(JUnit4.class)
public class B3SingleFormatTest {

  private static final Setter<Map<String, String>> setter =
      new Setter<Map<String, String>>() {
        @Override
        public void put(Map<String, String> carrier, String key, String value) {
          carrier.put(key, value);
        }
      };
  private static final Getter<Map<String, String>> getter =
      new Getter<Map<String, String>>() {
        @Nullable
        @Override
        public String get(Map<String, String> carrier, String key) {
          return carrier.get(key);
        }
      };
  private final B3SingleFormat format = new B3SingleFormat();
  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public final void verifyExtractFailsOnInsufficientComponents() throws SpanContextParseException {
    // given
    final Map<String, String> carrier = ImmutableMap.of("b3", "80f198ee56343ba864fe8b2a57d3eff7");

    // when / then
    thrown.expect(SpanContextParseException.class);
    format.extract(carrier, getter);
  }

  @Test
  public final void verifyExtractFailsOnInvalidTracestate() throws SpanContextParseException {
    // given
    final Map<String, String> carrier =
        ImmutableMap.of(
            "tracestate", "80f198ee56343ba864fe8b2a57d3eff7-05e3ac9a4f6e3b90-1-e457b5a2e4d86bd1");

    // when / then
    thrown.expect(SpanContextParseException.class);
    format.extract(carrier, getter);
  }

  @Test
  public final void verifyExtractFailsOnMissingHeaders() throws SpanContextParseException {
    // given
    final Map<String, String> carrier =
        ImmutableMap.of(
            "X-B3-TraceId",
            "80f198ee56343ba864fe8b2a57d3eff7",
            "X_B3_SPAN_ID",
            "05e3ac9a4f6e3b90",
            "X_B3_SAMPLED",
            "1",
            "X_B3_PARENT_SPAN_ID",
            "e457b5a2e4d86bd1");

    // when / then
    thrown.expect(SpanContextParseException.class);
    format.extract(carrier, getter);
  }

  @Test
  public final void verifyExtractParsesSampledSpanWithParentFromB3()
      throws SpanContextParseException {
    // given
    final Map<String, String> carrier =
        ImmutableMap.of(
            "b3", "80f198ee56343ba864fe8b2a57d3eff7-05e3ac9a4f6e3b90-1-e457b5a2e4d86bd1");

    // when
    final SpanContext result = format.extract(carrier, getter);

    // then
    assertEquals("80f198ee56343ba864fe8b2a57d3eff7", result.getTraceId().toLowerBase16());
    assertEquals("05e3ac9a4f6e3b90", result.getSpanId().toLowerBase16());
    assertTrue(result.getTraceOptions().isSampled());
  }

  @Test
  public final void verifyExtractParsesSampledSpanWithParentFromTracestate()
      throws SpanContextParseException {
    // given
    final Map<String, String> carrier =
        ImmutableMap.of(
            "tracestate",
            "b3=80f198ee56343ba864fe8b2a57d3eff7-05e3ac9a4f6e3b90-1-e457b5a2e4d86bd1");

    // when
    final SpanContext result = format.extract(carrier, getter);

    // then
    assertEquals("80f198ee56343ba864fe8b2a57d3eff7", result.getTraceId().toLowerBase16());
    assertEquals("05e3ac9a4f6e3b90", result.getSpanId().toLowerBase16());
    assertTrue(result.getTraceOptions().isSampled());
  }

  @Test
  public final void verifyExtractParsesSampledRootSpanFromB3() throws SpanContextParseException {
    // given
    final Map<String, String> carrier =
        ImmutableMap.of("b3", "4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-1");

    // when
    final SpanContext result = format.extract(carrier, getter);

    // then
    assertEquals("4bf92f3577b34da6a3ce929d0e0e4736", result.getTraceId().toLowerBase16());
    assertEquals("00f067aa0ba902b7", result.getSpanId().toLowerBase16());
    assertTrue(result.getTraceOptions().isSampled());
  }

  @Test
  public final void verifyExtractParsesSampledRootSpanFromTracestate()
      throws SpanContextParseException {
    // given
    final Map<String, String> carrier =
        ImmutableMap.of("tracestate", "b3=4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7-1");

    // when
    final SpanContext result = format.extract(carrier, getter);

    // then
    assertEquals("4bf92f3577b34da6a3ce929d0e0e4736", result.getTraceId().toLowerBase16());
    assertEquals("00f067aa0ba902b7", result.getSpanId().toLowerBase16());
    assertTrue(result.getTraceOptions().isSampled());
  }

  @Test
  public final void verifyExtractParsesNotYetSampledRootSpanFromB3()
      throws SpanContextParseException {
    // given
    final Map<String, String> carrier =
        ImmutableMap.of("b3", "4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7");

    // when
    final SpanContext result = format.extract(carrier, getter);

    // then
    assertEquals("4bf92f3577b34da6a3ce929d0e0e4736", result.getTraceId().toLowerBase16());
    assertEquals("00f067aa0ba902b7", result.getSpanId().toLowerBase16());
    assertFalse(result.getTraceOptions().isSampled());
  }

  @Test
  public final void verifyExtractParsesNotYetSampledRootSpanFromTracestate()
      throws SpanContextParseException {
    // given
    final Map<String, String> carrier =
        ImmutableMap.of("tracestate", "b3=4bf92f3577b34da6a3ce929d0e0e4736-00f067aa0ba902b7");

    // when
    final SpanContext result = format.extract(carrier, getter);

    // then
    assertEquals("4bf92f3577b34da6a3ce929d0e0e4736", result.getTraceId().toLowerBase16());
    assertEquals("00f067aa0ba902b7", result.getSpanId().toLowerBase16());
    assertFalse(result.getTraceOptions().isSampled());
  }

  @Test
  public final void verifyExtractParsesDebugSpanFromB3() throws SpanContextParseException {
    // given
    final Map<String, String> carrier =
        ImmutableMap.of(
            "b3", "80f198ee56343ba864fe8b2a57d3eff7-e457b5a2e4d86bd1-d-05e3ac9a4f6e3b90");

    // when
    final SpanContext result = format.extract(carrier, getter);

    // then
    assertEquals("80f198ee56343ba864fe8b2a57d3eff7", result.getTraceId().toLowerBase16());
    assertEquals("e457b5a2e4d86bd1", result.getSpanId().toLowerBase16());
    assertFalse(result.getTraceOptions().isSampled());
  }

  @Test
  public final void verifyExtractParseDebugSpanFromTracestate() throws SpanContextParseException {
    // given
    final Map<String, String> carrier =
        ImmutableMap.of(
            "tracestate",
            "b3=80f198ee56343ba864fe8b2a57d3eff7-e457b5a2e4d86bd1-d-05e3ac9a4f6e3b90");

    // when
    final SpanContext result = format.extract(carrier, getter);

    // then
    assertEquals("80f198ee56343ba864fe8b2a57d3eff7", result.getTraceId().toLowerBase16());
    assertEquals("e457b5a2e4d86bd1", result.getSpanId().toLowerBase16());
    assertFalse(result.getTraceOptions().isSampled());
  }

  @Test
  public final void verifyInjectFormatsSampledSpan() {
    // given
    final TraceOptions options = TraceOptions.builder().setIsSampled(true).build();
    final SpanContext context =
        SpanContext.create(
            TraceId.fromLowerBase16("80f198ee56343ba864fe8b2a57d3eff7"),
            SpanId.fromLowerBase16("e457b5a2e4d86bd1"),
            options,
            Tracestate.builder().build());
    final Map<String, String> carrier = new HashMap<String, String>();

    // when
    format.inject(context, carrier, setter);

    // then
    assertEquals("80f198ee56343ba864fe8b2a57d3eff7-e457b5a2e4d86bd1-1", carrier.get("b3"));
  }

  @Test
  public final void verifyInjectFormatsNonSampledSpan() {
    // given
    final TraceOptions options = TraceOptions.builder().setIsSampled(false).build();
    final SpanContext context =
        SpanContext.create(
            TraceId.fromLowerBase16("80f198ee56343ba864fe8b2a57d3eff7"),
            SpanId.fromLowerBase16("e457b5a2e4d86bd1"),
            options,
            Tracestate.builder().build());
    final Map<String, String> carrier = new HashMap<String, String>();

    // when
    format.inject(context, carrier, setter);

    // then
    assertEquals("80f198ee56343ba864fe8b2a57d3eff7-e457b5a2e4d86bd1-0", carrier.get("b3"));
  }

  @Test
  public final void verifyFieldsContainsB3() {
    // given
    // when
    final Collection<? extends String> fields = format.fields();

    // then
    assertThat(fields).containsExactly("b3");
  }
}
