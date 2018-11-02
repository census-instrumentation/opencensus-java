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

package io.opencensus.exporter.trace.stackdriver;

import static com.google.common.base.Preconditions.checkState;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.ServiceOptions;
import com.google.cloud.trace.v2.TraceServiceClient;
import com.google.cloud.trace.v2.stub.TraceServiceStub;
import com.google.common.annotations.VisibleForTesting;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.export.SpanExporter;
import io.opencensus.trace.export.SpanExporter.Handler;
import java.io.IOException;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

/**
 * An OpenCensus span exporter implementation which exports data to Stackdriver Trace.
 *
 * <p>Example of usage on Google Cloud VMs:
 *
 * <pre>{@code
 * public static void main(String[] args) {
 *   StackdriverTraceExporter.createAndRegister(
 *       StackdriverTraceConfiguration.builder()
 *           .setProjectId("MyStackdriverProjectId")
 *           .build());
 *   ... // Do work.
 * }
 * }</pre>
 *
 * @since 0.12
 */
public final class StackdriverTraceExporter {

  private static final String REGISTER_NAME = StackdriverTraceExporter.class.getName();
  private static final Object monitor = new Object();

  @GuardedBy("monitor")
  @Nullable
  private static Handler handler = null;

  /**
   * Creates and registers the Stackdriver Trace exporter to the OpenCensus library. Only one
   * Stackdriver exporter can be registered at any point.
   *
   * <p>If the {@code credentials} in the provided {@link StackdriverTraceConfiguration} is not set,
   * the exporter will use the default application credentials. See {@link
   * GoogleCredentials#getApplicationDefault}.
   *
   * <p>If the {@code projectId} in the provided {@link StackdriverTraceConfiguration} is not set,
   * the exporter will use the default project ID. See {@link ServiceOptions#getDefaultProjectId}.
   *
   * @param configuration the {@code StackdriverTraceConfiguration} used to create the exporter.
   * @throws IllegalStateException if a Stackdriver exporter is already registered.
   * @since 0.12
   */
  public static void createAndRegister(StackdriverTraceConfiguration configuration)
      throws IOException {
    synchronized (monitor) {
      checkState(handler == null, "Stackdriver exporter is already registered.");
      Credentials credentials = configuration.getCredentials();
      String projectId = configuration.getProjectId();

      // TODO(sebright): Handle null default project ID.
      projectId = projectId != null ? projectId : castNonNull(ServiceOptions.getDefaultProjectId());

      StackdriverV2ExporterHandler handler;
      TraceServiceStub stub = configuration.getTraceServiceStub();
      if (stub == null) {
        handler =
            StackdriverV2ExporterHandler.createWithCredentials(
                credentials != null ? credentials : GoogleCredentials.getApplicationDefault(),
                projectId);
      } else {
        handler = new StackdriverV2ExporterHandler(projectId, TraceServiceClient.create(stub));
      }

      registerInternal(handler);
    }
  }

  // TODO(sebright): Remove this method.
  @SuppressWarnings("nullness")
  private static <T> T castNonNull(@javax.annotation.Nullable T arg) {
    return arg;
  }

  private static void registerInternal(Handler newHandler) {
    synchronized (monitor) {
      handler = newHandler;
      register(Tracing.getExportComponent().getSpanExporter(), newHandler);
    }
  }

  /**
   * Registers the {@code StackdriverTraceExporter}.
   *
   * @param spanExporter the instance of the {@code SpanExporter} where this service is registered.
   */
  @VisibleForTesting
  static void register(SpanExporter spanExporter, Handler handler) {
    spanExporter.registerHandler(REGISTER_NAME, handler);
  }

  /**
   * Unregisters the Stackdriver Trace exporter from the OpenCensus library.
   *
   * @throws IllegalStateException if a Stackdriver exporter is not registered.
   * @since 0.12
   */
  public static void unregister() {
    synchronized (monitor) {
      checkState(handler != null, "Stackdriver exporter is not registered.");
      unregister(Tracing.getExportComponent().getSpanExporter());
      handler = null;
    }
  }

  /**
   * Unregisters the {@code StackdriverTraceExporter}.
   *
   * @param spanExporter the instance of the {@code SpanExporter} from where this service is
   *     unregistered.
   */
  @VisibleForTesting
  static void unregister(SpanExporter spanExporter) {
    spanExporter.unregisterHandler(REGISTER_NAME);
  }

  private StackdriverTraceExporter() {}
}
