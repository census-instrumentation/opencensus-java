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

package com.google.census;

import com.google.common.collect.UnmodifiableIterator;

import java.util.ArrayList;

/**
 * A map from Census metric names to metric values.
 */
public class MetricMap implements Iterable<Metric> {
  /**
   * Constructs a {@link MetricMap} from the given metrics.
   */
  public static MetricMap of(MetricName name, double value) {
    return builder().put(name, value).build();
  }

  /**
   * Constructs a {@link MetricMap} from the given metrics.
   */
  public static MetricMap of(MetricName name1, double value1,
      MetricName name2, double value2) {
    return builder().put(name1, value1).put(name2, value2).build();
  }

  /**
   * Constructs a {@link MetricMap} from the given metrics.
   */
  public static MetricMap of(MetricName name1, double value1,
      MetricName name2, double value2, MetricName name3, double value3) {
    return builder().put(name1, value1).put(name2, value2).put(name3, value3).build();
  }

  /**
   * Returns a {@link Builder} for the {@link MetricMap} class.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns the number of metrics in this {@link MetricsMap}.
   */
  public int size() {
    return metrics.size();
  }

  /**
   * Returns an {@link UnmodifiableIterator} over the name/value mappings in this {@link MetricMap}.
   */
  @Override
  public UnmodifiableIterator<Metric> iterator() {
    return new Iterator();
  }

  private final ArrayList<Metric> metrics;

  private MetricMap(ArrayList<Metric> metrics) {
    this.metrics = metrics;
  }

  /**
   * Builder for the {@link MetricMap} class.
   */
  public static class Builder {
    /**
     * Associates the {@link MetricName} with the given value. Subsequent updates to the same
     * {@link MetricName} are ignored.
     *
     * @param name the {@link MetricName}
     * @param value the value to be associated with {@code name}
     * @return this
     */
    public Builder put(MetricName name, double value) {
      metrics.add(new Metric(name, value));
      return this;
    }

    /**
     * Constructs a {@link MetricMap} from the current metrics.
     */
    public MetricMap build() {
      // Note: this makes adding metrics quadratic but is fastest for the sizes of MetricMaps that
      // we should see. We may want to go to a strategy of sort/eliminate for larger MetricMaps.
      for (int i = 0; i < metrics.size(); i++) {
        MetricName current = metrics.get(i).getName();
        for (int j = i + 1; j < metrics.size(); j++) {
          if (current.equals(metrics.get(j).getName())) {
            metrics.remove(j);
            j--;
          }
        }
      }
      return new MetricMap(metrics);
    }

    private ArrayList<Metric> metrics = new ArrayList<>();

    private Builder() {
    }
  }

  // Provides an UnmodifiableIterator over this instance's metrics.
  private class Iterator extends UnmodifiableIterator<Metric> {
    @Override public boolean hasNext() {
      return position < length;
    }

    @Override public Metric next() {
      return metrics.get(position++);
    }

    private final int length = metrics.size();
    private int position = 0;
  }
}
