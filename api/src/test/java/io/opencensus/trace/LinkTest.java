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

package io.opencensus.trace;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import io.opencensus.trace.Link.Type;
import java.util.Random;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Link}. */
@RunWith(JUnit4.class)
public class LinkTest {
  private final Random random = new Random(1234);
  private final SpanContext spanContext =
      SpanContext.create(
          TraceId.generateRandomId(random), SpanId.generateRandomId(random), TraceOptions.DEFAULT);

  @Test
  public void fromSpanContext_ChildLink() {
    Link link = Link.fromSpanContext(spanContext, Type.CHILD_LINKED_SPAN);
    assertThat(link.getTraceId()).isEqualTo(spanContext.getTraceId());
    assertThat(link.getSpanId()).isEqualTo(spanContext.getSpanId());
    assertThat(link.getType()).isEqualTo(Type.CHILD_LINKED_SPAN);
  }

  @Test
  public void fromSpanContext_ParentLink() {
    Link link = Link.fromSpanContext(spanContext, Type.PARENT_LINKED_SPAN);
    assertThat(link.getTraceId()).isEqualTo(spanContext.getTraceId());
    assertThat(link.getSpanId()).isEqualTo(spanContext.getSpanId());
    assertThat(link.getType()).isEqualTo(Type.PARENT_LINKED_SPAN);
  }

  @Test
  public void link_EqualsAndHashCode() {
    EqualsTester tester = new EqualsTester();
    tester
        .addEqualityGroup(
            Link.fromSpanContext(spanContext, Type.PARENT_LINKED_SPAN),
            Link.fromSpanContext(spanContext, Type.PARENT_LINKED_SPAN))
        .addEqualityGroup(
            Link.fromSpanContext(spanContext, Type.CHILD_LINKED_SPAN),
            Link.fromSpanContext(spanContext, Type.CHILD_LINKED_SPAN))
        .addEqualityGroup(Link.fromSpanContext(SpanContext.INVALID, Type.CHILD_LINKED_SPAN))
        .addEqualityGroup(Link.fromSpanContext(SpanContext.INVALID, Type.PARENT_LINKED_SPAN));
    tester.testEquals();
  }

  @Test
  public void link_ToString() {
    Link link = Link.fromSpanContext(spanContext, Type.CHILD_LINKED_SPAN);
    assertThat(link.toString()).contains(spanContext.getTraceId().toString());
    assertThat(link.toString()).contains(spanContext.getSpanId().toString());
    assertThat(link.toString()).contains("CHILD_LINKED_SPAN");
    link = Link.fromSpanContext(spanContext, Type.PARENT_LINKED_SPAN);
    assertThat(link.toString()).contains(spanContext.getTraceId().toString());
    assertThat(link.toString()).contains(spanContext.getSpanId().toString());
    assertThat(link.toString()).contains("PARENT_LINKED_SPAN");
  }
}
