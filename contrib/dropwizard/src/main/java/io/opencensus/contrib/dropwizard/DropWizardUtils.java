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

import io.opencensus.internal.Utils;

/** Util methods for generating the metric name(unique) and description. */
final class DropWizardUtils {

  private static final String BLANK_SPACE = " ";
  private static final String SEPARATOR_CHAR = "_";

  /**
   * Generates and returns the unique metric name.
   *
   * @param name the name of metric - original metric name
   * @param data the data, that we are interested in from current metric (eg. count, value, rate).
   * @return String
   */
  static String generateFullMetricName(String name, String data) {
    Utils.checkNotNull(name, "name");

    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append(name);
    if (data != null && !data.isEmpty()) {
      stringBuilder.append(SEPARATOR_CHAR);
      stringBuilder.append(data);
    }
    return stringBuilder.toString();
  }

  /**
   * Generates and returns the metric description.
   *
   * @param name the metric (eg. Gauge, Counter) - original metric
   * @param data the data, that we are interested in from current metric (eg. count, value, rate).
   * @return String
   */
  static String generateFullMetricDescription(String name, String data) {
    Utils.checkNotNull(name, "name");

    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("DropWizard Metric=");
    stringBuilder.append(name);
    if (data != null && !data.isEmpty()) {
      stringBuilder.append(BLANK_SPACE);
      stringBuilder.append("Data=");
      stringBuilder.append(data);
    }
    return stringBuilder.toString();
  }

  private DropWizardUtils() {}
}
