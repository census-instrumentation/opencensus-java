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

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.ServiceOptions;
import com.google.common.annotations.VisibleForTesting;
import io.opencensus.trace.export.SpanExporter;
import io.opencensus.trace.export.SpanExporter.Handler;
import java.io.IOException;

/**
 * An OpenCensus span exporter implementation which exports data to Stackdriver Trace.
 *
 * <p>Example of usage on Google Cloud VMs:
 *
 * <pre>{@code
 * public static void main(String[] args) {
 *   StackdriverExporter.createAndRegisterWithProjectId("MyStackdriverProjectId");
 *   ... // Do work.
 * }
 * }</pre>
 *
 * @deprecated Deprecated due to inconsistent naming. Use {@link StackdriverTraceExporter}.
 */
@Deprecated
public final class StackdriverExporter {

  /**
   * Creates and registers the Stackdriver Trace exporter to the OpenCensus library for an explicit
   * project ID and using explicit credentials. Only one Stackdriver exporter can be registered at
   * any point.
   *
   * @param credentials a credentials used to authenticate API calls.
   * @param projectId the cloud project id.
   * @throws IllegalStateException if a Stackdriver exporter is already registered.
   */
  public static void createAndRegisterWithCredentialsAndProjectId(
      Credentials credentials, String projectId) throws IOException {
    StackdriverTraceExporter.createAndRegisterWithCredentialsAndProjectId(credentials, projectId);
  }

  /**
   * Creates and registers the Stackdriver Trace exporter to the OpenCensus library for an explicit
   * project ID. Only one Stackdriver exporter can be registered at any point.
   *
   * <p>This uses the default application credentials see {@link
   * GoogleCredentials#getApplicationDefault}.
   *
   * <p>This is equivalent with:
   *
   * <pre>{@code
   * StackdriverExporter.createAndRegisterWithCredentialsAndProjectId(
   *     GoogleCredentials.getApplicationDefault(), projectId);
   * }</pre>
   *
   * @param projectId the cloud project id.
   * @throws IllegalStateException if a Stackdriver exporter is already registered.
   */
  public static void createAndRegisterWithProjectId(String projectId) throws IOException {
    StackdriverTraceExporter.createAndRegisterWithProjectId(projectId);
  }

  /**
   * Creates and registers the Stackdriver Trace exporter to the OpenCensus library. Only one
   * Stackdriver exporter can be registered at any point.
   *
   * <p>This uses the default application credentials see {@link
   * GoogleCredentials#getApplicationDefault}.
   *
   * <p>This uses the default project ID configured see {@link ServiceOptions#getDefaultProjectId}.
   *
   * <p>This is equivalent with:
   *
   * <pre>{@code
   * StackdriverExporter.createAndRegisterWithProjectId(ServiceOptions.getDefaultProjectId());
   * }</pre>
   *
   * @throws IllegalStateException if a Stackdriver exporter is already registered.
   */
  public static void createAndRegister() throws IOException {
    StackdriverTraceExporter.createAndRegister();
  }

  /**
   * Registers the {@code StackdriverExporter}.
   *
   * @param spanExporter the instance of the {@code SpanExporter} where this service is registered.
   */
  @VisibleForTesting
  static void register(SpanExporter spanExporter, Handler handler) {
    StackdriverTraceExporter.register(spanExporter, handler);
  }

  /**
   * Unregisters the Stackdriver Trace exporter from the OpenCensus library.
   *
   * @throws IllegalStateException if a Stackdriver exporter is not registered.
   */
  public static void unregister() {
    StackdriverTraceExporter.unregister();
  }

  /**
   * Unregisters the {@code StackdriverExporter}.
   *
   * @param spanExporter the instance of the {@code SpanExporter} from where this service is
   *     unregistered.
   */
  @VisibleForTesting
  static void unregister(SpanExporter spanExporter) {
    StackdriverTraceExporter.unregister(spanExporter);
  }

  private StackdriverExporter() {}
}
