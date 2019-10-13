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

import io.opencensus.trace.SpanContext;
import io.opencensus.trace.SpanId;
import io.opencensus.trace.TraceId;
import io.opencensus.trace.TraceOptions;
import io.opencensus.trace.Tracestate;
import io.opencensus.trace.propagation.SpanContextParseException;
import io.opencensus.trace.propagation.TextFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of the B3 propagation protocol. See <a
 * href=https://github.com/openzipkin/b3-propagation>b3-propagation</a>.
 */
public class B3SingleFormat extends TextFormat {

  private static final List<String> FIELDS = Collections.unmodifiableList(Arrays.asList("b3"));

  @Override
  public List<String> fields() {
    return FIELDS;
  }

  @Override
  public <C> void inject(final SpanContext spanContext, final C carrier, final Setter<C> setter) {
    final StringBuilder builder = new StringBuilder();
    builder.append(spanContext.getTraceId().toLowerBase16());
    builder.append('-');
    builder.append(spanContext.getSpanId().toLowerBase16());

    final TraceOptions traceOptions = spanContext.getTraceOptions();
    if (traceOptions != null) {
      builder.append('-');
      builder.append(traceOptions.isSampled() ? '1' : '0');
    }

    setter.put(carrier, "b3", builder.toString());
  }

  @Override
  public <C> SpanContext extract(final C carrier, final Getter<C> getter)
      throws SpanContextParseException {
    final String b3Header = getter.get(carrier, "b3");
    if (b3Header != null) {
      return parseB3Value(b3Header);
    }
    final String traceStateHeader = getter.get(carrier, "tracestate");
    if (traceStateHeader != null) {
      final String[] components = traceStateHeader.split("=", 2);
      if (components.length < 2 || !"b3".equalsIgnoreCase(components[0])) {
        throw new SpanContextParseException("invalid tracestate header");
      }
      return parseB3Value(components[1]);
    }
    throw new SpanContextParseException("Could not find b3 or tracestate header");
  }

  protected SpanContext parseB3Value(final String b3Header) throws SpanContextParseException {
    final String[] components = b3Header.split("-", 4);
    if (components.length < 2) {
      throw new SpanContextParseException("Invalid b3 (single) header");
    }
    final String traceIdString = components[0];
    final String spanIdString = components[1];
    final String flag = components.length > 2 ? components[2] : null;

    final TraceId traceId = TraceId.fromLowerBase16(traceIdString);
    final SpanId spanId = SpanId.fromLowerBase16(spanIdString);
    final TraceOptions traceOptions =
        flag != null
            ? TraceOptions.builder().setIsSampled("1".contentEquals(flag)).build()
            : TraceOptions.DEFAULT;
    final Tracestate tracestate = Tracestate.builder().build();

    return SpanContext.create(traceId, spanId, traceOptions, tracestate);
  }
}
