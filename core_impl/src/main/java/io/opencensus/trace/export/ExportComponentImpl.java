/*
 * Copyright 2017, Google Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.trace.export;

import io.opencensus.internal.EventQueue;
import io.opencensus.trace.SpanImpl.StartEndHandler;
import javax.annotation.Nullable;

/**
 * Implementation of the {@link ExportComponent}, implements also {@link StartEndHandler}.
 *
 * <p>Uses the provided {@link EventQueue} to defer processing/exporting of the {@link SpanData} to
 * avoid impacting the critical path.
 */
public final class ExportComponentImpl extends ExportComponent {
  private static final int EXPORTER_BUFFER_SIZE = 32;
  // Enforces that trace export exports data at least once every 2 seconds.
  private static final long EXPORTER_SCHEDULE_DELAY_MS = 2000;

  private final SpanExporterImpl spanExporter;

  @Override
  public SpanExporterImpl getSpanExporter() {
    return spanExporter;
  }

  @Nullable
  @Override
  public InProcessDebuggingHandler getInProcessDebuggingHandler() {
    // TODO(bdrutu): Implement this.
    return null;
  }

  /** Constructs a new {@code ExportComponentImpl}. */
  public ExportComponentImpl() {
    this.spanExporter = SpanExporterImpl.create(EXPORTER_BUFFER_SIZE, EXPORTER_SCHEDULE_DELAY_MS);
  }
}
