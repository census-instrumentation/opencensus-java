/*
 * Copyright 2020, OpenCensus Authors
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

package io.opencensus.contrib.observability.ready.util;

public class BasicSetup {

  /**
   * Enables OpenCensus metric and traces.
   *
   * <p>This will register all basic {@link io.opencensus.stats.View}s. When coupled
   * with an agent, it allows users to monitor client behavior.
   *
   * <p>Please note that in addition to calling this method, the application must:
   * <ul>
   *   <li>Include opencensus-contrib-observability-ready-util dependency on the classpath
   *   <li>Configure the OpenCensus agent
   * </ul>
   *
   * <p>Example usage for maven:
   * <pre>{@code
   *   <dependency>
   *     <groupId>io.opencensus</groupId>
   *     <artifactId>opencensus-contrib-observability-ready-util</artifactId>
   *     <version>${opencensus.version}</version>
   *   </dependency>
   * </pre>
   *
   * Java:
   * <pre>{@code
   *   BasicSetup.enableOpenCensusStats();
   * }</pre>
   */
  public static void enableOpenCensusStats() {
    // RpcViews.registerAllGrpcBasicViews();
  }

}