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

package com.google.instrumentation.stats;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A map from {@link MeasurementDescriptor}'s to measured values.
 */
public final class MeasurementMap implements Iterable<MeasurementValue> {
  /**
   * Constructs a {@link MeasurementMap} from the given {@link MeasurementDescriptor}
   * and associated value.
   */
  public static MeasurementMap of(MeasurementDescriptor name, double value) {
    return builder().put(name, value).build();
  }

  /**
   * Constructs a {@link MeasurementMap} from the given {@link MeasurementDescriptor}'s
   * and associated values.
   */
  public static MeasurementMap of(MeasurementDescriptor name1, double value1,
      MeasurementDescriptor name2, double value2) {
    return builder().put(name1, value1).put(name2, value2).build();
  }

  /**
   * Constructs a {@link MeasurementMap} from the given {@link MeasurementDescriptor}'s
   * and associated values.
   */
  public static MeasurementMap of(MeasurementDescriptor name1, double value1,
      MeasurementDescriptor name2, double value2, MeasurementDescriptor name3, double value3) {
    return builder().put(name1, value1).put(name2, value2).put(name3, value3).build();
  }

  /**
   * Returns a {@link Builder} for the {@link MeasurementMap} class.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns the number of measurements in this {@link MeasurementMap}.
   */
  public int size() {
    return measurements.size();
  }

  /**
   * Returns an {@link Iterator} over the name/value mappings in this {@link MeasurementMap}. The
   * {@code Iterator} does not support {@link Iterator#remove()}.
   */
  @Override
  public Iterator<MeasurementValue> iterator() {
    return new MeasurementMapIterator();
  }

  private final ArrayList<MeasurementValue> measurements;

  private MeasurementMap(ArrayList<MeasurementValue> measurements) {
    this.measurements = measurements;
  }

  /**
   * Builder for the {@link MeasurementMap} class.
   */
  public static class Builder {
    /**
     * Associates the {@link MeasurementDescriptor} with the given value. Subsequent updates to the
     * same {@link MeasurementDescriptor} are ignored.
     *
     * @param name the {@link MeasurementDescriptor}
     * @param value the value to be associated with {@code name}
     * @return this
     */
    public Builder put(MeasurementDescriptor name, double value) {
      measurements.add(new MeasurementValue(name, value));
      return this;
    }

    /**
     * Constructs a {@link MeasurementMap} from the current measurements.
     */
    public MeasurementMap build() {
      // Note: this makes adding measurements quadratic but is fastest for the sizes of
      // MeasurementMaps that we should see. We may want to go to a strategy of sort/eliminate
      // for larger MeasurementMaps.
      for (int i = 0; i < measurements.size(); i++) {
        MeasurementDescriptor current = measurements.get(i).getName();
        for (int j = i + 1; j < measurements.size(); j++) {
          if (current.equals(measurements.get(j).getName())) {
            measurements.remove(j);
            j--;
          }
        }
      }
      return new MeasurementMap(measurements);
    }

    private final ArrayList<MeasurementValue> measurements = new ArrayList<MeasurementValue>();

    private Builder() {
    }
  }

  // Provides an unmodifiable Iterator over this instance's measurements.
  private final class MeasurementMapIterator implements Iterator<MeasurementValue> {
    @Override
    public boolean hasNext() {
      return position < length;
    }

    @Override
    public MeasurementValue next() {
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
