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

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.value.AutoValue;
import com.google.common.annotations.VisibleForTesting;
import io.opencensus.common.Duration;
import io.opencensus.common.Function;
import io.opencensus.internal.StringUtil;
import io.opencensus.tags.TagKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * A View specifies an aggregation and a set of tag keys. The aggregation will be broken down by the
 * unique set of matching tag values for each measure.
 *
 * @since 0.8
 */
@Immutable
@AutoValue
// Suppress Checker Framework warning about missing @Nullable in generated equals method.
@AutoValue.CopyAnnotations
@SuppressWarnings({"nullness", "deprecation"})
public abstract class View {

  @VisibleForTesting static final int NAME_MAX_LENGTH = 255;

  View() {}

  /**
   * Name of view. Must be unique.
   *
   * @since 0.8
   */
  public abstract Name getName();

  /**
   * More detailed description, for documentation purposes.
   *
   * @since 0.8
   */
  public abstract String getDescription();

  /**
   * Measure type of this view.
   *
   * @since 0.8
   */
  public abstract Measure getMeasure();

  /**
   * The {@link Aggregation} associated with this {@link View}.
   *
   * @since 0.8
   */
  public abstract Aggregation getAggregation();

  /**
   * Columns (a.k.a Tag Keys) to match with the associated {@link Measure}.
   *
   * <p>{@link Measure} will be recorded in a "greedy" way. That is, every view aggregates every
   * measure. This is similar to doing a GROUPBY on view’s columns. Columns must be unique.
   *
   * @since 0.8
   */
  public abstract List<TagKey> getColumns();

  /**
   * Returns the time {@link AggregationWindow} for this {@code View}.
   *
   * @return the time {@link AggregationWindow}.
   * @since 0.8
   * @deprecated since 0.13. In the future all {@link View}s will be cumulative.
   */
  @Deprecated
  public abstract AggregationWindow getWindow();

  /**
   * Constructs a new {@link View}.
   *
   * @param name the {@link Name} of view. Must be unique.
   * @param description the description of view.
   * @param measure the {@link Measure} to be aggregated by this view.
   * @param aggregation the basic {@link Aggregation} that this view will support.
   * @param columns the {@link TagKey}s that this view will aggregate on. Columns should not contain
   *     duplicates.
   * @param window the {@link AggregationWindow} of view.
   * @return a new {@link View}.
   * @since 0.8
   * @deprecated in favor of {@link #create(Name, String, Measure, Aggregation, List)}.
   */
  @Deprecated
  public static View create(
      Name name,
      String description,
      Measure measure,
      Aggregation aggregation,
      List<TagKey> columns,
      AggregationWindow window) {
    checkArgument(new HashSet<TagKey>(columns).size() == columns.size(), "Columns have duplicate.");

    return new AutoValue_View(
        name,
        description,
        measure,
        aggregation,
        Collections.unmodifiableList(new ArrayList<TagKey>(columns)),
        window);
  }

  /**
   * Constructs a new {@link View}.
   *
   * @param name the {@link Name} of view. Must be unique.
   * @param description the description of view.
   * @param measure the {@link Measure} to be aggregated by this view.
   * @param aggregation the basic {@link Aggregation} that this view will support.
   * @param columns the {@link TagKey}s that this view will aggregate on. Columns should not contain
   *     duplicates.
   * @return a new {@link View}.
   * @since 0.13
   */
  public static View create(
      Name name,
      String description,
      Measure measure,
      Aggregation aggregation,
      List<TagKey> columns) {
    checkArgument(new HashSet<TagKey>(columns).size() == columns.size(), "Columns have duplicate.");

    return new AutoValue_View(
        name,
        description,
        measure,
        aggregation,
        Collections.unmodifiableList(new ArrayList<TagKey>(columns)),
        AggregationWindow.Cumulative.create());
  }

  /**
   * The name of a {@code View}.
   *
   * @since 0.8
   */
  // This type should be used as the key when associating data with Views.
  @Immutable
  @AutoValue
  // Suppress Checker Framework warning about missing @Nullable in generated equals method.
  @AutoValue.CopyAnnotations
  @SuppressWarnings("nullness")
  public abstract static class Name {

    Name() {}

    /**
     * Returns the name as a {@code String}.
     *
     * @return the name as a {@code String}.
     * @since 0.8
     */
    public abstract String asString();

    /**
     * Creates a {@code View.Name} from a {@code String}. Should be a ASCII string with a length no
     * greater than 255 characters.
     *
     * <p>Suggested format for name: {@code <web_host>/<path>}.
     *
     * @param name the name {@code String}.
     * @return a {@code View.Name} with the given name {@code String}.
     * @since 0.8
     */
    public static Name create(String name) {
      checkArgument(
          StringUtil.isPrintableString(name) && name.length() <= NAME_MAX_LENGTH,
          "Name should be a ASCII string with a length no greater than 255 characters.");
      return new AutoValue_View_Name(name);
    }
  }

  /**
   * The time window for a {@code View}.
   *
   * @since 0.8
   * @deprecated since 0.13. In the future all {@link View}s will be cumulative.
   */
  @Deprecated
  @Immutable
  public abstract static class AggregationWindow {

    private AggregationWindow() {}

    /**
     * Applies the given match function to the underlying data type.
     *
     * @since 0.8
     */
    public abstract <T> T match(
        Function<? super Cumulative, T> p0,
        Function<? super Interval, T> p1,
        Function<? super AggregationWindow, T> defaultFunction);

    /**
     * Cumulative (infinite interval) time {@code AggregationWindow}.
     *
     * @since 0.8
     * @deprecated since 0.13. In the future all {@link View}s will be cumulative.
     */
    @Deprecated
    @Immutable
    @AutoValue
    // Suppress Checker Framework warning about missing @Nullable in generated equals method.
    @AutoValue.CopyAnnotations
    @SuppressWarnings("nullness")
    public abstract static class Cumulative extends AggregationWindow {

      private static final Cumulative CUMULATIVE =
          new AutoValue_View_AggregationWindow_Cumulative();

      Cumulative() {}

      /**
       * Constructs a cumulative {@code AggregationWindow} that does not have an explicit {@code
       * Duration}. Instead, cumulative {@code AggregationWindow} always has an interval of infinite
       * {@code Duration}.
       *
       * @return a cumulative {@code AggregationWindow}.
       * @since 0.8
       */
      public static Cumulative create() {
        return CUMULATIVE;
      }

      @Override
      public final <T> T match(
          Function<? super Cumulative, T> p0,
          Function<? super Interval, T> p1,
          Function<? super AggregationWindow, T> defaultFunction) {
        return p0.apply(this);
      }
    }

    /**
     * Interval (finite interval) time {@code AggregationWindow}.
     *
     * @since 0.8
     * @deprecated since 0.13. In the future all {@link View}s will be cumulative.
     */
    @Deprecated
    @Immutable
    @AutoValue
    // Suppress Checker Framework warning about missing @Nullable in generated equals method.
    @AutoValue.CopyAnnotations
    @SuppressWarnings("nullness")
    public abstract static class Interval extends AggregationWindow {

      private static final Duration ZERO = Duration.create(0, 0);

      Interval() {}

      /**
       * Returns the {@code Duration} associated with this {@code Interval}.
       *
       * @return a {@code Duration}.
       * @since 0.8
       */
      public abstract Duration getDuration();

      /**
       * Constructs an interval {@code AggregationWindow} that has a finite explicit {@code
       * Duration}.
       *
       * <p>The {@code Duration} should be able to round to milliseconds. Currently interval window
       * cannot have smaller {@code Duration} such as microseconds or nanoseconds.
       *
       * @return an interval {@code AggregationWindow}.
       * @since 0.8
       */
      public static Interval create(Duration duration) {
        checkArgument(duration.compareTo(ZERO) > 0, "Duration must be positive");
        return new AutoValue_View_AggregationWindow_Interval(duration);
      }

      @Override
      public final <T> T match(
          Function<? super Cumulative, T> p0,
          Function<? super Interval, T> p1,
          Function<? super AggregationWindow, T> defaultFunction) {
        return p1.apply(this);
      }
    }
  }
}
