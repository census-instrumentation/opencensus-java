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

import io.opencensus.common.Function;
import javax.annotation.concurrent.Immutable;

/**
 * Immutable representation of a MeasureValue.
 */
// TODO(dpo): consider renaming to class Measurement.
@Immutable
public abstract class MeasureValue {
  /**
   * Applies the given match function to the underlying data type.
   */
  public abstract <T> T match(Function<DoubleMeasureValue, T> p0, Function<LongMeasureValue, T> p1);

  /**
   * Extracts the measured {@link Measure}.
   */
  // TODO(dpo): decide if this is useful.
  public abstract Measure getMeasure();

  @Immutable
  public static final class DoubleMeasureValue extends MeasureValue {
    private final Measure.DoubleMeasure measure;
    private final double value;

    private DoubleMeasureValue(Measure.DoubleMeasure measure, double value) {
      this.measure = measure;
      this.value = value;
    }

    /**
     * Constructs a measured value.
     */
    public static DoubleMeasureValue create(Measure.DoubleMeasure measure, double value) {
      return new DoubleMeasureValue(measure, value);
    }

    @Override
    public <T> T match(Function<DoubleMeasureValue, T> p0, Function<LongMeasureValue, T> p1) {
      return p0.apply(this);
    }

    @Override
    public Measure.DoubleMeasure getMeasure() {
      return measure;
    }

    /**
     * Extracts the associated value.
     */
    public double getValue() {
      return value;
    }
  }

  @Immutable
  public static final class LongMeasureValue extends MeasureValue {
    private final Measure.LongMeasure measure;
    private final long value;

    private LongMeasureValue(Measure.LongMeasure measure, long value) {
      this.measure = measure;
      this.value = value;
    }

    /**
     * Constructs a measured value.
     */
    public static LongMeasureValue create(Measure.LongMeasure measure, long value) {
      return new LongMeasureValue(measure, value);
    }

    @Override
    public <T> T match(Function<DoubleMeasureValue, T> p0, Function<LongMeasureValue, T> p1) {
      return p1.apply(this);
    }

    @Override
    public Measure.LongMeasure getMeasure() {
      return measure;
    }

    /**
     * Extracts the associated value.
     */
    public long getValue() {
      return value;
    }
  }
}
