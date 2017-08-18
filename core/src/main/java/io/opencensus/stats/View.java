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

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.value.AutoValue;
import io.opencensus.common.Duration;
import io.opencensus.common.Function;
import io.opencensus.tags.TagKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
   * Name of view. Must be unique. Should be a ASCII string with a length no greater than 255
   * characters.
   *
   * <p>Suggested format for name: {@code <web_host>/<path>}.
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
   * The {@link Aggregation}s associated with this {@link View}. {@link Aggregation}s should be
   * unique within one view.
   */
  public abstract List<Aggregation> getAggregations();

  /**
   * Columns (a.k.a Tag Keys) to match with the associated {@link Measure}.
   *
   * <p>{@link Measure} will be recorded in a "greedy" way. That is, every view aggregates every
   * measure. This is similar to doing a GROUPBY on view’s columns. Columns must be unique.
   */
  public abstract List<TagKey> getColumns();

  /**
   * Returns the time {@link Window} for this {@code View}.
   *
   * @return the time {@link Window}.
   */
  public abstract Window getWindow();

  /**
   * Constructs a new {@link View}.
   *
   * @param name the {@link Name} of view. Must be unique. Suggested format for name:
   *     {@code <web_host>/<path>}.
   * @param description the description of view.
   * @param measure the {@link Measure} to be aggregated by this view.
   * @param aggregations basic {@link Aggregation}s that this view will support. The aggregation
   *     list should not contain duplicates.
   * @param columns the {@link TagKey}s that this view will aggregate on. Columns should not contain
   *     duplicates.
   * @param window the {@link Window} of view.
   * @return a new {@link View}.
   */
  public static View create(
      Name name,
      String description,
      Measure measure,
      List<Aggregation> aggregations,
      List<? extends TagKey> columns,
      Window window) {
    checkArgument(new HashSet<Aggregation>(aggregations).size() == aggregations.size(),
        "Aggregations have duplicate.");
    checkArgument(new HashSet<TagKey>(columns).size() == columns.size(),
        "Columns have duplicate.");

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

  /** The time window for a {@code View}. */
  @Immutable
  public abstract static class Window {

    private Window() {}

    /**
     * Applies the given match function to the underlying data type.
     */
    public abstract <T> T match(
        Function<? super Cumulative, T> p0,
        Function<? super Interval, T> p1,
        Function<? super Window, T> defaultFunction);

    /** Cumulative (infinite interval) time {@code Window}. */
    @Immutable
    @AutoValue
    public abstract static class Cumulative extends Window {

      private static final Cumulative CUMULATIVE = new AutoValue_View_Window_Cumulative();

      Cumulative() {}

      /**
       * Constructs a cumulative {@code Window} that does not have an explicit {@code Duration}.
       * Instead, cumulative {@code Window} always has an interval of infinite {@code Duration}.
       *
       * @return a cumulative {@code Window}.
       */
      public static Cumulative create() {
        return CUMULATIVE;
      }

      @Override
      public final <T> T match(
          Function<? super Cumulative, T> p0,
          Function<? super Interval, T> p1,
          Function<? super Window, T> defaultFunction) {
        return  p0.apply(this);
      }
    }

    /** Interval (finite interval) time {@code Window.} */
    @Immutable
    @AutoValue
    public abstract static class Interval extends Window {

      Interval() {}

      /**
       * Returns the {@code Duration} associated with this {@code Interval}.
       *
       * @return a {@code Duration}.
       */
      public abstract Duration getDuration();


      /**
       * Constructs an interval {@code Window} that has a finite explicit {@code Duration}.
       *
       * @return an interval {@code Window}.
       */
      public static Interval create(Duration duration) {
        return new AutoValue_View_Window_Interval(duration);
      }

      @Override
      public final <T> T match(
          Function<? super Cumulative, T> p0,
          Function<? super Interval, T> p1,
          Function<? super Window, T> defaultFunction) {
        return  p1.apply(this);
      }
    }
  }
}
