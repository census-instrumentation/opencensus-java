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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.concurrent.Immutable;

/** A text annotation with a set of attributes. */
@Immutable
public final class Annotation {
  private static final Map<String, AttributeValue> EMPTY_ATTRIBUTES = Collections.emptyMap();

  private final String description;
  private final Map<String, AttributeValue> attributes;

  /**
   * Returns a new {@code Annotation} with the given description.
   *
   * @param description the text description of the {@code Annotation}.
   * @return a new {@code Annotation} with the given description.
   * @throws NullPointerException if {@code description} is {@code null}.
   */
  public static Annotation fromDescription(String description) {
    return new Annotation(description, EMPTY_ATTRIBUTES);
  }

  /**
   * Returns a new {@code Annotation} with the given description and set of attributes.
   *
   * @param description the text description of the {@code Annotation}.
   * @param attributes the attributes of the {@code Annotation}.
   * @return a new {@code Annotation} with the given description and set of attributes.
   * @throws NullPointerException if {@code description} or {@code attributes} are {@code null}.
   */
  public static Annotation fromDescriptionAndAttributes(
      String description, Map<String, AttributeValue> attributes) {
    return new Annotation(description, attributes);
  }

  /**
   * Return the description of the {@code Annotation}.
   *
   * @return the description of the {@code Annotation}.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Return the attributes of the {@code Annotation}.
   *
   * @return the attributes of the {@code Annotation}.
   */
  public Map<String, AttributeValue> getAttributes() {
    return attributes;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }

    if (!(obj instanceof Annotation)) {
      return false;
    }

    Annotation that = (Annotation) obj;
    return Objects.equal(description, that.description)
        && Objects.equal(attributes, that.attributes);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(description, attributes);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("description", description)
        .add("attributes", attributes)
        .toString();
  }

  private Annotation(String description, Map<String, AttributeValue> attributes) {
    this.description = checkNotNull(description, "description");
    this.attributes =
        Collections.unmodifiableMap(
            new HashMap<String, AttributeValue>(checkNotNull(attributes, "attributes")));
  }
}
