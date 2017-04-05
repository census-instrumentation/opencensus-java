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

import com.google.auto.value.AutoValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * MeasurementDescriptor.
 *
 * <p>Note: MeasurementDescriptor names are {@link String}s with enforced restrictions.
 */
@AutoValue
public abstract class MeasurementDescriptor {
  public static final int MAX_LENGTH = StringUtil.MAX_LENGTH;

  MeasurementDescriptor() {}

  /**
   * Constructs a new {@link MeasurementDescriptor}.
   */
  public static MeasurementDescriptor create(
      MeasurementDescriptor.Name name, String description, MeasurementUnit unit) {
    return new AutoValue_MeasurementDescriptor(name.asString(), description, unit);
  }

  /**
   * Constructs a new {@link MeasurementDescriptor}.
   */
  public static MeasurementDescriptor create(
      String name, String description, MeasurementUnit unit) {
    return new AutoValue_MeasurementDescriptor(StringUtil.sanitize(name), description, unit);
  }

  /**
   * Name of measurement, e.g. rpc_latency, cpu. Must be unique.
   */
  // TODO(sebright): Change the type of this field to MeasurementDescriptor.Name.
  public abstract String getName();

  /**
   * Detailed description of the measurement, used in documentation.
   */
  public abstract String getDescription();

  /**
   * The units in which {@link MeasurementDescriptor} values are measured.
   */
  public abstract MeasurementUnit getUnit();

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
   * The name of a {@code MeasurementDescriptor}.
   */
  // This type should be used as the key when associating data with MeasurementDescriptors.
  @AutoValue
  public abstract static class Name {

    Name() {}

    /**
     * Returns the name as a {@code String}.
     *
     * @return the name as a {@code String}.
     */
    public abstract String asString();

    /**
     * Creates a {@code MeasurementDescriptor.Name} from a {@code String}.
     *
     * @param name the name {@code String}.
     * @return a {@code MeasurementDescriptor.Name} with the given name {@code String}.
     */
    public static Name create(String name) {
      return new AutoValue_MeasurementDescriptor_Name(StringUtil.sanitize(name));
    }
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
  @AutoValue
  public abstract static class MeasurementUnit {

    MeasurementUnit() {}

    /**
     * Constructs a {@link MeasurementUnit}.
     */
    public static MeasurementUnit create(
        int power10, List<BasicUnit> numerators, List<BasicUnit> denominators) {
      return new AutoValue_MeasurementDescriptor_MeasurementUnit(
          power10,
          Collections.unmodifiableList(new ArrayList<BasicUnit>(numerators)),
          Collections.unmodifiableList(new ArrayList<BasicUnit>(denominators)));
    }

    /**
     * Constructs a {@link MeasurementUnit} without the optional {@code denominators}.
     */
    public static MeasurementUnit create(int power10, List<BasicUnit> numerators) {
      return new AutoValue_MeasurementDescriptor_MeasurementUnit(
          power10, numerators, Collections.<BasicUnit>emptyList());
    }

    /**
     * Unit multiplier (i.e. 10^power10).
     */
    public abstract int getPower10();

    /**
     * Unit Numerators.
     *
     * <p>Note: The returned list is unmodifiable and attempts to update it will throw an
     * UnsupportedOperationException.
     */
    public abstract List<BasicUnit> getNumerators();

    /**
     * Unit Denominators.
     *
     * <p>Note: The returned list is unmodifiable and attempts to update it will throw an
     * UnsupportedOperationException.
     */
    public abstract List<BasicUnit> getDenominators();
  }
}
