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

package io.opencensus.example.gameoflife;

import io.opencensus.tags.TagValue;
import java.util.logging.Logger;

/* Util methods. */
final class GolUtils {

  private static Logger logger = Logger.getLogger(GolUtils.class.getName());

  static int toInt(String s) {
    try {
      return Integer.valueOf(s);
    } catch (NumberFormatException exn) {
      return 0;
    }
  }

  static TagValue getTagValue(int dim, int gensPerGol, String suffix) {
    return TagValue.create(dim + "x" + dim + "-" + gensPerGol + "-" + suffix);
  }

  static String getRequest(String gen, int gensPerGol) {
    return "gol " + gensPerGol + " " + gen;
  }

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

  private GolUtils() {
  }
}
