/*
 * Copyright 2017, OpenCensus Authors
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

import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.propagation.B3Format;
import io.opencensus.trace.propagation.B3Format.HeaderName;
import java.text.ParseException;
import java.util.EnumMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link B3FormatImpl}. */
@RunWith(JUnit4.class)
public class B3FormatImplTest {
  private static final String TRACE_ID_BASE16 = "ff000000000000000000000000000041";
  private static final TraceId TRACE_ID = TraceId.fromLowerBase16(TRACE_ID_BASE16);
  private static final String SPAN_ID_BASE16 = "ff00000000000041";
  private static final SpanId SPAN_ID = SpanId.fromLowerBase16(SPAN_ID_BASE16);
  private static final byte[] TRACE_OPTIONS_BYTES = new byte[] {1};
  private static final TraceOptions TRACE_OPTIONS = TraceOptions.fromBytes(TRACE_OPTIONS_BYTES);
  private static final SpanContext SPAN_CONTEXT =
      SpanContext.create(TRACE_ID, SPAN_ID, TRACE_OPTIONS);
  private static final EnumMap<HeaderName, String> headers =
      new EnumMap<HeaderName, String>(HeaderName.class);
  private final B3Format b3Format = new B3FormatImpl();

  @Before
  public void setUp() {
    headers.put(HeaderName.X_B3_TRACE_ID, TRACE_ID_BASE16);
    headers.put(HeaderName.X_B3_SPAN_ID, SPAN_ID_BASE16);
    headers.put(HeaderName.X_B3_SAMPLED, "1");
  }

  @Test
  public void serializeValidContext() {
    assertThat(b3Format.toHeaders(SPAN_CONTEXT)).isEqualTo(headers);
  }

  @Test
  public void parseValidHeaders() throws ParseException {
    assertThat(b3Format.fromHeaders(headers)).isEqualTo(SPAN_CONTEXT);
  }

  // TODO(bdrutu): Add tests for invalid inputs + 8-byte trace id.
}
