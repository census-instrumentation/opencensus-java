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

package io.opencensus.tags;

import java.util.Iterator;

/**
 * Internal tagging utilities.
 *
 * @since 0.8
 */
@io.opencensus.common.Internal
public final class InternalUtils {
  private InternalUtils() {}

  /**
   * Internal tag accessor.
   *
   * @since 0.8
   */
  public static Iterator<Tag> getTags(TagContext tags) {
    return tags.getIterator();
  }
}
