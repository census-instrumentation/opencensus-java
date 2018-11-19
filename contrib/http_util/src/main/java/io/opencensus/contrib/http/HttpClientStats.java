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

import static com.google.common.base.Preconditions.checkNotNull;
import static io.opencensus.contrib.http.util.HttpMeasureConstants.HTTP_CLIENT_METHOD;
import static io.opencensus.contrib.http.util.HttpMeasureConstants.HTTP_CLIENT_RECEIVED_BYTES;
import static io.opencensus.contrib.http.util.HttpMeasureConstants.HTTP_CLIENT_ROUNDTRIP_LATENCY;
import static io.opencensus.contrib.http.util.HttpMeasureConstants.HTTP_CLIENT_SENT_BYTES;
import static io.opencensus.contrib.http.util.HttpMeasureConstants.HTTP_CLIENT_STATUS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

import io.opencensus.common.ExperimentalApi;
import io.opencensus.contrib.http.util.HttpTraceUtil;
import io.opencensus.tags.TagContext;
import io.opencensus.tags.TagValue;
import javax.annotation.Nullable;

/*>>>
import org.checkerframework.checker.nullness.qual.NonNull;
*/

/**
 * This helper class provides routine methods to measure stats for HTTP clients.
 *
 * @since 0.18
 */
@ExperimentalApi
public class HttpClientStats<Q /*>>> extends @NonNull Object*/, P /*>>> extends @NonNull Object*/>
    extends AbstractHttpStats<Q, P> {

  public HttpClientStats(HttpExtractor<Q, P> extractor) {
    super(extractor);
  }

  @Override
  public void requestStart(HttpStatsCtx statsCtx, Q request) {
    recordStartTime(statsCtx, request);
  }

  @Override
  public void requestEnd(HttpStatsCtx statsCtx, Q request, P response, @Nullable Throwable error) {
    checkNotNull(statsCtx, "statsCtx");
    checkNotNull(request, "request");
    checkNotNull(response, "response");
    double requestLatency = NANOSECONDS.toMillis(System.nanoTime() - statsCtx.getStartTime());

    String methodStr = extractor.getMethod(request);
    TagContext startCtx =
        tagger
            .currentBuilder()
            .put(HTTP_CLIENT_METHOD, TagValue.create(methodStr == null ? "" : methodStr))
            .put(
                HTTP_CLIENT_STATUS,
                TagValue.create(
                    HttpTraceUtil.parseResponseStatus(extractor.getStatusCode(response), error)
                        .toString()))
            .build();

    statsRecorder
        .newMeasureMap()
        .put(HTTP_CLIENT_ROUNDTRIP_LATENCY, requestLatency)
        .put(HTTP_CLIENT_SENT_BYTES, statsCtx.getRequestMessageSize())
        .put(HTTP_CLIENT_RECEIVED_BYTES, statsCtx.getResponseMessageSize())
        .record(startCtx);
  }
}
