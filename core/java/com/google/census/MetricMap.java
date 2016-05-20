/*
 * Copyright 2016, Google Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *    * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *
 *    * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
