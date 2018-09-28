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

package io.opencensus.exporter.trace.ocagent;

import io.opencensus.trace.export.SpanData;
import io.opencensus.trace.export.SpanExporter.Handler;
import java.util.Collection;
import javax.annotation.Nullable;

/** Exporting handler for OC-Agent Tracing. */
final class OcAgentTraceExporterHandler extends Handler {

  private static final String DEFAULT_END_POINT = "localhost:55678";
  private static final String DEFAULT_SERVICE_NAME = "OpenCensus";

  @Nullable private final OcAgentTraceServiceClients.ExportServiceClient client;

  OcAgentTraceExporterHandler() {
    this(null, null, null);
  }

  OcAgentTraceExporterHandler(
      @Nullable String endPoint, @Nullable String serviceName, @Nullable Boolean useInsecure) {
    if (endPoint == null) {
      endPoint = DEFAULT_END_POINT;
    }
    if (serviceName == null) {
      serviceName = DEFAULT_SERVICE_NAME;
    }
    if (useInsecure == null) {
      useInsecure = false;
    }
    client = OcAgentTraceServiceClients.openStreams(endPoint, useInsecure, serviceName);
  }

  @Override
  public void export(Collection<SpanData> spanDataList) {
    if (client == null) {
      return;
    }
    client.onExport(spanDataList);
  }
}
