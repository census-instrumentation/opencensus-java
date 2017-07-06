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

package io.opencensus.trace;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A class that represents all the possible values for an attribute. An attribute can have 3 types
 * of values: {@code String}, {@code Boolean} or {@code Long}.
 */
@Immutable
@AutoValue
public abstract class AttributeValue {
  /**
   * Returns an {@code AttributeValue} with a string value.
   *
   * @param stringValue The new value.
   * @return an {@code AttributeValue} with a string value.
   * @throws NullPointerException if {@code stringValue} is {@code null}.
   */
  public static AttributeValue stringAttributeValue(String stringValue) {
    return new AutoValue_AttributeValue(checkNotNull(stringValue, "stringValue"), null, null);
  }

  /**
   * Returns an {@code AttributeValue} with a boolean value.
   *
   * @param booleanValue The new value.
   * @return an {@code AttributeValue} with a boolean value.
   */
  public static AttributeValue booleanAttributeValue(boolean booleanValue) {
    return new AutoValue_AttributeValue(null, booleanValue, null);
  }

  /**
   * Returns an {@code AttributeValue} with a long value.
   *
   * @param longValue The new value.
   * @return an {@code AttributeValue} with a long value.
   */
  public static AttributeValue longAttributeValue(long longValue) {
    return new AutoValue_AttributeValue(null, null, longValue);
  }

  AttributeValue() {}

  /**
   * Returns the {@code String} value if this is a string {@code AttributeValue}, otherwise {@code
   * null}.
   *
   * @return the {@code String} value if this is a string {@code AttributeValue}, otherwise {@code
   *     null}.
   */
  @Nullable
  public abstract String getStringValue();

  /**
   * Returns the {@code Boolean} value if this is a boolean {@code AttributeValue}, otherwise {@code
   * null}.
   *
   * @return the {@code Boolean} value if this is a boolean {@code AttributeValue}, otherwise {@code
   *     null}.
   */
  @Nullable
  public abstract Boolean getBooleanValue();

  /**
   * Returns the {@code Long} value if this is a long {@code AttributeValue}, otherwise {@code
   * null}.
   *
   * @return the {@code Long} value if this is a long {@code AttributeValue}, otherwise {@code
   *     null}.
   */
  @Nullable
  public abstract Long getLongValue();
}
