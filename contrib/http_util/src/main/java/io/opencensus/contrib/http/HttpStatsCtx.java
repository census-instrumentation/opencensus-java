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

package io.opencensus.contrib.http;

import com.google.common.annotations.VisibleForTesting;
import io.opencensus.common.ExperimentalApi;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class provides storage for http client and server stats per request.
 *
 * @since 0.18
 */
@ExperimentalApi
public class HttpStatsCtx {
  @VisibleForTesting static final long INVALID_STARTTIME = -1;

  @VisibleForTesting long requestStartTime = INVALID_STARTTIME;
  @VisibleForTesting AtomicLong requestMessageSize = new AtomicLong();
  @VisibleForTesting AtomicLong responseMessageSize = new AtomicLong();

  /**
   * Increment the request content size by the number of bytes in the parameter.
   *
   * @param bytes bytes to add to current size of the request content.
   * @return value after addition.
   * @since 0.18
   */
  public long addAndGetRequestMessageSize(long bytes) {
    return requestMessageSize.addAndGet(bytes);
  }

  /**
   * Increment the response content size by the number of bytes in the parameter.
   *
   * @param bytes bytes to add to current size of the response content.
   * @return value after addition.
   * @since 0.18
   */
  public long addAndGetResponseMessageSize(long bytes) {
    return responseMessageSize.addAndGet(bytes);
  }

  long getRequestMessageSize() {
    return requestMessageSize.get();
  }

  long getResponseMessageSize() {
    return responseMessageSize.get();
  }

  void recordStartTime() {
    requestStartTime = System.nanoTime();
  }

  long getStartTime() {
    return requestStartTime;
  }
}
