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

package io.opencensus.implcore.stats;

import io.opencensus.stats.Measure;
import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.stats.Measure.MeasureLong;
import io.opencensus.stats.Measurement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

// TODO(songya): consider combining MeasureMapImpl and this class.
/** A map from {@link Measure}'s to measured values. */
final class MeasureMapInternal {

  /** Returns a {@link Builder} for the {@link MeasureMapInternal} class. */
  static Builder builder() {
    return new Builder();
  }

  /**
   * Returns an {@link Iterator} over the measure/value mappings in this {@link MeasureMapInternal}.
   * The {@code Iterator} does not support {@link Iterator#remove()}.
   */
  Iterator<Measurement> iterator() {
    return new MeasureMapInternalIterator();
  }

  private final ArrayList<Measurement> measurements;

  private MeasureMapInternal(ArrayList<Measurement> measurements) {
    this.measurements = measurements;
  }

  /** Builder for the {@link MeasureMapInternal} class. */
  static class Builder {
    /**
     * Associates the {@link MeasureDouble} with the given value. Subsequent updates to the same
     * {@link MeasureDouble} will overwrite the previous value.
     *
     * @param measure the {@link MeasureDouble}
     * @param value the value to be associated with {@code measure}
     * @return this
     */
    Builder put(MeasureDouble measure, double value) {
      measurements.add(Measurement.MeasurementDouble.create(measure, value));
      return this;
    }

    /**
     * Associates the {@link MeasureLong} with the given value. Subsequent updates to the same
     * {@link MeasureLong} will overwrite the previous value.
     *
     * @param measure the {@link MeasureLong}
     * @param value the value to be associated with {@code measure}
     * @return this
     */
    Builder put(MeasureLong measure, long value) {
      measurements.add(Measurement.MeasurementLong.create(measure, value));
      return this;
    }

    /** Constructs a {@link MeasureMapInternal} from the current measurements. */
    MeasureMapInternal build() {
      // Note: this makes adding measurements quadratic but is fastest for the sizes of
      // MeasureMapInternals that we should see. We may want to go to a strategy of sort/eliminate
      // for larger MeasureMapInternals.
      for (int i = measurements.size() - 1; i >= 0; i--) {
        for (int j = i - 1; j >= 0; j--) {
          if (measurements.get(i).getMeasure() == measurements.get(j).getMeasure()) {
            measurements.remove(j);
            j--;
          }
        }
      }
      return new MeasureMapInternal(measurements);
    }

    private final ArrayList<Measurement> measurements = new ArrayList<Measurement>();

    private Builder() {}
  }

  // Provides an unmodifiable Iterator over this instance's measurements.
  private final class MeasureMapInternalIterator implements Iterator<Measurement> {
    @Override
    public boolean hasNext() {
      return position < length;
    }

    @Override
    public Measurement next() {
      if (position >= measurements.size()) {
        throw new NoSuchElementException();
      }
      return measurements.get(position++);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

    private final int length = measurements.size();
    private int position = 0;
  }
}
