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

import com.google.auto.value.AutoValue;
import io.opencensus.internal.Utils;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.concurrent.Immutable;

/**
 * Options for every metric added to the {@link MetricRegistry}.
 *
 * @since 0.20
 */
@Immutable
@AutoValue
public abstract class MetricOptions {

  /**
   * Returns the description of the Metric.
   *
   * <p>Default value is {@code ""}.
   *
   * @return the description of the Metric.
   */
  public abstract String getDescription();

  /**
   * Returns the unit of the Metric.
   *
   * <p>Default value is {@code "1"}.
   *
   * @return the unit of the Metric.
   */
  public abstract String getUnit();

  /**
   * Returns the list of label keys for the Metric.
   *
   * <p>Default value is {@link Collections#emptyList()}.
   *
   * @return the list of label keys for the Metric.
   */
  public abstract List<LabelKey> getLabelKeys();

  /**
   * Returns the list of constant labels (they will be added to all the TimeSeries) for the Metric.
   *
   * <p>Default value is {@link Collections#emptyMap()}.
   *
   * @return the list of label keys for the Metric.
   */
  public abstract Map<LabelKey, LabelValue> getConstantLabels();

  /**
   * Returns a new {@link Builder} with default options.
   *
   * @return a new {@code Builder} with default options.
   * @since 0.5
   */
  public static Builder builder() {
    return new AutoValue_MetricOptions.Builder()
        .setDescription("")
        .setUnit("1")
        .setLabelKeys(Collections.<LabelKey>emptyList())
        .setConstantLabels(Collections.<LabelKey, LabelValue>emptyMap());
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setDescription(String labelKeys);

    public abstract Builder setUnit(String labelKeys);

    public abstract Builder setLabelKeys(List<LabelKey> labelKeys);

    public abstract Builder setConstantLabels(Map<LabelKey, LabelValue> constantLabels);

    abstract MetricOptions autoBuild();

    /**
     * Builds and returns a {@code MetricOptions} with the desired options.
     *
     * @return a {@code MetricOptions} with the desired options.
     * @since 0.20
     * @throws NullPointerException if {@code description}, OR {@code unit} is null, OR {@code
     *     labelKeys} is null OR any element of {@code labelKeys} is null, OR OR {@code
     *     constantLabels} is null OR any element of {@code constantLabels} is null.
     * @throws IllegalArgumentException if any {@code LabelKey} from the {@code labelKeys} is in the
     *     {@code constantLabels}.
     */
    public MetricOptions build() {
      MetricOptions options = autoBuild();
      Utils.checkNotNull(options.getDescription(), "description");
      Utils.checkNotNull(options.getUnit(), "unit");
      Utils.checkListElementNotNull(
          Utils.checkNotNull(options.getLabelKeys(), "labelKeys"), "labelKeys elements");
      Utils.checkMapElementNotNull(
          Utils.checkNotNull(options.getConstantLabels(), "constantLabels"),
          "constantLabels elements");
      Map<LabelKey, LabelValue> constantLabels = options.getConstantLabels();
      for (LabelKey labelKey : options.getLabelKeys()) {
        if (constantLabels.containsKey(labelKey)) {
          throw new IllegalArgumentException("LabelKey in both labelKeys and constantLabels.");
        }
      }
      return options;
    }

    Builder() {}
  }

  MetricOptions() {}
}
