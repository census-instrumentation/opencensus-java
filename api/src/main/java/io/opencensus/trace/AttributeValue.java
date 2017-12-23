/*
 * Copyright 2016-17, OpenCensus Authors
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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.auto.value.AutoValue;
import io.opencensus.common.Function;
import io.opencensus.internal.CheckerFrameworkUtils;
import javax.annotation.concurrent.Immutable;

/**
 * A class that represents all the possible values for an attribute. An attribute can have 3 types
 * of values: {@code String}, {@code Boolean} or {@code Long}.
 */
@Immutable
public abstract class AttributeValue {
  /**
   * Returns an {@code AttributeValue} with a string value.
   *
   * @param stringValue The new value.
   * @return an {@code AttributeValue} with a string value.
   * @throws NullPointerException if {@code stringValue} is {@code null}.
   */
  public static AttributeValue stringAttributeValue(String stringValue) {
    return AttributeValueString.create(stringValue);
  }

  /**
   * Returns an {@code AttributeValue} with a boolean value.
   *
   * @param booleanValue The new value.
   * @return an {@code AttributeValue} with a boolean value.
   */
  public static AttributeValue booleanAttributeValue(boolean booleanValue) {
    return AttributeValueBoolean.create(booleanValue);
  }

  /**
   * Returns an {@code AttributeValue} with a long value.
   *
   * @param longValue The new value.
   * @return an {@code AttributeValue} with a long value.
   */
  public static AttributeValue longAttributeValue(long longValue) {
    return AttributeValueLong.create(longValue);
  }

  AttributeValue() {}

  /**
   * Applies a function to the underlying value. The function that is called depends on the value's
   * type, which can be {@code String}, {@code Long}, or {@code Boolean}.
   *
   * @param stringFunction the function that should be applied if the value has type {@code String}.
   * @param longFunction the function that should be applied if the value has type {@code Long}.
   * @param booleanFunction the function that should be applied if the value has type {@code
   *     Boolean}.
   * @param defaultFunction the function that should be applied if the value has a type that was
   *     added after this {@code match} method was added to the API. See {@link
   *     io.opencensus.common.Functions} for some common functions for handling unknown types.
   * @return the result of the function applied to the underlying value.
   */
  public abstract <T> T match(
      Function<? super String, T> stringFunction,
      Function<? super Boolean, T> booleanFunction,
      Function<? super Long, T> longFunction,
      Function<Object, T> defaultFunction);

  @Immutable
  @AutoValue
  // Suppress Checker Framework warning about missing @Nullable in generated equals method.
  @AutoValue.CopyAnnotations
  @SuppressWarnings("nullness")
  abstract static class AttributeValueString extends AttributeValue {

    AttributeValueString() {}

    static AttributeValue create(String stringValue) {
      return new AutoValue_AttributeValue_AttributeValueString(
          checkNotNull(stringValue, "stringValue"));
    }

    @Override
    public final <T> T match(
        Function<? super String, T> stringFunction,
        Function<? super Boolean, T> booleanFunction,
        Function<? super Long, T> longFunction,
        Function<Object, T> defaultFunction) {
      return CheckerFrameworkUtils.<String, T>removeSuperFromFunctionParameterType(stringFunction)
          .apply(getStringValue());
    }

    abstract String getStringValue();
  }

  @Immutable
  @AutoValue
  // Suppress Checker Framework warning about missing @Nullable in generated equals method.
  @AutoValue.CopyAnnotations
  @SuppressWarnings("nullness")
  abstract static class AttributeValueBoolean extends AttributeValue {

    AttributeValueBoolean() {}

    static AttributeValue create(Boolean stringValue) {
      return new AutoValue_AttributeValue_AttributeValueBoolean(
          checkNotNull(stringValue, "stringValue"));
    }

    @Override
    public final <T> T match(
        Function<? super String, T> stringFunction,
        Function<? super Boolean, T> booleanFunction,
        Function<? super Long, T> longFunction,
        Function<Object, T> defaultFunction) {
      return CheckerFrameworkUtils.<Boolean, T>removeSuperFromFunctionParameterType(booleanFunction)
          .apply(getBooleanValue());
    }

    abstract Boolean getBooleanValue();
  }

  @Immutable
  @AutoValue
  // Suppress Checker Framework warning about missing @Nullable in generated equals method.
  @AutoValue.CopyAnnotations
  @SuppressWarnings("nullness")
  abstract static class AttributeValueLong extends AttributeValue {

    AttributeValueLong() {}

    static AttributeValue create(Long stringValue) {
      return new AutoValue_AttributeValue_AttributeValueLong(
          checkNotNull(stringValue, "stringValue"));
    }

    @Override
    public final <T> T match(
        Function<? super String, T> stringFunction,
        Function<? super Boolean, T> booleanFunction,
        Function<? super Long, T> longFunction,
        Function<Object, T> defaultFunction) {
      return CheckerFrameworkUtils.<Long, T>removeSuperFromFunctionParameterType(longFunction)
          .apply(getLongValue());
    }

    abstract Long getLongValue();
  }
}
