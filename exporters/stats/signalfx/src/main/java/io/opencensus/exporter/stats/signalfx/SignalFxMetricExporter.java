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

import com.signalfx.metrics.errorhandler.MetricError;
import com.signalfx.metrics.errorhandler.OnSendErrorHandler;
import com.signalfx.metrics.flush.AggregateMetricSender;
import com.signalfx.metrics.flush.AggregateMetricSender.Session;
import com.signalfx.metrics.protobuf.SignalFxProtocolBuffers.DataPoint;
import io.opencensus.exporter.metrics.util.MetricExporter;
import io.opencensus.metrics.export.Metric;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Worker {@code Thread} that polls Metric from Metrics library and exports to SignalFx.
 *
 * <p>{@code SignalFxStatsExporterWorkerThread} is a daemon {@code Thread}
 */
final class SignalFxMetricExporter extends MetricExporter {

  private static final Logger logger = Logger.getLogger(SignalFxMetricExporter.class.getName());

  private static final OnSendErrorHandler ERROR_HANDLER =
      new OnSendErrorHandler() {
        @Override
        public void handleError(MetricError error) {
          logger.log(Level.WARNING, "Unable to send metrics to SignalFx: {0}", error.getMessage());
        }
      };

  private final AggregateMetricSender sender;

  SignalFxMetricExporter(SignalFxMetricsSenderFactory factory, URI endpoint, String token) {
    this.sender = factory.create(endpoint, token, ERROR_HANDLER);

    logger.log(Level.FINE, "Initialized SignalFx exporter to {0}.", endpoint);
  }

  @Override
  public void export(Collection<Metric> metrics) {
    Session session = sender.createSession();

    try {
      for (Metric metric : metrics) {
        for (DataPoint datapoint : SignalFxSessionAdaptor.adapt(metric)) {
          session.setDatapoint(datapoint);
        }
      }
    } finally {
      try {
        session.close();
      } catch (IOException e) {
        logger.log(Level.FINE, "Unable to close the session", e);
      }
    }
  }
}
