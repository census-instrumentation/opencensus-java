/*
 * Copyright 2017, Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.testing.trace;

import com.google.common.annotations.VisibleForTesting;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.base.Annotation;
import io.opencensus.trace.base.AttributeValue;
import io.opencensus.trace.base.EndSpanOptions;
import io.opencensus.trace.base.Link;
import io.opencensus.trace.base.NetworkEvent;
import io.opencensus.trace.base.SpanId;
import io.opencensus.trace.base.TraceId;
import io.opencensus.trace.base.TraceOptions;
import java.util.EnumSet;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;

/**
 * Fake implementation for the {@link Span} class.
 *
 * <p>See {@link FakeSpanBuilder} for example of usages.
 */
@VisibleForTesting
public class FakeSpan extends Span {

  /**
   * Returns a {@code FakeSpan} with randomly generated trace ids, not sampled, no record events
   * option.
   *
   * @param random the {@code Random} instance used to generate the random trace ids.
   * @return a {@code FakeSpan} randomly generated.
   */
  public static FakeSpan generateRandomSpan(Random random) {
    return new FakeSpan(
        SpanContext.create(
            TraceId.generateRandomId(random),
            SpanId.generateRandomId(random),
            TraceOptions.DEFAULT),
        null);
  }

  /**
   * Returns a {@code FakeSpan} with randomly generated trace ids, not sampled, but with record
   * events option.
   *
   * @param random the {@code Random} instance used to generate the random trace ids.
   * @return a {@code FakeSpan} randomly generated.
   */
  public static FakeSpan generateRandomRecordEventsSpan(Random random) {
    return new FakeSpan(
        SpanContext.create(
            TraceId.generateRandomId(random),
            SpanId.generateRandomId(random),
            TraceOptions.DEFAULT),
        EnumSet.of(Options.RECORD_EVENTS));
  }

  /**
   * Returns a {@code FakeSpan} with randomly generated trace ids and sampled.
   *
   * @param random the {@code Random} instance used to generate the random trace ids.
   * @return a {@code FakeSpan} randomly generated.
   */
  public static FakeSpan generateRandomSampledSpan(Random random) {
    return new FakeSpan(
        SpanContext.create(
            TraceId.generateRandomId(random),
            SpanId.generateRandomId(random),
            TraceOptions.builder().setIsSampled().build()),
        EnumSet.of(Options.RECORD_EVENTS));
  }

  @Override
  public void addAttributes(Map<String, AttributeValue> attributes) {}

  @Override
  public void addAnnotation(String description, Map<String, AttributeValue> attributes) {}

  @Override
  public void addAnnotation(Annotation annotation) {}

  @Override
  public void addNetworkEvent(NetworkEvent networkEvent) {}

  @Override
  public void addLink(Link link) {}

  @Override
  public void end(EndSpanOptions options) {}

  private FakeSpan(SpanContext context, @Nullable EnumSet<Options> options) {
    super(context, options);
  }
}
