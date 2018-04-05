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
    // Test TagKeys
    assertThat(HttpMeasureConstants.HTTP_CLIENT_STATUS)
        .isEqualTo(TagKey.create("http_client_status"));
    assertThat(HttpMeasureConstants.HTTP_CLIENT_METHOD)
        .isEqualTo(TagKey.create("http_client_method"));
    assertThat(HttpMeasureConstants.HTTP_CLIENT_PATH).isEqualTo(TagKey.create("http_client_path"));
    assertThat(HttpMeasureConstants.HTTP_CLIENT_HOST).isEqualTo(TagKey.create("http_client_host"));
    assertThat(HttpMeasureConstants.HTTP_SERVER_STATUS)
        .isEqualTo(TagKey.create("http_server_status"));
    assertThat(HttpMeasureConstants.HTTP_SERVER_METHOD)
        .isEqualTo(TagKey.create("http_server_method"));
    assertThat(HttpMeasureConstants.HTTP_SERVER_PATH).isEqualTo(TagKey.create("http_server_path"));
    assertThat(HttpMeasureConstants.HTTP_SERVER_HOST).isEqualTo(TagKey.create("http_server_host"));

    // Test measures
    assertThat(HttpMeasureConstants.HTTP_CLIENT_SENT_BYTES.getUnit()).isEqualTo("By");
    assertThat(HttpMeasureConstants.HTTP_CLIENT_RECEIVED_BYTES.getUnit()).isEqualTo("By");
    assertThat(HttpMeasureConstants.HTTP_CLIENT_ROUNDTRIP_LATENCY.getUnit()).isEqualTo("ms");
    assertThat(HttpMeasureConstants.HTTP_SERVER_RECEIVED_BYTES.getUnit()).isEqualTo("By");
    assertThat(HttpMeasureConstants.HTTP_SERVER_SENT_BYTES.getUnit()).isEqualTo("By");
    assertThat(HttpMeasureConstants.HTTP_SERVER_LATENCY.getUnit()).isEqualTo("ms");

    assertThat(HttpMeasureConstants.HTTP_CLIENT_SENT_BYTES.getName())
        .contains("opencensus.io/http/client");
    assertThat(HttpMeasureConstants.HTTP_CLIENT_RECEIVED_BYTES.getName())
        .contains("opencensus.io/http/client");
    assertThat(HttpMeasureConstants.HTTP_CLIENT_ROUNDTRIP_LATENCY.getName())
        .contains("opencensus.io/http/client");
    assertThat(HttpMeasureConstants.HTTP_SERVER_RECEIVED_BYTES.getName())
        .contains("opencensus.io/http/server");
    assertThat(HttpMeasureConstants.HTTP_SERVER_SENT_BYTES.getName())
        .contains("opencensus.io/http/server");
    assertThat(HttpMeasureConstants.HTTP_SERVER_LATENCY.getName())
        .contains("opencensus.io/http/server");
  }
}
