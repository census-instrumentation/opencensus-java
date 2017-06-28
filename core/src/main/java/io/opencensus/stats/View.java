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
 * A View specifies an aggregation and a set of tag keys. The aggregation will be broken
 * down by the unique set of matching tag values for each measure.
 */
@Immutable
public abstract class View {
  /**
   * Name of view. Must be unique.
   */
  public abstract Name getViewName();

  /**
   * Name of view, as a {@code String}.
   */
  public final String getName() {
    return getViewName().asString();
  }

  /**
   * More detailed description, for documentation purposes.
   */
  public abstract String getDescription();

  /**
   * Measure type of this view.
   */
  public abstract Measure getMeasure();

  /**
   * Dimensions (a.k.a Tag Keys) to match with the associated {@link Measure}.
   * If no dimensions are specified, then all stats are recorded. Dimensions must be unique.
   *
   * <p>Note: The returned list is unmodifiable, attempts to update it will throw an
   * UnsupportedOperationException.
   */
  public abstract List<TagKey> getDimensions();

  /**
   * Applies the given match function to the underlying data type.
   */
  public abstract <T> T match(
      Function<DistributionView, T> p0,
      Function<IntervalView, T> p1);

  /**
   * The name of a {@code View}.
   */
  // This type should be used as the key when associating data with Views.
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
     * Creates a {@code View.Name} from a {@code String}.
     *
     * @param name the name {@code String}.
     * @return a {@code View.Name} with the given name {@code String}.
     */
    public static Name create(String name) {
      return new AutoValue_View_Name(name);
    }
  }

  /**
   * A {@link View} for distribution-base aggregations.
   */
  @Immutable
  @AutoValue
  public abstract static class DistributionView extends View {
    /**
     * Constructs a new {@link DistributionView}.
     */
    public static DistributionView create(
        String name,
        String description,
        Measure measure,
        DistributionAggregationDescriptor distributionAggregationDescriptor,
        List<TagKey> tagKeys) {
      return create(
          Name.create(name),
          description,
          measure,
          distributionAggregationDescriptor,
          tagKeys);
    }

    /**
     * Constructs a new {@link DistributionView}.
     */
    public static DistributionView create(
        Name name,
        String description,
        Measure measure,
        DistributionAggregationDescriptor distributionAggregationDescriptor,
        List<TagKey> tagKeys) {
      return new AutoValue_View_DistributionView(
          name,
          description,
          measure,
          Collections.unmodifiableList(new ArrayList<TagKey>(tagKeys)),
          distributionAggregationDescriptor);
    }

    /**
     * The {@link DistributionAggregationDescriptor} associated with this
     * {@link DistributionView}.
     */
    public abstract DistributionAggregationDescriptor getDistributionAggregationDescriptor();

    @Override
    public <T> T match(
        Function<DistributionView, T> p0,
        Function<IntervalView, T> p1) {
      return p0.apply(this);
    }
  }

  /**
   * A {@link View} for interval-based aggregations.
   */
  @Immutable
  @AutoValue
  public abstract static class IntervalView extends View {
    /**
     * Constructs a new {@link IntervalView}.
     */
    public static IntervalView create(
        String name,
        String description,
        Measure measure,
        IntervalAggregationDescriptor intervalAggregationDescriptor,
        List<TagKey> tagKeys) {
      return create(
          Name.create(name),
          description,
          measure,
          intervalAggregationDescriptor,
          tagKeys);
    }

    /**
     * Constructs a new {@link IntervalView}.
     */
    public static IntervalView create(
        Name name,
        String description,
        Measure measure,
        IntervalAggregationDescriptor intervalAggregationDescriptor,
        List<TagKey> tagKeys) {
      return new AutoValue_View_IntervalView(
          name,
          description,
          measure,
          Collections.unmodifiableList(new ArrayList<TagKey>(tagKeys)),
          intervalAggregationDescriptor);
    }

    /**
     * The {@link IntervalAggregationDescriptor} associated with this
     * {@link IntervalView}.
     */
    public abstract IntervalAggregationDescriptor getIntervalAggregationDescriptor();

    @Override
    public <T> T match(
        Function<DistributionView, T> p0,
        Function<IntervalView, T> p1) {
      return p1.apply(this);
    }
  }
}
