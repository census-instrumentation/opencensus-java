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

package io.opencensus.implcore.metrics;

import io.opencensus.common.Clock;
import io.opencensus.metrics.export.Metric;
import javax.annotation.Nullable;

interface Meter {
  /**
   * Provides a {@link io.opencensus.metrics.export.Metric} with one or more {@link
   * io.opencensus.metrics.export.TimeSeries}.
   *
   * @param clock the clock used to get the time.
   * @return a {@code Metric}.
   */
  @Nullable
  Metric getMetric(Clock clock);
}
