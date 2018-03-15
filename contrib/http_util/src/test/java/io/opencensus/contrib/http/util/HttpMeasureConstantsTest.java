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

import io.opencensus.tags.TagKey;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Test for {@link HttpMeasureConstants}. */
@RunWith(JUnit4.class)
public class HttpMeasureConstantsTest {

  @Test
  public void constants() {
    // Test Tags
    assertThat(HttpMeasureConstants.HTTP_STATUS_CODE).isEqualTo(TagKey.create("http.status"));
    assertThat(HttpMeasureConstants.HTTP_METHOD).isEqualTo(TagKey.create("http.method"));
    assertThat(HttpMeasureConstants.HTTP_PATH).isEqualTo(TagKey.create("http.path"));
    assertThat(HttpMeasureConstants.HTTP_HOST).isEqualTo(TagKey.create("http.host"));

    // Test measurement descriptors.
    assertThat(HttpMeasureConstants.HTTP_CLIENT_REQUEST_COUNT.getUnit()).isEqualTo("1");
    assertThat(HttpMeasureConstants.HTTP_CLIENT_REQUEST_BYTES.getUnit()).isEqualTo("By");
    assertThat(HttpMeasureConstants.HTTP_CLIENT_RESPONSE_BYTES.getUnit()).isEqualTo("By");
    assertThat(HttpMeasureConstants.HTTP_CLIENT_LATENCY.getUnit()).isEqualTo("ms");
    assertThat(HttpMeasureConstants.HTTP_SERVER_REQUEST_COUNT.getUnit()).isEqualTo("1");
    assertThat(HttpMeasureConstants.HTTP_SERVER_REQUEST_BYTES.getUnit()).isEqualTo("By");
    assertThat(HttpMeasureConstants.HTTP_SERVER_RESPONSE_BYTES.getUnit()).isEqualTo("By");
    assertThat(HttpMeasureConstants.HTTP_SERVER_LATENCY.getUnit()).isEqualTo("ms");

    assertThat(HttpMeasureConstants.HTTP_CLIENT_REQUEST_COUNT.getName())
        .contains("opencensus.io/http/client");
    assertThat(HttpMeasureConstants.HTTP_CLIENT_REQUEST_BYTES.getName())
        .contains("opencensus.io/http/client");
    assertThat(HttpMeasureConstants.HTTP_CLIENT_RESPONSE_BYTES.getName())
        .contains("opencensus.io/http/client");
    assertThat(HttpMeasureConstants.HTTP_CLIENT_LATENCY.getName())
        .contains("opencensus.io/http/client");
    assertThat(HttpMeasureConstants.HTTP_SERVER_REQUEST_COUNT.getName())
        .contains("opencensus.io/http/server");
    assertThat(HttpMeasureConstants.HTTP_SERVER_REQUEST_BYTES.getName())
        .contains("opencensus.io/http/server");
    assertThat(HttpMeasureConstants.HTTP_SERVER_RESPONSE_BYTES.getName())
        .contains("opencensus.io/http/server");
    assertThat(HttpMeasureConstants.HTTP_SERVER_LATENCY.getName())
        .contains("opencensus.io/http/server");
  }
}
