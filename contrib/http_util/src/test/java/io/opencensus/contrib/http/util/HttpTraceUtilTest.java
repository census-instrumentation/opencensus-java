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

import static com.google.common.truth.Truth.assertThat;

import io.opencensus.trace.Status;
import io.opencensus.trace.Status.CanonicalCode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link HttpTraceUtilTest}. */
@RunWith(JUnit4.class)
public class HttpTraceUtilTest {

  @Test
  public void parseResponseStatusSucceed() {
    assertThat(HttpTraceUtil.parseResponseStatus(201, null)).isEqualTo(Status.OK);
  }

  @Test
  public void parseResponseStatusNoResponse() {
    assertThat(HttpTraceUtil.parseResponseStatus(0, null).getDescription()).isEqualTo(null);
    assertThat(HttpTraceUtil.parseResponseStatus(0, null).getCanonicalCode())
        .isEqualTo(CanonicalCode.UNKNOWN);
  }

  @Test
  public void parseResponseStatusErrorWithMessage() {
    Throwable error = new Exception("testError");
    assertThat(HttpTraceUtil.parseResponseStatus(0, error).getDescription()).isEqualTo("testError");
  }

  @Test
  public void parseResponseStatusErrorWithoutMessage() {
    Throwable error = new NullPointerException();
    assertThat(HttpTraceUtil.parseResponseStatus(0, error).getDescription())
        .isEqualTo("NullPointerException");
  }

  private static void parseResponseStatus(
      int code, CanonicalCode expectedCanonicalCode, String expectedDesc) {
    Status status = HttpTraceUtil.parseResponseStatus(code, null);
    assertThat(status.getCanonicalCode()).isEqualTo(expectedCanonicalCode);
    assertThat(status.getDescription()).isEqualTo(expectedDesc);
  }

  @Test
  public void parseResponseStatusCode_100() {
    parseResponseStatus(100, CanonicalCode.UNKNOWN, "Continue");
  }

  @Test
  public void parseResponseStatusCode_101() {
    parseResponseStatus(101, CanonicalCode.UNKNOWN, "Switching Protocols");
  }

  @Test
  public void parseResponseStatusError_400() {
    parseResponseStatus(400, CanonicalCode.INVALID_ARGUMENT, null);
  }

  @Test
  public void parseResponseStatusError_401() {
    parseResponseStatus(401, CanonicalCode.UNAUTHENTICATED, null);
  }

  @Test
  public void parseResponseStatusError_402() {
    parseResponseStatus(402, CanonicalCode.UNKNOWN, "Payment Required");
  }

  @Test
  public void parseResponseStatusError_403() {
    parseResponseStatus(403, CanonicalCode.PERMISSION_DENIED, null);
  }

  @Test
  public void parseResponseStatusError_404() {
    parseResponseStatus(404, CanonicalCode.NOT_FOUND, null);
  }

  @Test
  public void parseResponseStatusError_405() {
    parseResponseStatus(405, CanonicalCode.UNKNOWN, "Method Not Allowed");
  }

  @Test
  public void parseResponseStatusError_406() {
    parseResponseStatus(406, CanonicalCode.UNKNOWN, "Not Acceptable");
  }

  @Test
  public void parseResponseStatusError_407() {
    parseResponseStatus(407, CanonicalCode.UNKNOWN, "Proxy Authentication Required");
  }

  @Test
  public void parseResponseStatusError_408() {
    parseResponseStatus(408, CanonicalCode.UNKNOWN, "Request Time-out");
  }

  @Test
  public void parseResponseStatusError_409() {
    parseResponseStatus(409, CanonicalCode.UNKNOWN, "Conflict");
  }

  @Test
  public void parseResponseStatusError_410() {
    parseResponseStatus(410, CanonicalCode.UNKNOWN, "Gone");
  }

  @Test
  public void parseResponseStatusError_411() {
    parseResponseStatus(411, CanonicalCode.UNKNOWN, "Length Required");
  }

  @Test
  public void parseResponseStatusError_412() {
    parseResponseStatus(412, CanonicalCode.UNKNOWN, "Precondition Failed");
  }

  @Test
  public void parseResponseStatusError_413() {
    parseResponseStatus(413, CanonicalCode.UNKNOWN, "Request Entity Too Large");
  }

  @Test
  public void parseResponseStatusError_414() {
    parseResponseStatus(414, CanonicalCode.UNKNOWN, "Request-URI Too Large");
  }

  @Test
  public void parseResponseStatusError_415() {
    parseResponseStatus(415, CanonicalCode.UNKNOWN, "Unsupported Media Type");
  }

  @Test
  public void parseResponseStatusError_416() {
    parseResponseStatus(416, CanonicalCode.UNKNOWN, "Requested range not satisfiable");
  }

  @Test
  public void parseResponseStatusError_417() {
    parseResponseStatus(417, CanonicalCode.UNKNOWN, "Expectation Failed");
  }

  @Test
  public void parseResponseStatusError_429() {
    parseResponseStatus(429, CanonicalCode.RESOURCE_EXHAUSTED, null);
  }

  @Test
  public void parseResponseStatusError_500() {
    parseResponseStatus(500, CanonicalCode.UNKNOWN, "Internal Server Error");
  }

  @Test
  public void parseResponseStatusError_501() {
    parseResponseStatus(501, CanonicalCode.UNIMPLEMENTED, null);
  }

  @Test
  public void parseResponseStatusError_502() {
    parseResponseStatus(502, CanonicalCode.UNKNOWN, "Bad Gateway");
  }

  @Test
  public void parseResponseStatusError_503() {
    parseResponseStatus(503, CanonicalCode.UNAVAILABLE, null);
  }

  @Test
  public void parseResponseStatusError_504() {
    parseResponseStatus(504, CanonicalCode.DEADLINE_EXCEEDED, null);
  }

  @Test
  public void parseResponseStatusError_505() {
    parseResponseStatus(505, CanonicalCode.UNKNOWN, "HTTP Version not supported");
  }

  @Test
  public void parseResponseStatusError_Others() {
    // some random status code
    assertThat(HttpTraceUtil.parseResponseStatus(434, null).getCanonicalCode())
        .isEqualTo(CanonicalCode.UNKNOWN);
    assertThat(HttpTraceUtil.parseResponseStatus(517, null).getCanonicalCode())
        .isEqualTo(CanonicalCode.UNKNOWN);
  }
}
