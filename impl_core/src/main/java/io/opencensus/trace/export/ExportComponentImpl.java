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

import javax.annotation.Nullable;

/** Implementation of the {@link ExportComponent}. */
public final class ExportComponentImpl extends ExportComponent {
  private static final int EXPORTER_BUFFER_SIZE = 32;
  // Enforces that trace export exports data at least once every 2 seconds.
  private static final long EXPORTER_SCHEDULE_DELAY_MS = 2000;

  private final SpanExporterImpl spanExporter;
  private final RunningSpanStoreImpl runningSpanStore;
  private final SampledSpanStoreImpl sampledSpanStore;

  @Override
  public SpanExporterImpl getSpanExporter() {
    return spanExporter;
  }

  @Nullable
  @Override
  public RunningSpanStoreImpl getRunningSpanStore() {
    return runningSpanStore;
  }

  @Nullable
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
  public static ExportComponentImpl createWithInProcessStores() {
    return new ExportComponentImpl(true);
  }

  /**
   * Returns a new {@code ExportComponentImpl} that has {@code null} instances for {@link
   * RunningSpanStore} and {@link SampledSpanStore}.
   *
   * @return a new {@code ExportComponentImpl}.
   */
  public static ExportComponentImpl createWithoutInProcessStores() {
    return new ExportComponentImpl(false);
  }

  /**
   * Constructs a new {@code ExportComponentImpl}.
   *
   * @param supportInProcessStores {@code true} to instantiate {@link RunningSpanStore} and {@link
   *     SampledSpanStore}.
   */
  private ExportComponentImpl(boolean supportInProcessStores) {
    this.spanExporter = SpanExporterImpl.create(EXPORTER_BUFFER_SIZE, EXPORTER_SCHEDULE_DELAY_MS);
    this.runningSpanStore = supportInProcessStores ? new RunningSpanStoreImpl() : null;
    this.sampledSpanStore = supportInProcessStores ? new SampledSpanStoreImpl() : null;
  }
}
