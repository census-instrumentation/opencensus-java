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

package io.opencensus.stats;

import com.google.auto.value.AutoValue;
import io.opencensus.common.Function;
import io.opencensus.internal.CheckerFrameworkUtils;
import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.stats.Measure.MeasureLong;
import javax.annotation.concurrent.Immutable;

/** Immutable representation of a Measurement. */
@Immutable
public abstract class Measurement {

  /** Applies the given match function to the underlying data type. */
  public abstract <T> T match(
      Function<? super MeasurementDouble, T> p0,
      Function<? super MeasurementLong, T> p1,
      Function<? super Measurement, T> defaultFunction);

  /** Extracts the measured {@link Measure}. */
  public abstract Measure getMeasure();

  // Prevents this class from being subclassed anywhere else.
  private Measurement() {}

  /** {@code Double} typed {@link Measurement}. */
  @Immutable
  @AutoValue
  // Suppress Checker Framework warning about missing @Nullable in generated equals method.
  @AutoValue.CopyAnnotations
  @SuppressWarnings("nullness")
  public abstract static class MeasurementDouble extends Measurement {
    MeasurementDouble() {}

    /** Constructs a new {@link MeasurementDouble}. */
    public static MeasurementDouble create(MeasureDouble measure, double value) {
      return new AutoValue_Measurement_MeasurementDouble(measure, value);
    }

    @Override
    public abstract MeasureDouble getMeasure();

    public abstract double getValue();

    @Override
    public <T> T match(
        Function<? super MeasurementDouble, T> p0,
        Function<? super MeasurementLong, T> p1,
        Function<? super Measurement, T> defaultFunction) {
      return CheckerFrameworkUtils.<MeasurementDouble, T>removeSuperFromFunctionParameterType(p0)
          .apply(this);
    }
  }

  /** {@code Long} typed {@link Measurement}. */
  @Immutable
  @AutoValue
  // Suppress Checker Framework warning about missing @Nullable in generated equals method.
  @AutoValue.CopyAnnotations
  @SuppressWarnings("nullness")
  public abstract static class MeasurementLong extends Measurement {
    MeasurementLong() {}

    /** Constructs a new {@link MeasurementLong}. */
    public static MeasurementLong create(MeasureLong measure, long value) {
      return new AutoValue_Measurement_MeasurementLong(measure, value);
    }

    @Override
    public abstract MeasureLong getMeasure();

    public abstract long getValue();

    @Override
    public <T> T match(
        Function<? super MeasurementDouble, T> p0,
        Function<? super MeasurementLong, T> p1,
        Function<? super Measurement, T> defaultFunction) {
      return CheckerFrameworkUtils.<MeasurementLong, T>removeSuperFromFunctionParameterType(p1)
          .apply(this);
    }
  }
}
