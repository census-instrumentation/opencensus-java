/*
 * Copyright 2017, OpenCensus Authors
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

package io.opencensus.trace;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import io.opencensus.trace.Link.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Link}. */
@RunWith(JUnit4.class)
public class LinkTest {
  private final Map<String, AttributeValue> attributesMap = new HashMap<String, AttributeValue>();
  private final Random random = new Random(1234);
  private final SpanContext spanContext =
      SpanContext.create(
          TraceId.generateRandomId(random), SpanId.generateRandomId(random), TraceOptions.DEFAULT);

  @Before
  public void setUp() {
    attributesMap.put("MyAttributeKey0", AttributeValue.stringAttributeValue("MyStringAttribute"));
    attributesMap.put("MyAttributeKey1", AttributeValue.longAttributeValue(10));
    attributesMap.put("MyAttributeKey2", AttributeValue.booleanAttributeValue(true));
  }

  @Test
  public void fromSpanContext_ChildLink() {
    Link link = Link.fromSpanContext(spanContext, Type.CHILD_LINKED_SPAN);
    assertThat(link.getTraceId()).isEqualTo(spanContext.getTraceId());
    assertThat(link.getSpanId()).isEqualTo(spanContext.getSpanId());
    assertThat(link.getType()).isEqualTo(Type.CHILD_LINKED_SPAN);
  }

  @Test
  public void fromSpanContext_ChildLink_WithAttributes() {
    Link link = Link.fromSpanContext(spanContext, Type.CHILD_LINKED_SPAN, attributesMap);
    assertThat(link.getTraceId()).isEqualTo(spanContext.getTraceId());
    assertThat(link.getSpanId()).isEqualTo(spanContext.getSpanId());
    assertThat(link.getType()).isEqualTo(Type.CHILD_LINKED_SPAN);
    assertThat(link.getAttributes()).isEqualTo(attributesMap);
  }

  @Test
  public void fromSpanContext_ParentLink() {
    Link link = Link.fromSpanContext(spanContext, Type.PARENT_LINKED_SPAN);
    assertThat(link.getTraceId()).isEqualTo(spanContext.getTraceId());
    assertThat(link.getSpanId()).isEqualTo(spanContext.getSpanId());
    assertThat(link.getType()).isEqualTo(Type.PARENT_LINKED_SPAN);
  }

  @Test
  public void fromSpanContext_ParentLink_WithAttributes() {
    Link link = Link.fromSpanContext(spanContext, Type.PARENT_LINKED_SPAN, attributesMap);
    assertThat(link.getTraceId()).isEqualTo(spanContext.getTraceId());
    assertThat(link.getSpanId()).isEqualTo(spanContext.getSpanId());
    assertThat(link.getType()).isEqualTo(Type.PARENT_LINKED_SPAN);
    assertThat(link.getAttributes()).isEqualTo(attributesMap);
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
        .addEqualityGroup(Link.fromSpanContext(SpanContext.INVALID, Type.PARENT_LINKED_SPAN))
        .addEqualityGroup(
            Link.fromSpanContext(spanContext, Type.PARENT_LINKED_SPAN, attributesMap),
            Link.fromSpanContext(spanContext, Type.PARENT_LINKED_SPAN, attributesMap));
    tester.testEquals();
  }

  @Test
  public void link_ToString() {
    Link link = Link.fromSpanContext(spanContext, Type.CHILD_LINKED_SPAN, attributesMap);
    assertThat(link.toString()).contains(spanContext.getTraceId().toString());
    assertThat(link.toString()).contains(spanContext.getSpanId().toString());
    assertThat(link.toString()).contains("CHILD_LINKED_SPAN");
    for (Map.Entry<String, AttributeValue> entry : attributesMap.entrySet()) {
      // This depends on HashMap#toString(), via AbstractMap#toString(), having a specified format.
      // In particular, each entry is formatted as `key=value`, with no spaces around the `=`.
      // If Link is changed to use something other than a HashMap, this may no longer pass.
      assertThat(link.toString()).contains(entry.getKey() + "=" + entry.getValue());
    }
    link = Link.fromSpanContext(spanContext, Type.PARENT_LINKED_SPAN, attributesMap);
    assertThat(link.toString()).contains(spanContext.getTraceId().toString());
    assertThat(link.toString()).contains(spanContext.getSpanId().toString());
    assertThat(link.toString()).contains("PARENT_LINKED_SPAN");
    for (Map.Entry<String, AttributeValue> entry : attributesMap.entrySet()) {
      assertThat(link.toString()).contains(entry.getKey() + "=" + entry.getValue());
    }
  }
}
