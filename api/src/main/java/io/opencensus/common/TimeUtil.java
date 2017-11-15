/*
 * Copyright 2017, OpenCensus Authors
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

package io.opencensus.common;

/** Util class for {@link Timestamp} and {@link Duration}. */
final class TimeUtil {
  static final long MAX_SECONDS = 315576000000L;
  static final int MAX_NANOS = 999999999;
  static final long MILLIS_PER_SECOND = 1000L;
  static final long NANOS_PER_MILLI = 1000 * 1000;
  static final long NANOS_PER_SECOND = NANOS_PER_MILLI * MILLIS_PER_SECOND;

  private TimeUtil() {}
}
