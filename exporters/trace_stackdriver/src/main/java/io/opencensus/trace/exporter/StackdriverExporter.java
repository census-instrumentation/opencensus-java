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

package io.opencensus.trace.exporter;

import static com.google.common.base.Preconditions.checkState;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.common.annotations.VisibleForTesting;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.export.SpanExporter;
import io.opencensus.trace.export.SpanExporter.Handler;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.annotation.concurrent.GuardedBy;

/**
 * An OpenCensus span exporter implementation which exports data to Stackdriver Trace.
 *
 * <p>Example of usage on Google Cloud VMs:
 *
 * <pre><code>
 *   public static void main(String[] args) {
 *     StackdriverExporter.createAndRegister("MyStackdriverProjectId");
 *     ... // Do work.
 *   }
 * </code></pre>
 */
public final class StackdriverExporter {
  private static final String REGISTER_NAME = StackdriverExporter.class.getName();
  private static final List<String> STACKDRIVER_TRACE_WRITER_SCOPE =
      Collections.singletonList("https://www.googleapis.com/auth/trace.append");
  private static final Object monitor = new Object();

  @GuardedBy("monitor")
  private static Handler handler = null;

  /**
   * Creates and registers the Stackdriver Trace exporter to the OpenCensus library. Only one
   * Stackdriver exporter can be registered at any point.
   *
   * @param credentials a credentials used to authenticate API calls.
   * @param projectId the cloud project id.
   * @throws IllegalStateException if a Stackdriver exporter already registered.
   */
  public static void createAndRegisterWithCredentials(Credentials credentials, String projectId)
      throws IOException {
    synchronized (monitor) {
      checkState(handler == null, "exporter already registered");
      handler = StackdriverV1ExporterHandler.createWithCredentials(credentials, projectId);
      register(Tracing.getExportComponent().getSpanExporter(), handler);
    }
  }

  /**
   * Creates and registers the Stackdriver Trace exporter to the OpenCensus library. Only one
   * Stackdriver exporter can be registered at any point.
   *
   * <p>This uses the default application credentials see {@link
   * GoogleCredentials#getApplicationDefault()}. If you do not have default application credentials
   * configured use {@link #createAndRegisterWithCredentials(Credentials, String)}.
   *
   * @param projectId the cloud project id.
   * @throws IllegalStateException if a Stackdriver exporter already registered.
   */
  public static void createAndRegister(String projectId) throws IOException {
    synchronized (monitor) {
      checkState(handler == null, "exporter already registered");
      handler = StackdriverV1ExporterHandler.create(projectId);
      register(Tracing.getExportComponent().getSpanExporter(), handler);
    }
  }

  /**
   * Registers the {@code StackdriverExporter}.
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
   * @throws IllegalStateException if a Stackdriver exporter not registered.
   */
  public static void unregister() {
    synchronized (monitor) {
      checkState(handler != null, "exporter not registered");
      unregister(Tracing.getExportComponent().getSpanExporter());
      handler = null;
    }
  }

  /**
   * Unregisters the {@code StackdriverExporter}.
   *
   * @param spanExporter the instance of the {@code SpanExporter} from where this service is
   *     unregistered.
   */
  @VisibleForTesting
  static void unregister(SpanExporter spanExporter) {
    spanExporter.unregisterHandler(REGISTER_NAME);
  }

  private StackdriverExporter() {}
}
