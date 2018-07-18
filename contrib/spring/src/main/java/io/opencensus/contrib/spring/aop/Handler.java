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

package io.opencensus.contrib.spring.aop;

import io.opencensus.common.Scope;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanBuilder;
import io.opencensus.trace.Status;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import org.aspectj.lang.ProceedingJoinPoint;

/** Handler defines common logic for wrapping a span around the specified JoinPoint. */
final class Handler {
  private static final Tracer tracer = Tracing.getTracer();

  private Handler() {}

  static Object proceed(ProceedingJoinPoint call, SpanBuilder builder, String... annotations)
      throws Throwable {
    Scope scope = builder.startScopedSpan();
    try {
      for (String annotation : annotations) {
        tracer.getCurrentSpan().addAnnotation(annotation);
      }

      return call.proceed();

    } catch (Throwable t) {
      StringWriter sw = new StringWriter(512);

      Map<String, AttributeValue> attributes = new HashMap<String, AttributeValue>();
      attributes.put("message", AttributeValue.stringAttributeValue(t.getMessage()));
      attributes.put("stackTrace", AttributeValue.stringAttributeValue(sw.toString()));

      Span span = tracer.getCurrentSpan();
      span.addAnnotation("error", attributes);
      span.setStatus(Status.UNKNOWN);
      throw t;
    } finally {
      scope.close();
    }
  }
}
