/*
 * Copyright 2018, OpenCensus Authors
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

package io.opencensus.metrics;

import com.google.auto.value.AutoValue;
import io.opencensus.common.ExperimentalApi;
import io.opencensus.common.Function;
import io.opencensus.internal.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * Class that holds a list of either {@link TimeSeriesGauge} or {@link TimeSeriesCumulative}.
 *
 * @since 0.16
 */
@ExperimentalApi
@Immutable
public abstract class TimeSeriesList {

  TimeSeriesList() {}

  /**
   * Applies the given match function to the underlying data type.
   *
   * @since 0.16
   */
  public abstract <T> T match(
      Function<? super TimeSeriesGaugeList, T> gaugeListFunction,
      Function<? super TimeSeriesCumulativeList, T> cumulativeListFunction,
      Function<? super TimeSeriesList, T> defaultFunction);

  /**
   * Class that holds a list of {@link TimeSeriesGauge}.
   *
   * @since 0.16
   */
  @ExperimentalApi
  @Immutable
  @AutoValue
  public abstract static class TimeSeriesGaugeList extends TimeSeriesList {

    TimeSeriesGaugeList() {}

    @Override
    public final <T> T match(
        Function<? super TimeSeriesGaugeList, T> gaugeListFunction,
        Function<? super TimeSeriesCumulativeList, T> cumulativeListFunction,
        Function<? super TimeSeriesList, T> defaultFunction) {
      return gaugeListFunction.apply(this);
    }

    /**
     * Creates a {@link TimeSeriesGaugeList}.
     *
     * @param list a list of {@link TimeSeriesGauge}.
     * @return a {code TimeSeriesGaugeList}.
     * @since 0.16
     */
    public static TimeSeriesGaugeList create(List<TimeSeriesGauge> list) {
      Utils.checkNotNull(list, "list");
      Utils.checkListElementNotNull(list, "timeSeriesGauge");
      return new AutoValue_TimeSeriesList_TimeSeriesGaugeList(
          Collections.unmodifiableList(new ArrayList<TimeSeriesGauge>(list)));
    }

    /**
     * Returns the list of {@link TimeSeriesGauge}.
     *
     * @return the list of {@code TimeSeriesGauge}.
     * @since 0.16
     */
    public abstract List<TimeSeriesGauge> getList();
  }

  /**
   * Class that holds a list of {@link TimeSeriesCumulative}.
   *
   * @since 0.16
   */
  @ExperimentalApi
  @Immutable
  @AutoValue
  public abstract static class TimeSeriesCumulativeList extends TimeSeriesList {

    TimeSeriesCumulativeList() {}

    @Override
    public final <T> T match(
        Function<? super TimeSeriesGaugeList, T> gaugeListFunction,
        Function<? super TimeSeriesCumulativeList, T> cumulativeListFunction,
        Function<? super TimeSeriesList, T> defaultFunction) {
      return cumulativeListFunction.apply(this);
    }

    /**
     * Creates a {@link TimeSeriesCumulativeList}.
     *
     * @param list a list of {@link TimeSeriesCumulative}.
     * @return a {code TimeSeriesCumulativeList}.
     * @since 0.16
     */
    public static TimeSeriesCumulativeList create(List<TimeSeriesCumulative> list) {
      Utils.checkNotNull(list, "list");
      Utils.checkListElementNotNull(list, "timeSeriesCumulative");
      return new AutoValue_TimeSeriesList_TimeSeriesCumulativeList(
          Collections.unmodifiableList(new ArrayList<TimeSeriesCumulative>(list)));
    }

    /**
     * Returns the list of {@link TimeSeriesCumulative}.
     *
     * @return the list of {@code TimeSeriesCumulative}.
     * @since 0.16
     */
    public abstract List<TimeSeriesCumulative> getList();
  }
}
