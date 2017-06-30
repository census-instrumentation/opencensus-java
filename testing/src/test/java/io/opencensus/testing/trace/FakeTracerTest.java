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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link FakeTracer}. */
@RunWith(JUnit4.class)
public class FakeTracerTest {
  private static final String SPAN_NAME = "MySpanName";
  private final FakeSpan parentSpan = FakeSpan.generateParentSpan(SPAN_NAME);
  private final FakeTracer tracer = new FakeTracer();

  @Test
  public void startRootSpan() {
    FakeSpan rootSpan = tracer.spanBuilder(BlankSpan.INSTANCE, SPAN_NAME).startSpan();
    assertThat(rootSpan.getName()).isEqualTo(SPAN_NAME);
    assertThat(rootSpan.isRoot()).isTrue();
    rootSpan.end();
  }

  @Test
  public void startChildSpan() {
    FakeSpan childSpan = tracer.spanBuilder(parentSpan, SPAN_NAME).startSpan();
    assertThat(childSpan.getName()).isEqualTo(SPAN_NAME);
    assertThat(childSpan.isChildOf(parentSpan)).isTrue();
    childSpan.end();
  }

  @Test
  public void startSpanWitRemoteParent() {
    FakeSpan remoteChildSpan =
        tracer.spanBuilderWithRemoteParent(parentSpan.getContext(), SPAN_NAME).startSpan();
    assertThat(remoteChildSpan.getName()).isEqualTo(SPAN_NAME);
    assertThat(remoteChildSpan.isChildOf(parentSpan)).isFalse();
    assertThat(remoteChildSpan.isRemoteChildOf(parentSpan.getContext())).isTrue();
    remoteChildSpan.end();
  }
}
