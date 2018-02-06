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

package io.opencensus.implcore.trace.export;

import io.opencensus.common.Duration;
import io.opencensus.implcore.internal.EventQueue;
import io.opencensus.trace.export.ExportComponent;
import io.opencensus.trace.export.RunningSpanStore;
import io.opencensus.trace.export.SampledSpanStore;

/** Implementation of the {@link ExportComponent}. */
public final class ExportComponentImpl extends ExportComponent {
  private static final int EXPORTER_BUFFER_SIZE = 32;
  // Enforces that trace export exports data at least once every 5 seconds.
  private static final Duration EXPORTER_SCHEDULE_DELAY = Duration.create(5, 0);

  private final SpanExporterImpl spanExporter;
  private final RunningSpanStoreImpl runningSpanStore;
  private final SampledSpanStoreImpl sampledSpanStore;

  @Override
  public SpanExporterImpl getSpanExporter() {
    return spanExporter;
  }

  @Override
  public RunningSpanStoreImpl getRunningSpanStore() {
    return runningSpanStore;
  }

  @Override
  public SampledSpanStoreImpl getSampledSpanStore() {
    return sampledSpanStore;
  }

  /**
   * Returns a new {@code ExportComponentImpl} that has valid instances for {@link RunningSpanStore}
   * and {@link SampledSpanStore}.
   *
   * @return a new {@code ExportComponentImpl}.
   */
  public static ExportComponentImpl createWithInProcessStores(EventQueue eventQueue) {
    return new ExportComponentImpl(true, eventQueue);
  }

  /**
   * Returns a new {@code ExportComponentImpl} that has {@code null} instances for {@link
   * RunningSpanStore} and {@link SampledSpanStore}.
   *
   * @return a new {@code ExportComponentImpl}.
   */
  public static ExportComponentImpl createWithoutInProcessStores(EventQueue eventQueue) {
    return new ExportComponentImpl(false, eventQueue);
  }

  /**
   * Constructs a new {@code ExportComponentImpl}.
   *
   * @param supportInProcessStores {@code true} to instantiate {@link RunningSpanStore} and {@link
   *     SampledSpanStore}.
   */
  private ExportComponentImpl(boolean supportInProcessStores, EventQueue eventQueue) {
    this.spanExporter = SpanExporterImpl.create(EXPORTER_BUFFER_SIZE, EXPORTER_SCHEDULE_DELAY);
    this.runningSpanStore =
        supportInProcessStores
            ? new InProcessRunningSpanStoreImpl()
            : RunningSpanStoreImpl.getNoopRunningSpanStoreImpl();
    this.sampledSpanStore =
        supportInProcessStores
            ? new InProcessSampledSpanStoreImpl(eventQueue)
            : SampledSpanStoreImpl.getNoopSampledSpanStoreImpl();
  }
}
