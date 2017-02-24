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
 * A class that represents a set of Labels. Each Label is a key-value pair, where the key is a
 * string and the value is a {@link LabelValue}, which can be one of String/Long/Boolean.
 *
 * @see LabelValue
 */
@Immutable
public final class Labels {
  // This object is an immutable Map.
  private final Map<String, LabelValue> labels;

  private Labels(Map<String, LabelValue> labels) {
    // Make labels an immutable map.
    this.labels = Collections.unmodifiableMap(new HashMap<String, LabelValue>(labels));
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
   * Returns the {@link LabelValue} associated with the key, or {@code null} if it does not exist.
   *
   * @param key The label key to lookup.
   * @return The {@code LabelValue} associated with the key, or {@code null} if it does not exist.
   * @throws NullPointerException if key is {@code null}.
   */
  @Nullable
  public LabelValue getLabelValue(String key) {
    return labels.get(checkNotNull(key, "key"));
  }

  /**
   * Returns The full set of labels as an immutable {@link Map}.
   *
   * @return The full set of labels as an immutable {@code Map}.
   */
  public Map<String, LabelValue> getAll() {
    // It is safe to return the labels directly because it is an immutable Map.
    return labels;
  }

  @Override
  public String toString() {
    MoreObjects.ToStringHelper toStringHelper = MoreObjects.toStringHelper(this);
    for (Map.Entry<String, LabelValue> entry : labels.entrySet()) {
      toStringHelper.add(entry.getKey(), entry.getValue());
    }
    return toStringHelper.toString();
  }

  /**
   * Builder class for {@link Labels}.
   */
  public static final class Builder {
    private final Map<String, LabelValue> labels = new HashMap<String, LabelValue>();

    private Builder() {}

    /**
     * Puts a label with a string value. Replaces any existing label with the key.
     *
     * @param key Key to associate with the value.
     * @param stringValue The new value.
     * @return this.
     * @throws NullPointerException if {@code key} or {@code stringValue} are {@code null}.
     */
    public Builder putStringLabel(String key, String stringValue) {
      labels.put(
          checkNotNull(key, "key"),
          LabelValue.stringLabelValue(checkNotNull(stringValue, "stringValue")));
      return this;
    }

    /**
     * Puts a label with a boolean value. Replaces any existing label with the key.
     *
     * @param key Key to associate with the value.
     * @param booleanValue The new value.
     * @return this.
     * @throws NullPointerException if {@code key} is {@code null}.
     */
    public Builder putBooleanLabel(String key, boolean booleanValue) {
      labels.put(checkNotNull(key, "key"), LabelValue.booleanLabelValue(booleanValue));
      return this;
    }

    /**
     * Puts a label with a long value. Replaces any existing label with the key.
     *
     * @param key Key to associate with the value.
     * @param longValue The new value.
     * @return this.
     * @throws NullPointerException if {@code key} is {@code null}.
     */
    public Builder putLongLabel(String key, long longValue) {
      labels.put(checkNotNull(key, "key"), LabelValue.longLabelValue(longValue));
      return this;
    }

    /**
     * Builds and returns a {@code Labels} with the desired values.
     *
     * @return a {@code Labels} with the desired values.
     */
    public Labels build() {
      return new Labels(labels);
    }
  }
}
