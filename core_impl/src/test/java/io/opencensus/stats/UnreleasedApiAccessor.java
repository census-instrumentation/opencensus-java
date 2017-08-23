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

import io.opencensus.stats.Aggregation.Mean;
import io.opencensus.stats.Aggregation.Range;
import io.opencensus.stats.Aggregation.StdDev;

/**
 * Class for accessing methods in the {@code io.opencensus.stats} package that haven't been made
 * public yet.
 */
public final class UnreleasedApiAccessor {
  private UnreleasedApiAccessor() {}

  public static Range createRange() {
    return Range.create();
  }

  public static Mean createMean() {
    return Mean.create();
  }

  public static StdDev createStdDev() {
    return StdDev.create();
  }
}