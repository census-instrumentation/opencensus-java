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

package io.opencensus.implcore.stats.export;

import com.google.common.annotations.VisibleForTesting;
import io.opencensus.stats.export.ExportComponent;

/** Implementation for {@link ExportComponent}. */
public final class ExportComponentImpl extends ExportComponent {

  @VisibleForTesting static final int EXPORTER_BUFFER_SIZE = 32;
  // Enforces exporting data at least once every 2 seconds.
  @VisibleForTesting static final long EXPORTER_SCHEDULE_DELAY_MS = 2000;

  private final StatsExporterImpl statsExporter;

  @Override
  public StatsExporterImpl getStatsExporter() {
    return statsExporter;
  }

  /**
   * Returns a new {@code ExportComponentImpl}.
   *
   * @return a new {@code ExportComponentImpl}.
   */
  public static ExportComponentImpl create() {
    return new ExportComponentImpl();
  }

  private ExportComponentImpl() {
    this(StatsExporterImpl.create(EXPORTER_BUFFER_SIZE, EXPORTER_SCHEDULE_DELAY_MS));
  }

  @VisibleForTesting
  ExportComponentImpl(StatsExporterImpl statsExporter) {
    this.statsExporter = statsExporter;
  }
}
