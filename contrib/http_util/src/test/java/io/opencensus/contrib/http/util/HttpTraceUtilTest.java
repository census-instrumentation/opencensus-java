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

import com.google.common.collect.ImmutableSet;
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

  @Test
  public void parseResponseStatusError_499() {
    assertThat(HttpTraceUtil.parseResponseStatus(499, null).getCanonicalCode())
        .isEqualTo(CanonicalCode.CANCELLED);
  }

  @Test
  public void parseResponseStatusError_500() {
    assertThat(
            ImmutableSet.of(CanonicalCode.INTERNAL, CanonicalCode.UNKNOWN, CanonicalCode.DATA_LOSS))
        .contains(HttpTraceUtil.parseResponseStatus(500, null).getCanonicalCode());
  }

  @Test
  public void parseResponseStatusError_400() {
    assertThat(
            ImmutableSet.of(
                CanonicalCode.FAILED_PRECONDITION,
                CanonicalCode.OUT_OF_RANGE,
                CanonicalCode.INVALID_ARGUMENT))
        .contains(HttpTraceUtil.parseResponseStatus(400, null).getCanonicalCode());
  }

  @Test
  public void parseResponseStatusError_504() {
    assertThat(HttpTraceUtil.parseResponseStatus(504, null).getCanonicalCode())
        .isEqualTo(CanonicalCode.DEADLINE_EXCEEDED);
  }

  @Test
  public void parseResponseStatusError_404() {
    assertThat(HttpTraceUtil.parseResponseStatus(404, null).getCanonicalCode())
        .isEqualTo(CanonicalCode.NOT_FOUND);
  }

  @Test
  public void parseResponseStatusError_409() {
    assertThat(ImmutableSet.of(CanonicalCode.ALREADY_EXISTS, CanonicalCode.ABORTED))
        .contains(HttpTraceUtil.parseResponseStatus(409, null).getCanonicalCode());
  }

  @Test
  public void parseResponseStatusError_403() {
    assertThat(HttpTraceUtil.parseResponseStatus(403, null).getCanonicalCode())
        .isEqualTo(CanonicalCode.PERMISSION_DENIED);
  }

  @Test
  public void parseResponseStatusError_401() {
    assertThat(HttpTraceUtil.parseResponseStatus(401, null).getCanonicalCode())
        .isEqualTo(CanonicalCode.UNAUTHENTICATED);
  }

  @Test
  public void parseResponseStatusError_429() {
    assertThat(HttpTraceUtil.parseResponseStatus(429, null).getCanonicalCode())
        .isEqualTo(CanonicalCode.RESOURCE_EXHAUSTED);
  }

  @Test
  public void parseResponseStatusError_501() {
    assertThat(HttpTraceUtil.parseResponseStatus(501, null).getCanonicalCode())
        .isEqualTo(CanonicalCode.UNIMPLEMENTED);
  }

  @Test
  public void parseResponseStatusError_503() {
    assertThat(HttpTraceUtil.parseResponseStatus(503, null).getCanonicalCode())
        .isEqualTo(CanonicalCode.UNAVAILABLE);
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
