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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A service class to encode/decode {@link ServerStats} as defined by the spec.
 *
 * <p>See <a
 * href="https://github.com/census-instrumentation/opencensus-specs/blob/master/encodings/CensusServerStatsEncoding.md">opencensus-server-stats-specs</a>
 * for encoding {@code ServerStats}
 *
 * <p>Use {@code ServerStatsEncoding.toBytes(ServerStats stats)} to encode.
 *
 * <p>Use {@code ServerStatsEncoding.parseBytes(byte[] serialized)} to decode.
 */
final class ServerStatsEncoding {

  private ServerStatsEncoding() {}

  /**
   * Encodes the {@link ServerStats} as per the Opencensus Summary Span specification.
   *
   * @param stats {@code ServerStats} to encode.
   * @return encoded byte array.
   */
  public static byte[] toBytes(ServerStats stats) {
    // Should this be optimized to not include invalid values?

    ByteBuffer bb = ByteBuffer.allocate(ServerStatsFieldEnums.getTotalSize());
    bb.order(ByteOrder.LITTLE_ENDIAN);

    bb.put((byte) ServerStatsFieldEnums.Id.SERVER_STATS_LB_LATENCY_ID.value());
    bb.putLong(stats.lbLatencyNs());

    bb.put((byte) ServerStatsFieldEnums.Id.SERVER_STATS_SERVICE_LATENCY_ID.value());
    bb.putLong(stats.serviceLatencyNs());

    bb.put((byte) ServerStatsFieldEnums.Id.SERVER_STATS_TRACE_OPTION_ID.value());
    bb.put(stats.traceOption());
    return bb.array();
  }

  /**
   * Decodes serialized byte array to create {@link ServerStats} as per Opencensus Summary Span
   * specification.
   *
   * @param serialized encoded {@code ServerStats} in byte array.
   * @return decoded {@code ServerStats}. null if decoding fails.
   */
  public static ServerStats parseBytes(byte[] serialized) {
    final ByteBuffer bb = ByteBuffer.wrap(serialized);
    bb.order(ByteOrder.LITTLE_ENDIAN);
    long serviceLatencyNs = 0L;
    long lbLatencyNs = 0L;
    byte traceOption = (byte) 0;
    while (bb.hasRemaining()) {
      ServerStatsFieldEnums.Id id = ServerStatsFieldEnums.Id.valueOf((int) bb.get() & 0xFF);
      if (id == null) {
        // Skip remaining;
        bb.position(bb.limit());
      } else {
        switch (id) {
          case SERVER_STATS_LB_LATENCY_ID:
            lbLatencyNs = bb.getLong();
            break;
          case SERVER_STATS_SERVICE_LATENCY_ID:
            serviceLatencyNs = bb.getLong();
            break;
          case SERVER_STATS_TRACE_OPTION_ID:
            traceOption = bb.get();
            break;
        }
      }
    }
    try {
      return ServerStats.create(lbLatencyNs, serviceLatencyNs, traceOption);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
