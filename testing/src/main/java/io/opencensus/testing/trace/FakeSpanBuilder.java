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
import io.opencensus.trace.SpanBuilder;
import io.opencensus.trace.base.Sampler;
import java.util.List;

// Need to use <code></code> in the javadoc because @code tag does not allow multiline contents
// including '@' sign. See https://bugs.openjdk.java.net/browse/JDK-8130754.

/**
 * Fake implementation for the {@link SpanBuilder} class.
 *
 * <p>Example of usages with mock tracer using Mockito and JUnit4:
 *
 * <pre><code>{@literal @}RunWith(JUnit4.class)
 * public class OpenCensusIntegrationTest {
 *   private final Span fakeParentSpan = FakeSpan.generateRandomSpan(random);
 *   private final Span spySpan = spy(FakeSpan.generateRandomSpan(random));
 *   private final SpanBuilder spySpanBuilder = spy(new FakeSpanBuilder());
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
public class FakeSpanBuilder extends SpanBuilder {

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
