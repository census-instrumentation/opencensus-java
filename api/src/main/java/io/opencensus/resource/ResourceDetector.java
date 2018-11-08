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

package io.opencensus.resource;

import io.opencensus.common.ExperimentalApi;
import java.util.List;

/*>>>
import org.checkerframework.checker.nullness.qual.Nullable;
*/

/** Interface to detect resource information. */
@ExperimentalApi
public interface ResourceDetector {

  /**
   * Returns a {@link Resource} that runs all input resources sequentially and merges their results.
   * In case a type of label key is already set, the first set value is takes precedence. If the
   * detector cannot find resource information, the returned resource is null.
   *
   * @param resources a list of resources.
   * @return a {@code Resource}.
   * @since 0.18
   */
  /*@Nullable*/
  Resource multiDetector(List</*@Nullable*/ Resource> resources);
}
