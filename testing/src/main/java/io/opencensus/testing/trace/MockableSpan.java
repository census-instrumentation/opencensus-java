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
import io.opencensus.trace.EndSpanOptions;
import io.opencensus.trace.Sampler;
import io.opencensus.trace.Span;
import io.opencensus.trace.SpanBuilder;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.base.Annotation;
import io.opencensus.trace.base.AttributeValue;
import io.opencensus.trace.base.Link;
import io.opencensus.trace.base.NetworkEvent;
import io.opencensus.trace.base.SpanId;
import io.opencensus.trace.base.TraceId;
import io.opencensus.trace.base.TraceOptions;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;

/**
 * Mockable implementation for the {@link Span} class.
 *
 * <p>Not {@code final} to allow easy mocking.
 *
 * <p>Example of usages with Mockito and JUnit4:
 *
 * <pre><code>{@literal @}RunWith(JUnit4.class)
 * public class OpenCensusIntegrationTest {
 *   private final Span fakeParentSpan = MockableSpan.generateRandomSpan(random);
 *   private final Span spySpan = spy(MockableSpan.generateRandomSpan(random));
 *   private final SpanBuilder spySpanBuilder = spy(new MockableSpan.Builder());
 *  {@literal @}Mock private Tracer tracer;
 *
 *  {@literal @}Before
 *   public void setUp() throws Exception {
 *     MockitoAnnotations.initMocks(this);
 *     when(spySpanBuilder.startSpan()).thenReturn(spySpan);
 *     when(tracer.spanBuilderWithExplicitParent(anyString(), same(fakeParentSpan)))
 *         .thenReturn(spySpanBuilder);
 *   }
 *
 *  {@literal @}Test
 *   public void testSpanBuilder() {
 *     try (NonThrowingCloseable ws = tracer.withSpan(fakeParentSpan)) {
 *       // Call the code to be tested.
 *
 *       // Here can verify options set when build the Span:
 *       // verify(spySpanBuilder).setRecordEvents(eq(true));
 *
 *       // Here can verify events recorded to the Span:
 *       // verify(spySpan).addAnnotation(eq(Annotation.fromDescription("My annotation text")));
 *
 *       // Here can verify the end of the Span:
 *       // verify(spySpan).end(EndSpanOptions.builder().setStatus(Status.OK).build());
 *     }
 *   }
 * }
 * </code></pre>
 */
@VisibleForTesting
public class MockableSpan extends Span {

  /**
   * Returns a {@code MockableSpan} with randomly generated trace ids, not sampled, no record events
   * option.
   *
   * @param random the {@code Random} instance used to generate the random trace ids.
   * @return a {@code MockableSpan} randomly generated.
   */
  public static MockableSpan generateRandomSpan(Random random) {
    return new MockableSpan(
        SpanContext.create(
            TraceId.generateRandomId(random),
            SpanId.generateRandomId(random),
            TraceOptions.DEFAULT),
        null);
  }

  /**
   * Returns a {@code MockableSpan} with randomly generated trace ids, not sampled, but with record
   * events option.
   *
   * @param random the {@code Random} instance used to generate the random trace ids.
   * @return a {@code MockableSpan} randomly generated.
   */
  public static MockableSpan generateRandomRecordEventsSpan(Random random) {
    return new MockableSpan(
        SpanContext.create(
            TraceId.generateRandomId(random),
            SpanId.generateRandomId(random),
            TraceOptions.DEFAULT),
        EnumSet.of(Options.RECORD_EVENTS));
  }

  /**
   * Returns a {@code MockableSpan} with randomly generated trace ids and sampled.
   *
   * @param random the {@code Random} instance used to generate the random trace ids.
   * @return a {@code MockableSpan} randomly generated.
   */
  public static MockableSpan generateRandomSampledSpan(Random random) {
    return new MockableSpan(
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

  private MockableSpan(SpanContext context, @Nullable EnumSet<Options> options) {
    super(context, options);
  }

  /**
   * Mockable implementation for the {@link SpanBuilder} class.
   *
   * <p>Not {@code final} to allow easy mocking.
   *
   */
  @VisibleForTesting
  public static class Builder extends SpanBuilder {

    @Override
    public SpanBuilder setSampler(Sampler sampler) {
      return this;
    }

    @Override
    public SpanBuilder setParentLinks(List<Span> parentLinks) {
      return this;
    }

    @Override
    public SpanBuilder setRecordEvents(boolean recordEvents) {
      return this;
    }

    @Override
    public Span startSpan() {
      return null;
    }
  }
}
