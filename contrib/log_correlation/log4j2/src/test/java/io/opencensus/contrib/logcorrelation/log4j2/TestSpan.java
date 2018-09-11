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

import io.opencensus.trace.Annotation;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.EndSpanOptions;
import io.opencensus.trace.Link;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import java.util.EnumSet;
import java.util.Map;

// Simple test Span that holds a SpanContext. The tests cannot use Span directly, since it is
// abstract.
final class TestSpan extends Span {
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
