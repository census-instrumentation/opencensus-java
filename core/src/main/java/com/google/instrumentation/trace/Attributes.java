/*
 * Copyright 2016, Google Inc.
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A class that represents a set of Attributes. Each Attribute is a key-value pair, where the key is
 * a string and the value is a {@link AttributeValue}, which can be one of String/Long/Boolean.
 */
@Immutable
public final class Attributes {
  // This object is an immutable Map.
  private final Map<String, AttributeValue> attributes;

  private Attributes(Map<String, AttributeValue> attributes) {
    // Make attributes an immutable map.
    this.attributes = Collections.unmodifiableMap(new HashMap<String, AttributeValue>(attributes));
  }

  /**
   * Returns a new {@link Builder} for this class.
   *
   * @return a new {@code Builder} for this class.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns the {@link AttributeValue} associated with the key, or {@code null} if it does not
   * exist.
   *
   * @param key The attribute key to lookup.
   * @return The {@code AttributeValue} associated with the key, or {@code null} if it does not
   *     exist.
   * @throws NullPointerException if key is {@code null}.
   */
  @Nullable
  public AttributeValue getAttributeValue(String key) {
    return attributes.get(checkNotNull(key, "key"));
  }

  /**
   * Returns The full set of attributes as an immutable {@link Map}.
   *
   * @return The full set of attributes as an immutable {@code Map}.
   */
  public Map<String, AttributeValue> getAll() {
    // It is safe to return the attributes directly because it is an immutable Map.
    return attributes;
  }

  @Override
  public String toString() {
    MoreObjects.ToStringHelper toStringHelper = MoreObjects.toStringHelper(this);
    for (Map.Entry<String, AttributeValue> entry : attributes.entrySet()) {
      toStringHelper.add(entry.getKey(), entry.getValue());
    }
    return toStringHelper.toString();
  }

  /** Builder class for {@link Attributes}. */
  public static final class Builder {
    private final Map<String, AttributeValue> attributes = new HashMap<String, AttributeValue>();

    private Builder() {}

    /**
     * Puts an attribute with a string value. Replaces any existing attribute with the key.
     *
     * @param key Key to associate with the value.
     * @param stringValue The new value.
     * @return this.
     * @throws NullPointerException if {@code key} or {@code stringValue} are {@code null}.
     */
    public Builder putStringAttribute(String key, String stringValue) {
      attributes.put(
          checkNotNull(key, "key"),
          AttributeValue.stringAttributeValue(checkNotNull(stringValue, "stringValue")));
      return this;
    }

    /**
     * Puts an attribute with a boolean value. Replaces any existing attribute with the key.
     *
     * @param key Key to associate with the value.
     * @param booleanValue The new value.
     * @return this.
     * @throws NullPointerException if {@code key} is {@code null}.
     */
    public Builder putBooleanAttribute(String key, boolean booleanValue) {
      attributes.put(checkNotNull(key, "key"), AttributeValue.booleanAttributeValue(booleanValue));
      return this;
    }

    /**
     * Puts an attribute with a long value. Replaces any existing attribute with the key.
     *
     * @param key Key to associate with the value.
     * @param longValue The new value.
     * @return this.
     * @throws NullPointerException if {@code key} is {@code null}.
     */
    public Builder putLongAttribute(String key, long longValue) {
      attributes.put(checkNotNull(key, "key"), AttributeValue.longAttributeValue(longValue));
      return this;
    }

    /**
     * Builds and returns a {@code Attributes} with the desired values.
     *
     * @return a {@code Attributes} with the desired values.
     */
    public Attributes build() {
      return new Attributes(attributes);
    }
  }
}
