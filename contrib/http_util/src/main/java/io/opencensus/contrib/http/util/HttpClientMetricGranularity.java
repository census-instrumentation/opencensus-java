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

package io.opencensus.contrib.http.util;

/** Enum class that determines the granularity at which opencensus http metrics are emitted. */
public enum HttpClientMetricGranularity {
  // Method granularity records all http metrics tagged with URI method
  // in all requests sent from client.
  METHOD,
  // HOST granularity records all http metrics tagged with URI method and
  // host name in all request sent from client.
  HOST
}
