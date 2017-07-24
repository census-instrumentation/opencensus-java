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
import io.opencensus.common.Functions;
import io.opencensus.common.Timestamp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.concurrent.Immutable;


/** The aggregated data for a particular {@link View}. */
@Immutable
@AutoValue
public abstract class ViewData {

  // Prevents this class from being subclassed anywhere else.
  ViewData() {}

  /** The {@link View} associated with this {@link ViewData}. */
  public abstract View getView();

  /**
   * The {@link AggregationData}s grouped by combination of {@link TagValue}s, associated with this
   * {@link ViewData}.
   */
  public abstract Map<List<TagValue>, List<AggregationData>> getAggregationMap();

  /**
   * Returns the {@link WindowData} associated with this {@link ViewData}.
   *
   * @return the {@code WindowData}.
   */
  public abstract WindowData getWindowData();

  /** Constructs a new {@link ViewData}. */
  public static ViewData create(
      View view, Map<List<TagValue>, List<AggregationData>> map, final WindowData windowData) {
    view.getWindow()
        .match(
            new Function<View.Window.CumulativeWindow, Void>() {
              @Override
              public Void apply(View.Window.CumulativeWindow arg) {
                if (!(windowData instanceof WindowData.CumulativeWindowData)) {
                  throw new IllegalArgumentException(
                      "Window and WindowData types mismatch. "
                          + "Window: "
                          + arg
                          + " WindowData: "
                          + windowData);
                }
                return null;
              }
            },
            new Function<View.Window.IntervalWindow, Void>() {
              @Override
              public Void apply(View.Window.IntervalWindow arg) {
                if (!(windowData instanceof WindowData.IntervalWindowData)) {
                  throw new IllegalArgumentException(
                      "Window and WindowData types mismatch. "
                          + "Window: "
                          + arg
                          + " WindowData: "
                          + windowData);
                }
                return null;
              }
            },
            Functions.<Void>throwIllegalArgumentException());

    Map<List<TagValue>, List<AggregationData>> deepCopy =
        new HashMap<List<TagValue>, List<AggregationData>>();
    for (Entry<List<TagValue>, List<AggregationData>> entry : map.entrySet()) {
      deepCopy.put(
          Collections.unmodifiableList(new ArrayList<TagValue>(entry.getKey())),
          Collections.unmodifiableList(new ArrayList<AggregationData>(entry.getValue())));
    }

    return new AutoValue_ViewData(view, Collections.unmodifiableMap(deepCopy), windowData);
  }

  /** The {@code WindowData} for a {@link ViewData}. */
  @Immutable
  public abstract static class WindowData {

    private WindowData() {}

    /** Applies the given match function to the underlying data type. */
    public abstract <T> T match(
        Function<? super CumulativeWindowData, T> p0,
        Function<? super IntervalWindowData, T> p1,
        Function<? super WindowData, T> defaultFunction);

    /** Cumulative {@code WindowData.} */
    @Immutable
    @AutoValue
    public abstract static class CumulativeWindowData extends WindowData {

      CumulativeWindowData() {}

      /**
       * Returns the start {@code Timestamp} for a {@link CumulativeWindowData}.
       *
       * @return the start {@code Timestamp}.
       */
      public abstract Timestamp getStart();

      /**
       * Returns the end {@code Timestamp} for a {@link CumulativeWindowData}.
       *
       * @return the end {@code Timestamp}.
       */
      public abstract Timestamp getEnd();

      @Override
      public final <T> T match(
          Function<? super CumulativeWindowData, T> p0,
          Function<? super IntervalWindowData, T> p1,
          Function<? super WindowData, T> defaultFunction) {
        return p0.apply(this);
      }

      /** Constructs a new {@link CumulativeWindowData}. */
      public static CumulativeWindowData create(Timestamp start, Timestamp end) {
        if (start.compareTo(end) > 0) {
          throw new IllegalArgumentException("Start time is later than end time.");
        }
        return new AutoValue_ViewData_WindowData_CumulativeWindowData(start, end);
      }
    }

    /** Interval {@code WindowData.} */
    @Immutable
    @AutoValue
    public abstract static class IntervalWindowData extends WindowData {

      IntervalWindowData() {}

      /**
       * Returns the end {@code Timestamp} for an {@link IntervalWindowData}.
       *
       * @return the end {@code Timestamp}.
       */
      public abstract Timestamp getEnd();

      @Override
      public final <T> T match(
          Function<? super CumulativeWindowData, T> p0,
          Function<? super IntervalWindowData, T> p1,
          Function<? super WindowData, T> defaultFunction) {
        return p1.apply(this);
      }

      /** Constructs a new {@link IntervalWindowData}. */
      public static IntervalWindowData create(Timestamp end) {
        return new AutoValue_ViewData_WindowData_IntervalWindowData(end);
      }
    }
  }
}
