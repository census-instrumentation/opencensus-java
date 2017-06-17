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

/**
 * Immutable representation of a MeasurementValue.
 */
public class MeasurementValue {

  /**
   * Constructs a measured value.
   */
  public static MeasurementValue create(MeasurementDescriptor name, double value) {
    return new MeasurementValue(name, value);
  }

  /**
   * Extracts the measured {@link MeasurementDescriptor}.
   */
  public MeasurementDescriptor getMeasurement() {
    return name;
  }

  /**
   * Extracts the associated value.
   */
  public double getValue() {
    return value;
  }

  private final MeasurementDescriptor name;
  private final double value;

  private MeasurementValue(MeasurementDescriptor name, double value) {
    this.name = name;
    this.value = value;
  }
}
