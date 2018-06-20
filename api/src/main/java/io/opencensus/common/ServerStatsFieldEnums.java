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

package io.opencensus.common;

import java.util.TreeMap;

/**
 * A Enum representation for Ids and Size for attributes of {@code ServerStats}.
 *
 * <p>See <a
 * href="https://github.com/census-instrumentation/opencensus-specs/blob/master/encodings/CensusServerStatsEncoding.md">opencensus-server-stats-specs</a>
 * for the field ids and their length defined for Server Stats
 *
 * @since 0.15
 */
public final class ServerStatsFieldEnums {

  public enum Id {
    SERVER_STATS_LB_LATENCY_ID(0),
    SERVER_STATS_SERVICE_LATENCY_ID(1),
    SERVER_STATS_TRACE_OPTION_ID(2);

    private final int value;

    Id(int value) {
      this.value = value;
    }

    /**
     * Returns the numerical value of the id.
     *
     * @return the numerical value of the id.
     * @since 0.15
     */
    public int value() {
      return value;
    }

    private static final TreeMap<Integer, Id> map = new TreeMap<Integer, Id>();

    static {
      for (Id id : Id.values()) {
        map.put(id.value, id);
      }
    }

    /**
     * Returns the {@code Id} representing the value value of the id.
     *
     * @param value integer value for which {@code Id} is being requested.
     * @return the numerical value of the id. null if the id is not valid
     * @since 0.15
     */
    public static Id valueOf(int value) {
      return (Id) map.get(value);
    }
  }

  public enum Size {
    SERVER_STATS_LB_LATENCY_SIZE(8),
    SERVER_STATS_SERVICE_LATENCY_SIZE(8),
    SERVER_STATS_TRACE_OPTION_SIZE(1);

    private final int value;

    Size(int value) {
      this.value = value;
    }

    /**
     * Returns the numerical value of the id.
     *
     * @return the numerical value of the id.
     * @since 0.15
     */
    public int value() {
      return value;
    }
  }

  private static final int TOTALSIZE = computeTotalSize();

  private ServerStatsFieldEnums() {}

  /**
   * Computes total size required to encode all fields in {@code ServerStats}
   *
   * @return total size required to encode all fields in {@code ServerStats}.
   * @since 0.15
   */
  private static int computeTotalSize() {
    int sum = 0;
    for (Size sizeValue : Size.values()) {
      sum += sizeValue.value();
      sum += 1; // For Id
    }
    return sum;
  }

  /**
   * Returns the total size required to encode the {@code ServerStats}
   *
   * @return the total size required to encode all fields in {@code ServerStats}.
   */
  public static int getTotalSize() {
    return TOTALSIZE;
  }
}
