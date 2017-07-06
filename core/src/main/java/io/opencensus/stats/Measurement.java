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

package io.opencensus.stats;

import com.google.auto.value.AutoValue;
import io.opencensus.common.Function;
import io.opencensus.stats.Measure.DoubleMeasure;
import io.opencensus.stats.Measure.LongMeasure;
import javax.annotation.concurrent.Immutable;

/** Immutable representation of a Measurement. */
@Immutable
public abstract class Measurement {

  /**
   * Applies the given match function to the underlying data type.
   */
  public abstract <T> T match(
      Function<? super DoubleMeasurement, T> p0, Function<? super LongMeasurement, T> p1);

  /**
   * Extracts the measured {@link Measure}.
   */
  public abstract Measure getMeasure();

  // Prevents this class from being subclassed anywhere else.
  private Measurement() {
  }

  @Immutable
  @AutoValue
  public abstract static class DoubleMeasurement extends Measurement {

    DoubleMeasurement() {
    }

    /**
     * Constructs a new {@link DoubleMeasurement}.
     */
    public static DoubleMeasurement create(DoubleMeasure measure, double value) {
      return new AutoValue_Measurement_DoubleMeasurement(measure, value);
    }

    @Override
    public abstract DoubleMeasure getMeasure();

    public abstract Double getValue();

    @Override
    public <T> T match(
        Function<? super DoubleMeasurement, T> p0, Function<? super LongMeasurement, T> p1) {
      return p0.apply(this);
    }
  }

  @Immutable
  @AutoValue
  public abstract static class LongMeasurement extends Measurement {

    LongMeasurement() {
    }

    /**
     * Constructs a new {@link LongMeasurement}.
     */
    public static LongMeasurement create(LongMeasure measure, long value) {
      return new AutoValue_Measurement_LongMeasurement(measure, value);
    }

    @Override
    public abstract LongMeasure getMeasure();

    public abstract Long getValue();

    @Override
    public <T> T match(
        Function<? super DoubleMeasurement, T> p0, Function<? super LongMeasurement, T> p1) {
      return p1.apply(this);
    }
  }
}
