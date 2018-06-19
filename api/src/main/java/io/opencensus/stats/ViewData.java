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

import com.google.auto.value.AutoValue;
import io.opencensus.common.Duration;
import io.opencensus.common.Function;
import io.opencensus.common.Functions;
import io.opencensus.common.Timestamp;
import io.opencensus.internal.Utils;
import io.opencensus.stats.Aggregation.Count;
import io.opencensus.stats.Aggregation.Distribution;
import io.opencensus.stats.Aggregation.LastValue;
import io.opencensus.stats.Aggregation.Sum;
import io.opencensus.stats.AggregationData.CountData;
import io.opencensus.stats.AggregationData.DistributionData;
import io.opencensus.stats.AggregationData.LastValueDataDouble;
import io.opencensus.stats.AggregationData.LastValueDataLong;
import io.opencensus.stats.AggregationData.SumDataDouble;
import io.opencensus.stats.AggregationData.SumDataLong;
import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.stats.Measure.MeasureLong;
import io.opencensus.tags.TagValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.concurrent.Immutable;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

/**
 * The aggregated data for a particular {@link View}.
 *
 * @since 0.8
 */
@Immutable
@AutoValue
@AutoValue.CopyAnnotations
@SuppressWarnings("deprecation")
public abstract class ViewData {

  // Prevents this class from being subclassed anywhere else.
  ViewData() {}

  /**
   * The {@link View} associated with this {@link ViewData}.
   *
   * @since 0.8
   */
  public abstract View getView();

  /**
   * The {@link AggregationData} grouped by combination of tag values, associated with this {@link
   * ViewData}.
   *
   * @since 0.8
   */
  public abstract Map<List</*@Nullable*/ TagValue>, AggregationData> getAggregationMap();

  /**
   * Returns the {@link AggregationWindowData} associated with this {@link ViewData}.
   *
   * <p>{@link AggregationWindowData} is deprecated since 0.13, please avoid using this method. Use
   * {@link #getStart()} and {@link #getEnd()} instead.
   *
   * @return the {@code AggregationWindowData}.
   * @since 0.8
   * @deprecated in favor of {@link #getStart()} and {@link #getEnd()}.
   */
  @Deprecated
  public abstract AggregationWindowData getWindowData();

  /**
   * Returns the start {@code Timestamp} for a {@link ViewData}.
   *
   * @return the start {@code Timestamp}.
   * @since 0.13
   */
  public abstract Timestamp getStart();

  /**
   * Returns the end {@code Timestamp} for a {@link ViewData}.
   *
   * @return the end {@code Timestamp}.
   * @since 0.13
   */
  public abstract Timestamp getEnd();

  /**
   * Constructs a new {@link ViewData}.
   *
   * @param view the {@link View} associated with this {@link ViewData}.
   * @param map the mapping from {@link TagValue} list to {@link AggregationData}.
   * @param windowData the {@link AggregationWindowData}.
   * @return a {@code ViewData}.
   * @throws IllegalArgumentException if the types of {@code Aggregation} and {@code
   *     AggregationData} don't match, or the types of {@code Window} and {@code WindowData} don't
   *     match.
   * @since 0.8
   * @deprecated in favor of {@link #create(View, Map, Timestamp, Timestamp)}.
   */
  @Deprecated
  public static ViewData create(
      final View view,
      Map<? extends List</*@Nullable*/ TagValue>, ? extends AggregationData> map,
      final AggregationWindowData windowData) {
    checkWindow(view.getWindow(), windowData);
    final Map<List</*@Nullable*/ TagValue>, AggregationData> deepCopy =
        new HashMap<List</*@Nullable*/ TagValue>, AggregationData>();
    for (Entry<? extends List</*@Nullable*/ TagValue>, ? extends AggregationData> entry :
        map.entrySet()) {
      checkAggregation(view.getAggregation(), entry.getValue(), view.getMeasure());
      deepCopy.put(
          Collections.unmodifiableList(new ArrayList</*@Nullable*/ TagValue>(entry.getKey())),
          entry.getValue());
    }
    return windowData.match(
        new Function<ViewData.AggregationWindowData.CumulativeData, ViewData>() {
          @Override
          public ViewData apply(ViewData.AggregationWindowData.CumulativeData arg) {
            return createInternal(
                view, Collections.unmodifiableMap(deepCopy), arg, arg.getStart(), arg.getEnd());
          }
        },
        new Function<ViewData.AggregationWindowData.IntervalData, ViewData>() {
          @Override
          public ViewData apply(ViewData.AggregationWindowData.IntervalData arg) {
            Duration duration = ((View.AggregationWindow.Interval) view.getWindow()).getDuration();
            return createInternal(
                view,
                Collections.unmodifiableMap(deepCopy),
                arg,
                arg.getEnd()
                    .addDuration(Duration.create(-duration.getSeconds(), -duration.getNanos())),
                arg.getEnd());
          }
        },
        Functions.<ViewData>throwAssertionError());
  }

  /**
   * Constructs a new {@link ViewData}.
   *
   * @param view the {@link View} associated with this {@link ViewData}.
   * @param map the mapping from {@link TagValue} list to {@link AggregationData}.
   * @param start the start {@link Timestamp} for this {@link ViewData}.
   * @param end the end {@link Timestamp} for this {@link ViewData}.
   * @return a {@code ViewData}.
   * @throws IllegalArgumentException if the types of {@code Aggregation} and {@code
   *     AggregationData} don't match.
   * @since 0.13
   */
  public static ViewData create(
      View view,
      Map<? extends List</*@Nullable*/ TagValue>, ? extends AggregationData> map,
      Timestamp start,
      Timestamp end) {
    Map<List</*@Nullable*/ TagValue>, AggregationData> deepCopy =
        new HashMap<List</*@Nullable*/ TagValue>, AggregationData>();
    for (Entry<? extends List</*@Nullable*/ TagValue>, ? extends AggregationData> entry :
        map.entrySet()) {
      checkAggregation(view.getAggregation(), entry.getValue(), view.getMeasure());
      deepCopy.put(
          Collections.unmodifiableList(new ArrayList</*@Nullable*/ TagValue>(entry.getKey())),
          entry.getValue());
    }
    return createInternal(
        view,
        Collections.unmodifiableMap(deepCopy),
        AggregationWindowData.CumulativeData.create(start, end),
        start,
        end);
  }

  // Suppresses a nullness warning about calls to the AutoValue_ViewData constructor. The generated
  // constructor does not have the @Nullable annotation on TagValue.
  private static ViewData createInternal(
      View view,
      Map<List</*@Nullable*/ TagValue>, AggregationData> aggregationMap,
      AggregationWindowData window,
      Timestamp start,
      Timestamp end) {
    @SuppressWarnings("nullness")
    Map<List<TagValue>, AggregationData> map = aggregationMap;
    return new AutoValue_ViewData(view, map, window, start, end);
  }

  private static void checkWindow(
      View.AggregationWindow window, final AggregationWindowData windowData) {
    window.match(
        new Function<View.AggregationWindow.Cumulative, Void>() {
          @Override
          public Void apply(View.AggregationWindow.Cumulative arg) {
            Utils.checkArgument(
                windowData instanceof AggregationWindowData.CumulativeData,
                createErrorMessageForWindow(arg, windowData));
            return null;
          }
        },
        new Function<View.AggregationWindow.Interval, Void>() {
          @Override
          public Void apply(View.AggregationWindow.Interval arg) {
            Utils.checkArgument(
                windowData instanceof AggregationWindowData.IntervalData,
                createErrorMessageForWindow(arg, windowData));
            return null;
          }
        },
        Functions.</*@Nullable*/ Void>throwAssertionError());
  }

  private static String createErrorMessageForWindow(
      View.AggregationWindow window, AggregationWindowData windowData) {
    return "AggregationWindow and AggregationWindowData types mismatch. "
        + "AggregationWindow: "
        + window.getClass().getSimpleName()
        + " AggregationWindowData: "
        + windowData.getClass().getSimpleName();
  }

  private static void checkAggregation(
      final Aggregation aggregation, final AggregationData aggregationData, final Measure measure) {
    aggregation.match(
        new Function<Sum, Void>() {
          @Override
          public Void apply(Sum arg) {
            measure.match(
                new Function<MeasureDouble, Void>() {
                  @Override
                  public Void apply(MeasureDouble arg) {
                    Utils.checkArgument(
                        aggregationData instanceof SumDataDouble,
                        createErrorMessageForAggregation(aggregation, aggregationData));
                    return null;
                  }
                },
                new Function<MeasureLong, Void>() {
                  @Override
                  public Void apply(MeasureLong arg) {
                    Utils.checkArgument(
                        aggregationData instanceof SumDataLong,
                        createErrorMessageForAggregation(aggregation, aggregationData));
                    return null;
                  }
                },
                Functions.</*@Nullable*/ Void>throwAssertionError());
            return null;
          }
        },
        new Function<Count, Void>() {
          @Override
          public Void apply(Count arg) {
            Utils.checkArgument(
                aggregationData instanceof CountData,
                createErrorMessageForAggregation(aggregation, aggregationData));
            return null;
          }
        },
        new Function<Distribution, Void>() {
          @Override
          public Void apply(Distribution arg) {
            Utils.checkArgument(
                aggregationData instanceof DistributionData,
                createErrorMessageForAggregation(aggregation, aggregationData));
            return null;
          }
        },
        new Function<LastValue, Void>() {
          @Override
          public Void apply(LastValue arg) {
            measure.match(
                new Function<MeasureDouble, Void>() {
                  @Override
                  public Void apply(MeasureDouble arg) {
                    Utils.checkArgument(
                        aggregationData instanceof LastValueDataDouble,
                        createErrorMessageForAggregation(aggregation, aggregationData));
                    return null;
                  }
                },
                new Function<MeasureLong, Void>() {
                  @Override
                  public Void apply(MeasureLong arg) {
                    Utils.checkArgument(
                        aggregationData instanceof LastValueDataLong,
                        createErrorMessageForAggregation(aggregation, aggregationData));
                    return null;
                  }
                },
                Functions.</*@Nullable*/ Void>throwAssertionError());
            return null;
          }
        },
        new Function<Aggregation, Void>() {
          @Override
          public Void apply(Aggregation arg) {
            // TODO(songya): remove this once Mean aggregation is completely removed. Before that
            // we need to continue supporting Mean, since it could still be used by users and some
            // deprecated RPC views.
            if (arg instanceof Aggregation.Mean) {
              Utils.checkArgument(
                  aggregationData instanceof AggregationData.MeanData,
                  createErrorMessageForAggregation(aggregation, aggregationData));
              return null;
            }
            throw new AssertionError();
          }
        });
  }

  private static String createErrorMessageForAggregation(
      Aggregation aggregation, AggregationData aggregationData) {
    return "Aggregation and AggregationData types mismatch. "
        + "Aggregation: "
        + aggregation.getClass().getSimpleName()
        + " AggregationData: "
        + aggregationData.getClass().getSimpleName();
  }

  /**
   * The {@code AggregationWindowData} for a {@link ViewData}.
   *
   * @since 0.8
   * @deprecated since 0.13, please use start and end {@link Timestamp} instead.
   */
  @Deprecated
  @Immutable
  public abstract static class AggregationWindowData {

    private AggregationWindowData() {}

    /**
     * Applies the given match function to the underlying data type.
     *
     * @since 0.8
     */
    public abstract <T> T match(
        Function<? super CumulativeData, T> p0,
        Function<? super IntervalData, T> p1,
        Function<? super AggregationWindowData, T> defaultFunction);

    /**
     * Cumulative {@code AggregationWindowData}.
     *
     * @since 0.8
     * @deprecated since 0.13, please use start and end {@link Timestamp} instead.
     */
    @Deprecated
    @Immutable
    @AutoValue
    @AutoValue.CopyAnnotations
    public abstract static class CumulativeData extends AggregationWindowData {

      CumulativeData() {}

      /**
       * Returns the start {@code Timestamp} for a {@link CumulativeData}.
       *
       * @return the start {@code Timestamp}.
       * @since 0.8
       */
      public abstract Timestamp getStart();

      /**
       * Returns the end {@code Timestamp} for a {@link CumulativeData}.
       *
       * @return the end {@code Timestamp}.
       * @since 0.8
       */
      public abstract Timestamp getEnd();

      @Override
      public final <T> T match(
          Function<? super CumulativeData, T> p0,
          Function<? super IntervalData, T> p1,
          Function<? super AggregationWindowData, T> defaultFunction) {
        return p0.apply(this);
      }

      /**
       * Constructs a new {@link CumulativeData}.
       *
       * @since 0.8
       */
      public static CumulativeData create(Timestamp start, Timestamp end) {
        if (start.compareTo(end) > 0) {
          throw new IllegalArgumentException("Start time is later than end time.");
        }
        return new AutoValue_ViewData_AggregationWindowData_CumulativeData(start, end);
      }
    }

    /**
     * Interval {@code AggregationWindowData}.
     *
     * @since 0.8
     * @deprecated since 0.13, please use start and end {@link Timestamp} instead.
     */
    @Deprecated
    @Immutable
    @AutoValue
    @AutoValue.CopyAnnotations
    public abstract static class IntervalData extends AggregationWindowData {

      IntervalData() {}

      /**
       * Returns the end {@code Timestamp} for an {@link IntervalData}.
       *
       * @return the end {@code Timestamp}.
       * @since 0.8
       */
      public abstract Timestamp getEnd();

      @Override
      public final <T> T match(
          Function<? super CumulativeData, T> p0,
          Function<? super IntervalData, T> p1,
          Function<? super AggregationWindowData, T> defaultFunction) {
        return p1.apply(this);
      }

      /**
       * Constructs a new {@link IntervalData}.
       *
       * @since 0.8
       */
      public static IntervalData create(Timestamp end) {
        return new AutoValue_ViewData_AggregationWindowData_IntervalData(end);
      }
    }
  }
}
