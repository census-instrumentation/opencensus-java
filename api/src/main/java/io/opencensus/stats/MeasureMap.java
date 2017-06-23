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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A map from {@link Measure}'s to measured values.
 */
public final class MeasureMap implements Iterable<MeasureValue> {
  /**
   * Constructs a {@link MeasureMap} from the given {@link Measure}
   * and associated value.
   */
  public static MeasureMap of(Measure.DoubleMeasure measure, double value) {
    return builder().put(measure, value).build();
  }

  /**
   * Constructs a {@link MeasureMap} from the given {@link Measure}'s
   * and associated values.
   */
  public static MeasureMap of(Measure.DoubleMeasure measure1, double value1,
      Measure.DoubleMeasure measure2, double value2) {
    return builder().put(measure1, value1).put(measure2, value2).build();
  }

  /**
   * Constructs a {@link MeasureMap} from the given {@link Measure}'s
   * and associated values.
   */
  public static MeasureMap of(Measure.DoubleMeasure measure1, double value1,
      Measure.DoubleMeasure measure2, double value2,
      Measure.DoubleMeasure measure3, double value3) {
    return builder().put(measure1, value1).put(measure2, value2).put(measure3, value3)
        .build();
  }

  /**
   * Returns a {@link Builder} for the {@link MeasureMap} class.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns the number of measures in this {@link MeasureMap}.
   */
  public int size() {
    return measures.size();
  }

  /**
   * Returns an {@link Iterator} over the measure/value mappings in this {@link MeasureMap}.
   * The {@code Iterator} does not support {@link Iterator#remove()}.
   */
  @Override
  public Iterator<MeasureValue> iterator() {
    return new MeasureMapIterator();
  }

  private final ArrayList<MeasureValue> measures;

  private MeasureMap(ArrayList<MeasureValue> measures) {
    this.measures = measures;
  }

  /**
   * Builder for the {@link MeasureMap} class.
   */
  public static class Builder {
    /**
     * Associates the {@link Measure} with the given value. Subsequent updates to the
     * same {@link Measure} are ignored.
     *
     * @param measure the {@link Measure}
     * @param value the value to be associated with {@code measure}
     * @return this
     */
    public Builder put(Measure.DoubleMeasure measure, double value) {
      measures.add(MeasureValue.DoubleMeasureValue.create(measure, value));
      return this;
    }

    /**
     * Constructs a {@link MeasureMap} from the current measures.
     */
    public MeasureMap build() {
      // Note: this makes adding measures quadratic but is fastest for the sizes of
      // MeasureMaps that we should see. We may want to go to a strategy of sort/eliminate
      // for larger MeasureMaps.
      for (int i = 0; i < measures.size(); i++) {
        String current =
            measures.get(i).getMeasure().getName();
        for (int j = i + 1; j < measures.size(); j++) {
          if (current.equals(measures.get(j).getMeasure().getName())) {
            measures.remove(j);
            j--;
          }
        }
      }
      return new MeasureMap(measures);
    }

    private final ArrayList<MeasureValue> measures = new ArrayList<MeasureValue>();

    private Builder() {
    }
  }

  // Provides an unmodifiable Iterator over this instance's measures.
  private final class MeasureMapIterator implements Iterator<MeasureValue> {
    @Override
    public boolean hasNext() {
      return position < length;
    }

    @Override
    public MeasureValue next() {
      if (position >= measures.size()) {
        throw new NoSuchElementException();
      }
      return measures.get(position++);
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }

    private final int length = measures.size();
    private int position = 0;
  }
}
