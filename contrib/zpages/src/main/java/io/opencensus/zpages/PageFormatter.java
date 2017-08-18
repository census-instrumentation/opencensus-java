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

package io.opencensus.zpages;

import java.io.OutputStream;
import java.util.Map;

/**
 * Main interface for all the HTML pages. This allows implementations of different HTTP Servers to
 * use the same logic for all Z-Pages. See {@code ZPageHttpHandler} for an example of using this
 * interface.
 */
abstract class PageFormatter {

  /**
   * Emits the HTML generated page to the {@code outputStream}.
   *
   * @param queryMap the query components map.
   * @param outputStream the output {@code OutputStream}.
   */
  public abstract void emitHtml(Map<String, String> queryMap, OutputStream outputStream);
}
