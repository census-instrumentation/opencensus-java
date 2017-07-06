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

import io.opencensus.trace.export.SpanExporter.Handler;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.ThreadSafe;

/** Implementation of the {@link Handler} which logs all the exported {@link SpanData}. */
@ThreadSafe
public final class LoggingExportHandler extends Handler {

  private static final Logger logger = Logger.getLogger(LoggingExportHandler.class.getName());
  private static final String REGISTER_NAME = LoggingExportHandler.class.getName();
  private static final LoggingExportHandler INSTANCE = new LoggingExportHandler();

  private LoggingExportHandler() {}

  /**
   * Registers the {@code LoggingHandler} to the {@code ExportComponent}.
   *
   * @param spanExporter the instance of the {@code SpanExporter} where this service is registered.
   */
  public static void register(SpanExporter spanExporter) {
    spanExporter.registerHandler(REGISTER_NAME, INSTANCE);
  }

  /**
   * Unregisters the {@code LoggingHandler} from the {@code ExportComponent}.
   *
   * @param spanExporter the instance of the {@code SpanExporter} from where this service is
   *     unregistered.
   */
  public static void unregister(SpanExporter spanExporter) {
    spanExporter.unregisterHandler(REGISTER_NAME);
  }

  @Override
  public void export(Collection<SpanData> spanDataList) {
    // TODO(bdrutu): Use JSON as a standard format for logging SpanData and define this to be
    // compatible between languages.
    for (SpanData spanData : spanDataList) {
      logger.log(Level.INFO, spanData.toString());
    }
  }
}
