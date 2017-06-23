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
import io.opencensus.internal.StringUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * Measure.
 */
@Immutable
public abstract class Measure {
  private final String name;
  private final String description;
  private final Unit unit;

  // TODO(dpo): ensure that measure names are unique.
  private Measure(String name, String description, Unit unit) {
    // TODO(dpo): consider if there should be any restricitons on name (e.g. size, characters).
    this.name = name;
    this.description = description;
    this.unit = unit;
  }

  /**
   * Applies the given match function to the underlying data type.
   */
  public abstract <T> T match(Function<DoubleMeasure, T> p0, Function<LongMeasure, T> p1);

  /**
   * Name of measurement, as a {@code String}.
   */
  public final String getName() {
    return name;
  }

  /**
   * Detailed description of the measurement, used in documentation.
   */
  public String getDescription() {
    return description;
  }

  /**
   * The units in which {@link Measure} values are measured.
   */
  public Unit getUnit() {
    return unit;
  }

  @Override
  public String toString() {
    return name;
  }

  public static final class DoubleMeasure extends Measure {
    private DoubleMeasure(String name, String description, Unit unit) {
      super(name, description, unit);
    }

    /**
     * Constructs a new {@link DoubleMeasure}.
     */
    public static DoubleMeasure create(String name, String description, Unit unit) {
      return new DoubleMeasure(name, description, unit);
    }

    @Override
    public <T> T match(Function<DoubleMeasure, T> p0, Function<LongMeasure, T> p1) {
      return p0.apply(this);
    }
  }

  public static final class LongMeasure extends Measure {
    private LongMeasure(String name, String description, Unit unit) {
      super(name, description, unit);
    }

    /**
     * Constructs a new {@link LongMeasure}.
     */
    public static LongMeasure create(String name, String description, Unit unit) {
      return new LongMeasure(name, description, unit);
    }

    @Override
    public <T> T match(Function<DoubleMeasure, T> p0, Function<LongMeasure, T> p1) {
      return p1.apply(this);
    }
  }

  /**
   * Fundamental units of measurement.
   */
  public enum BasicUnit {
    SCALAR,
    BITS,
    BYTES,
    SECONDS,
    CORES;
  }

  /**
   * Unit lets you build compound units of the form
   * 10^n * (A * B * ...) / (X * Y * ...),
   * where the elements in the numerator and denominator are all BasicUnits.  A
   * Unit must have at least one BasicUnit in its numerator.
   *
   * <p>To specify multiplication in the numerator or denominator, simply specify
   * multiple numerator or denominator fields.  For example:
   *
   * <p>- byte-seconds (i.e. bytes * seconds):
   *     numerator: BYTES
   *     numerator: SECS
   *
   * <p>- events/sec^2 (i.e. rate of change of events/sec):
   *     numerator: SCALAR
   *     denominator: SECS
   *     denominator: SECS
   *
   * <p>To specify multiples (in power of 10) of units, specify a non-zero power10
   * value, for example:
   *
   * <p>- MB/s (i.e. megabytes / s):
   *     power10: 6
   *     numerator: BYTES
   *     denominator: SECS
   *
   * <p>- nanoseconds
   *     power10: -9
   *     numerator: SECS
   */
  @Immutable
  public static class Unit {
    private final int power10;
    private final List<BasicUnit> numerators;
    private final List<BasicUnit> denominators;

    Unit(int power10, List<BasicUnit> numerators, List<BasicUnit> denominators) {
      this.power10 = power10;
      this.numerators = Collections.unmodifiableList(new ArrayList<BasicUnit>(numerators));
      this.denominators = Collections.unmodifiableList(new ArrayList<BasicUnit>(denominators));
    }

    /**
     * Constructs a {@link Unit}.
     */
    public static Unit create(
        int power10, List<BasicUnit> numerators, List<BasicUnit> denominators) {
      return new Unit(power10, numerators, denominators);
    }

    /**
     * Constructs a {@link Unit} without the optional {@code denominators}.
     */
    // TODO(dpo): consider whether this should be a builder instead.
    public static Unit create(int power10, List<BasicUnit> numerators) {
      return new Unit(
          power10, numerators, Collections.<BasicUnit>emptyList());
    }

    /**
     * Unit multiplier (i.e. 10^power10).
     */
    public int getPower10() {
      return power10;
    }

    /**
     * Unit Numerators.
     *
     * <p>Note: The returned list is unmodifiable and attempts to update it will throw an
     * UnsupportedOperationException.
     */
    public List<BasicUnit> getNumerators() {
      return numerators;
    }

    /**
     * Unit Denominators.
     *
     * <p>Note: The returned list is unmodifiable and attempts to update it will throw an
     * UnsupportedOperationException.
     */
    public List<BasicUnit> getDenominators() {
      return denominators;
    }
  }
}
