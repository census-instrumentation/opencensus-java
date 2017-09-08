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

package io.opencensus.trace.propagation;

import static com.google.common.base.Preconditions.checkNotNull;

import io.opencensus.trace.SpanContext;
import java.text.ParseException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * Implementation of the B3 propagation protocol. See
 * <a href=https://github.com/openzipkin/b3-propagation>b3-propagation</a>.
 *
 * <p>This is only recommended to be used for backwards compatibility with systems like Zipkin.
 * For new implementations we recommend using the {@code HttpFormat}.
 *
 * <p>Example of usage on the client:
 *
 * <pre>{@code
 * private static final Tracer tracer = Tracing.getTracer();
 * private static final B3Format b3Format = Tracing.getPropagationComponent().getB3Format();
 * void onSendRequest() {
 *   try (Scope ss = tracer.spanBuilder("Sent.MyRequest").startScopedSpan()) {
 *     Map<HeaderName, String> b3Headers  =
 *         b3Format.toHeaders(tracer.getCurrentContext().context());
 *     // Send the request including the b3Headers and wait for the response.
 *   }
 * }
 * }</pre>
 *
 * <p>Example of usage on the server:
 *
 * <pre>{@code
 * private static final Tracer tracer = Tracing.getTracer();
 * private static final B3Format b3Format = Tracing.getPropagationComponent().getB3Format();
 * void onRequestReceived() {
 *   // Get the B3 headers from the request.
 *   Map<HeaderName, String> b3Headers = ...
 *   SpanContext spanContext = b3Format.fromHeaders(b3Headers);
 *   try (Scope ss =
 *            tracer.spanBuilderWithRemoteParent("Recv.MyRequest", spanContext).startScopedSpan()) {
 *     // Handle request and send response back.
 *   }
 * }
 * }</pre>
 */
public abstract class B3Format {
  public enum HeaderName {
    X_B3_TRACE_ID("X─B3─TraceId"),
    X_B3_SPAN_ID("X─B3─SpanId"),
    X_B3_PARENT_SPAN_ID("X─B3─ParentSpanId"),
    X_B3_SAMPLED("X─B3─Sampled"),
    X_B3_FLAGS("X-B3-Flags");

    private final String value;
    private final String lowerValue;

    private HeaderName(String value) {
      this.value = value;
      this.lowerValue = value.toLowerCase(Locale.US);
    }

    /**
     * Returns the value of this header used in HTTP requests.
     *
     * @return the value of this header used in HTTP requests.
     */
    public String getValue() {
      return value;
    }

    /**
     * Returns the ASCII value of this header (lowercase).
     *
     * @return the ASCII value of this header (lowercase).
     */
    public String getLowerValue() {
      return lowerValue;
    }
  }

  private static final NoopB3Format NOOP_B3_FORMAT = new NoopB3Format();

  /**
   * Serializes a {@link SpanContext} using the b3-propagation format.
   *
   * @param spanContext the {@code SpanContext} to serialize.
   * @return a set of headers that must be propagated on the wire.
   * @throws NullPointerException if the {@code spanContext} is {@code null}.
   */
  public abstract Map<HeaderName, String> toHeaders(SpanContext spanContext);

  /**
   * Parses the {@link SpanContext} from the b3-propagation format.
   *
   * @param headers a set of headers from which the {@code SpanContext} will be parsed.
   * @return the parsed {@code SpanContext}.
   * @throws NullPointerException if the {@code headers} is {@code null}.
   */
  public abstract SpanContext fromHeaders(Map<HeaderName, String> headers) throws ParseException;

  /**
   * Returns the no-op implementation of the {@code B3Format}.
   *
   * @return the no-op implementation of the {@code B3Format}.
   */
  static B3Format getNoopB3Format() {
    return NOOP_B3_FORMAT;
  }

  private static final class NoopB3Format extends B3Format {
    private NoopB3Format() {}

    @Override
    public Map<HeaderName, String> toHeaders(SpanContext spanContext) {
      checkNotNull(spanContext, "spanContext");
      return Collections.emptyMap();
    }

    @Override
    public SpanContext fromHeaders(Map<HeaderName, String> headers) {
      checkNotNull(headers, "headers");
      return SpanContext.INVALID;
    }
  }
}
