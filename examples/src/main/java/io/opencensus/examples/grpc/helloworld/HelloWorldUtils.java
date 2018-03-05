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

package io.opencensus.examples.grpc.helloworld;

import java.util.logging.Logger;
import javax.annotation.Nullable;

/** Util methods. */
final class HelloWorldUtils {

  private static Logger logger = Logger.getLogger(HelloWorldUtils.class.getName());

  static int getPortOrDefaultFromArgs(String[] args, int index, int defaultPort) {
    int portNumber = defaultPort;
    if (index < args.length) {
      try {
        portNumber = Integer.parseInt(args[index]);
      } catch (NumberFormatException e) {
        logger.warning(
            String.format("Port %s is invalid, use default port %d.", args[index], defaultPort));
      }
    }
    return portNumber;
  }

  static String getStringOrDefaultFromArgs(
      String[] args, int index, @Nullable String defaultString) {
    String s = defaultString;
    if (index < args.length) {
      s = args[index];
    }
    return s;
  }

  private HelloWorldUtils() {
  }
}
