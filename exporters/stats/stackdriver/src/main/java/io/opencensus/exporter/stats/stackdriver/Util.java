/*
 * Copyright 2019, OpenCensus Authors
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

package io.opencensus.exporter.stats.stackdriver;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;

final class Util {
  @VisibleForTesting static final String DEFAULT_DISPLAY_NAME_PREFIX = "OpenCensus/";
  @VisibleForTesting static final String CUSTOM_METRIC_DOMAIN = "custom.googleapis.com/";

  @VisibleForTesting
  static final String CUSTOM_OPENCENSUS_DOMAIN = CUSTOM_METRIC_DOMAIN + "opencensus/";

  static String exceptionMessage(Throwable e) {
    return e.getMessage() != null ? e.getMessage() : e.getClass().getName();
  }

  static String getDomain(@javax.annotation.Nullable String metricNamePrefix) {
    String domain;
    if (Strings.isNullOrEmpty(metricNamePrefix)) {
      domain = CUSTOM_OPENCENSUS_DOMAIN;
    } else {
      if (!metricNamePrefix.endsWith("/")) {
        domain = metricNamePrefix + '/';
      } else {
        domain = metricNamePrefix;
      }
    }
    return domain;
  }

  static String getDisplayNamePrefix(@javax.annotation.Nullable String metricNamePrefix) {
    if (metricNamePrefix == null) {
      return DEFAULT_DISPLAY_NAME_PREFIX;
    } else {
      if (!metricNamePrefix.endsWith("/") && !metricNamePrefix.isEmpty()) {
        metricNamePrefix += '/';
      }
      return metricNamePrefix;
    }
  }

  private Util() {}
}
