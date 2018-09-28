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

package io.opencensus.metrics;

import io.opencensus.common.ToDoubleFunction;
import io.opencensus.internal.Utils;
import java.util.List;

/** No-op implementations of DoubleGaugeMetric class. */
final class NoopDoubleGaugeMetric extends DoubleGaugeMetric {
  static NoopDoubleGaugeMetric createInstance(
      String name, String description, String unit, List<LabelKey> labelKeys) {
    return new NoopDoubleGaugeMetric(name, description, unit, labelKeys);
  }

  /** Creates a new {@code NoopDoubleGaugeMetric}. */
  NoopDoubleGaugeMetric(String name, String description, String unit, List<LabelKey> labelKeys) {
    Utils.checkNotNull(name, "name");
    Utils.checkNotNull(description, "description");
    Utils.checkNotNull(unit, "unit");
    Utils.checkNotNull(labelKeys, "labelKeys should not be null.");
    Utils.checkListElementNotNull(labelKeys, "labelKeys element should not be null.");
  }

  @Override
  public NoopPoint addPoint(List<LabelValue> labelValues) {
    Utils.checkNotNull(labelValues, "labelValues should not be null.");
    Utils.checkListElementNotNull(labelValues, "labelValues element should not be null.");
    return NoopPoint.getInstance();
  }

  @Override
  public <T> void addPoint(List<LabelValue> labelValues, T obj, ToDoubleFunction<T> function) {
    Utils.checkNotNull(labelValues, "labelValues should not be null.");
    Utils.checkListElementNotNull(labelValues, "labelValues element should not be null.");
    Utils.checkNotNull(function, "function");
  }

  @Override
  public NoopPoint getDefaultPoint() {
    return NoopPoint.getInstance();
  }

  /** No-op implementations of Point class. */
  private static final class NoopPoint extends Point {
    static final NoopPoint INSTANCE = new NoopPoint();

    @Override
    public void inc() {}

    @Override
    public void inc(double amt) {}

    @Override
    public void dec() {}

    @Override
    public void dec(double amt) {}

    @Override
    public void set(double val) {}

    /**
     * Returns a {@code NoopPoint}.
     *
     * @return a {@code NoopPoint}.
     */
    static NoopPoint getInstance() {
      return INSTANCE;
    }
  }
}
