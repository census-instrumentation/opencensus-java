/*
 * Copyright 2016, Google Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *    * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *
 *    * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.google.census;

import com.google.common.annotations.VisibleForTesting;

/**
 * Census constants for collecting rpc stats.
 */
public final class RpcConstants {
  /**
   * Census defined tag keys.
   */
  public static final TagKey RPC_STATUS = new TagKey("/rpc/status");
  public static final TagKey RPC_CLIENT_METHOD = new TagKey("/rpc/client_method");
  public static final TagKey RPC_SERVER_METHOD = new TagKey("/rpc/server_method");

  /**
   * Census defined metric names.
   */
  public static final MetricName RPC_CLIENT_BYTES_RECEIVED =
      new MetricName("/rpc/client/bytes_received");
  public static final MetricName RPC_CLIENT_BYTES_SENT = new MetricName("/rpc/client/bytes_sent");
  public static final MetricName RPC_CLIENT_LATENCY = new MetricName("/rpc/client/latency");
  public static final MetricName RPC_SERVER_BYTES_RECEIVED =
      new MetricName("/rpc/server/bytes_received");
  public static final MetricName RPC_SERVER_BYTES_SENT = new MetricName("/rpc/server/bytes_sent");
  public static final MetricName RPC_SERVER_LATENCY = new MetricName("/rpc/server/latency");

  @VisibleForTesting RpcConstants() {
    throw new AssertionError();
  }
}
