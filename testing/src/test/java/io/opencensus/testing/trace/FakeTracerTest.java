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

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.trace.BlankSpan;
import io.opencensus.trace.Span;
import io.opencensus.trace.Span.Options;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.base.Annotation;
import io.opencensus.trace.base.AttributeValue;
import io.opencensus.trace.base.EndSpanOptions;
import io.opencensus.trace.base.Link;
import io.opencensus.trace.base.NetworkEvent;
import io.opencensus.trace.base.SpanId;
import io.opencensus.trace.base.StartSpanOptions;
import io.opencensus.trace.base.TraceId;
import io.opencensus.trace.base.TraceOptions;
import io.opencensus.trace.samplers.Samplers;
import java.util.Map;
import java.util.Random;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link FakeTracer}. */
@RunWith(JUnit4.class)
public class FakeTracerTest {
  private static final String SPAN_NAME = "MySpanName";
  private final Random random = new Random(1234);
  private final SpanContext parentContext =
      SpanContext.create(
          TraceId.generateRandomId(random), SpanId.generateRandomId(random), TraceOptions.DEFAULT);
  private final Span parentSpan = new SpanWithContext(parentContext);
  private final FakeTracer tracer = new FakeTracer();

  @Test
  public void startRootSpan() {
    FakeSpan rootSpan = (FakeSpan) tracer.spanBuilder(BlankSpan.INSTANCE, SPAN_NAME).startSpan();
    assertThat(rootSpan.getName()).isEqualTo(SPAN_NAME);
    assertThat(rootSpan.getParentSpanContext()).isNull();
    assertThat(rootSpan.getStartSpanOptions()).isEqualTo(StartSpanOptions.DEFAULT);
    rootSpan.end();
  }

  @Test
  public void startForceRootSpan() {
    FakeSpan rootSpan =
        (FakeSpan) tracer.spanBuilder(parentSpan, SPAN_NAME).becomeRoot().startSpan();
    assertThat(rootSpan.getName()).isEqualTo(SPAN_NAME);
    assertThat(rootSpan.getParentSpanContext()).isNull();
    assertThat(rootSpan.getStartSpanOptions()).isEqualTo(StartSpanOptions.DEFAULT);
    rootSpan.end();
  }

  @Test
  public void startForceRootSpan_WithOptions() {
    FakeSpan rootSpan =
        (FakeSpan)
            tracer
                .spanBuilder(parentSpan, SPAN_NAME)
                .becomeRoot()
                .setRecordEvents(true)
                .setSampler(Samplers.alwaysSample())
                .startSpan();
    assertThat(rootSpan.getName()).isEqualTo(SPAN_NAME);
    assertThat(rootSpan.getParentSpanContext()).isNull();
    assertThat(rootSpan.getContext().getTraceOptions().isSampled()).isTrue();
    assertThat(rootSpan.getOptions()).contains(Options.RECORD_EVENTS);
    assertThat(rootSpan.getStartSpanOptions())
        .isEqualTo(
            StartSpanOptions.builder()
                .setRecordEvents(true)
                .setSampler(Samplers.alwaysSample())
                .build());
    rootSpan.end();
  }

  @Test
  public void startChildSpan() {
    FakeSpan childSpan = (FakeSpan) tracer.spanBuilder(parentSpan, SPAN_NAME).startSpan();
    assertThat(childSpan.getName()).isEqualTo(SPAN_NAME);
    assertThat(childSpan.getParentSpanContext()).isEqualTo(parentContext);
    assertThat(childSpan.getContext().getTraceId()).isEqualTo(parentContext.getTraceId());
    assertThat(childSpan.getStartSpanOptions()).isEqualTo(StartSpanOptions.DEFAULT);
    childSpan.end();
  }

  @Test
  public void startChildSpan_WithOptions() {
    FakeSpan childSpan =
        (FakeSpan)
            tracer
                .spanBuilder(parentSpan, SPAN_NAME)
                .setRecordEvents(true)
                .setSampler(Samplers.alwaysSample())
                .startSpan();
    assertThat(childSpan.getName()).isEqualTo(SPAN_NAME);
    assertThat(childSpan.getParentSpanContext()).isEqualTo(parentContext);
    assertThat(childSpan.getContext().getTraceId()).isEqualTo(parentContext.getTraceId());
    assertThat(childSpan.getContext().getTraceOptions().isSampled()).isTrue();
    assertThat(childSpan.getOptions()).contains(Options.RECORD_EVENTS);
    assertThat(childSpan.getStartSpanOptions())
        .isEqualTo(
            StartSpanOptions.builder()
                .setRecordEvents(true)
                .setSampler(Samplers.alwaysSample())
                .build());
    childSpan.end();
  }

  @Test
  public void startSpanWitRemoteParent() {
    FakeSpan remoteChildSpan =
        (FakeSpan) tracer.spanBuilderWithRemoteParent(parentContext, SPAN_NAME).startSpan();
    assertThat(remoteChildSpan.getName()).isEqualTo(SPAN_NAME);
    assertThat(remoteChildSpan.getParentSpanContext()).isEqualTo(parentContext);
    assertThat(remoteChildSpan.getContext().getTraceId()).isEqualTo(parentContext.getTraceId());
    assertThat(remoteChildSpan.getStartSpanOptions()).isEqualTo(StartSpanOptions.DEFAULT);
    remoteChildSpan.end();
  }

  @Test
  public void startSpanWitRemoteParent_WithOptions() {
    FakeSpan remoteChildSpan =
        (FakeSpan)
            tracer
                .spanBuilderWithRemoteParent(parentContext, SPAN_NAME)
                .setRecordEvents(true)
                .setSampler(Samplers.alwaysSample())
                .startSpan();
    assertThat(remoteChildSpan.getName()).isEqualTo(SPAN_NAME);
    assertThat(remoteChildSpan.getParentSpanContext()).isEqualTo(parentContext);
    assertThat(remoteChildSpan.getContext().getTraceId()).isEqualTo(parentContext.getTraceId());
    assertThat(remoteChildSpan.getContext().getTraceOptions().isSampled()).isTrue();
    assertThat(remoteChildSpan.getOptions()).contains(Options.RECORD_EVENTS);
    assertThat(remoteChildSpan.getStartSpanOptions())
        .isEqualTo(
            StartSpanOptions.builder()
                .setRecordEvents(true)
                .setSampler(Samplers.alwaysSample())
                .build());
    remoteChildSpan.end();
  }

  private static final class SpanWithContext extends Span {

    private SpanWithContext(SpanContext context) {
      super(context, null);
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
  }
}
