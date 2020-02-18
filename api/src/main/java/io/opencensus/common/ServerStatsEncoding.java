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
 *
 * @since 0.16
 */
public final class ServerStatsEncoding {

  private ServerStatsEncoding() {}

  /**
   * The current encoding version. The value is {@value #CURRENT_VERSION}
   *
   * @since 0.16
   */
  public static final byte CURRENT_VERSION = (byte) 0;

  /**
   * Encodes the {@link ServerStats} as per the Opencensus Summary Span specification.
   *
   * @param stats {@code ServerStats} to encode.
   * @return encoded byte array.
   * @since 0.16
   */
  public static byte[] toBytes(ServerStats stats) {
    // Should this be optimized to not include invalid values?

    ByteBuffer bb = ByteBuffer.allocate(ServerStatsFieldEnums.getTotalSize() + 1);
    bb.order(ByteOrder.LITTLE_ENDIAN);

    // put version
    bb.put(CURRENT_VERSION);

    bb.put((byte) ServerStatsFieldEnums.Id.SERVER_STATS_LB_LATENCY_ID.value());
    bb.putLong(stats.getLbLatencyNs());

    bb.put((byte) ServerStatsFieldEnums.Id.SERVER_STATS_SERVICE_LATENCY_ID.value());
    bb.putLong(stats.getServiceLatencyNs());

    bb.put((byte) ServerStatsFieldEnums.Id.SERVER_STATS_TRACE_OPTION_ID.value());
    bb.put(stats.getTraceOption());
    return bb.array();
  }

  /**
   * Decodes serialized byte array to create {@link ServerStats} as per Opencensus Summary Span
   * specification.
   *
   * @param serialized encoded {@code ServerStats} in byte array.
   * @return decoded {@code ServerStats}. null if decoding fails.
   * @since 0.16
   */
  public static ServerStats parseBytes(byte[] serialized)
      throws ServerStatsDeserializationException {
    final ByteBuffer bb = ByteBuffer.wrap(serialized);
    bb.order(ByteOrder.LITTLE_ENDIAN);
    long serviceLatencyNs = 0L;
    long lbLatencyNs = 0L;
    byte traceOption = (byte) 0;

    // Check the version first.
    if (!bb.hasRemaining()) {
      System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName() + 1);
      // throw exception if the buffer is empty
      throw new ServerStatsDeserializationException("Serialized ServerStats buffer is empty");
    }
    byte version = bb.get();

    // check if the version is invalid 
    if (version > CURRENT_VERSION || version < 0) {
      System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName() + 2);
      // throw exepction since the version is invalid
      throw new ServerStatsDeserializationException("Invalid ServerStats version: " + version);
    }

    // enter while-loop if the byte-buffer is != null
    while (bb.hasRemaining()) {
      System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName() + 3);
      ServerStatsFieldEnums.Id id = ServerStatsFieldEnums.Id.valueOf((int) bb.get() & 0xFF);
      
      // set the buffer position to the buffers limit if id is null
      if (id == null) {
        System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName() + 4);
        // Skip remaining;
        bb.position(bb.limit());
        
        // if id != null
      } else {
        System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName() + 5);
        
        // enter switch block
        switch (id) {

          // if id is of value SERVER_STATS_LB_LATENCY_ID
          case SERVER_STATS_LB_LATENCY_ID:
            System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName() + 6);
            lbLatencyNs = bb.getLong();

            // break from switch block
            break;

          // if id is of value SERVER_STATS_SERVICE_LATENCY_ID
          case SERVER_STATS_SERVICE_LATENCY_ID:
            System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName() + 7);
            serviceLatencyNs = bb.getLong();

            // break from switch block
            break;

          // if id is of value SERVER_STATS_TRACE_OPTION_ID,   
          case SERVER_STATS_TRACE_OPTION_ID:
            System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName() + 8);
            traceOption = bb.get();

            // break from switch block
            break;
        }
      }
    }

    //  try to return the values from lbLatencyNs, serviceLatencyNs, traceOption
    try {
      System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName() + 9);

      // successfully returns values
      return ServerStats.create(lbLatencyNs, serviceLatencyNs, traceOption);
    
    // catch exception if not possible to return values
    } catch (IllegalArgumentException e) {
      System.out.println(Thread.currentThread().getStackTrace()[1].getMethodName() + 10);
      
      // throw exception if serialized Service-stats contains invalid values
      throw new ServerStatsDeserializationException(
          "Serialized ServiceStats contains invalid values: " + e.getMessage());
    }
  }
}
