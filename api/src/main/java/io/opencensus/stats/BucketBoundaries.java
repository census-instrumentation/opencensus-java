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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.auto.value.AutoValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * The bucket boundaries for a histogram.
 *
 * @since 0.8
 */
@Immutable
@AutoValue
public abstract class BucketBoundaries {

  /**
   * @param bucketBoundaries the boundaries for the buckets in the underlying histogram.
   * @return a new {@code BucketBoundaries} with the specified boundaries.
   * @throws NullPointerException if {@code bucketBoundaries} is null.
   * @throws IllegalArgumentException if {@code bucketBoundaries} is not sorted.
   * @since 0.8
   */
  public static final BucketBoundaries create(List<Double> bucketBoundaries) {
    checkNotNull(bucketBoundaries, "bucketBoundaries list should not be null.");
    List<Double> bucketBoundariesCopy = new ArrayList<Double>(bucketBoundaries); // Deep copy.
    // Check if sorted.
    if (bucketBoundariesCopy.size() > 1) {
      double lower = bucketBoundariesCopy.get(0);
      for (int i = 1; i < bucketBoundariesCopy.size(); i++) {
        double next = bucketBoundariesCopy.get(i);
        checkArgument(lower < next, "Bucket boundaries not sorted.");
        lower = next;
      }
    }
    return new AutoValue_BucketBoundaries(Collections.unmodifiableList(bucketBoundariesCopy));
  }

  /**
   * Returns a list of histogram bucket boundaries.
   *
   * @return a list of histogram bucket boundaries.
   * @since 0.8
   */
  public abstract List<Double> getBoundaries();
}
