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

package io.opencensus.stats.export;

/**
 * Class that holds the implementation instances for {@link StatsExporter}.
 */
public abstract class ExportComponent {

  private static final NoopExportComponent NOOP_EXPORT_COMPONENT = new NoopExportComponent();

  /**
   * Returns the no-op implementation of the {@code ExportComponent}.
   *
   * @return the no-op implementation of the {@code ExportComponent}.
   */
  public static ExportComponent getNoopExportComponent() {
    return NOOP_EXPORT_COMPONENT;
  }

  /**
   * Returns the {@link StatsExporter} which can be used to register handlers to export all the
   * {@code ViewData}.
   *
   * @return the implementation of the {@code StatsExporter} or no-op if no implementation linked in
   *     the binary.
   */
  public abstract StatsExporter getStatsExporter();

  private static final class NoopExportComponent extends ExportComponent {
    @Override
    public StatsExporter getStatsExporter() {
      return StatsExporter.getNoopStatsExporter();
    }
  }
}
