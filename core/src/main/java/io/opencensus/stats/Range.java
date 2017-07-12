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

package io.opencensus.stats;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.value.AutoValue;
import javax.annotation.concurrent.Immutable;

/** The range of a population's values. */
@Immutable
@AutoValue
public abstract class Range {

  /**
   * Returns a {@code Range} with the given bounds.
   *
   * @param min the minimum of the population values.
   * @param max the maximum of the population values.
   * @return a {@code Range} with the given bounds.
   */
  static Range create(double min, double max) {
    checkArgument(min <= max, "max should be greater or equal to min.");
    return new AutoValue_Range(min, max);
  }

  /**
   * Returns the minimum of the population values.
   *
   * @return the minimum of the population values.
   */
  public abstract double getMin();

  /**
   * Returns the maximum of the population values.
   *
   * @return the maximum of the population values.
   */
  public abstract double getMax();
}