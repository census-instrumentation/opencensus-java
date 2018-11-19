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

import io.opencensus.common.ExperimentalApi;
import io.opencensus.contrib.http.util.HttpMeasureConstants;
import io.opencensus.stats.Stats;
import io.opencensus.stats.StatsRecorder;
import io.opencensus.tags.Tagger;
import io.opencensus.tags.Tags;
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
abstract class AbstractHttpStats<Q /*>>> extends @NonNull Object*/, P> {

  final StatsRecorder statsRecorder;
  final Tagger tagger;
  final HttpExtractor<Q, P> extractor;

  /**
   * Creates a {@link AbstractHttpStats} with given parameters.
   *
   * @param extractor the {@code HttpExtractor} used to extract information from the
   *     request/response.
   * @since 0.18
   */
  public AbstractHttpStats(HttpExtractor<Q, P> extractor) {
    checkNotNull(extractor, "extractor");
    this.extractor = extractor;
    this.statsRecorder = Stats.getStatsRecorder();
    this.tagger = Tags.getTagger();
  }

  void recordStartTime(HttpStatsCtx statsCtx, Q request) {
    checkNotNull(statsCtx, "statsCtx");
    checkNotNull(request, "request");
    statsCtx.recordStartTime();
  }

  /**
   * This method marks the beginning of a request.
   *
   * @param statsCtx {@link HttpStatsCtx} holds context associated with the http request.
   * @param request the request entity
   * @since 0.18
   */
  public abstract void requestStart(HttpStatsCtx statsCtx, Q request);

  /**
   * This method marks the end of a request and records stats.
   *
   * <p>Client Stats: {@link HttpMeasureConstants#HTTP_CLIENT_ROUNDTRIP_LATENCY} {@link
   * HttpMeasureConstants#HTTP_CLIENT_RECEIVED_BYTES} {@link
   * HttpMeasureConstants#HTTP_CLIENT_SENT_BYTES}
   *
   * <p>Server Stats: {@link HttpMeasureConstants#HTTP_SERVER_LATENCY} {@link
   * HttpMeasureConstants#HTTP_SERVER_RECEIVED_BYTES} {@link
   * HttpMeasureConstants#HTTP_SERVER_SENT_BYTES}
   *
   * @param statsCtx {@link HttpStatsCtx} holds context associated with the http request.
   * @param request the request entity
   * @param response the response entity
   * @param error the error occurs when processing the response.
   * @since 0.18
   */
  public abstract void requestEnd(
      HttpStatsCtx statsCtx, Q request, P response, @Nullable Throwable error);
}
