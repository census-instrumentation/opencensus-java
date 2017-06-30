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

import io.opencensus.trace.Span;
import io.opencensus.trace.Span.Options;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.base.SpanId;
import io.opencensus.trace.base.TraceOptions;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Random;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link FakeSpan}. */
@RunWith(JUnit4.class)
public class FakeSpanTest {
  private static final String SPAN_NAME = "MySpanName";
  private final Random random = new Random(1234);
  private final FakeSpan parentSpan = FakeSpan.generateParentSpan(SPAN_NAME);
  private final SpanContext spanContext =
      SpanContext.create(
          parentSpan.getContext().getTraceId(),
          SpanId.generateRandomId(random),
          TraceOptions.DEFAULT);
  private final EnumSet<Options> noRecordSpanOptions = EnumSet.noneOf(Options.class);

  @Test
  public void spanWithParent() {
    FakeSpan span =
        new FakeSpan(
            spanContext,
            noRecordSpanOptions,
            SPAN_NAME,
            parentSpan,
            null,
            Collections.<Span>emptyList());
    assertThat(span.getName()).isEqualTo(SPAN_NAME);
    assertThat(span.isChildOf(parentSpan)).isTrue();
    assertThat(span.isRemoteChildOf(parentSpan.getContext())).isFalse();
    assertThat(span.isRoot()).isFalse();
    assertThat(span.getContext()).isEqualTo(spanContext);
    assertThat(span.getOptions()).isEqualTo(noRecordSpanOptions);
    assertThat(span.getParentLinks()).isEmpty();
  }

  @Test
  public void spanWithRemoteParent() {
    FakeSpan span =
        new FakeSpan(
            spanContext,
            noRecordSpanOptions,
            SPAN_NAME,
            null,
            parentSpan.getContext(),
            Collections.<Span>emptyList());
    assertThat(span.getName()).isEqualTo(SPAN_NAME);
    assertThat(span.isChildOf(parentSpan)).isFalse();
    assertThat(span.isRemoteChildOf(parentSpan.getContext())).isTrue();
    assertThat(span.isRoot()).isFalse();
    assertThat(span.getContext()).isEqualTo(spanContext);
    assertThat(span.getOptions()).isEqualTo(noRecordSpanOptions);
    assertThat(span.getParentLinks()).isEmpty();
  }

  @Test
  public void spanWithoutParent() {
    FakeSpan span =
        new FakeSpan(
            spanContext, noRecordSpanOptions, SPAN_NAME, null, null, Collections.<Span>emptyList());
    assertThat(span.getName()).isEqualTo(SPAN_NAME);
    assertThat(span.isChildOf(parentSpan)).isFalse();
    assertThat(span.isRemoteChildOf(parentSpan.getContext())).isFalse();
    assertThat(span.isRoot()).isTrue();
    assertThat(span.getContext()).isEqualTo(spanContext);
    assertThat(span.getOptions()).isEqualTo(noRecordSpanOptions);
    assertThat(span.getParentLinks()).isEmpty();
  }

  @Test
  public void generatedSpan() {
    FakeSpan span = FakeSpan.generateParentSpan(SPAN_NAME);
    assertThat(span.getName()).isEqualTo(SPAN_NAME);
    assertThat(span.isChildOf(parentSpan)).isFalse();
    assertThat(span.isRemoteChildOf(parentSpan.getContext())).isFalse();
    assertThat(span.isRoot()).isTrue();
    assertThat(span.getParentLinks()).isEmpty();
  }
}
