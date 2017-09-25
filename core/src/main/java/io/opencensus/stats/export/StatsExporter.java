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

import io.opencensus.stats.View;
import io.opencensus.stats.ViewData;
import java.util.Collection;
import javax.annotation.concurrent.ThreadSafe;

/** A service that is used by the library to export {@code ViewData}. */
@ThreadSafe
public abstract class StatsExporter {

  private static final NoopStatsExporter NOOP_STATS_EXPORTER = new NoopStatsExporter();

  /**
   * Returns the no-op implementation of the {@code StatsExporter}.
   *
   * @return the no-op implementation of the {@code StatsExporter}.
   */
  public static NoopStatsExporter getNoopStatsExporter() {
    return NOOP_STATS_EXPORTER;
  }

  /**
   * Registers a new service handler that is used by the library to export {@code ViewData}.
   *
   * @param name the name of the service handler. Must be unique for each service.
   * @param handler the service handler that is called for each ended sampled span.
   */
  public abstract void registerHandler(String name, Handler handler);

  /**
   * Unregisters the service handler with the provided name.
   *
   * @param name the name of the service handler that will be unregistered.
   */
  public abstract void unregisterHandler(String name);

  /**
   * An abstract class that exports recorded {@code ViewData} in their own format.
   *
   * <p>To export data this MUST be register to to the ExportComponent using {@link
   * #registerHandler(String, Handler)}.
   */
  public abstract static class Handler {

    /**
     * Register a {@code View}. Each {@code View} must be registered first, so that its stats can be
     * exported appropriately.
     *
     * <p>Implementation SHOULD not block the calling thread. It should execute the export on a
     * different thread if possible.
     *
     * @param view the {@code View} to be registered.
     */
    public abstract void registerView(View view);

    /**
     * Exports a list of {@code ViewData}.
     *
     * <p>Implementation SHOULD not block the calling thread. It should execute the export on a
     * different thread if possible.
     *
     * @param viewDataList a list of {@code ViewData} objects to be exported.
     */
    public abstract void export(Collection<ViewData> viewDataList);
  }

  private static final class NoopStatsExporter extends StatsExporter {

    @Override
    public void registerHandler(String name, Handler handler) {}

    @Override
    public void unregisterHandler(String name) {}
  }
}
