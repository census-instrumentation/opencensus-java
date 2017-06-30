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
import io.opencensus.trace.samplers.Samplers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link FakeSpanBuilder}. */
@RunWith(JUnit4.class)
public class FakeSpanBuilderTest {
  private static final String SPAN_NAME = "MySpanName";
  private final FakeSpan parentSpan = FakeSpan.generateParentSpan(SPAN_NAME);

  @Test
  public void startRootSpan() {
    FakeSpan rootSpan = FakeSpanBuilder.createWithParent(BlankSpan.INSTANCE, SPAN_NAME).startSpan();
    assertThat(rootSpan.getName()).isEqualTo(SPAN_NAME);
    assertThat(rootSpan.isRoot()).isTrue();
    assertThat(rootSpan.getContext().getTraceOptions().isSampled()).isFalse();
    assertThat(rootSpan.getOptions()).doesNotContain(Options.RECORD_EVENTS);
    rootSpan.end();
  }

  @Test
  public void startRootSpan_WithOptions() {
    FakeSpan rootSpan =
        FakeSpanBuilder.createWithParent((Span) null, SPAN_NAME)
            .setRecordEvents(true)
            .setSampler(Samplers.alwaysSample())
            .startSpan();
    assertThat(rootSpan.getName()).isEqualTo(SPAN_NAME);
    assertThat(rootSpan.isRoot()).isTrue();
    assertThat(rootSpan.getContext().getTraceOptions().isSampled()).isTrue();
    assertThat(rootSpan.getOptions()).contains(Options.RECORD_EVENTS);
    rootSpan.end();
  }

  @Test
  public void startChildSpan() {
    FakeSpan childSpan = FakeSpanBuilder.createWithParent(parentSpan, SPAN_NAME).startSpan();
    assertThat(childSpan.getName()).isEqualTo(SPAN_NAME);
    assertThat(childSpan.isChildOf(parentSpan)).isTrue();
    assertThat(childSpan.getContext().getTraceOptions().isSampled()).isFalse();
    assertThat(childSpan.getOptions()).doesNotContain(Options.RECORD_EVENTS);
    childSpan.end();
  }

  @Test
  public void startChildSpan_WithOptions() {
    FakeSpan childSpan =
        FakeSpanBuilder.createWithParent(parentSpan, SPAN_NAME)
            .setRecordEvents(true)
            .setSampler(Samplers.alwaysSample())
            .startSpan();
    assertThat(childSpan.getName()).isEqualTo(SPAN_NAME);
    assertThat(childSpan.isChildOf(parentSpan)).isTrue();
    assertThat(childSpan.getContext().getTraceOptions().isSampled()).isTrue();
    assertThat(childSpan.getOptions()).contains(Options.RECORD_EVENTS);
    childSpan.end();
  }

  @Test
  public void startSpanWitRemoteParent() {
    FakeSpan remoteChildSpan =
        FakeSpanBuilder.createWithRemoteParent(parentSpan.getContext(), SPAN_NAME).startSpan();
    assertThat(remoteChildSpan.getName()).isEqualTo(SPAN_NAME);
    assertThat(remoteChildSpan.isChildOf(parentSpan)).isFalse();
    assertThat(remoteChildSpan.isRemoteChildOf(parentSpan.getContext())).isTrue();
    assertThat(remoteChildSpan.getContext().getTraceOptions().isSampled()).isFalse();
    assertThat(remoteChildSpan.getOptions()).doesNotContain(Options.RECORD_EVENTS);
    remoteChildSpan.end();
  }

  @Test
  public void startSpanWitRemoteParent_WithOptions() {
    FakeSpan remoteChildSpan =
        FakeSpanBuilder.createWithRemoteParent(parentSpan.getContext(), SPAN_NAME)
            .setRecordEvents(true)
            .setSampler(Samplers.alwaysSample())
            .startSpan();
    assertThat(remoteChildSpan.getName()).isEqualTo(SPAN_NAME);
    assertThat(remoteChildSpan.isChildOf(parentSpan)).isFalse();
    assertThat(remoteChildSpan.isRemoteChildOf(parentSpan.getContext())).isTrue();
    assertThat(remoteChildSpan.getContext().getTraceOptions().isSampled()).isTrue();
    assertThat(remoteChildSpan.getOptions()).contains(Options.RECORD_EVENTS);
    remoteChildSpan.end();
  }
}
