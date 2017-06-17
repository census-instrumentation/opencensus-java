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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * A ViewDescriptor specifies an aggregation and a set of tag keys. The aggregation will be broken
 * down by the unique set of matching tag values for each measurement.
 */
@Immutable
public abstract class ViewDescriptor {
  /**
   * Name of view. Must be unique.
   */
  public abstract Name getViewDescriptorName();

  /**
   * Name of view, as a {@code String}.
   */
  public final String getName() {
    return getViewDescriptorName().asString();
  }

  /**
   * More detailed description, for documentation purposes.
   */
  public abstract String getDescription();

  /**
   * Measurement type of this view.
   */
  public abstract MeasurementDescriptor getMeasurementDescriptor();

  /**
   * Tag keys to match with the associated {@link MeasurementDescriptor}. If no keys are specified,
   * then all stats are recorded. Keys must be unique.
   *
   * <p>Note: The returned list is unmodifiable, attempts to update it will throw an
   * UnsupportedOperationException.
   */
  public abstract List<TagKey> getTagKeys();

  /**
   * Applies the given match function to the underlying data type.
   */
  public abstract <T> T match(
      Function<DistributionViewDescriptor, T> p0,
      Function<IntervalViewDescriptor, T> p1);

  /**
   * The name of a {@code ViewDescriptor}.
   */
  // This type should be used as the key when associating data with ViewDescriptors.
  @Immutable
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
     * Creates a {@code ViewDescriptor.Name} from a {@code String}.
     *
     * @param name the name {@code String}.
     * @return a {@code ViewDescriptor.Name} with the given name {@code String}.
     */
    public static Name create(String name) {
      return new AutoValue_ViewDescriptor_Name(name);
    }
  }

  /**
   * A {@link ViewDescriptor} for distribution-base aggregations.
   */
  @Immutable
  @AutoValue
  public abstract static class DistributionViewDescriptor extends ViewDescriptor {
    /**
     * Constructs a new {@link DistributionViewDescriptor}.
     */
    public static DistributionViewDescriptor create(
        String name,
        String description,
        MeasurementDescriptor measurementDescriptor,
        DistributionAggregationDescriptor distributionAggregationDescriptor,
        List<TagKey> tagKeys) {
      return create(
          Name.create(name),
          description,
          measurementDescriptor,
          distributionAggregationDescriptor,
          tagKeys);
    }

    /**
     * Constructs a new {@link DistributionViewDescriptor}.
     */
    public static DistributionViewDescriptor create(
        Name name,
        String description,
        MeasurementDescriptor measurementDescriptor,
        DistributionAggregationDescriptor distributionAggregationDescriptor,
        List<TagKey> tagKeys) {
      return new AutoValue_ViewDescriptor_DistributionViewDescriptor(
          name,
          description,
          measurementDescriptor,
          Collections.unmodifiableList(new ArrayList<TagKey>(tagKeys)),
          distributionAggregationDescriptor);
    }

    /**
     * The {@link DistributionAggregationDescriptor} associated with this
     * {@link DistributionViewDescriptor}.
     */
    public abstract DistributionAggregationDescriptor getDistributionAggregationDescriptor();

    @Override
    public <T> T match(
        Function<DistributionViewDescriptor, T> p0,
        Function<IntervalViewDescriptor, T> p1) {
      return p0.apply(this);
    }
  }

  /**
   * A {@link ViewDescriptor} for interval-based aggregations.
   */
  @Immutable
  @AutoValue
  public abstract static class IntervalViewDescriptor extends ViewDescriptor {
    /**
     * Constructs a new {@link IntervalViewDescriptor}.
     */
    public static IntervalViewDescriptor create(
        String name,
        String description,
        MeasurementDescriptor measurementDescriptor,
        IntervalAggregationDescriptor intervalAggregationDescriptor,
        List<TagKey> tagKeys) {
      return create(
          Name.create(name),
          description,
          measurementDescriptor,
          intervalAggregationDescriptor,
          tagKeys);
    }

    /**
     * Constructs a new {@link IntervalViewDescriptor}.
     */
    public static IntervalViewDescriptor create(
        Name name,
        String description,
        MeasurementDescriptor measurementDescriptor,
        IntervalAggregationDescriptor intervalAggregationDescriptor,
        List<TagKey> tagKeys) {
      return new AutoValue_ViewDescriptor_IntervalViewDescriptor(
          name,
          description,
          measurementDescriptor,
          Collections.unmodifiableList(new ArrayList<TagKey>(tagKeys)),
          intervalAggregationDescriptor);
    }

    /**
     * The {@link IntervalAggregationDescriptor} associated with this
     * {@link IntervalViewDescriptor}.
     */
    public abstract IntervalAggregationDescriptor getIntervalAggregationDescriptor();

    @Override
    public <T> T match(
        Function<DistributionViewDescriptor, T> p0,
        Function<IntervalViewDescriptor, T> p1) {
      return p1.apply(this);
    }
  }
}
