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

package io.opencensus.exporter.trace.logging;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import io.opencensus.exporter.trace.logging.LoggingTraceExporter.LoggingExporterHandler;
import io.opencensus.trace.export.SpanExporter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link LoggingExporter}. */
@RunWith(JUnit4.class)
public class LoggingExporterTest {
  @Mock private SpanExporter spanExporter;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void registerUnregisterLoggingService() {
    LoggingExporter.register(spanExporter);
    verify(spanExporter)
        .registerHandler(
            eq("io.opencensus.exporter.trace.logging.LoggingTraceExporter"),
            any(LoggingExporterHandler.class));
    LoggingExporter.unregister(spanExporter);
    verify(spanExporter)
        .unregisterHandler(eq("io.opencensus.exporter.trace.logging.LoggingTraceExporter"));
  }
}
