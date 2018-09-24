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

  private static final String DEFAULT_HOST = "localhost";
  private static final int DEFAULT_PORT = 55678;
  private static final String DEFAULT_SERVICE_NAME = "OpenCensus";

  // private final String host;
  // private final int port;
  // private final String serviceName;
  // private final boolean useInsecure;

  OcAgentTraceExporterHandler() {
    this(null, null, null, null);
  }

  OcAgentTraceExporterHandler(
      @Nullable String host,
      @Nullable Integer port,
      @Nullable String serviceName,
      @Nullable Boolean useInsecure) {
    // this.host = host == null ? DEFAULT_HOST : host;
    // this.port = port == null ? DEFAULT_PORT : port;
    // this.serviceName = serviceName == null ? DEFAULT_SERVICE_NAME : serviceName;
    // this.useInsecure = useInsecure == null ? false : useInsecure;
  }

  @Override
  public void export(Collection<SpanData> spanDataList) {
    // TODO(songya): implement this.
    // for (SpanData spanData : spanDataList) {
    // }
  }
}
