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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Link}. */
@RunWith(JUnit4.class)
public class AnnotationTest {
  @Test(expected = NullPointerException.class)
  public void fromDescription_NullDescription() {
    Annotation.fromDescription(null);
  }

  @Test
  public void fromDescription() {
    Annotation annotation = Annotation.fromDescription("MyAnnotationText");
    assertThat(annotation.getDescription()).isEqualTo("MyAnnotationText");
    assertThat(annotation.getAttributes().size()).isEqualTo(0);
  }

  @Test(expected = NullPointerException.class)
  public void fromDescriptionAndAttributes_NullDescription() {
    Annotation.fromDescriptionAndAttributes(null, Collections.<String, AttributeValue>emptyMap());
  }

  @Test(expected = NullPointerException.class)
  public void fromDescriptionAndAttributes_NullAttributes() {
    Annotation.fromDescriptionAndAttributes("", null);
  }

  @Test
  public void fromDescriptionAndAttributes() {
    Map<String, AttributeValue> attributes = new HashMap<String, AttributeValue>();
    attributes.put(
        "MyStringAttributeKey", AttributeValue.stringAttributeValue("MyStringAttributeValue"));
    Annotation annotation = Annotation.fromDescriptionAndAttributes("MyAnnotationText", attributes);
    assertThat(annotation.getDescription()).isEqualTo("MyAnnotationText");
    assertThat(annotation.getAttributes()).isEqualTo(attributes);
  }

  @Test
  public void fromDescriptionAndAttributes_EmptyAttributes() {
    Annotation annotation =
        Annotation.fromDescriptionAndAttributes(
            "MyAnnotationText", Collections.<String, AttributeValue>emptyMap());
    assertThat(annotation.getDescription()).isEqualTo("MyAnnotationText");
    assertThat(annotation.getAttributes().size()).isEqualTo(0);
  }

  @Test
  public void annotation_EqualsAndHashCode() {
    EqualsTester tester = new EqualsTester();
    Map<String, AttributeValue> attributes = new HashMap<String, AttributeValue>();
    attributes.put(
        "MyStringAttributeKey", AttributeValue.stringAttributeValue("MyStringAttributeValue"));
    tester
        .addEqualityGroup(
            Annotation.fromDescription("MyAnnotationText"),
            Annotation.fromDescriptionAndAttributes(
                "MyAnnotationText", Collections.<String, AttributeValue>emptyMap()))
        .addEqualityGroup(
            Annotation.fromDescriptionAndAttributes("MyAnnotationText", attributes),
            Annotation.fromDescriptionAndAttributes("MyAnnotationText", attributes))
        .addEqualityGroup(Annotation.fromDescription("MyAnnotationText2"));
    tester.testEquals();
  }

  @Test
  public void annotation_ToString() {
    Annotation annotation = Annotation.fromDescription("MyAnnotationText");
    assertThat(annotation.toString()).contains("MyAnnotationText");
    Map<String, AttributeValue> attributes = new HashMap<String, AttributeValue>();
    attributes.put(
        "MyStringAttributeKey", AttributeValue.stringAttributeValue("MyStringAttributeValue"));
    annotation = Annotation.fromDescriptionAndAttributes("MyAnnotationText2", attributes);
    assertThat(annotation.toString()).contains("MyAnnotationText2");
    assertThat(annotation.toString()).contains(attributes.toString());
  }
}
