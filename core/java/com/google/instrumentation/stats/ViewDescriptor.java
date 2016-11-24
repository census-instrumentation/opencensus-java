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
 * A ViewDescriptor specifies an Aggregation and a set of tag keys. The Aggregation will be broken
 * down by the unique set of matching tag values for each measurement.
 */
public final class ViewDescriptor {
  /**
   * Constructs a new {@link ViewDescriptor}.
   */
  public static ViewDescriptor create(
      String name,
      String description,
      MeasurementDescriptor measurementDescriptor,
      AggregationDescriptor aggregationDescriptor,
      List<TagKey> tagKeys) {
    return new ViewDescriptor(
        name, description, measurementDescriptor, aggregationDescriptor, tagKeys);
  }

  /**
   * Name of view. Must be unique.
   */
  public final String getName() {
    return name;
  }

  /**
   * More detailed description, for documentation purposes.
   */
  public final String getDescription() {
    return description;
  }

  /**
   * Measurement type of this view.
   */
  public final MeasurementDescriptor getMeasurementDescriptor() {
    return measurementDescriptor;
  }

  /**
   * Aggregation type of this ViewDescriptor.
   */
  public final AggregationDescriptor getAggregationDescriptor() {
    return aggregationDescriptor;
  }

  /**
   * Tag keys to match with the associated {@link MeasurementDescriptor}. If no keys are specified,
   * then all stats are recorded. Keys must be unique.
   *
   * <p>Note: The returned list is unmodifiable, attempts to update it will throw an
   * UnsupportedOperationException.
   */
  public final List<TagKey> getTagKeys() {
    return tagKeys;
  }

  private final String name;
  private final String description;
  private final MeasurementDescriptor measurementDescriptor;
  private final AggregationDescriptor aggregationDescriptor;
  private final List<TagKey> tagKeys;

  private ViewDescriptor(
      String name,
      String description,
      MeasurementDescriptor measurementDescriptor,
      AggregationDescriptor aggregationDescriptor,
      List<TagKey> tagKeys) {
    this.name = name;
    this.description = description;
    this.measurementDescriptor = measurementDescriptor;
    this.aggregationDescriptor = aggregationDescriptor;
    this.tagKeys = Collections.unmodifiableList(new ArrayList<TagKey>(tagKeys));
  }
}
