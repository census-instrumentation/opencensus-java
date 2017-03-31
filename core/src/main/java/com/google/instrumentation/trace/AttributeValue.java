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
import com.google.common.base.Objects;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A class that represents all the possible values for an attribute. An attribute can have 3 types
 * of values: {@code String}, {@code Boolean} or {@code Long}.
 */
@Immutable
public final class AttributeValue {
  private final String stringValue;
  private final Boolean booleanValue;
  private final Long longValue;

  /**
   * Returns an {@code AttributeValue} with a string value.
   *
   * @param stringValue The new value.
   * @return an {@code AttributeValue} with a string value.
   * @throws NullPointerException if {@code stringValue} is {@code null}.
   */
  public static AttributeValue stringAttributeValue(String stringValue) {
    return new AttributeValue(checkNotNull(stringValue, "stringValue"), null, null);
  }

  /**
   * Returns an {@code AttributeValue} with a boolean value.
   *
   * @param booleanValue The new value.
   * @return an {@code AttributeValue} with a boolean value.
   */
  public static AttributeValue booleanAttributeValue(boolean booleanValue) {
    return new AttributeValue(null, booleanValue, null);
  }

  /**
   * Returns an {@code AttributeValue} with a long value.
   *
   * @param longValue The new value.
   * @return an {@code AttributeValue} with a long value.
   */
  public static AttributeValue longAttributeValue(long longValue) {
    return new AttributeValue(null, null, longValue);
  }

  private AttributeValue(
      @Nullable String stringValue, @Nullable Boolean booleanValue, @Nullable Long longValue) {
    this.stringValue = stringValue;
    this.booleanValue = booleanValue;
    this.longValue = longValue;
  }

  /**
   * Returns the {@code String} value if this is a string {@code AttributeValue}, otherwise {@code
   * null}.
   *
   * @return the {@code String} value if this is a string {@code AttributeValue}, otherwise {@code
   *     null}.
   */
  @Nullable
  public String getStringValue() {
    return stringValue;
  }

  /**
   * Returns the {@code Boolean} value if this is a boolean {@code AttributeValue}, otherwise {@code
   * null}.
   *
   * @return the {@code Boolean} value if this is a boolean {@code AttributeValue}, otherwise {@code
   *     null}.
   */
  @Nullable
  public Boolean getBooleanValue() {
    return booleanValue;
  }

  /**
   * Returns the {@code Long} value if this is a long {@code AttributeValue}, otherwise {@code
   * null}.
   *
   * @return the {@code Long} value if this is a long {@code AttributeValue}, otherwise {@code
   *     null}.
   */
  @Nullable
  public Long getLongValue() {
    return longValue;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }

    if (!(obj instanceof AttributeValue)) {
      return false;
    }

    AttributeValue that = (AttributeValue) obj;
    return Objects.equal(stringValue, that.stringValue)
        && Objects.equal(booleanValue, that.booleanValue)
        && Objects.equal(longValue, that.longValue);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(stringValue, booleanValue, longValue);
  }

  @Override
  public String toString() {
    if (getStringValue() != null) {
      return MoreObjects.toStringHelper(this)
          .add("type", "string")
          .add("value", getStringValue())
          .toString();
    } else if (getBooleanValue() != null) {
      return MoreObjects.toStringHelper(this)
          .add("type", "boolean")
          .add("value", getBooleanValue())
          .toString();
    } else if (getLongValue() != null) {
      return MoreObjects.toStringHelper(this)
          .add("type", "long")
          .add("value", getLongValue())
          .toString();
    }
    // This should never happen
    throw new RuntimeException("Not a supported attribute value");
  }
}
