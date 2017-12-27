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

package io.opencensus.exporter.stats.signalfx;

import com.google.common.annotations.VisibleForTesting;
import com.signalfx.metrics.errorhandler.MetricError;
import com.signalfx.metrics.errorhandler.OnSendErrorHandler;
import com.signalfx.metrics.flush.AggregateMetricSender;
import com.signalfx.metrics.flush.AggregateMetricSender.Session;
import com.signalfx.metrics.protobuf.SignalFxProtocolBuffers.DataPoint;
import io.opencensus.common.Duration;
import io.opencensus.stats.View;
import io.opencensus.stats.ViewData;
import io.opencensus.stats.ViewManager;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Worker {@code Thread} that polls ViewData from the Stats's ViewManager and exports to SignalFx.
 *
 * <p>{@code SignalFxStatsExporterWorkerThread} is a daemon {@code Thread}
 */
final class SignalFxStatsExporterWorkerThread extends Thread {

  private static final Logger logger =
      Logger.getLogger(SignalFxStatsExporterWorkerThread.class.getName());

  private static final OnSendErrorHandler ERROR_HANDLER =
      new OnSendErrorHandler() {
        @Override
        public void handleError(MetricError error) {
          logger.log(Level.WARNING, "Unable to send metrics to SignalFx: {0}", error.getMessage());
        }
      };

  private final long intervalMs;
  private final ViewManager views;
  private final AggregateMetricSender sender;

  SignalFxStatsExporterWorkerThread(
      SignalFxMetricsSenderFactory factory,
      URI endpoint,
      String token,
      Duration interval,
      ViewManager views) {
    this.intervalMs =
        TimeUnit.SECONDS.toMillis(interval.getSeconds())
            + TimeUnit.NANOSECONDS.toMillis(interval.getNanos());
    this.views = views;
    this.sender = factory.create(endpoint, token, ERROR_HANDLER);

    setDaemon(true);
    setName(getClass().getSimpleName());
    logger.log(Level.FINE, "Initialized SignalFx exporter to {0}.", endpoint);
  }

  @VisibleForTesting
  void export() {
    try (Session session = sender.createSession()) {
      for (View view : views.getAllExportedViews()) {
        ViewData data = views.getView(view.getName());
        for (DataPoint datapoint : SignalFxSessionAdaptor.adapt(data)) {
          session.setDatapoint(datapoint);
        }
      }
    } catch (IOException e) {
      // Does not happen, flush/close errors are communicated through the sender's error handlers.
    }
  }

  @Override
  public void run() {
    while (true) {
      try {
        export();
        Thread.sleep(intervalMs);
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
        break;
      } catch (Throwable e) {
        logger.log(Level.WARNING, "Exception thrown by the SignalFx stats exporter", e);
      }
    }
    logger.log(Level.INFO, "SignalFx stats exporter stopped.");
  }
}
