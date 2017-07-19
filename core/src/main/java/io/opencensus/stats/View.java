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
import io.opencensus.common.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A View specifies an aggregation and a set of tag keys. The aggregation will be broken
 * down by the unique set of matching tag values for each measure.
 */
@Immutable
@AutoValue
public abstract class View {

  View() {
  }

  /**
   * Name of view. Must be unique.
   */
  public abstract Name getName();

  /**
   * More detailed description, for documentation purposes.
   */
  public abstract String getDescription();

  /**
   * Measure type of this view.
   */
  public abstract Measure getMeasure();

  /**
   * The {@link Aggregation}s associated with this {@link View}.
   */
  public abstract List<Aggregation> getAggregations();

  /**
   * Columns (a.k.a Tag Keys) to match with the associated {@link Measure}.
   *
   * <p>{@link Measure} will be recorded in a "greedy" way. That is, every view aggregates every
   * measure. This is similar to doing a GROUPBY on view’s columns. If no columns are specified,
   * then all stats will be recorded. Columns must be unique.
   *
   * <p>Note: The returned list is unmodifiable, attempts to update it will throw an
   * UnsupportedOperationException.
   */
  public abstract List<TagKey> getColumns();

  /**
   * Returns the time window for this {@code View}.
   *
   * @return the time window. Time window is null for cumulative views.
   */
  @Nullable
  public abstract Duration getWindow();

  /**
   * Constructs a new {@link View}.
   */
  public static View create(
      Name name,
      String description,
      Measure measure,
      List<Aggregation> aggregations,
      List<TagKey> columns,
      @Nullable Duration window) {
    return new AutoValue_View(
        name,
        description,
        measure,
        Collections.unmodifiableList(new ArrayList<Aggregation>(aggregations)),
        Collections.unmodifiableList(new ArrayList<TagKey>(columns)),
        window);
  }

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
}
