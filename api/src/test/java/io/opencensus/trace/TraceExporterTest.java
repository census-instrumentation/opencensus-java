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

package io.opencensus.trace;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opencensus.trace.TraceExporter.LoggingSpanExporterHandler;
import io.opencensus.trace.TraceExporter.SpanExporter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link TraceExporter}. */
@RunWith(JUnit4.class)
public class TraceExporterTest {
  @Mock private TraceExporter traceExporter;
  @Mock private SpanExporter spanExporter;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void registerUnregisterLoggingService() {
    when(traceExporter.getSpanExporter()).thenReturn(spanExporter);
    LoggingSpanExporterHandler.register(traceExporter);
    verify(spanExporter)
        .registerHandler(
            eq("io.opencensus.trace.LoggingSpanExporterHandler"),
            any(LoggingSpanExporterHandler.class));
    LoggingSpanExporterHandler.unregister(traceExporter);
    verify(spanExporter).unregisterHandler(eq("io.opencensus.trace.LoggingSpanExporterHandler"));
  }
}
