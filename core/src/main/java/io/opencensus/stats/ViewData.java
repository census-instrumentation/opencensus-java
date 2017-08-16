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
   * The {@link AggregationData}s grouped by combination of tag values, associated with this
   * {@link ViewData}.
   */
  // TODO(sebright): Create a TagValue class.
  public abstract Map<List<Object>, List<AggregationData>> getAggregationMap();

  /**
   * Returns the {@link WindowData} associated with this {@link ViewData}.
   *
   * @return the {@code WindowData}.
   */
  public abstract WindowData getWindowData();

  /** Constructs a new {@link ViewData}. */
  public static ViewData create(
      View view,
      Map<? extends List<? extends Object>, List<AggregationData>> map,
      final WindowData windowData) {
    view.getWindow()
        .match(
            new Function<View.Window.Cumulative, Void>() {
              @Override
              public Void apply(View.Window.Cumulative arg) {
                if (!(windowData instanceof WindowData.CumulativeData)) {
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
            new Function<View.Window.Interval, Void>() {
              @Override
              public Void apply(View.Window.Interval arg) {
                if (!(windowData instanceof WindowData.IntervalData)) {
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

    Map<List<Object>, List<AggregationData>> deepCopy =
        new HashMap<List<Object>, List<AggregationData>>();
    for (Entry<? extends List<? extends Object>, List<AggregationData>> entry : map.entrySet()) {
      deepCopy.put(
          Collections.unmodifiableList(new ArrayList<Object>(entry.getKey())),
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
        Function<? super CumulativeData, T> p0,
        Function<? super IntervalData, T> p1,
        Function<? super WindowData, T> defaultFunction);

    /** Cumulative {@code WindowData.} */
    @Immutable
    @AutoValue
    public abstract static class CumulativeData extends WindowData {

      CumulativeData() {}

      /**
       * Returns the start {@code Timestamp} for a {@link CumulativeData}.
       *
       * @return the start {@code Timestamp}.
       */
      public abstract Timestamp getStart();

      /**
       * Returns the end {@code Timestamp} for a {@link CumulativeData}.
       *
       * @return the end {@code Timestamp}.
       */
      public abstract Timestamp getEnd();

      @Override
      public final <T> T match(
          Function<? super CumulativeData, T> p0,
          Function<? super IntervalData, T> p1,
          Function<? super WindowData, T> defaultFunction) {
        return p0.apply(this);
      }

      /** Constructs a new {@link CumulativeData}. */
      public static CumulativeData create(Timestamp start, Timestamp end) {
        if (start.compareTo(end) > 0) {
          throw new IllegalArgumentException("Start time is later than end time.");
        }
        return new AutoValue_ViewData_WindowData_CumulativeData(start, end);
      }
    }

    /** Interval {@code WindowData.} */
    @Immutable
    @AutoValue
    public abstract static class IntervalData extends WindowData {

      IntervalData() {}

      /**
       * Returns the end {@code Timestamp} for an {@link IntervalData}.
       *
       * @return the end {@code Timestamp}.
       */
      public abstract Timestamp getEnd();

      @Override
      public final <T> T match(
          Function<? super CumulativeData, T> p0,
          Function<? super IntervalData, T> p1,
          Function<? super WindowData, T> defaultFunction) {
        return p1.apply(this);
      }

      /** Constructs a new {@link IntervalData}. */
      public static IntervalData create(Timestamp end) {
        return new AutoValue_ViewData_WindowData_IntervalData(end);
      }
    }
  }
}
