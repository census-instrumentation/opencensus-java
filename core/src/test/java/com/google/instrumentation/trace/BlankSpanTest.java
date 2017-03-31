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

package com.google.instrumentation.trace;

import static com.google.common.truth.Truth.assertThat;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link BlankSpan}. */
@RunWith(JUnit4.class)
public class BlankSpanTest {
  @Test
  public void hasInvalidContextAndDefaultSpanOptions() {
    assertThat(BlankSpan.INSTANCE.getContext()).isEqualTo(SpanContext.INVALID);
    assertThat(BlankSpan.INSTANCE.getOptions().isEmpty()).isTrue();
  }

  @Test
  public void doNotCrash() {
    Map<String, AttributeValue> attributes = new HashMap<String, AttributeValue>();
    attributes.put(
        "MyStringAttributeKey", AttributeValue.stringAttributeValue("MyStringAttributeValue"));
    Map<String, AttributeValue> multipleAttributes = new HashMap<String, AttributeValue>();
    multipleAttributes.put(
        "MyStringAttributeKey", AttributeValue.stringAttributeValue("MyStringAttributeValue"));
    multipleAttributes.put(
        "MyBooleanAttributeKey", AttributeValue.booleanAttributeValue(true));
    multipleAttributes.put(
        "MyLongAttributeKey", AttributeValue.longAttributeValue(123));
    // Tests only that all the methods are not crashing/throwing errors.
    BlankSpan.INSTANCE.addAttributes(attributes);
    BlankSpan.INSTANCE.addAttributes(multipleAttributes);
    BlankSpan.INSTANCE.addAnnotation("MyAnnotation");
    BlankSpan.INSTANCE.addAnnotation("MyAnnotation", attributes);
    BlankSpan.INSTANCE.addAnnotation("MyAnnotation", multipleAttributes);
    BlankSpan.INSTANCE.addAnnotation(Annotation.fromDescription("MyAnnotation"));
    BlankSpan.INSTANCE.addNetworkEvent(NetworkEvent.builder(NetworkEvent.Type.SENT, 1L).build());
    BlankSpan.INSTANCE.addLink(Link.fromSpanContext(SpanContext.INVALID, Link.Type.CHILD));
    BlankSpan.INSTANCE.end(EndSpanOptions.DEFAULT);
    BlankSpan.INSTANCE.end();
  }

  @Test
  public void blankSpan_ToString() {
    assertThat(BlankSpan.INSTANCE.toString()).isEqualTo("BlankSpan");
  }
}
