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

package io.opencensus.trace.samplers;

import io.opencensus.internal.Utils;
import io.opencensus.trace.TraceId;

final class SamplingProbabilityUtils {
  private SamplingProbabilityUtils() {}

  static boolean shouldSampleTrace(double probability, TraceId traceId) {
    long idUpperBound = computeTraceIdUpperBound(probability);
    return shouldSampleTrace(idUpperBound, traceId);
  }

  static boolean shouldSampleTrace(long idUpperBound, TraceId traceId) {
    return Math.abs(traceId.getLowerLong()) < idUpperBound;
  }

  static long computeTraceIdUpperBound(double probability) {
    Utils.checkArgument(
        probability >= 0.0 && probability <= 1.0, "probability must be in range [0.0, 1.0]");
    long idUpperBound;
    // Special case the limits, to avoid any possible issues with lack of precision across
    // double/long boundaries. For probability == 0.0, we use Long.MIN_VALUE as this guarantees
    // that we will never sample a trace, even in the case where the id == Long.MIN_VALUE, since
    // Math.Abs(Long.MIN_VALUE) == Long.MIN_VALUE.
    if (probability == 0.0) {
      idUpperBound = Long.MIN_VALUE;
    } else if (probability == 1.0) {
      idUpperBound = Long.MAX_VALUE;
    } else {
      idUpperBound = (long) (probability * Long.MAX_VALUE);
    }
    return idUpperBound;
  }
}
