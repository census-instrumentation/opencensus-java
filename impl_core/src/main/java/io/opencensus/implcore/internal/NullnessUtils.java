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

import io.opencensus.common.Function;

/*>>>
import org.checkerframework.checker.nullness.qual.KeyForBottom;
*/

/** Utility methods for working around Checker Framework issues. */
public final class NullnessUtils {
  private NullnessUtils() {}

  /**
   * Work around https://github.com/typetools/checker-framework/issues/1712 by removing {@code ?
   * super} from a {@code Function}'s argument type.
   */
  // TODO(sebright): Remove this method once the issue is fixed.
  public static <A, B> Function<A, B> removeSuperFromFunctionParameterType(
      Function<? super /*@KeyForBottom*/ A, B> function) {
    @SuppressWarnings("unchecked")
    Function<A, B> castFunction = (Function<A, B>) function;
    return castFunction;
  }
}
