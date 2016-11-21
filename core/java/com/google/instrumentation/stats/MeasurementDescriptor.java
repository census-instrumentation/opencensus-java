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

package com.google.instrumentation.stats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * MeasurementDescriptor.
 *
 * <p>Note: MeasurementDescriptor names are {@link String}s with enforced restrictions.
 */
public final class MeasurementDescriptor {
  public static final int MAX_LENGTH = StringUtil.MAX_LENGTH;

  /**
   * Constructs a new {@link MeasurementDescriptor}.
   */
  public static MeasurementDescriptor create(
      String name, String description, MeasurementUnit unit) {
    return new MeasurementDescriptor(name, description, unit);
  }

  /**
   * Name of measurement, e.g. rpc_latency, cpu. Must be unique.
   */
  public String getName() {
    return name;
  }

  /**
   * Detailed description of the measurement, used in documentation.
   */
  public String getDescription() {
    return description;
  }

  /**
   * The units in which {@link MeasurementDescriptor} values are measured.
   */
  public MeasurementUnit getUnit() {
    return unit;
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof MeasurementDescriptor)
        && name.equals(((MeasurementDescriptor) obj).name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public String toString() {
    return name;
  }

  private final String name;
  private final String description;
  private final MeasurementUnit unit;

  private MeasurementDescriptor(String name, String description, MeasurementUnit unit) {
    this.name = StringUtil.sanitize(name);
    this.description = description;
    this.unit = unit;
  }

  /**
   * Fundamental units of measurement.
   */
  public enum BasicUnit {
    SCALAR,
    BITS,
    BYTES,
    SECS,
    CORES,
    MAX_UNITS;
  }

  /**
   * MeasurementUnit lets you build compound units of the form
   * 10^n * (A * B * ...) / (X * Y * ...),
   * where the elements in the numerator and denominator are all BasicUnits.  A
   * MeasurementUnit must have at least one BasicUnit in its numerator.
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
  public static final class MeasurementUnit {
    /**
     * Constructs a {@link MeasurementUnit}.
     */
    public static MeasurementUnit create(
        int power10, List<BasicUnit> numerators, List<BasicUnit> denominators) {
      return new MeasurementUnit(power10, numerators, denominators);
    }

    /**
     * Constructs a {@link MeasurementUnit} without the optional {@code denominators}.
     */
    public static MeasurementUnit create(int power10, List<BasicUnit> numerators) {
      return new MeasurementUnit(power10, numerators, new ArrayList<BasicUnit>());
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
    public final List<BasicUnit> getDenominators() {
      return denominators;
    }

    private final int power10;
    private final List<BasicUnit> numerators;
    private final List<BasicUnit> denominators;

    private MeasurementUnit(int power10, List<BasicUnit> numerators, List<BasicUnit> denominators) {
      this.power10 = power10;
      this.numerators = Collections.unmodifiableList(new ArrayList<BasicUnit>(numerators));
      this.denominators =  Collections.unmodifiableList(new ArrayList<BasicUnit>(denominators));
    }
  }
}
