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

package io.opencensus.implcore.internal;

import javax.annotation.Nullable;

/**
 * Utility methods for suppressing nullness warnings and working around Checker Framework issues.
 */
public final class CheckerFrameworkUtils {
  private CheckerFrameworkUtils() {}

  /** Suppresses warnings about a nullable value. */
  // TODO(sebright): Try to remove all uses of this method.
  @SuppressWarnings("nullness")
  public static <T> T castNonNull(@Nullable T arg) {
    return arg;
  }
}
