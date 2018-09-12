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

package io.opencensus.temporary.metrics;

import com.google.auto.value.AutoValue;
import io.opencensus.common.ExperimentalApi;
import io.opencensus.internal.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * {@link MetricDescriptor} defines a {@code Metric} type and its schema.
 *
 * @since 0.16
 */
@ExperimentalApi
@Immutable
@AutoValue
public abstract class MetricDescriptor {

  MetricDescriptor() {}

  /**
   * Creates a {@link MetricDescriptor}.
   *
   * @param name name of {@code MetricDescriptor}.
   * @param description description of {@code MetricDescriptor}.
   * @param unit the metric unit.
   * @param type type of {@code MetricDescriptor}.
   * @param labelKeys the label keys associated with the {@code MetricDescriptor}.
   * @return a {@code MetricDescriptor}.
   * @since 0.16
   */
  public static MetricDescriptor create(
      String name, String description, String unit, Type type, List<LabelKey> labelKeys) {
    Utils.checkNotNull(labelKeys, "labelKeys");
    Utils.checkListElementNotNull(labelKeys, "labelKey");
    return new AutoValue_MetricDescriptor(
        name,
        description,
        unit,
        type,
        Collections.unmodifiableList(new ArrayList<LabelKey>(labelKeys)));
  }

  /**
   * Returns the metric descriptor name.
   *
   * @return the metric descriptor name.
   * @since 0.16
   */
  public abstract String getName();

  /**
   * Returns the description of this metric descriptor.
   *
   * @return the description of this metric descriptor.
   * @since 0.16
   */
  public abstract String getDescription();

  /**
   * Returns the unit of this metric descriptor.
   *
   * @return the unit of this metric descriptor.
   * @since 0.16
   */
  public abstract String getUnit();

  /**
   * Returns the type of this metric descriptor.
   *
   * @return the type of this metric descriptor.
   * @since 0.16
   */
  public abstract Type getType();

  /**
   * Returns the label keys associated with this metric descriptor.
   *
   * @return the label keys associated with this metric descriptor.
   * @since 0.16
   */
  public abstract List<LabelKey> getLabelKeys();

  /**
   * The kind of metric. It describes how the data is reported.
   *
   * <p>A gauge is an instantaneous measurement of a value.
   *
   * <p>A cumulative measurement is a value accumulated over a time interval. In a time series,
   * cumulative measurements should have the same start time and increasing end times, until an
   * event resets the cumulative value to zero and sets a new start time for the following points.
   *
   * @since 0.16
   */
  public enum Type {

    /**
     * An instantaneous measurement of an int64 value.
     *
     * @since 0.16
     */
    GAUGE_INT64,

    /**
     * An instantaneous measurement of a double value.
     *
     * @since 0.16
     */
    GAUGE_DOUBLE,

    /**
     * An cumulative measurement of an int64 value.
     *
     * @since 0.16
     */
    CUMULATIVE_INT64,

    /**
     * An cumulative measurement of a double value.
     *
     * @since 0.16
     */
    CUMULATIVE_DOUBLE,

    /**
     * An cumulative measurement of a distribution value.
     *
     * @since 0.16
     */
    CUMULATIVE_DISTRIBUTION,
  }
}
