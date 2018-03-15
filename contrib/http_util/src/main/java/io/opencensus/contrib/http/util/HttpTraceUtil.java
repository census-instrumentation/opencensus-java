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

package io.opencensus.contrib.http.util;

import io.opencensus.trace.Status;
import javax.annotation.Nullable;

/**
 * A helper class to provide convenience methods for tracing.
 *
 * @since 0.13.0
 */
public final class HttpTraceUtil {

  private HttpTraceUtil() {}

  /**
   * Parse OpenCensus Status from HTTP response status code.
   *
   * <p>This method serves a default routine to map HTTP status code to Open Census Status. The
   * mapping is defined in <a
   * href="https://github.com/googleapis/googleapis/blob/master/google/rpc/code.proto">Google API
   * canonical error code</a>, and the behavior is defined in <a
   * href="https://github.com/census-instrumentation/opencensus-specs/blob/master/trace/HTTP.md">OpenCensus
   * Specs</a>.
   *
   * @param statusCode the HTTP response status code. {@code 0} means invalid response.
   * @param error the error occured during response transmission (optional).
   * @return the corresponding OpenCensus {@code Status}.
   * @since 0.13
   */
  public static final Status parseResponseStatus(int statusCode, @Nullable Throwable error) {
    String message = null;

    if (error != null) {
      message = error.getMessage();
      if (message == null) {
        message = error.getClass().getSimpleName();
      }
    }

    // set status according to response
    if (statusCode == 0) {
      return Status.UNKNOWN.withDescription(message);
    } else {
      if (statusCode >= 200 && statusCode < 400) {
        return Status.OK;
      } else {
        // error code, try parse it
        switch (statusCode) {
          case 499:
            return Status.CANCELLED.withDescription(message);
          case 500:
            return Status.INTERNAL.withDescription(message); // Can also be UNKNOWN, DATA_LOSS
          case 400:
            return Status.INVALID_ARGUMENT.withDescription(
                message); // Can also be FAILED_PRECONDITION, OUT_OF_RANGE
          case 504:
            return Status.DEADLINE_EXCEEDED.withDescription(message);
          case 404:
            return Status.NOT_FOUND.withDescription(message);
          case 409:
            return Status.ALREADY_EXISTS.withDescription(message); // Can also be ABORTED
          case 403:
            return Status.PERMISSION_DENIED.withDescription(message);
          case 401:
            return Status.UNAUTHENTICATED.withDescription(message);
          case 429:
            return Status.RESOURCE_EXHAUSTED.withDescription(message);
          case 501:
            return Status.UNIMPLEMENTED.withDescription(message);
          case 503:
            return Status.UNAVAILABLE.withDescription(message);
          default:
            return Status.UNKNOWN.withDescription(message);
        }
      }
    }
  }
}
