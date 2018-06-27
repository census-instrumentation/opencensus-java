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

import io.opencensus.internal.Utils;
import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.stats.Measure.MeasureLong;
import io.opencensus.tags.TagContext;
import java.util.Map;
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
   * Associate the contextual information of an {@code Exemplar} to this {@link MeasureMap}.
   *
   * <p>If this method is called multiple times, the string maps should be merged. If there are
   * multiple values with the same key in the maps, only the last value will be kept. I.e equivalent
   * to {@code new HashMap<>().putAll(map1).putAll(map2)}.
   *
   * @param attachments contextual information of an {@code Exemplar}.
   * @return this
   * @since 0.16
   */
  public MeasureMap putAttachments(Map<String, String> attachments) {
    // Provides a default no-op implementation to avoid breaking other existing sub-classes.
    Utils.checkNotNull(attachments, "attachments");
    return this;
  }

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
