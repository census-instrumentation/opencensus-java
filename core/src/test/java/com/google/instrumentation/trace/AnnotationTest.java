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
    assertThat(annotation.getAttributes().getAll().size()).isEqualTo(0);
  }

  @Test(expected = NullPointerException.class)
  public void fromDescriptionAndAttributes_NullDescription() {
    Annotation.fromDescriptionAndAttributes(null, Attributes.EMPTY);
  }

  @Test(expected = NullPointerException.class)
  public void fromDescriptionAndAttributes_NullAttributes() {
    Annotation.fromDescriptionAndAttributes("", null);
  }

  @Test
  public void fromDescriptionAndAttributes() {
    Attributes attributes =
        Attributes.builder()
            .putStringAttribute("MyStringAttributeKey", "MyStringAttributeValue")
            .build();
    Annotation annotation = Annotation.fromDescriptionAndAttributes("MyAnnotationText", attributes);
    assertThat(annotation.getDescription()).isEqualTo("MyAnnotationText");
    assertThat(annotation.getAttributes().getAll()).isEqualTo(attributes.getAll());
  }

  @Test
  public void fromDescriptionAndAttributes_EmptyAttributes() {
    Annotation annotation =
        Annotation.fromDescriptionAndAttributes("MyAnnotationText", Attributes.EMPTY);
    assertThat(annotation.getDescription()).isEqualTo("MyAnnotationText");
    assertThat(annotation.getAttributes().getAll().size()).isEqualTo(0);
  }
}
