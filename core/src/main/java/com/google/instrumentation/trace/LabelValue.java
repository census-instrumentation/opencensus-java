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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A class that represents all the possible values for a label in {@link Labels}.. A label can have
 * 3 types of values: {@link String}, {@link Boolean} or {@link Long}.
 */
@Immutable
public final class LabelValue {
  private final String stringValue;
  private final Boolean booleanValue;
  private final Long longValue;

  static LabelValue stringLabelValue(String stringValue) {
    return new LabelValue(stringValue, null, null);
  }

  static LabelValue booleanLabelValue(boolean booleanValue) {
    return new LabelValue(null, booleanValue, null);
  }

  static LabelValue longLabelValue(long longValue) {
    return new LabelValue(null, null, longValue);
  }

  private LabelValue(String stringValue, Boolean booleanValue, Long longValue) {
    this.stringValue = stringValue;
    this.booleanValue = booleanValue;
    this.longValue = longValue;
  }

  /**
   * Returns the {@link String} value if this is a string {@code LabelValue}, otherwise {@code
   * null}.
   *
   * @return the {@link String} value if this is a string {@code LabelValue}, otherwise {@code
   *     null}.
   */
  @Nullable
  public String getStringValue() {
    return stringValue;
  }

  /**
   * Returns the {@link Boolean} value if this is a boolean {@code LabelValue}, otherwise {@code
   * null}.
   *
   * @return the {@link Boolean} value if this is a boolean {@code LabelValue}, otherwise {@code
   *     null}.
   */
  @Nullable
  public Boolean getBooleanValue() {
    return booleanValue;
  }

  /**
   * Returns the {@link Long} value if this is a long {@code LabelValue}, otherwise {@code null}.
   *
   * @return the {@link Long} value if this is a long {@code LabelValue}, otherwise {@code null}.
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

    if (!(obj instanceof LabelValue)) {
      return false;
    }

    LabelValue that = (LabelValue) obj;
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
    throw new RuntimeException("Not a supported label value");
  }
}
