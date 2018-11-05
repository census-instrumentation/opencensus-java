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

import io.opencensus.common.ExperimentalApi;
import io.opencensus.trace.Status;
import javax.annotation.Nullable;

/**
 * A helper class to provide convenience methods for tracing.
 *
 * @since 0.18
 */
@ExperimentalApi
public final class HttpTraceUtil {
  private static final int STATUS_200 = 200;
  private static final int STATUS_100 = 100;
  private static final int STATUS_101 = 101;
  private static final int STATUS_400 = 400;
  private static final int STATUS_401 = 401;
  private static final int STATUS_402 = 402;
  private static final int STATUS_403 = 403;
  private static final int STATUS_404 = 404;
  private static final int STATUS_405 = 405;
  private static final int STATUS_406 = 406;
  private static final int STATUS_407 = 407;
  private static final int STATUS_408 = 408;
  private static final int STATUS_409 = 409;
  private static final int STATUS_410 = 410;
  private static final int STATUS_411 = 411;
  private static final int STATUS_412 = 412;
  private static final int STATUS_413 = 413;
  private static final int STATUS_414 = 414;
  private static final int STATUS_415 = 415;
  private static final int STATUS_416 = 416;
  private static final int STATUS_417 = 417;
  private static final int STATUS_429 = 429;
  private static final int STATUS_500 = 500;
  private static final int STATUS_501 = 501;
  private static final int STATUS_502 = 502;
  private static final int STATUS_503 = 503;
  private static final int STATUS_504 = 504;
  private static final int STATUS_505 = 505;

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
   * @since 0.18
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
      if (statusCode >= STATUS_200 && statusCode < STATUS_400) {
        return Status.OK;
      } else {
        // error code, try parse it
        switch (statusCode) {
          case STATUS_100:
            return Status.UNKNOWN.withDescription("Continue");
          case STATUS_101:
            return Status.UNKNOWN.withDescription("Switching Protocols");
          case STATUS_400:
            return Status.INVALID_ARGUMENT.withDescription(message);
          case STATUS_401:
            return Status.UNAUTHENTICATED.withDescription(message);
          case STATUS_402:
            return Status.UNKNOWN.withDescription("Payment Required");
          case STATUS_403:
            return Status.PERMISSION_DENIED.withDescription(message);
          case STATUS_404:
            return Status.NOT_FOUND.withDescription(message);
          case STATUS_405:
            return Status.UNKNOWN.withDescription("Method Not Allowed");
          case STATUS_406:
            return Status.UNKNOWN.withDescription("Not Acceptable");
          case STATUS_407:
            return Status.UNKNOWN.withDescription("Proxy Authentication Required");
          case STATUS_408:
            return Status.UNKNOWN.withDescription("Request Time-out");
          case STATUS_409:
            return Status.UNKNOWN.withDescription("Conflict");
          case STATUS_410:
            return Status.UNKNOWN.withDescription("Gone");
          case STATUS_411:
            return Status.UNKNOWN.withDescription("Length Required");
          case STATUS_412:
            return Status.UNKNOWN.withDescription("Precondition Failed");
          case STATUS_413:
            return Status.UNKNOWN.withDescription("Request Entity Too Large");
          case STATUS_414:
            return Status.UNKNOWN.withDescription("Request-URI Too Large");
          case STATUS_415:
            return Status.UNKNOWN.withDescription("Unsupported Media Type");
          case STATUS_416:
            return Status.UNKNOWN.withDescription("Requested range not satisfiable");
          case STATUS_417:
            return Status.UNKNOWN.withDescription("Expectation Failed");
          case STATUS_429:
            return Status.RESOURCE_EXHAUSTED.withDescription(message);
          case STATUS_500:
            return Status.UNKNOWN.withDescription("Internal Server Error");
          case STATUS_501:
            return Status.UNIMPLEMENTED.withDescription(message);
          case STATUS_502:
            return Status.UNKNOWN.withDescription("Bad Gateway");
          case STATUS_503:
            return Status.UNAVAILABLE.withDescription(message);
          case STATUS_504:
            return Status.DEADLINE_EXCEEDED.withDescription(message);
          case STATUS_505:
            return Status.UNKNOWN.withDescription("HTTP Version not supported");
          default:
            return Status.UNKNOWN.withDescription(message);
        }
      }
    }
  }
}
