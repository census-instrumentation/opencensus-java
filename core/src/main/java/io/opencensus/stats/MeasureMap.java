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

import io.opencensus.stats.Measure.MeasureDouble;
import io.opencensus.stats.Measure.MeasureLong;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A map from {@link Measure}'s to measured values.
 */
public final class MeasureMap {

  /**
   * Returns a {@link Builder} for the {@link MeasureMap} class.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns an {@link Iterator} over the measure/value mappings in this {@link MeasureMap}.
   * The {@code Iterator} does not support {@link Iterator#remove()}.
   */
  public Iterator<Measurement> iterator() {
    return new MeasureMapIterator();
  }

  private final ArrayList<Measurement> measurements;

  private MeasureMap(ArrayList<Measurement> measurements) {
    this.measurements = measurements;
  }

  /**
   * Builder for the {@link MeasureMap} class.
   */
  public static class Builder {
    /**
     * Associates the {@link MeasureDouble} with the given value. Subsequent updates to the
     * same {@link MeasureDouble} are ignored.
     *
     * @param measure the {@link MeasureDouble}
     * @param value the value to be associated with {@code measure}
     * @return this
     */
    public Builder put(MeasureDouble measure, double value) {
      measurements.add(Measurement.MeasurementDouble.create(measure, value));
      return this;
    }

    /**
     * Associates the {@link MeasureLong} with the given value. Subsequent updates to the
     * same {@link MeasureLong} are ignored.
     *
     * @param measure the {@link MeasureLong}
     * @param value the value to be associated with {@code measure}
     * @return this
     */
    public Builder put(MeasureLong measure, long value) {
      measurements.add(Measurement.MeasurementLong.create(measure, value));
      return this;
    }

    /**
     * Constructs a {@link MeasureMap} from the current measurements.
     */
    public MeasureMap build() {
      // Note: this makes adding measurements quadratic but is fastest for the sizes of
      // MeasureMaps that we should see. We may want to go to a strategy of sort/eliminate
      // for larger MeasureMaps.
      for (int i = 0; i < measurements.size(); i++) {
        for (int j = i + 1; j < measurements.size(); j++) {
          if (measurements.get(i).getMeasure() == measurements.get(j).getMeasure()) {
            measurements.remove(j);
            j--;
          }
        }
      }
      return new MeasureMap(measurements);
    }

    private final ArrayList<Measurement> measurements = new ArrayList<Measurement>();

    private Builder() {
    }
  }

  // Provides an unmodifiable Iterator over this instance's measurements.
  private final class MeasureMapIterator implements Iterator<Measurement> {
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
