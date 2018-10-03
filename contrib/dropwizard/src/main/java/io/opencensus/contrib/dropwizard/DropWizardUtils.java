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

package io.opencensus.contrib.dropwizard;

import com.codahale.metrics.Metric;

/** Util methods for generating the metric name(unique) and description. */
final class DropWizardUtils {

  /**
   * Returns the metric name.
   *
   * @param name the metric name
   * @param suffix the suffix, that we are interested in from current metric (eg. count, value,
   *     rate).
   * @return a string
   */
  static String generateFullMetricName(String name, String suffix) {
    return String.format("%s_%s", name, suffix);
  }

  /**
   * Returns the metric description.
   *
   * @param metricName the metric name
   * @param metric the dropwizard metric class.
   * @return a String
   */
  static String generateFullMetricDescription(String metricName, Metric metric) {
    return String.format(
        "Collected from Dropwizard (metric=%s, type=%s)", metricName, metric.getClass().getName());
  }

  private DropWizardUtils() {}
}
