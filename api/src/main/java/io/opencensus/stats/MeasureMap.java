/*
 * Copyright 2017, OpenCensus Authors
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

import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.stats.Measure.MeasureLong;
import io.opencensus.tags.TagContext;
import io.opencensus.trace.SpanContext;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * A map from {@link Measure}s to measured values to be recorded at the same time.
 *
 * @since 0.8
 */
@NotThreadSafe
public abstract class MeasureMap {

  /**
   * Associates the {@link MeasureDouble} with the given value. Subsequent updates to the same
   * {@link MeasureDouble} will overwrite the previous value.
   *
   * @param measure the {@link MeasureDouble}
   * @param value the value to be associated with {@code measure}
   * @return this
   * @since 0.8
   */
  public abstract MeasureMap put(MeasureDouble measure, double value);

  /**
   * Associates the {@link MeasureLong} with the given value. Subsequent updates to the same {@link
   * MeasureLong} will overwrite the previous value.
   *
   * @param measure the {@link MeasureLong}
   * @param value the value to be associated with {@code measure}
   * @return this
   * @since 0.8
   */
  public abstract MeasureMap put(MeasureLong measure, long value);

  /**
   * Associate measurements with a {@link SpanContext}.
   *
   * @param spanContext the {@link SpanContext}
   * @return this
   * @since 0.15
   */
  public abstract MeasureMap withSpanContext(SpanContext spanContext);

  /**
   * Records all of the measures at the same time, with the current {@link TagContext}.
   *
   * <p>This method records all of the stats in the {@code MeasureMap} every time it is called.
   *
   * @since 0.8
   */
  public abstract void record();

  /**
   * Records all of the measures at the same time, with an explicit {@link TagContext}.
   *
   * <p>This method records all of the stats in the {@code MeasureMap} every time it is called.
   *
   * @param tags the tags associated with the measurements.
   * @since 0.8
   */
  public abstract void record(TagContext tags);
}
