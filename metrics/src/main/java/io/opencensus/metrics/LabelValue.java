/*
 * Copyright 2018, OpenCensus Authors
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

package io.opencensus.metrics;

import com.google.auto.value.AutoValue;
import io.opencensus.common.ExperimentalApi;
import javax.annotation.concurrent.Immutable;

/**
 * The value of a {@code Label} associated with a {@code TimeSeries}.
 *
 * @since 0.15
 */
@ExperimentalApi
@Immutable
@AutoValue
public abstract class LabelValue {

  LabelValue() {}

  /**
   * Creates a {@link LabelValue}.
   *
   * @param value the value of a {@code Label}.
   * @return a {@code LabelValue}.
   * @since 0.15
   */
  public static LabelValue create(String value) {
    return new AutoValue_LabelValue(value, true);
  }

  /**
   * Creates a {@link LabelValue} with unset value.
   *
   * @return a {@code LabelValue} with unset value.
   * @since 0.15
   */
  public static LabelValue create() {
    return new AutoValue_LabelValue("", false);
  }

  /**
   * Returns the value of this {@link LabelValue}.
   *
   * @return the value.
   * @since 0.15
   */
  public abstract String getValue();

  /**
   * Returns {@code true} if value field is set, {@code false} otherwise.
   *
   * @return {@code true} if value field is set, {@code false} otherwise.
   * @since 0.15
   */
  public abstract boolean hasValue();
}
