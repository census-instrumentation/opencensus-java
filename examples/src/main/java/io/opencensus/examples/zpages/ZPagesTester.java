/*
 * Copyright 2017, OpenCensus Authors
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

package io.opencensus.examples.zpages;

import io.opencensus.common.Scope;
import io.opencensus.contrib.grpc.metrics.RpcMeasureConstants;
import io.opencensus.contrib.grpc.metrics.RpcViews;
import io.opencensus.contrib.zpages.ZPageHandlers;
import io.opencensus.stats.MeasureMap;
import io.opencensus.stats.Stats;
import io.opencensus.stats.StatsRecorder;
import io.opencensus.tags.TagValue;
import io.opencensus.tags.Tagger;
import io.opencensus.tags.Tags;
import io.opencensus.trace.SpanBuilder;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.samplers.Samplers;
import java.util.Collections;

/** Testing only class for the UI. */
public class ZPagesTester {

  private ZPagesTester() {}

  private static final Tagger tagger = Tags.getTagger();
  private static final Tracer tracer = Tracing.getTracer();
  private static final StatsRecorder statsRecorder = Stats.getStatsRecorder();

  private static final String SPAN_NAME = "SampleSpan";

  /** Main method. */
  public static void main(String[] args) throws Exception {
    Tracing.getExportComponent()
        .getSampledSpanStore()
        .registerSpanNamesForCollection(Collections.singletonList(SPAN_NAME));
    RpcViews.registerAllViews(); // Use old RPC constants to get interval stats.
    SpanBuilder spanBuilder =
        tracer.spanBuilder(SPAN_NAME).setRecordEvents(true).setSampler(Samplers.alwaysSample());

    try (Scope scope = spanBuilder.startScopedSpan()) {
      tracer.getCurrentSpan().addAnnotation("Starts recording.");
      MeasureMap measureMap =
          statsRecorder
              .newMeasureMap()
              // Client measurements.
              .put(RpcMeasureConstants.RPC_CLIENT_STARTED_COUNT, 1)
              .put(RpcMeasureConstants.RPC_CLIENT_FINISHED_COUNT, 1)
              .put(RpcMeasureConstants.RPC_CLIENT_ROUNDTRIP_LATENCY, 1.0)
              .put(RpcMeasureConstants.RPC_CLIENT_REQUEST_COUNT, 1)
              .put(RpcMeasureConstants.RPC_CLIENT_RESPONSE_COUNT, 1)
              .put(RpcMeasureConstants.RPC_CLIENT_REQUEST_BYTES, 1e5)
              .put(RpcMeasureConstants.RPC_CLIENT_RESPONSE_BYTES, 1e5)
              .put(RpcMeasureConstants.RPC_CLIENT_UNCOMPRESSED_REQUEST_BYTES, 1e5)
              .put(RpcMeasureConstants.RPC_CLIENT_UNCOMPRESSED_RESPONSE_BYTES, 1e5)
              // Server measurements.
              .put(RpcMeasureConstants.RPC_SERVER_STARTED_COUNT, 1)
              .put(RpcMeasureConstants.RPC_SERVER_FINISHED_COUNT, 1)
              .put(RpcMeasureConstants.RPC_SERVER_SERVER_LATENCY, 1.0)
              .put(RpcMeasureConstants.RPC_SERVER_REQUEST_COUNT, 1)
              .put(RpcMeasureConstants.RPC_SERVER_RESPONSE_COUNT, 1)
              .put(RpcMeasureConstants.RPC_SERVER_REQUEST_BYTES, 1e5)
              .put(RpcMeasureConstants.RPC_SERVER_RESPONSE_BYTES, 1e5)
              .put(RpcMeasureConstants.RPC_SERVER_UNCOMPRESSED_REQUEST_BYTES, 1e5)
              .put(RpcMeasureConstants.RPC_SERVER_UNCOMPRESSED_RESPONSE_BYTES, 1e5);
      measureMap.record(
          tagger
              .currentBuilder()
              .put(RpcMeasureConstants.RPC_STATUS, TagValue.create("OK"))
              .build());
      MeasureMap measureMapErrors =
          statsRecorder
              .newMeasureMap()
              .put(RpcMeasureConstants.RPC_CLIENT_ERROR_COUNT, 1)
              .put(RpcMeasureConstants.RPC_SERVER_ERROR_COUNT, 1);
      measureMapErrors.record(
          tagger
              .currentBuilder()
              .put(RpcMeasureConstants.RPC_STATUS, TagValue.create("UNKNOWN"))
              .build());

      Thread.sleep(200); // sleep for fake work.
      tracer.getCurrentSpan().addAnnotation("Finish recording.");
    }

    ZPageHandlers.startHttpServerAndRegisterAll(8080);
  }
}
